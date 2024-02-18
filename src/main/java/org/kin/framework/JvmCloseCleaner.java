package org.kin.framework;

import com.google.common.base.Preconditions;
import org.kin.framework.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 用于控制jvm close时, 释放占用资源
 *
 * @author huangjianqin
 * @date 2019/2/28
 */
public final class JvmCloseCleaner {
    private final Logger log = LoggerFactory.getLogger(JvmCloseCleaner.class);

    /** 最低优先级 */
    public static final int MIN_PRIORITY = 1;
    /** 一般优先级 */
    public static final int MIDDLE_PRIORITY = 5;
    /** 最高优先级 */
    public static final int MAX_PRIORITY = 10;

    private static final JvmCloseCleaner INSTANCE = new JvmCloseCleaner();

    private final List<DelegatingOrderedCloseable> orderedCloseableList = new CopyOnWriteArrayList<>();

    private JvmCloseCleaner() {
        waitingClose();
    }

    private void waitingClose() {
        //等spring容器完全初始化后执行
        Thread hookThread = new Thread(() -> {
            this.orderedCloseableList.sort(Comparator.comparingInt(DelegatingOrderedCloseable::getNPriority));
            for (DelegatingOrderedCloseable wrapper : this.orderedCloseableList) {
                wrapper.close();
            }
        });
        hookThread.setName("Shutdown-Hook");
        Runtime.getRuntime().addShutdownHook(hookThread);
    }

    public void add(Closeable closeable) {
        add(MIN_PRIORITY, closeable);

    }

    public void addAll(Closeable... closeables) {
        addAll(MIN_PRIORITY, Arrays.asList(closeables));
    }

    public void addAll(Collection<Closeable> closeables) {
        addAll(MIN_PRIORITY, closeables);
    }

    public void add(int priority, Closeable closeable) {
        addAll(priority, closeable);
    }

    public void addAll(int priority, Closeable... closeables) {
        addAll(priority, Arrays.asList(closeables));
    }

    public void addAll(int priority, Collection<Closeable> closeables) {
        Preconditions.checkArgument(0 < priority && priority <= MAX_PRIORITY, "priority must be range from %s to %s", MIN_PRIORITY, MAX_PRIORITY);
        List<DelegatingOrderedCloseable> orderedCloseableList = closeables.stream().map(c -> new DelegatingOrderedCloseable(c, priority)).collect(Collectors.toList());
        this.orderedCloseableList.addAll(orderedCloseableList);
    }

    public void add(String name, Closeable closeable) {
        add(name, MIN_PRIORITY, closeable);

    }
    public void add(String name, int priority, Closeable closeable) {
        Preconditions.checkArgument(0 < priority && priority <= MAX_PRIORITY, "priority must be range from %s to %s", MIN_PRIORITY, MAX_PRIORITY);
        this.orderedCloseableList.add(new DelegatingOrderedCloseable(name, closeable, priority));
    }

    public static JvmCloseCleaner instance() {
        return INSTANCE;
    }

    //---------------------------------------------------------------------------------------------------

    /**
     * 支持带优先级的且委托其他{@link Closeable}执行close逻辑的{@link Closeable}实例
     */
    private class DelegatingOrderedCloseable implements Closeable {
        @Nullable
        private String name;
        /** 真正的{@link Closeable}实例 */
        private final Closeable closeable;
        /** 优先级 */
        private final int priority;

        public DelegatingOrderedCloseable(Closeable closeable, int priority) {
            this.closeable = closeable;
            this.priority = priority;
        }

        public DelegatingOrderedCloseable(String name, Closeable closeable, int priority) {
            this.name = name;
            this.closeable = closeable;
            this.priority = priority;
        }

        /**
         * 获取取反后的优先级
         */
        public int getNPriority() {
            return -priority;
        }

        @Override
        public void close() {
            String name = StringUtils.isNotBlank(this.name) ? this.name : closeable.getClass().getName();
            log.info("{} shutting down...", name);
            long startTime = System.currentTimeMillis();
            try {
                closeable.close();
                long endTime = System.currentTimeMillis();
                log.info("{} shutdown complete, cost {} ms", name, endTime - startTime);
            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                log.error("{} shutdown fail, cost {} ms {}", name, endTime - startTime, e);
            }
        }

        //getter
        @Nullable
        public String getName() {
            return name;
        }

        public Closeable getCloseable() {
            return closeable;
        }

        public int getPriority() {
            return priority;
        }
    }
}
