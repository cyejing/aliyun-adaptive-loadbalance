package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class BucketRate {

    public static final int DEFAULT_BUCKET = 1000;
    public static final int BUCKET_SIZE = 100;


    private int[] buckets = new int[BUCKET_SIZE];

    private AtomicInteger index = new AtomicInteger();

    private final AtomicInteger currentBucket = new AtomicInteger(DEFAULT_BUCKET);
    private final long sampleInterval;
    private volatile long threshold;

    public BucketRate(long sampleInterval) {
        this.sampleInterval = sampleInterval;

        threshold = System.currentTimeMillis() + sampleInterval;
    }

    public int getAvgBucket() {
        checkAndResetWindow();
        int totalBucket = 0;
        int size;
        if (index.get() > BUCKET_SIZE) {
            size = BUCKET_SIZE;
            for (int i = 0; i < BUCKET_SIZE; i++) {
                totalBucket += buckets[i];
            }
        } else {
            size = index.get();
            for (int i = 0; i < size; i++) {
                totalBucket += buckets[i];
            }
        }
        if (size == 0) {
            return DEFAULT_BUCKET;
        }
        return totalBucket / size;
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
        int bucket;
        if (threshold < now &&  (bucket = currentBucket.get()) != DEFAULT_BUCKET) {
            int i = index.getAndIncrement();
            buckets[i % BUCKET_SIZE] = bucket;
            currentBucket.set(DEFAULT_BUCKET);
            threshold = now + sampleInterval;
        }
    }
}
