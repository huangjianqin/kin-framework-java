package org.kin.framework.utils;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2024/5/25
 */
public class CertUtilsTest {
    public static void main(String[] args) {
        Date startDate = Date.from(TimeUtils.parseDateTime("2024-05-25 12:00:00")
                .atZone(ZoneOffset.systemDefault())
                .toInstant());
        Date endDate = Date.from(TimeUtils.parseDateTime("2024-05-25 12:00:00")
                .atZone(ZoneOffset.systemDefault())
                .toInstant());
        /*
            CN：Common Name（常用名称）
            O：Organization（组织）
            OU：Organizational Unit（组织单位）
            L：Locality（地区）
            ST：State or Province（州或省）
            C：Country（国家）
         */
        Map<String, Object> map = CertUtils.genCertAndKeys(
                "CN=Master, OU=37, O=37, L=GD, ST=GZ, C=CN",
                "CN=Server, OU=37A, O=37A, L=GD, ST=FS, C=CN",
                "CN=Client, OU=37A, O=37A, L=GD, ST=FS, C=CN",
                startDate, endDate);
        System.out.println(map);
    }
}
