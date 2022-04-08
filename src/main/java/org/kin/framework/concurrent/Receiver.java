package org.kin.framework.concurrent;

/**
 * 消息逻辑处理实现
 *
 * @author huangjianqin
 * @date 2020-04-16
 */
public abstract class Receiver<MSG> {
    /**
     * 接受并处理消息
     *
     * @param message 消息实现
     */
    public abstract void receive(MSG message);

    /**
     * Receiver初始化
     */
    protected void onStart() {
    }

    /**
     * Receiver closed并清理占用资源
     */
    protected void onStop() {
    }
}
