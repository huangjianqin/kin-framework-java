package org.kin.framework.csp;

/**
 * tps监控模式
 * @author huangjianqin
 * @date 2022/4/13
 */
public enum TpsModel {
    /** 模糊模式 */
    FUZZY("模糊"),
    /** 精准模式 */
    PROTO("精准"),
    ;

    /** 描述 */
    private final String desc;

    TpsModel(String desc) {
        this.desc = desc;
    }

    //getter
    public String getDesc() {
        return desc;
    }
}
