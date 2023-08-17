package org.kin.framework.io;

import org.kin.framework.utils.FixEWMA;
import org.kin.framework.utils.Maths;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 支持动态伸缩的byte[]
 *
 * @author huangjianqin
 * @date 2021/12/15
 */
public final class ScalableByteArray implements Input, Output {
    /** 每次分配数组的容量 */
    private final int allocSize;
    /** 底层byte[] list */
    private List<byte[]> byteArrays = new ArrayList<>();
    /** 下一reader index */
    private int readerIndex;
    /** 下一writer index */
    private int writerIndex;
    /**
     * 基于历史分配容量, 预测下次写的容量, 进而防止突发高容量而导致分配过多byte[], 当预测容量小于, 可以适当释放部分byte[]
     * todo β值可修改
     */
    private final FixEWMA ewma = new FixEWMA(0.5);

    public ScalableByteArray() {
        //默认256 bytes
        this(256);
    }

    public ScalableByteArray(int allocSize) {
        if (!Maths.isPower2(allocSize)) {
            //取最接近的2的n次方作为分配的初始容量
            allocSize = Maths.round2Power2(allocSize);
        }
        this.allocSize = allocSize;
        //init one byte[]
        byteArrays.add(new byte[allocSize]);
    }

    /**
     * 获取当前读所在数组节点index
     */
    private int getReadArrOffset() {
        return getReadArrOffset(readerIndex);
    }

    private int getReadArrOffset(int readerIndex) {
        return readerIndex / allocSize;
    }

    /**
     * 获取当前读所在数组offset
     */
    private int getBytesReadOffset() {
        return getBytesReadOffset(readerIndex);
    }

    private int getBytesReadOffset(int readerIndex) {
        return readerIndex % allocSize;
    }

    /**
     * 获取当前写所在数组节点index
     */
    private int getWriteArrOffset() {
        return getWriteArrOffset(writerIndex);
    }

    /**
     * 获取当前写所在数组节点index
     */
    private int getWriteArrOffset(int writerIndex) {
        return writerIndex / allocSize;
    }

    /**
     * 获取当前写所在数组offset
     */
    private int getBytesWriteOffset() {
        return writerIndex % allocSize;
    }

    /**
     * 校验是否reader index <= writer index
     */
    private void validReaderIndex(int readerIndex) {
        if (readerIndex > writerIndex) {
            throw new IndexOutOfBoundsException(String.format("readerIndex: %d, writeOffset: %d", readerIndex, writerIndex));
        }
    }

    @Override
    public byte readByte() {
        validReaderIndex(readerIndex);
        byte[] bytes = byteArrays.get(getReadArrOffset());
        byte ret = bytes[getBytesReadOffset()];
        readerIndex++;
        return ret;
    }

    @Override
    public Input readBytes(byte[] dst, int dstIndex, int length) {
        if (Objects.isNull(dst)) {
            throw new IllegalArgumentException("dst is null");
        }
        if (dstIndex < 0) {
            throw new IndexOutOfBoundsException("dstIndex < 0");
        }
        if (readableBytes() < length) {
            throw new IndexOutOfBoundsException("length is greater than readableBytes");
        }
        while (readableBytes() > 0) {
            dst[dstIndex] = readByte();
        }
        return this;
    }

    @Override
    public int readableBytes() {
        return writerIndex - readerIndex;
    }

    @Override
    public int readerIndex() {
        return readerIndex;
    }

    @Override
    public Input readerIndex(int readerIndex) {
        if (readerIndex < 0) {
            throw new IndexOutOfBoundsException("readerIndex < 0");
        }
        validReaderIndex(readerIndex);
        this.readerIndex = readerIndex;
        return this;
    }

    @Override
    public boolean readerIndexSupported() {
        return true;
    }

    @Override
    public void writeByte(int value) {
        int bytesWriteOffset = getBytesWriteOffset();
        int writeArrOffset = getWriteArrOffset();
        if (writeArrOffset + 1 > byteArrays.size()) {
            //超过单个byte[]容量
            expand();
        }
        bytesWriteOffset = getBytesWriteOffset();

        byte[] bytes = byteArrays.get(writeArrOffset);
        bytes[bytesWriteOffset] = (byte) value;
        writerIndex++;
    }

    private void expand() {
        //列表byteArrays没有多余的byte[]可用, 则动态create
        byteArrays.add(new byte[allocSize]);
    }

    @Override
    public void writeBytes(byte[] value, int startIdx, int len) {
        if (Objects.isNull(value)) {
            throw new IllegalArgumentException("value is null");
        }
        if (startIdx < 0) {
            throw new IllegalArgumentException("startIdx is null");
        }
        if (len <= 0) {
            throw new IllegalArgumentException("len is less than or equal to 0");
        }

        int bytesWriteOffset = getBytesWriteOffset();
        int bytesRemaining = allocSize - bytesWriteOffset;
        if (bytesRemaining >= len) {
            //当前bytes有足够空间
            byte[] bytes = byteArrays.get(getWriteArrOffset());
            System.arraycopy(value, startIdx, bytes, bytesWriteOffset, len);
        } else {
            //当前bytes没有足够空间, 写期间需要扩容
            int writeLen = bytesRemaining;
            int writeOffset = bytesWriteOffset;
            int writerIndex = this.writerIndex;
            int tmpLen = len;
            while (tmpLen > 0) {
                byte[] bytes = byteArrays.get(getWriteArrOffset(writerIndex));
                System.arraycopy(value, startIdx, bytes, writeOffset, writeLen);

                expand();
                startIdx += writeLen;
                tmpLen -= writeLen;
                writeOffset = 0;
                writerIndex += writeLen;
                writeLen = Math.min(tmpLen, allocSize);
            }
        }
        writerIndex += len;
    }

