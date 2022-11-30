package org.kin.framework.utils;

import java.util.List;
import java.util.Map;

/**
 * @author huangjianqin
 * @date 2022/11/29
 */
public class YamlUtilsTest {
    public static void main(String[] args) {
        System.out.println(YamlUtils.loadYaml2Bean("test.yaml", Config.class));
    }

    @ConfigurationProperties("kin")
    public static class Config {
        private int a;
        private String b;
        private String c;
        private List<String> d;
        private PropertiesUtilsTest.Config2 c2;
        private List<PropertiesUtilsTest.Item> il;
        private Map<String, PropertiesUtilsTest.Item> im;
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

        public PropertiesUtilsTest.Config2 getC2() {
            return c2;
        }

        public void setC2(PropertiesUtilsTest.Config2 c2) {
            this.c2 = c2;
        }

        public List<PropertiesUtilsTest.Item> getIl() {
            return il;
        }

        public void setIl(List<PropertiesUtilsTest.Item> il) {
            this.il = il;
        }

        public Map<String, PropertiesUtilsTest.Item> getIm() {
            return im;
        }

        public void setIm(Map<String, PropertiesUtilsTest.Item> im) {
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
        private PropertiesUtilsTest.Config3 c3;

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

        public PropertiesUtilsTest.Config3 getC3() {
            return c3;
        }

        public void setC3(PropertiesUtilsTest.Config3 c3) {
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
