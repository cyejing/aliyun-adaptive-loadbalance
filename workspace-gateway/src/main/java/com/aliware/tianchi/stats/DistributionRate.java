package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DistributionRate {

    private int size;
    private double[] requestRTTs;
    private AtomicInteger index = new AtomicInteger(0);

    private long delayThreshold;

    public DistributionRate(long delay, int size) {
        this.size = size;
        this.requestRTTs = new double[size];

        this.delayThreshold = System.currentTimeMillis() + delay;
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
        if (delayThreshold > System.currentTimeMillis()) {
            return;
        }
        int i = index.getAndIncrement();
        requestRTTs[i % size] = v;
    }
}
