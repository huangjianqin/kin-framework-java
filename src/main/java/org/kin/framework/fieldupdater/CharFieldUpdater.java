package org.kin.framework.fieldupdater;

/**
 * @author huangjianqin
 * @date 2021/11/27
 */
public interface CharFieldUpdater<U> {
    void set(U obj, char newValue);

    char get(U obj);
}
