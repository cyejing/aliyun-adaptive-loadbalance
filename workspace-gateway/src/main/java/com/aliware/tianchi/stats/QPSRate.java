package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class QPSRate {

    private int size;
    private int[] qpsHistory;

    private AtomicInteger index = new AtomicInteger(0);

    private final AtomicInteger currentQPS = new AtomicInteger(0);

    private final long sampleInterval;
    private volatile long threshold;
    private volatile long delayThreshold;


    public QPSRate(long delay,long sampleInterval,int size){
        this.size = size;
        this.qpsHistory = new int[size];
        this.sampleInterval = sampleInterval;

        threshold = System.currentTimeMillis() + sampleInterval;
        this.delayThreshold = System.currentTimeMillis() + delay;
    }

    public void note() {
        checkAndResetWindow();
        currentQPS.incrementAndGet();
    }

    public int getAvgQPS() {
        checkAndResetWindow();
        int totalQPS = 0;
        int s = index.get();
        int total;
        if (s >= size) {
            for (int i = 0; i < size; i++) {
                totalQPS += qpsHistory[i];
            }
            total = size;
        } else {
            for (int i = 0; i < s; i++) {
                totalQPS += qpsHistory[i];
            }
            total = s;
        }
        if (s == 0) {
            return 0;
        }
        return totalQPS / total;
    }


    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        if (threshold < now) {
            if (delayThreshold < now) {
                int bucket = currentQPS.get();
                int i = index.getAndIncrement();
                qpsHistory[i % size] = bucket;
            }
            currentQPS.set(0);
            threshold = now + sampleInterval;
        }
    }
}
