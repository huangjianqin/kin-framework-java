package org.kin.framework.utils;

import org.kin.framework.proxy.ProxyInvoker;

/**
 * @author huangjianqin
 * @date 2021/7/20
 */
public class ClassScanUtilsTest {
    public static void main(String[] args) {
        System.out.println(ClassScanUtils.scan("org.kin", ProxyInvoker.class));
        System.out.println(ClassScanUtils.scan("org.kin", SPI.class));
    }
}
