package org.kin.framework.utils;

import com.google.common.base.Function;

/**
 * 基于内存实现的bloom filter
 * @author huangjianqin
 * @date 2022/8/12
 */
public final class CommonBloomFilter<T> extends AbstractBloomFilter<T>{
    /** 压缩率, 即long的bit长度 */
    private static final int COMPRESS_RATE = 64;
    /** 基于long, 相当于压缩byte[]长度, 缩短了64倍 */
    private long[] bitmap;

    public CommonBloomFilter(int predictElementSize) {
        super(predictElementSize);
        initBitMap();
    }

    public CommonBloomFilter(int predictElementSize, double fpp) {
        super(predictElementSize, fpp);
        initBitMap();
    }

    public CommonBloomFilter(int predictElementSize, double fpp, Function<T, byte[]> mapper) {
        super(predictElementSize, fpp, mapper);
        initBitMap();
    }

    /**
     * 初始化内存bitmap
     */
    private void initBitMap(){
        //不满足一个long则补1
        int len = (int) (getBitmapLength() / COMPRESS_RATE + 1);
        bitmap = new long[len];
    }

    @Override
    protected void put(long[] indices) {
        for (long index : indices) {
            int i = (int) (index / COMPRESS_RATE);
            int bit = (int) (index % COMPRESS_RATE);

            bitmap[i] = bitmap[i] | (1L << bit);
        }
    }

    @Override
    protected boolean contains(long[] indices) {
        for (long index : indices) {
            int i = (int) (index / COMPRESS_RATE);
            int bit = (int) (index % COMPRESS_RATE);

            if((bitmap[i] & (1L << bit)) == 0){
                //存在一个不为1
                return false;
            }
        }
        return true;
    }
}
