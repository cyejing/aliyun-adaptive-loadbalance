package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Born
 */
public class MeasuredRate {
    private final AtomicInteger maxBucket = new AtomicInteger(0);
    private final AtomicInteger lastBucket = new AtomicInteger(0);
    private final AtomicInteger currentBucket = new AtomicInteger(0);
    private final long sampleInterval;
    private volatile long threshold;

    public MeasuredRate(long sampleInterval){
        this.sampleInterval = sampleInterval;

        threshold = System.currentTimeMillis() + sampleInterval;
    }

    public int getCount() {
        checkAndResetWindow();
        return lastBucket.get();
    }


    public int getMaxCount() {
        checkAndResetWindow();
        return maxBucket.get();
    }

    public int getCurrentCount() {
        checkAndResetWindow();
        return currentBucket.get();
    }

    public void increment() {
        checkAndResetWindow();
        currentBucket.incrementAndGet();
    }

    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        if(threshold < now) {
            lastBucket.set(currentBucket.get());
            currentBucket.set(0);
            threshold = now + sampleInterval;
            if (maxBucket.get() < lastBucket.get()) {
                maxBucket.set(lastBucket.get());
            }
        }
    }


}
