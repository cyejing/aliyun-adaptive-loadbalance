package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class BucketRate {

    public static final int DEFAULT_BUCKET = 1000;


    private int size;
    private int[] buckets;

    private AtomicInteger index = new AtomicInteger(0);

    private final AtomicInteger currentBucket = new AtomicInteger(DEFAULT_BUCKET);
    private final long sampleInterval;
    private volatile long threshold;
    private volatile long delayThreshold;

    public BucketRate(long delay, long sampleInterval, int size) {
        this.size = size;
        this.buckets = new int[size];
        this.sampleInterval = sampleInterval;

        this.threshold = System.currentTimeMillis() + sampleInterval;
        this.delayThreshold = System.currentTimeMillis() + delay;
    }

    public int getAvgBucket() {
        checkAndResetWindow();
        int totalBucket = 0;
        int s = index.get();
        int total;
        if (s >= size) {
            for (int i = 0; i < size; i++) {
                totalBucket += buckets[i];
            }
            total = size;
        } else {
            for (int i = 0; i < s; i++) {
                totalBucket += buckets[i];
            }
            total = s;
        }
        if (s == 0) {
            return DEFAULT_BUCKET;
        }
        return totalBucket / total;
    }


    public int getCurrentCount() {
        checkAndResetWindow();
        return currentBucket.get();
    }

    public void noteValue(int i) {
        checkAndResetWindow();
        if (i < currentBucket.get()) {
            this.currentBucket.updateAndGet(j -> i < j ? i : j);
        }
    }

    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        int bucket;
        if (threshold < now && (bucket = currentBucket.get()) != DEFAULT_BUCKET) {
            if (delayThreshold < now) {
                int i = index.getAndIncrement();
                buckets[i % size] = bucket;
            }
            currentBucket.set(DEFAULT_BUCKET);
            threshold = now + sampleInterval;
        }
    }
}
