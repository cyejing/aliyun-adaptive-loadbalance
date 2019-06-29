package com.aliware.tianchi.stats;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DistributionRate {

    private int size;
    private double[] requestRTTs;
    private double[] currs;
    private volatile double curr = 0;
    private AtomicInteger index = new AtomicInteger(0);

    private long delayThreshold;
    private volatile long startTime;

    public DistributionRate(long delay, int size) {
        this.size = size;
        this.requestRTTs = new double[size];
        this.currs = new double[10];

        this.delayThreshold = System.currentTimeMillis() + delay;
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
        if (delayThreshold > now) {
            return;
        }
        int i = index.getAndIncrement();
        double mean = getMean();

        if (i == 0) {
            this.startTime = System.currentTimeMillis();
        } else if (i % (size) == 0) {
            this.currs[(i / size) % 10] = (1000D / (now - startTime) * (size)) / (1000D / mean);
            if (((i / size) % 10) == 0) {
                this.currs[(i / size) % 10] += 20;
            }
            this.startTime = now;
        }

        requestRTTs[i % size] = v;
    }

    public double getCurr() {
        double totalCurr = 0;
        int s = index.get() / size;
        int total;
        if (s >= 10) {
            for (int i = 0; i < 10; i++) {
                totalCurr += currs[i];
            }
            total = 10;
        } else {
            for (int i = 0; i < s; i++) {
                totalCurr += currs[i];
            }
            total = s;
        }
        if (s == 0) {
            return 0D;
        }
        return totalCurr / total;
    }

    public String toMeanString() {
        return Arrays.toString(requestRTTs);
    }
}
