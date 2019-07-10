package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;
import static com.aliware.tianchi.stats.DataCollector.BETA;

import java.time.LocalDateTime;
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
    private volatile AtomicBoolean calc = new AtomicBoolean(false);

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
        if (now > threshold) {

            if (calc.compareAndSet(false, true)) {
                if (now > threshold) {
                    double oWeight = this.weight;
                    double nWeight = i * (1000D / (now - threshold + interval));
                    this.throughputRate = nWeight;
                    double devWeight = Math.abs(nWeight - oWeight);
                    double weightTran = oWeight * (1 - ALPHA) + nWeight * ALPHA;

                    if (nWeight > oWeight || devWeight > (oWeight * BETA)) {
                        System.out.println(LocalDateTime.now().toString()+" 吞吐上升,nWeight:"+nWeight+" oWeight:"+oWeight+" devWeight:"+devWeight+" rate:"+(oWeight * BETA));
                        this.weight = weightTran;
                        this.rise.set(1);
                    } else {
                        this.weight = oWeight;
                    }

                    this.throughput.set(0);
                    this.threshold = now + interval;
                }
                calc.compareAndSet(true, false);
            }

        }
    }

    public boolean isRise() {
        if (rise.get() > 0) {
            return true;
        }
        return false;
    }

    public void decrementRise() {
        this.rise.decrementAndGet();
    }

}
