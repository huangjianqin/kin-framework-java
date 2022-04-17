package org.kin.framework.csp;

import org.kin.framework.utils.CollectionUtils;
import org.kin.framework.utils.StringUtils;
import org.kin.framework.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author huangjianqin
 * @date 2022/4/13
 */
final class TpsRule {
    private static final Logger log = LoggerFactory.getLogger(TpsRule.class);

    /** tps rule开始生效时间 */
    private final long startTime;
    /** tps rule group name */
    private String group;
    /** group级别的tps rule monitor */
    private TpsMonitor tpsMonitor;
    /** key -> 比group更细粒度的监控key, value -> tps monitor */
    private volatile Map<String, TpsMonitor> childMonitorMap = new HashMap<>();

    public TpsRule(TpsRuleGroupOptions groupOptions) {
        this.startTime = TimeUtils.trimMillsOfSecond(System.currentTimeMillis());
        //创建group级别tps monitor, 默认配置
        this.tpsMonitor = new TpsMonitor(startTime, TimeUnit.SECONDS, TpsModel.FUZZY, TpsMonitorType.MONITOR, -1);
        //应用配置, 更新rule
        applyOptions(groupOptions);
    }

    /**
     * stop所有细粒度的tps监控
     */
    private synchronized void stopAllChildMonitors() {
        childMonitorMap = new HashMap<>(4);
    }

    /**
     * 校验tps limit
     */
    public boolean checkTps(List<String> keys) {
        long now = System.currentTimeMillis();
        //获取group级别的window slot
        TpsMonitor.TpsSlot tpsSlot = tpsMonitor.getAndResetTpsSlot(now);

        //1. check keys
        //通过tps的window slot
        List<TpsMonitor.SlotCounter> passedSlots = new ArrayList<>();
        for (String key : keys) {
            //遍历所有child monitor
            for (Map.Entry<String, TpsMonitor> entry : childMonitorMap.entrySet()) {
                if (!StringUtils.match(entry.getKey(), key)) {
                    //key匹配
                    continue;
                }

                TpsMonitor tpsMonitor = entry.getValue();
                //获取child monitor window slot
                TpsMonitor.TpsSlot currentTps = tpsMonitor.getAndResetTpsSlot(now);
                //child max tps
                long maxTps = tpsMonitor.getMaxTps();
                //tps count
                TpsMonitor.SlotCounter counter = currentTps.getCounter(key);
                //是否tps溢出
                boolean overLimit = maxTps >= 0 && counter.count.longValue() >= maxTps;
                if (overLimit) {
                    log.warn("tps over limit, group=`{}`, barrier=`{}`，monitorType=`{}`, maxTps=`{}`",
                            group, entry.getKey(), tpsMonitor.getMonitorType(), maxTps + "/" + tpsMonitor.getUnit());
                    if (tpsMonitor.isInterceptMode()) {
                        //child monitor若是intercept mode, 则直接返回false, 中断请求
                        currentTps.getCounter(key).interceptedCount.incrementAndGet();
                        tpsSlot.getCounter(key).interceptedCount.incrementAndGet();
                        return false;
                    }
                } else {
                    passedSlots.add(counter);
                }
            }
        }

        //2. check total tps
        //group max tps
        long maxTps = tpsMonitor.getMaxTps();
        //group级别是否tps溢出
        boolean overLimit = maxTps >= 0 && tpsSlot.getCounter(group).count.longValue() >= maxTps;
        if (overLimit) {
            log.warn("tps over limit, group=`{}`, barrier=`{}`，monitorType=`{}`, maxTps=`{}`",
                    group, "groupRule", tpsMonitor.getMonitorType(), maxTps + "/" + tpsMonitor.getUnit());
            if (tpsMonitor.isInterceptMode()) {
                //group monitor若是intercept mode, 则直接返回false, 中断请求
                tpsSlot.getCounter(group).interceptedCount.incrementAndGet();
                return false;
            }
        }

        //统计tps pass次数
        tpsSlot.getCounter(group).count.incrementAndGet();
        for (TpsMonitor.SlotCounter passedTpsSlot : passedSlots) {
            passedTpsSlot.count.incrementAndGet();
        }

        //3. check pass
        return true;
    }

    /**
     * 应用tps配置
     */
    public synchronized void applyOptions(TpsRuleGroupOptions groupOptions) {
        //1. reset all tps monitor if null
        if (groupOptions == null) {
            log.debug("clear all tps monitor for group '{}'", group);
            this.tpsMonitor.clear();
            this.stopAllChildMonitors();
            return;
        }

        //2. check group name
        String group = groupOptions.getGroup();
        if(StringUtils.isBlank(group)){
            throw new IllegalArgumentException("tps group name is blank");
        }

        log.debug("tps group '{}' options applying...", group);

        if(StringUtils.isNotBlank(this.group) && !groupOptions.getGroup().equals(this.group)){
            log.debug("options group name '{}' conflict", group);
            return;
        }

        //3. check tps rule
        TpsRuleOptions groupRule = groupOptions.getGroupRule();
        this.group = group;
        if (groupRule == null) {
            log.debug("clear group tps monitor for group '{}'", group);
            this.tpsMonitor.clear();
        } else {
            log.debug("update group tps monitor for group '{}', '{}'", group, groupRule);
            this.tpsMonitor.updateMonitorConfig(groupRule.getMonitorType(), groupRule.getMaxTps());
        }

        //4. check rest child rules
        Map<String, TpsRuleOptions> childRuleMap = groupOptions.getChildRuleMap();
        if (CollectionUtils.isEmpty(childRuleMap)) {
            log.debug("clear all child tps monitor for group '{}'", group);
            this.stopAllChildMonitors();
        } else {
            Map<String, TpsMonitor> newChildMonitorMap = new HashMap<>(this.childMonitorMap.size());
            for (Map.Entry<String, TpsRuleOptions> entry : childRuleMap.entrySet()) {
                String key = entry.getKey();
                TpsRuleOptions options = entry.getValue();

                TpsMonitor tpsMonitor;
                if (this.childMonitorMap.containsKey(key)) {
                    //update rule
                    tpsMonitor = this.childMonitorMap.get(key);
                    log.debug("update child tps monitor for group '{}' key '{}', '{}'", group, key, options);

                    if (!Objects.equals(tpsMonitor.getUnit(), options.getUnit()) ||
                            !Objects.equals(tpsMonitor.getModel(), options.getModel())) {
                        tpsMonitor = new TpsMonitor(startTime, options);
                    } else {
                        tpsMonitor.updateMonitorConfig(options.getMonitorType(), options.getMaxTps());
                    }
                } else {
                    //add rule
                    log.debug("add child tps monitor for group '{}' key '{}', '{}'", group, key, options);
                    tpsMonitor = new TpsMonitor(startTime, options);
                }
                newChildMonitorMap.put(key, tpsMonitor);
            }

            for (String key : newChildMonitorMap.keySet()) {
                if(!this.childMonitorMap.containsKey(key)){
                    continue;
                }

                log.debug("delete child tps monitor for group '{}' key '{}'", group, key);
            }

            this.childMonitorMap = newChildMonitorMap;
        }
    }

    //getter
    TpsMonitor getTpsMonitor() {
        return tpsMonitor;
    }

    Map<String, TpsMonitor> getChildMonitorMap() {
        return childMonitorMap;
    }
}
