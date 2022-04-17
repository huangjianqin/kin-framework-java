package org.kin.framework.csp;

import org.kin.framework.Closeable;
import org.kin.framework.collection.CopyOnWriteMap;
import org.kin.framework.concurrent.SimpleThreadFactory;
import org.kin.framework.concurrent.ThreadPoolUtils;
import org.kin.framework.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * tps定义管理
 * @author huangjianqin
 * @date 2022/4/13
 */
public final class TpsRuleManager implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(TpsRuleManager.class);

    /** tps rule监控信息打印 */
    private final ScheduledExecutorService scheduler;

    /** 单例 */
    private static TpsRuleManager INSTANCE = new TpsRuleManager();

    /** 单例 */
    public static TpsRuleManager instance(){
        return INSTANCE;
    }

    /** key -> group , value -> tps rule 定义  */
    public Map<String, TpsRule> ruleMap = new CopyOnWriteMap<>();

    private TpsRuleManager() {
        scheduler = ThreadPoolUtils.scheduledThreadPoolBuilder()
                .coreThreads(1)
                .setRemoveOnCancelPolicy()
                .metric()
                .threadFactory(new SimpleThreadFactory("tpsRuleManager-scheduler", true))
                .build();
        scheduler.scheduleWithFixedDelay(new TpsMonitorReporter(), 900, 900, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建新的tsp rule
     */
    public synchronized void createTpsRule(TpsRuleGroupOptions groupOptions){
        String group = groupOptions.getGroup();
        if(ruleMap.containsKey(group)){
            throw new IllegalArgumentException(String.format("tps group '%s' has registered", group));
        }

        TpsRule tpsRule = new TpsRule(groupOptions);
        ruleMap.putIfAbsent(group, tpsRule);
    }

    /**
     * check tps
     */
    public boolean entry(String group, String... keys) {
        return entry(group, Arrays.asList(keys));
    }

    /**
     * check tps
     */
    public boolean entry(String group, List<String> keys) {
        TpsRule rule = ruleMap.get(group);
        if (Objects.nonNull(rule)) {
            return rule.checkTps(keys);
        }
        return true;
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }

    //--------------------------------------------------------------------
    /**
     * 打印tps rule信息
     */
    private final class TpsMonitorReporter implements Runnable {
        /** 上次打印秒钟数 */
        private long lastReportSecond = 0L;
        /** 上次打印分钟数 */
        private long lastReportMinutes = 0L;

        @Override
        public void run() {
            try {
                StringBuilder stringBuilder = new StringBuilder();

                long now = System.currentTimeMillis();
                Set<Map.Entry<String, TpsRule>> entries = ruleMap.entrySet();

                long tempSecond = 0L;
                long tempMinutes = 0L;

                //遍历所有tps rule
                for (Map.Entry<String, TpsRule> ruleEntry : entries) {
                    TpsRule rule = ruleEntry.getValue();
                    //获取上一秒window
                    TpsMonitor groupTpsMonitor = rule.getTpsMonitor();
                    TpsMonitor.TpsSlot slot = groupTpsMonitor.getTpsSlot(now - 1000L);
                    if (slot == null) {
                        continue;
                    }

                    if (lastReportSecond != 0L && lastReportSecond == slot.windowTime) {
                        //已经打印过了
                        continue;
                    }

                    String group = ruleEntry.getKey();
                    tempSecond = slot.windowTime;

                    //{group name}|group|time unit|last second window time|count|intercepted count
                    String separator = "|";
                    String timeFormatOfSecond = TimeUtils.formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(now - 1000L), ZoneId.systemDefault()));
                    stringBuilder.append(group).append(separator).append("group").append(separator).append(groupTpsMonitor.getUnit())
                            .append(separator).append(timeFormatOfSecond).append(separator)
                            .append(slot.getCounter(group).count.get()).append(separator)
                            .append(slot.getCounter(group).interceptedCount.get()).append(System.lineSeparator());

                    //遍历其所有child tps rule
                    for (Map.Entry<String, TpsMonitor> childEntry : rule.getChildMonitorMap().entrySet()) {
                        String key = childEntry.getKey();
                        TpsMonitor monitor = childEntry.getValue();

                        //获取上一window
                        TpsMonitor.TpsSlot tpsSlot = monitor.getTpsSlot(now - monitor.getUnit().toMillis(1));
                        if (tpsSlot == null) {
                            continue;
                        }

                        if (monitor.getUnit() == TimeUnit.SECONDS) {
                            if (lastReportSecond != 0L && lastReportSecond == tpsSlot.windowTime) {
                                //已经打印过了
                                continue;
                            }
                        }
                        if (monitor.getUnit() == TimeUnit.MINUTES) {
                            if (lastReportMinutes != 0L && lastReportMinutes == tpsSlot.windowTime) {
                                //已经打印过了
                                continue;
                            }
                        }

                        //{group name}|key|time unit|child window time|count|intercepted count
                        timeFormatOfSecond = TimeUtils.formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(tpsSlot.windowTime), ZoneId.systemDefault()));
                        tempMinutes = tpsSlot.windowTime;
                        if (monitor.isProtoModel()) {
                            //精准模式, 打印每一个key的tps信息
                            Map<String, TpsMonitor.SlotCounter> childTpsSlots = ((TpsMonitor.MultiTpsSlot) tpsSlot).counterMap;
                            for (Map.Entry<String, TpsMonitor.SlotCounter> counterEntry : childTpsSlots.entrySet()) {
                                stringBuilder.append(group).append(separator).append(key).append(separator)
                                        .append(monitor.getUnit()).append(separator).append(timeFormatOfSecond).append(separator)
                                        .append(counterEntry.getKey()).append(separator)
                                        .append(counterEntry.getValue().count).append(separator)
                                        .append(counterEntry.getValue().interceptedCount).append(System.lineSeparator());
                            }
                        } else {
                            stringBuilder.append(group).append(separator).append(key).append(separator)
                                    .append(monitor.getUnit()).append(separator).append(timeFormatOfSecond).append(separator)
                                    .append(tpsSlot.getCounter(group).count.get()).append(separator)
                                    .append(tpsSlot.getCounter(group).interceptedCount.get()).append(System.lineSeparator());
                        }
                    }
                }

                //更新打印时间
                if (tempSecond > 0) {
                    lastReportSecond = tempSecond;
                }

                if (tempMinutes > 0) {
                    lastReportMinutes = tempMinutes;
                }

                if (stringBuilder.length() > 0) {
                    log.debug("tps reporting... ".concat(System.lineSeparator()).concat(stringBuilder.toString()));
                }
            } catch (Throwable throwable) {
                log.debug("tps report error", throwable);
            }
        }
    }
}
