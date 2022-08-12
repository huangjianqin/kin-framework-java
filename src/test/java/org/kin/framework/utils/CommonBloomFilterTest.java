package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2022/8/12
 */
public class CommonBloomFilterTest {
    public static void main(String[] args) {
        CommonBloomFilter<String> bloomFilter = new CommonBloomFilter<>(1000, 0.03);

        bloomFilter.put("1");
        bloomFilter.put("2");
        bloomFilter.put("3");
        bloomFilter.put("4");
        bloomFilter.put("5");
        bloomFilter.put("6");
        bloomFilter.put("7");

        System.out.println(bloomFilter.contains("1"));
        System.out.println(bloomFilter.contains("2"));
        System.out.println(bloomFilter.contains("3"));
        System.out.println(bloomFilter.contains("4"));
        System.out.println(bloomFilter.contains("5"));
        System.out.println(bloomFilter.contains("6"));
        System.out.println(bloomFilter.contains("7"));
        System.out.println("---------------------------");
        System.out.println(bloomFilter.contains("8"));
        System.out.println(bloomFilter.contains("9"));
        System.out.println(bloomFilter.contains("10"));
        System.out.println(bloomFilter.contains("aa"));
        System.out.println(bloomFilter.contains("bbfdsfsdfsdfsdfs"));
        System.out.println(bloomFilter.contains("cc"));
        System.out.println(bloomFilter.contains("dd"));
        System.out.println(bloomFilter.contains("ee"));
    }
}
