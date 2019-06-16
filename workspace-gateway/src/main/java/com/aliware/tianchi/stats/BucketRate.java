package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class BucketRate {
    public static final int DEFAULT_BUCKET = 1000;

    private final AtomicInteger maxBucket = new AtomicInteger(DEFAULT_BUCKET);
    private final AtomicInteger lastBucket = new AtomicInteger(DEFAULT_BUCKET);
    private final AtomicInteger currentBucket = new AtomicInteger(DEFAULT_BUCKET);
    private final long sampleInterval;
    private final long sampleInterval1;
    private volatile long threshold;
    private volatile long threshold1;

    public BucketRate(long sampleInterval,long sampleInterval1){
        this.sampleInterval = sampleInterval;
        this.sampleInterval1 = sampleInterval1;

        threshold = System.currentTimeMillis() + sampleInterval;
        threshold1 = System.currentTimeMillis() + sampleInterval1;
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

    public void setBucket(int i) {
        checkAndResetWindow();
        if (i < currentBucket.get()) {
            this.currentBucket.updateAndGet(j -> i < j ? i : j);
        }
    }

    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        if(threshold < now) {
            lastBucket.set(currentBucket.get());
            currentBucket.set(DEFAULT_BUCKET);
            threshold = now + sampleInterval;
        }
        if (threshold1 < now) {
            maxBucket.set(lastBucket.get());
            threshold1 = now + sampleInterval1;
        }
    }
}
