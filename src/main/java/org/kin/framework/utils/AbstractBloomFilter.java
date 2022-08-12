package org.kin.framework.utils;


import com.google.common.base.Preconditions;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * bloom filter通用抽象
 *
 * @author huangjianqin
 * @date 2022/8/12
 */
public abstract class AbstractBloomFilter<T> {
    /** bit数组长度 */
    protected final long bitmapLength;
    /** Hash函数个数 */
    protected final int hashFunctionNum;
    /** 目标对象转换成byte[]逻辑 */
    protected final Function<T, byte[]> mapper;

    protected AbstractBloomFilter(int predictElementSize) {
        //FYI, for 3%, we always get 5 hash functions
        this(predictElementSize, 0.03, o -> o.toString().getBytes(StandardCharsets.UTF_8));
    }

    protected AbstractBloomFilter(int predictElementSize, double fpp) {
        this(predictElementSize, fpp, o -> o.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param predictElementSize 预估数据量
     * @param fpp                误判率
     */
    protected AbstractBloomFilter(int predictElementSize, double fpp, Function<T, byte[]> mapper) {
        Preconditions.checkArgument(predictElementSize > 0, "predictElementSize must be greater than 0");
        Preconditions.checkArgument(fpp > 0, "fpp must be greater than 0");

        //计算bit数组长度
        bitmapLength = (int) (-predictElementSize * Math.log(fpp) / (Math.log(2) * Math.log(2)));
        //计算hash函数个数
        hashFunctionNum = Math.max(1, (int) Math.round((double) bitmapLength / predictElementSize * Math.log(2)));
        this.mapper = mapper;
    }

    /**
     * 插入对象
     *
     * @param object 目标对象
     */
    public final void put(T object) {
        Preconditions.checkNotNull(object);
        put(getBitIndices(object));
    }

    /**
     * 子类自定义实现, 将对应index的bit改为1
     *
     * @param indices index数组
     */
    protected abstract void put(long[] indices);

    /**
     * 检查元素在集合中是否存在(基于bloom filter的特性, 可能误判)
     *
     * @param object 目标对象
     * @return true即可能存在, false即肯定不存在
     */
    public final boolean contains(T object) {
        Preconditions.checkNotNull(object);
        return contains(getBitIndices(object));
    }

    /**
     * 子类自定义实现, 检查元素在集合中是否存在, 即对应index的bit是否都为1
     *
     * @param indices index数组
     * @return true即可能存在, false即肯定不存在
     */
    protected abstract boolean contains(long[] indices);

    /**
     * 计算目标对象哈希后映射到Bitmap的哪些index上
     * 参考{@link com.google.common.hash.BloomFilterStrategies#MURMUR128_MITZ_64}的put(T,Funnel,int,LockFreeBitArray)
     *
     * @param object 元素值
     * @return bit下标的数组
     */
    private long[] getBitIndices(T object) {
        byte[] bytes = mapper.apply(object);
        long[] longs = MurmurHash3.hash128(bytes);
        long hash1 = longs[0];
        long hash2 = longs[1];

        long[] indeces = new long[hashFunctionNum];
        //起点hash1
        long combinedHash = hash1;
        for (int i = 0; i < hashFunctionNum; i++) {
            //保证combinedHash为正数并且在[0, bitmapLength)范围内
            indeces[i] = (combinedHash & Long.MAX_VALUE) % bitmapLength;
            //递增hash2
            combinedHash += hash2;
        }

        return indeces;
    }

    //getter
    public long getBitmapLength() {
        return bitmapLength;
    }

    public int getHashFunctionNum() {
        return hashFunctionNum;
    }

    public Function<T, byte[]> getMapper() {
        return mapper;
    }
}
