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
    private double devWeight;

    private long interval;
    private long threshold;

    private long maxThreshold;

    public ThroughputRate(long interval) {
        this.interval = interval;
        this.threshold = System.currentTimeMillis() + interval;
        this.maxThreshold = System.currentTimeMillis() + interval * 10;
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
            double oWeight = this.weight;
            double nWeight = i * (1000D / (now - threshold + interval));
            this.throughputRate = nWeight;
            nWeight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

            this.weight = nWeight > oWeight ? nWeight : oWeight;

            if (now > maxThreshold) {
                this.weight = nWeight;
                this.maxThreshold = now + interval * 10;
            }

            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }

}
