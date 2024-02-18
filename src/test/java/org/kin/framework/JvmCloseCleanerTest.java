package org.kin.framework;

/**
 * @author huangjianqin
 * @date 2024/2/18
 */
public class JvmCloseCleanerTest {
    public static void main(String[] args) throws InterruptedException {
        JvmCloseCleaner.instance().add(() -> {
            System.out.println("1 shutdown");
        });

        JvmCloseCleaner.instance().add(() -> {
            throw new RuntimeException("2 shutdown error");
        });

        JvmCloseCleaner.instance().add("3", 3, () -> {
            System.out.println("3 shutdown");
        });

        JvmCloseCleaner.instance().add("4", 3, () -> {
            throw new RuntimeException("4 shutdown error");
        });

        Thread.sleep(1_000);
    }
}
