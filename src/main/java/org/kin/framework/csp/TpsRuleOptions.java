package org.kin.framework.csp;

import java.util.concurrent.TimeUnit;

/**
 * tps rule配置
 * @author huangjianqin
 * @date 2022/4/13
 */
public final class TpsRuleOptions {
    /** tps limit */
    private long maxTps = -1;
    /** tps时间单位 */
    private TimeUnit unit = TimeUnit.SECONDS;
    /** tps rule 模式 */
    private  TpsModel model = TpsModel.FUZZY;
    /** tps rule 监控模式 */
    private TpsMonitorType monitorType = TpsMonitorType.MONITOR;

    private TpsRuleOptions() {
    }

    //getter
    public long getMaxTps() {
        return maxTps;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public TpsModel getModel() {
        return model;
    }

    public TpsMonitorType getMonitorType() {
        return monitorType;
    }

    @Override
    public String toString() {
        return "TpsRuleOptions{" +
                "maxTps=" + maxTps +
                ", unit=" + unit +
                ", model=" + model +
                ", monitorType=" + monitorType +
                '}';
    }

    //------------------------------------------builder
    public static Builder builder() {
        return new Builder();
    }

    /** builder **/
    public static class Builder {
        private final TpsRuleOptions tpsRuleOptions = new TpsRuleOptions();

        public Builder maxTps(long maxTps) {
            tpsRuleOptions.maxTps = maxTps;
            return this;
        }

        public Builder unit(TimeUnit unit) {
            tpsRuleOptions.unit = unit;
            return this;
        }

        public Builder model(TpsModel model) {
            tpsRuleOptions.model = model;
            return this;
        }

        public Builder monitorType(TpsMonitorType monitorType) {
            tpsRuleOptions.monitorType = monitorType;
            return this;
        }

        public TpsRuleOptions build() {
            return tpsRuleOptions;
        }
    }
}
