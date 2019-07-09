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
    private volatile AtomicBoolean rise = new AtomicBoolean(false);

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
        int i = throughput.get();
        long now = System.currentTimeMillis();
        if (now > threshold) {
            double oWeight = this.weight;
            double nWeight = i * (1000D / (now - threshold + interval));
            this.throughputRate = nWeight;
            nWeight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

            if (nWeight > oWeight) {
                this.weight = nWeight;
                this.rise.compareAndSet(false, true);
            }else{
                this.weight = oWeight;
                this.rise.compareAndSet(true, false);
            }


            if (now > maxThreshold) {
                this.weight = nWeight;
                this.maxThreshold = now + interval * 10;
            }

            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }

    public boolean isRise() {
        if (rise.get()) {
            return true;
        }
        return false;
    }
}
