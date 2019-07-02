package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class ThroughputRate {

    AtomicInteger throughput = new AtomicInteger(0);

    private double throughputRate;

    private long interval;
    private long threshold;

    public ThroughputRate(long interval) {
        this.interval = interval;
        this.threshold = System.currentTimeMillis() + interval;
    }

    public int note() {
        checkAndSet();
        return throughput.incrementAndGet();
    }

    public double getThroughputRate() {
        checkAndSet();
        return throughputRate;
    }

    public void checkAndSet() {
        int i = throughput.get();
        long now = System.currentTimeMillis();
        if (now > threshold) {
            this.throughputRate = i * (1000D / (now - threshold + interval));
            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }



}
