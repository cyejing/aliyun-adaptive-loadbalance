package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Born
 */
public class DistributionRate {

    private volatile double rt;
    private volatile double maxRt;

    private AtomicInteger request = new AtomicInteger(0);
    private AtomicLong ms = new AtomicLong(0);

    private final long sampleInterval;
    private volatile long threshold;

    public DistributionRate(long sampleInterval) {
        this.sampleInterval = sampleInterval;
    }

    public void noteValue(long i) {
        checkAndResetWindow();
        request.incrementAndGet();
        ms.addAndGet(i);
    }

    public double getMean() {
        checkAndResetWindow();
        return rt;
    }

    public double getMaxMean() {
        checkAndResetWindow();
        return maxRt;
    }

    private void checkAndResetWindow() {
        long now = System.currentTimeMillis();
        if(threshold < now) {
            rt = getRT();
            request.set(0);
            ms.set(0);
            threshold = now + sampleInterval;
            if (maxRt < rt) {
                maxRt = rt;
            }
        }
    }

    private double getRT() {
        if (request.get() < 1) {
            return 0.0;
        } else {
            return new Double(ms.get()) /  request.get();
        }
    }

}
