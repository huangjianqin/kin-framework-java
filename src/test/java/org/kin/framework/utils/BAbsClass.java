package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2023/6/20
 */
@Extension("B")
public class BAbsClass extends AbsClass{
    private final String desc;

    public BAbsClass(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BAbsClass{" +
                "desc='" + desc + '\'' +
                '}';
    }
}
