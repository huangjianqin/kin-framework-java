package org.kin.framework.utils;

import java.io.IOException;
import java.util.*;

/**
 * @author huangjianqin
 * @date 2022/11/28
 */
public class PropertiesUtilsTest {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

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

        //item list
        Map<String, Object> map = new HashMap<>();
        map.put("i1", 0);
        map.put("i2", "i");
        Map<String, Object> map1 = new HashMap<>();
        map1.put("i1", 1);
        map1.put("i2", "ii1");
        Map<String, Object> map2 = new HashMap<>();
        map2.put("i1", 2);
        map2.put("i2", "iii2");
        properties.put("kin.il", Arrays.asList(map, map1, map2));

        //item map
        Map<String, Object> map3 = new HashMap<>();
        map3.put("a", map);
        map3.put("b", map3);
        map3.put("c", map2);
        properties.put("kin.im", map3);

        //primitive map
        Map<String, Object> map4 = new HashMap<>();
        map4.put("a", 1);
        map4.put("b", 2);
        map4.put("c", 3);
        properties.put("kin.ip", map4);

        System.out.println(PropertiesUtils.toPropertiesBean(properties, Config.class));
    }

    @ConfigurationProperties("kin")
    public static class Config {
        private int a;
        private String b;
        private String c;
        private List<String> d;
        private Config2 c2;
        private List<Item> il;
        private Map<String, Item> im;
        private Map<String, Integer> ip;

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

        public List<Item> getIl() {
            return il;
        }

        public void setIl(List<Item> il) {
            this.il = il;
        }

        public Map<String, Item> getIm() {
            return im;
        }

        public void setIm(Map<String, Item> im) {
            this.im = im;
        }

        public Map<String, Integer> getIp() {
            return ip;
        }

        public void setIp(Map<String, Integer> ip) {
            this.ip = ip;
        }

        @Override
        public String toString() {
            return "Config{" +
                    "a=" + a +
                    ", b='" + b + '\'' +
                    ", c='" + c + '\'' +
                    ", d=" + d +
                    ", c2=" + c2 +
                    ", il=" + il +
                    ", im=" + im +
                    ", ip=" + ip +
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

    public static class Item {
        private int i1;
        private String i2;

        //setter && getter

        public int getI1() {
            return i1;
        }

        public void setI1(int i1) {
            this.i1 = i1;
        }

        public String getI2() {
            return i2;
        }

        public void setI2(String i2) {
            this.i2 = i2;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "i1=" + i1 +
                    ", i2='" + i2 + '\'' +
                    '}';
        }
    }
}
