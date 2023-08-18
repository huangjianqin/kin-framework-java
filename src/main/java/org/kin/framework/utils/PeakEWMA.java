package org.kin.framework.utils;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * 基于时间差(耗时)β变化的peak EWMA预测工具
 * 一般需要lazy init
 *
 * @author huangjianqin
 * @date 2021/12/8
 */
public class PeakEWMA extends AbstractEWMA {
    private static final AtomicLongFieldUpdater<PeakEWMA> STAMP = AtomicLongFieldUpdater.newUpdater(PeakEWMA.class, "stamp");

    /** (希腊字母)EWMA的时间常量 */
    private final long tau;
    private volatile long stamp;

    /**
     * @param lifeTime     speed of convergence，即收敛的速度, 控制β(like most decay process).
     *                     可以理解为数据的生命周期, 比如说rpc请求时间, 可接受最大请求时间为10s, 那么该值应该设置为10_000
     * @param initialValue 初始ewma值, 一般不为0, 接收到第一个数据时, 才创建{@link PeakEWMA}实例
     */
    public PeakEWMA(long lifeTime, double initialValue) {
        //tau约等于一半lifeTime, 计算出来的ewma值相对更新接近真实, 特别是刚开始的时候
        this.tau = (long) (lifeTime / Math.log(2));
        this.ewma = initialValue;
        STAMP.set(this, now());
    }


    @Override
    public synchronized void observe(double x) {
        long now = now();
        //距离上次计算逝去的时间
        double elapsed = Math.max(0, now - STAMP.get(this));

        STAMP.set(this, now);

        //修正β, elapsed越大, β越小
        //可以认为时间过得越长, 当前越不靠谱, 所以下降得更慢些
        double w = Math.exp(-elapsed / tau);
        super.observe(w, x);
    }

    @Override
    public synchronized void reset(double value) {
        STAMP.set(this, now());
        super.reset(value);
    }

    /**
     * nano转millis
     */
    private long now() {
        return System.nanoTime() / 1_000_000;
    }

    @Override
    public String toString() {
        return "PeakEWMA(value=" + ewma + ", age=" + (now() - STAMP.get(this)) + ")";
    }
}