    @Override
    public int writableBytes() {
        //默认无上限
        return Integer.MAX_VALUE;
    }

    /**
     * 返回write index
     */
    public int writerIndex() {
        return writerIndex;
    }

    /**
     * 设置write index
     */
    public void writerIndex(int writerIndex) {
        int limit = byteArrays.size() * allocSize;
        if(writerIndex > limit){
            throw new IndexOutOfBoundsException("writerIndex >= " + limit);
        }
        this.writerIndex = writerIndex;
    }

    /**
     * 预备从头写
     */
    public void clear() {
        //先重置reader index
        readerIndex = 0;

        //记录clear之前写入的字节数
        ewma.insert(readableBytes());

        //后重置writer index
        writerIndex = 0;

        //预测下次write size, 并尝试释放当前byteArrays中多余的byte[]
        int predictWriteSize = (int) this.ewma.getEwma();
        //下次write byte[]数量
        int byteArrayNum = predictWriteSize / allocSize + 1;
        if (byteArrayNum >= byteArrays.size()) {
            return;
        }

        //释放当前byteArrays中多余的byte[]
        List<byte[]> newByteArrays = new ArrayList<>(byteArrayNum);
        for (int i = 0; i < byteArrayNum; i++) {
            newByteArrays.add(byteArrays.get(i));
        }
        byteArrays = newByteArrays;
    }

    /**
     * 移除无效写入的数组节点, 并切换到预备读
     */
    public void flip() {
        int writeArrOffset = getWriteArrOffset();
        if (writerIndex == 0) {
            //数组还没写入bytes
            byteArrays = new ArrayList<>(byteArrays.subList(0, writeArrOffset));
        } else {
            //数组已写入bytes
            byteArrays = new ArrayList<>(byteArrays.subList(0, writeArrOffset + 1));
        }

        //重置read index
        readerIndex = 0;
    }

    /**
     * 将readable bytes转换成单个byte[]
     */
    public byte[] toByteArray() {
        if (writerIndex == 0) {
            return new byte[0];
        }

        byte[] bytes = new byte[readableBytes()];
        walkReadableBytes(new ByteArrayWriteBytesFunc(bytes));
        return bytes;
    }

    /**
     * 将readable bytes转换成预备可读的{@link ByteBuffer}
     */
    @Nullable
    public ByteBuffer toByteBuffer() {
        return toByteBuffer(false);
    }

    /**
     * 将readable bytes转换成预备可读的{@link ByteBuffer}
     */
    @Nullable
    public ByteBuffer toByteBuffer(boolean direct) {
        if (writerIndex == 0) {
            return null;
        }

        int len = readableBytes();
        ByteBuffer byteBuffer;
        if (direct) {
            byteBuffer = ByteBuffer.allocateDirect(len);
        } else {
            byteBuffer = ByteBuffer.allocate(len);
        }

        walkReadableBytes(byteBuffer::put);
        ByteBufferUtils.toReadMode(byteBuffer);
        return byteBuffer;
    }

    /**
     * 遍历所有可读字节数组
     */
    private void walkReadableBytes(WriteBytesFunc func){
        int readerIndex = this.readerIndex;
        int writeArrOffset = getWriteArrOffset();
        int bytesWriteOffset = getBytesWriteOffset();

        while (readerIndex < writerIndex) {
            int readArrOffset = getReadArrOffset(readerIndex);
            byte[] bytes = byteArrays.get(readArrOffset);
            int bytesReadOffset = getBytesReadOffset(readerIndex);

            int len;
            //copy byte[]
            if (readArrOffset == writeArrOffset) {
                //reader index和writer index在同一bytes
                len = bytesWriteOffset - bytesReadOffset;
            } else {
                //reader index和writer index不在同一bytes
                len = allocSize - bytesReadOffset;
            }
            func.writeBytes(bytes, bytesReadOffset, len);

            readerIndex += len;
        }
    }

    /**
     * 保证足够可写字节数
     */
    public void ensureWritableBytes(int writableBytes){
        int curWritableBytes = byteArrays.size() * allocSize - writerIndex;
        if(curWritableBytes >= writableBytes){
            //本来就够了
            return;
        }

        //需要补足的字节数
        int newWriteIndex = this.writerIndex + writableBytes;
        int newWriteArrOffset = getWriteArrOffset(newWriteIndex);
        int writeArrOffset = getWriteArrOffset(this.writerIndex - 1);
        int needArray = newWriteArrOffset - writeArrOffset;
        if(needArray > 0){
            for (int i = 0; i < needArray; i++) {
                expand();
            }
        }
    }

    @Override
    public String toString() {
        return "ScalableByteArray{" +
                "allocSize=" + allocSize +
                ", byteArrays=" + byteArrays.stream().map(Arrays::toString).collect(Collectors.toList()) +
                ", readerIndex=" + readerIndex +
                ", writerIndex=" + writerIndex +
                '}';
    }

    //------------------------------------------------------------------------------------

    /**
     * 将一块bytes写入目标bytes统一处理
     */
    @FunctionalInterface
    private interface WriteBytesFunc{
        /**
         * 写bytes
         * @param src   源数组
         * @param offset    源数组起始offset
         * @param length    write bytes长度
         */
        void writeBytes(byte[] src, int offset, int length);
    }

    /**
     * 基于bytes的{@link WriteBytesFunc}实现
     */
    private static class ByteArrayWriteBytesFunc implements WriteBytesFunc{
        private final byte[] bytes;
        private int offset;

        public ByteArrayWriteBytesFunc(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void writeBytes(byte[] src, int offset, int length) {
            System.arraycopy(src, offset, bytes, this.offset, length);
            this.offset += length;
        }

        //getter
        public byte[] getBytes() {
            return bytes;
        }
    }
}
