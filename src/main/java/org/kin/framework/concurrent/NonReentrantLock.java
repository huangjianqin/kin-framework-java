package org.kin.framework.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author huangjianqin
 * @date 2020-06-14
 */
public class NonReentrantLock extends AbstractQueuedSynchronizer implements Lock {
    private static final int PENDING = 0;
    private static final int DONE = 1;

    @Override
    protected boolean tryAcquire(int acquires) {
        if (compareAndSetState(PENDING, DONE)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    @Override
    protected boolean tryRelease(int releases) {
        Thread exclusiveOwnerThread = getExclusiveOwnerThread();
        if (Thread.currentThread() != exclusiveOwnerThread) {
            throw new IllegalMonitorStateException("owner is " + exclusiveOwnerThread);
        }
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }

    @Override
    public void lock() {
        acquire(DONE);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        acquireInterruptibly(DONE);
    }

    @Override
    public boolean tryLock() {
        return tryAcquire(DONE);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return tryAcquireNanos(DONE, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        release(DONE);
    }

    @Override
    public Condition newCondition() {
        return new ConditionObject();
    }

    @Override
    protected boolean isHeldExclusively() {
        return super.isHeldExclusively() && getState() != PENDING && Thread.currentThread() == getExclusiveOwnerThread();
    }
}
