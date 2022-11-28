package org.kin.framework.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author huangjianqin
 * @date 2022/11/28
 */
public class PropertiesUtilsTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("kin.a", "1");
        properties.put("kin.b", "2");
        properties.put("kin.c", "3");
        properties.put("kin.e", "4");
        properties.put("kin.f", Arrays.asList("1", "2", "3"));

        System.out.println(PropertiesUtils.toBean(properties, Config.class));
    }

    @ConfigurationProperties("kin")
    public static class Config {
        private int a;
        private String b;
        private String c;
        private List<String> d;

        //setter && getter
        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        @ConfigurationProperties("e")
        public void setC(String c) {
            this.c = c;
        }

        public List<String> getD() {
            return d;
        }

        @ConfigurationProperties("f")
        public void setD(List<String> d) {
            this.d = d;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "a=" + a +
                    ", b='" + b + '\'' +
                    ", c='" + c + '\'' +
                    ", d=" + d +
                    '}';
        }
    }
}
