package org.kin.framework.utils;

import java.util.List;

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
