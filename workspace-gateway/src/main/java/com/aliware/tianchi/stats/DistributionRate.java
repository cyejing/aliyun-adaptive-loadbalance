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
    private double[] currs;
    private AtomicInteger index = new AtomicInteger(0);

    private long delayThreshold;
    private volatile long startTime;

    public DistributionRate(long delay, int size) {
        this.size = size;
        this.requestRTTs = new double[size];
        this.currs = new double[]{bucket,bucket,bucket,bucket,bucket,
                bucket,bucket,bucket};


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
            if (((i / size) % currs.length) == 0) {
                this.currs[(i / size) % currs.length] = this.bucket;
            }else{
                this.currs[(i / size) % currs.length] = (1000D / (now - startTime) * (size)) / (1000D / mean);
            }
            this.startTime = now;
        }

        requestRTTs[i % size] = v;
    }

    public double getCurr() {
        double totalCurr = 0;
        for (int i = 0; i < currs.length; i++) {
            totalCurr += currs[i];
        }
        return totalCurr / currs.length;
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
        for (int i = 0; i < currs.length; i++) {
            currs[i] = bucket;
        }
    }

    public String toMeanString() {
        return Arrays.toString(requestRTTs);
    }
}
