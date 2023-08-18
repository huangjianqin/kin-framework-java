package org.kin.framework.utils;

/**
 * 固定β的ewma预测工具
 *
 * @author huangjianqin
 * @date 2021/12/16
 */
public class SimpleEWMA extends AbstractEWMA {
    private final double w;

    public SimpleEWMA(double w) {
        this.w = w;
    }

    @Override
    public synchronized void observe(double x) {
        super.observe(w, x);
    }
}
