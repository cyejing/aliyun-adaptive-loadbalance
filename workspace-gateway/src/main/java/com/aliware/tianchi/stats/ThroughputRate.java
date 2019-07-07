package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class ThroughputRate {

    AtomicInteger throughput = new AtomicInteger(0);

    private double throughputRate;
    private double weight;

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

    public double getWeight() {
        checkAndSet();
        return weight;
    }

    public void checkAndSet() {
        int i = throughput.get();
        long now = System.currentTimeMillis();
        if (now > threshold) {
            double nWeight = i * (1000D / (now - threshold + interval));

            double oWeight = this.weight;
            this.weight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

            this.throughputRate = nWeight;
            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }



}
