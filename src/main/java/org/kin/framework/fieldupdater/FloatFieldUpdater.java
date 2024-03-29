package org.kin.framework.fieldupdater;

/**
 * @author huangjianqin
 * @date 2021/11/27
 */
public interface FloatFieldUpdater<U> {
    void set(U obj, float newValue);

    float get(U obj);
}
