package org.kin.framework.utils;

import java.util.Arrays;

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

        long[] ipv6Num = NetUtils.ipv6ToLong("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
        System.out.println(Arrays.toString(ipv6Num));
        System.out.println(NetUtils.longToIpv6(ipv6Num));

        long[] ipv6Num1 = NetUtils.ipv6ToLong("ffff:ffff:ffff:::ffff:ffff:ffff");
        System.out.println(Arrays.toString(ipv6Num1));
        System.out.println(NetUtils.longToIpv6(ipv6Num1));
        System.out.println(NetUtils.compareIpv6Num(ipv6Num, ipv6Num1));

        long[] ipv6Num2 = NetUtils.ipv6ToLong("ffff:::::::ffff");
        System.out.println(Arrays.toString(ipv6Num2));
        System.out.println(NetUtils.longToIpv6(ipv6Num2));
        System.out.println(NetUtils.compareIpv6Num(ipv6Num, ipv6Num2));
        System.out.println(NetUtils.compareIpv6Num(ipv6Num1, ipv6Num2));
    }
}
