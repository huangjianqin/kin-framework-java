package org.kin.framework.csp;

/**
 * tps rule监控类型
 *
 * @author huangjianqin
 * @date 2022/4/13
 */
public enum TpsMonitorType {
    /** monitor mode */
    MONITOR("only monitor, not reject request."),
    /** intercept mode */
    INTERCEPT("reject request if tps over limit");

    /** 描述 */
    private final String desc;

    TpsMonitorType(String desc) {
        this.desc = desc;
    }

    //getter
    public String getDesc() {
        return desc;
    }
}
