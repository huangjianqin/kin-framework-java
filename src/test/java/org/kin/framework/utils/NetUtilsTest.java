package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2019-12-12
 */
public class NetUtilsTest {
    public static void main(String[] args) {
        System.out.println(NetUtils.getLocalAddress());
        System.out.println(NetUtils.getLocalhost());
        System.out.println(NetUtils.getLocalhostIp());
        System.out.println(NetUtils.getLocalhost4Ip());
        System.out.println(NetUtils.getLocalhost6Ip());
        //检查端口是否被占用
        System.out.println(NetUtils.isValidPort(16888));
    }
}
