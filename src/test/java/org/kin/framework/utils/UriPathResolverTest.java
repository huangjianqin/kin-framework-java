package org.kin.framework.utils;

/**
 * @author huangjianqin
 * @date 2022/11/18
 */
public class UriPathResolverTest {
    public static void main(String[] args) {
        UriPathResolver resolver1 = new UriPathResolver("/a/b");
        UriPathResolver resolver2 = new UriPathResolver("/a/*");
        UriPathResolver resolver3 = new UriPathResolver("/a/*/b");
        UriPathResolver resolver4 = new UriPathResolver("/a/{p1}/{p2}/{p3}");

        System.out.println("1--------------");
        System.out.println(resolver1.matches("/a/b/c"));
        System.out.println(resolver1.matches("/a/"));
        System.out.println(resolver1.matches("/a/*"));

        System.out.println("2--------------");
        System.out.println(resolver2.matches("/a/b/c"));
        System.out.println(resolver2.matches("/a/"));
        System.out.println(resolver2.matches("/a/*"));

        System.out.println("3--------------");
        System.out.println(resolver3.matches("/a/b/c"));
        System.out.println(resolver3.matches("/a/"));
        System.out.println(resolver3.matches("/a/*"));
        System.out.println(resolver3.matches("/a/b"));
        System.out.println(resolver3.matches("/a/aa/b"));

        System.out.println("4--------------");
        System.out.println(resolver4.matches("/a/b/c/d"));
        System.out.println(resolver4.match("/a/b/c/d"));
        System.out.println(resolver4.matches("/a/"));
        System.out.println(resolver4.matches("/a/*"));
    }
}
