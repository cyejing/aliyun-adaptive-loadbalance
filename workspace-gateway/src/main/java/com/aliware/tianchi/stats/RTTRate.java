package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class RTTRate {

    private int size;
    private double[] requestRTTs;
    private AtomicInteger index = new AtomicInteger(0);

    private long delayThreshold;

    public RTTRate(long delay,int size) {
        this.size = size;
        this.requestRTTs = new double[size];

        this.delayThreshold = System.currentTimeMillis() + delay;
    }

    public void noteRTT(double v) {
        if (delayThreshold > System.currentTimeMillis()) {
            return;
        }
        int i = index.getAndIncrement();
        requestRTTs[i % size] = v;
    }

    public double getMean() {
        double totalRTT = 0;
        int s = index.get();
        if (s >= size) {
            for (int i = 0; i < size; i++) {
                totalRTT += requestRTTs[i];
            }
        } else {
            for (int i = 0; i < s; i++) {
                totalRTT += requestRTTs[i];
            }
        }
        if (s == 0) {
            return 100d;
        }
        return totalRTT / size;
    }

    public int getOneQPS() {
        return (new Double(1000 / getMean()).intValue());
    }

}
