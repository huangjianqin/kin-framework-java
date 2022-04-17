package org.kin.framework.csp;

import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.utils.TimeUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 监控tps计数统计
 *
 * @author huangjianqin
 * @date 2022/4/13
 */
final class TpsMonitor {
    /** 与{@link TpsRule#startTime}一致, 即{@link TpsRule}注册时间 */
    private final long startTime;
    /** tps window时间单位 */
    private final TimeUnit unit;
    /** item -> window内tps计数统计 */
    private final List<TpsSlot> slotList;
    /** window内tps计数最大上线 */
    private long maxTps = -1;
    /** tps模式 */
    private TpsModel model;
    /** tps监控类型 */
    private TpsMonitorType monitorType = TpsMonitorType.MONITOR;

    TpsMonitor(long startTime, TpsRuleOptions options) {
        this(startTime, options.getUnit(), options.getModel(), options.getMonitorType(), options.getMaxTps());
    }

    TpsMonitor(long startTime, TimeUnit unit, TpsModel model, TpsMonitorType monitorType, long maxTps) {
        if (unit.equals(TimeUnit.MINUTES)) {
            this.startTime = TimeUtils.trimMillsOfMinute(startTime);
        } else if (unit.equals(TimeUnit.HOURS)) {
            this.startTime = TimeUtils.trimMillsOfHour(startTime);
        } else {
            this.startTime = startTime;
        }
        this.unit = unit;
        this.maxTps = maxTps;
        this.model = model;
        this.monitorType = monitorType;

        int slotSize = 11;
        List<TpsSlot> slotList = new ArrayList<>(slotSize);
        for (int i = 0; i < slotSize; i++) {
            slotList.add(isProtoModel() ? new MultiTpsSlot() : new SingleTpsSlot());
        }
        this.slotList = slotList;
    }

    /**
     * 获取{@link TpsSlot}实例, 如果窗口时间已过期, 则{@link TpsSlot#reset(long)}
     *
     * @param timestamp 时间戳
     * @return tps slot
     */
    TpsSlot getAndResetTpsSlot(long timestamp) {
        //时间间隔
        long distance = timestamp - startTime;
        //window槽位总数
        int slotSize = slotList.size();
        //单个window时间
        long singleWindowTime = unit.toMillis(1);
        //经历过多少个window
        long windowNum = (distance < 0 ? distance + singleWindowTime * slotSize : distance) / singleWindowTime;
        //当前window的起始时间
        long curWindowTime = startTime + windowNum * singleWindowTime;
        //取槽位index
        int slotIndex = (int) windowNum % slotSize;
        //槽位
        TpsSlot tpsSlot = slotList.get(slotIndex);
        if (tpsSlot.windowTime != curWindowTime) {
            //如果该槽位对应的窗口时间已过期, 则reset
            tpsSlot.reset(curWindowTime);
        }
        return tpsSlot;
    }

    /**
     * 获取{@link TpsSlot}实例
     *
     * @param timestamp 时间戳
     * @return tps slot
     */
    @Nullable
    TpsSlot getTpsSlot(long timestamp) {
        //时间间隔
        long distance = timestamp - startTime;
        //window槽位总数
        int slotSize = slotList.size();
        //单个window时间
        long singleWindowTime = unit.toMillis(1);
        //经历过多少个window
        long windowNum = (distance < 0 ? distance + singleWindowTime * slotSize : distance) / singleWindowTime;
        //当前window的起始时间
        long curWindowTime = startTime + windowNum * singleWindowTime;
        //取槽位index
        int slotIndex = (int) windowNum % slotSize;
        //槽位
        TpsSlot tpsSlot = slotList.get(slotIndex);
        if (tpsSlot.windowTime != curWindowTime) {
            return null;
        }
        return tpsSlot;
    }

    /**
     * clear, 不监控
     */
    void clear() {
        this.monitorType = TpsMonitorType.MONITOR;
        this.maxTps = -1;
    }

    /**
     * 更新监控配置
     */
    void updateMonitorConfig(TpsMonitorType monitorType, long maxTps) {
        this.monitorType = monitorType;
        this.maxTps = maxTps;
    }

    /**
     * 是否是精准模式
     */
    boolean isProtoModel() {
        return TpsModel.PROTO.equals(this.model);
    }

    /**
     * 是否是拦截模式
     */
    boolean isInterceptMode() {
        return TpsMonitorType.INTERCEPT.equals(this.monitorType);
    }

    //getter
    List<TpsSlot> getSlotList() {
        return slotList;
    }

    TpsModel getModel() {
        return model;
    }

    TpsMonitorType getMonitorType() {
        return monitorType;
    }

    long getMaxTps() {
        return maxTps;
    }

    TimeUnit getUnit() {
        return unit;
    }

    //--------------------------------------------------------------------------------

    /**
     * 窗口槽位
     */
    static abstract class TpsSlot {
        /** 窗口起始时间 */
        protected long windowTime = 0L;

        /**
         * 获取tps计数器
         */
        abstract SlotCounter getCounter(String key);

        /**
         * 重置tps计数器
         */
        abstract void reset(long windowTime);
    }

    /**
     * 不区分key, 所有流入都计数,
     */
    static final class SingleTpsSlot extends TpsSlot {
        /** 计数器 */
        SlotCounter counter = new SlotCounter();

        @Override
        SlotCounter getCounter(String key) {
            return counter;
        }

        @Override
        synchronized void reset(long windowTime) {
            if (this.windowTime == windowTime) {
                return;
            }
            this.windowTime = windowTime;
            counter.count.set(0L);
            counter.interceptedCount.set(0);
        }

        @Override
        public String toString() {
            return "SingleTpsSlot{" +
                    "windowTime=" + windowTime +
                    ", counter=" + counter +
                    "} " + super.toString();
        }
    }

    static final class MultiTpsSlot extends TpsSlot {
        /** 计数器 */
        Map<String, SlotCounter> counterMap = new CopyOnWriteMap<>();

        @Override
        SlotCounter getCounter(String key) {
            if (!counterMap.containsKey(key)) {
                counterMap.putIfAbsent(key, new SlotCounter());
            }
            return counterMap.get(key);
        }

        @Override
        synchronized void reset(long windowTime) {
            if (this.windowTime == windowTime) {
                return;
            }
            this.windowTime = windowTime;
            counterMap = new CopyOnWriteMap<>();
        }

        @Override
        public String toString() {
            return "MultiTpsSlot{" +
                    "windowTime=" + windowTime +
                    ", counterMap=" + counterMap +
                    "} " + super.toString();
        }
    }

    /**
     * tps计数器
     */
    static final class SlotCounter {
        /** tps统计次数 */
        final AtomicLong count = new AtomicLong();
        /** tps limit拦截次数 */
        final AtomicLong interceptedCount = new AtomicLong();

        @Override
        public String toString() {
            return "{" + count + "|" + interceptedCount + '}';
        }
    }
}
