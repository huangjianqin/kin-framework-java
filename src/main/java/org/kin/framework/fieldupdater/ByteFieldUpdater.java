package org.kin.framework.fieldupdater;

/**
 * @author huangjianqin
 * @date 2021/11/27
 */
public interface ByteFieldUpdater<U> {
    void set(U obj, byte newValue);

    byte get(U obj);
}
