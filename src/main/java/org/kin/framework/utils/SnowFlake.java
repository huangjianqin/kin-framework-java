package org.kin.framework.utils;

/**
 * 1bit + 41bits 时间戳 + 10bit机器id(5bit data center id+ 5bit worker id) + 12bit sequence id
 *
 * @author huangjianqin
 * @date 2022/11/5
 */
public class SnowFlake {
    /** 开始时间截 (2015-01-01) */
    public static final long TWEPOCH = 1420041600000L;

    /** 机器id所占的位数 */
    public static final long WORKER_ID_BITS = 5L;
    /** 数据标识id所占的位数 */
    public static final long DATACENTER_ID_BITS = 5L;
    /** 支持的最大机器id, 结果是31 */
    public static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    /** 支持的最大数据标识id, 结果是31 */
    public static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /** 序列在id中占的位数 */
    public static final long SEQUENCE_BITS = 12L;
    /** 最大序列值, 结果是4095 */
    public static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /** 机器ID向左移12位 */
    public static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    /** 数据标识id向左移17位(12+5) */
    public static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    /** 时间截向左移22位(5+5+12) */
    public static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;


    /** 工作机器ID(0~31) */
    protected final long workerId;
    /** 数据中心ID(0~31) */
    protected final long datacenterId;
    /** 上次毫秒内序列(0~4095) */
    protected long lastSequence = 0L;
    /** 上次生成ID的时间截 */
    protected long lastTimestamp = -1L;

    /**
     * @param workerId     工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public SnowFlake(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    /**
     * 获得下一个ID (该方法是线程安全的)
     *
     * @return SnowflakeId
     */
    public long nextId() {
        long timestamp;
        long sequence;
        synchronized (this) {
            timestamp = System.currentTimeMillis();

            //如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过, 则应当抛出异常
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                        String.format("clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
            }

            //如果是同一时间生成的，则进行毫秒内序列
            if (lastTimestamp == timestamp) {
                lastSequence = (lastSequence + 1) & MAX_SEQUENCE;
                //毫秒内序列溢出
                if (lastSequence == 0) {
                    //阻塞到下一个毫秒,获得新的时间戳
                    timestamp = waitNextMillis(lastTimestamp);
                }
            }
            //时间戳改变，毫秒内序列重置
            else {
                lastSequence = 0L;
            }
            //记录上次生成ID的时间截
            lastTimestamp = timestamp;
            //记录最后确定的sequence值
            sequence = lastSequence;
        }

        //组装成64位ID
        return ((timestamp - TWEPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     *
     * @param lastTimestamp 上次生成ID的时间截
     * @return 当前时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = timestampGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timestampGen();
        }
        return timestamp;
    }

    /**
     * 返回以毫秒为单位的当前时间
     *
     * @return 当前时间(毫秒)
     */
    private long timestampGen() {
        return System.currentTimeMillis();
    }

    //getter
    public long getWorkerId() {
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }
}