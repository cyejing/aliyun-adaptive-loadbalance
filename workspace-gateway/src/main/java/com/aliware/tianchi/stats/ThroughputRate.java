package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        int i = this.throughput.get();
        long now = System.currentTimeMillis();
        long t = this.threshold;
        if (now > t) {
            synchronized (this) {
                if (now > threshold) {
                    System.out.println(LocalDateTime.now().toString() + " 时间变化weight" + this.weight);
                    double oWeight = this.weight;
                    double nWeight = i * (1000D / (now - t + interval));
                    this.throughputRate = nWeight;
                    double devWeight = Math.abs(nWeight - oWeight);
                    nWeight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

                    if (nWeight > oWeight || devWeight > 1200) {
                        this.weight = nWeight;
                        this.rise.set(1);
                    }else{
                        this.weight = oWeight;
                        this.rise.decrementAndGet();
                    }

                    this.throughput.set(0);
                    this.threshold = now + interval;
                }
            }
        }
    }

    public boolean isRise() {
        if (rise.get() > 0) {
            return true;
        }
        return false;
    }
}
