package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class ThroughputRate {

    AtomicInteger throughput = new AtomicInteger(0);

    private volatile double throughputRate;
    private volatile double weight;
    private volatile AtomicInteger rise = new AtomicInteger(0);

    private long interval;
    private volatile long threshold;

    private volatile long maxThreshold;

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
        int i = this.throughput.get();
        long now = System.currentTimeMillis();
        long t = this.threshold;
        if (now > t) {
            double oWeight = this.weight;
            double nWeight = i * (1000D / (now - t + interval));
            this.throughputRate = nWeight;
            double devWeight = Math.abs(nWeight - oWeight);
            nWeight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

            if (nWeight > oWeight) {
                this.weight = nWeight;
                this.rise.set(2);
            }else{
                this.weight = oWeight;
                this.rise.decrementAndGet();
            }

            if (now > maxThreshold || devWeight > 1200) {
                this.weight = nWeight;
                this.maxThreshold = now + interval * 10;
            }

            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }

    public boolean isRise() {
        if (rise.get() > 0) {
            return true;
        }
        return false;
    }
}
