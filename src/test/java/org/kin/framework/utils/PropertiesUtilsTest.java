package org.kin.framework.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author huangjianqin
 * @date 2022/11/28
 */
public class PropertiesUtilsTest {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

        properties.load(PropertiesUtilsTest.class.getClassLoader().getResourceAsStream("test.properties"));

        properties.put("kin.a", "1");
        properties.put("kin.b", "2");
        properties.put("kin.c", "3");
        properties.put("kin.e", "4");
        properties.put("kin.f", Arrays.asList("1", "2", "3"));
        properties.put("kin.c2.A.a2", "11");
        properties.put("kin.c2.A.b2", "12");
        properties.put("kin.c2.A.c2", "13");
        properties.put("kin.c2.A.c3.B.a3", "21");
        properties.put("kin.c2.A.c3.B.b3", "22");
        properties.put("kin.c2.A.c3.B.c3", "23");

        System.out.println(PropertiesUtils.toBean(properties, Config.class));
    }

    @ConfigurationProperties("kin")
    public static class Config {
        private int a;
        private String b;
        private String c;
        private List<String> d;
        private Config2 c2;

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

        public void setC(String c) {
            this.c = c;
        }

        public List<String> getD() {
            return d;
        }

        public void setD(List<String> d) {
            this.d = d;
        }

        public Config2 getC2() {
            return c2;
        }

        public void setC2(Config2 c2) {
            this.c2 = c2;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "a=" + a +
                    ", b='" + b + '\'' +
                    ", c='" + c + '\'' +
                    ", d=" + d +
                    ", c2=" + c2 +
                    '}';
        }
    }

    @ConfigurationProperties("A")
    public static class Config2 {
        private int a2;
        private String b2;
        private String c2;
        private Config3 c3;

        //setter && getter
        public int getA2() {
            return a2;
        }

        public void setA2(int a2) {
            this.a2 = a2;
        }

        public String getB2() {
            return b2;
        }

        public void setB2(String b2) {
            this.b2 = b2;
        }

        public String getC2() {
            return c2;
        }

        public void setC2(String c2) {
            this.c2 = c2;
        }

        public Config3 getC3() {
            return c3;
        }

        public void setC3(Config3 c3) {
            this.c3 = c3;
        }

        @Override
        public String toString() {
            return "Config2{" +
                    "a2=" + a2 +
                    ", b2='" + b2 + '\'' +
                    ", c2='" + c2 + '\'' +
                    ", c3=" + c3 +
                    '}';
        }
    }

    @ConfigurationProperties("B")
    public static class Config3 {
        private int a3;
        private String b3;
        private String c3;

        public int getA3() {
            return a3;
        }

        public void setA3(int a3) {
            this.a3 = a3;
        }

        public String getB3() {
            return b3;
        }

        public void setB3(String b3) {
            this.b3 = b3;
        }

        public String getC3() {
            return c3;
        }

        public void setC3(String c3) {
            this.c3 = c3;
        }

        @Override
        public String toString() {
            return "Config3{" +
                    "a3=" + a3 +
                    ", b3='" + b3 + '\'' +
                    ", c3='" + c3 + '\'' +
                    '}';
        }
    }
}
