package com.aliware.tianchi.stats;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DistributionRate {

    private int bucket = 1000;
    private int size;
    private double[] requestRTTs;
    private volatile double curr;
    private AtomicInteger index = new AtomicInteger(0);

    private volatile int currIndex = 0;
    private volatile long currThreshold;
    private long currInternal;
    private long delayThreshold;
    private volatile long startTime;

    public DistributionRate(long delay, int size,int currInternal) {
        this.size = size;
        this.currInternal = currInternal;
        this.requestRTTs = new double[size];

        this.delayThreshold = System.currentTimeMillis() + delay;
        this.currThreshold = System.currentTimeMillis() + currInternal;
        this.startTime = System.currentTimeMillis();
    }

    public double getMean() {
        double totalRTT = 0;
        int s = index.get();
        int total;
        if (s >= size) {
            for (int i = 0; i < size; i++) {
                totalRTT += requestRTTs[i];
            }
            total = size;
        } else {
            for (int i = 0; i < s; i++) {
                totalRTT += requestRTTs[i];
            }
            total = s;
        }
        if (s == 0) {
            return 100d;
        }
        return totalRTT / total;
    }


    public void calc(double v) {
        long now = System.currentTimeMillis();
        long st = startTime;
        if (delayThreshold > now) {
            return;
        }
        int i = index.getAndIncrement();
        requestRTTs[i % size] = v;
        double mean = getMean();

        if (i == 0) {
            this.startTime = now;
            this.currThreshold = now + currInternal;
            this.currIndex = 0;
        }
//        else if (i % (rSize) == 0) {
//            this.curr = (1000D / (now - st) * (rSize)) / (1000D / mean);
//            this.startTime = now;
//        }

        if (now > currThreshold) {
            this.curr = (1000D / (now - st) * (i - currIndex)) / (1000D / mean);
            this.currIndex = i;
            this.startTime = now;
            this.currThreshold = now + currInternal;
        }

    }

    public double getCurr() {
        return this.curr;
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
    }

    public String toMeanString() {
        return Arrays.toString(requestRTTs);
    }
}
