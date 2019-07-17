package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;
import static com.aliware.tianchi.stats.DataCollector.BETA;
import static com.aliware.tianchi.stats.DataCollector.GAMMA;
import static com.aliware.tianchi.stats.DataCollector.REFRESH;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class ThroughputRate {

    AtomicInteger throughput = new AtomicInteger(0);

    private volatile double throughputRate;
    private volatile double weight = 1000;
    private volatile AtomicInteger rise = new AtomicInteger(0);
    private volatile AtomicBoolean calc = new AtomicBoolean(false);

    private long interval;
    private volatile long threshold;
    private volatile long weightThreshold;


    private int bucket;


    public ThroughputRate(long interval) {
        this.interval = interval;
        this.threshold = System.currentTimeMillis() + interval;
        this.weightThreshold = System.currentTimeMillis() + interval;
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
                double t = threshold;
                if (now > t) {
                    double oWeight = this.weight;
                    double nWeight = i * (1000D / (now - t + interval));
                    this.throughputRate = nWeight;
                    double devWeight = Math.abs(nWeight - oWeight);
                    double weightTran = oWeight * (1 - ALPHA) + nWeight * ALPHA;

                    if (nWeight > oWeight) {
                        System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" 吞吐上升,nWeight:"+nWeight+" oWeight:"+oWeight+" riseWeight:"+nWeight * BETA);
                        this.weight = nWeight + nWeight * BETA;
                    } else if (nWeight < oWeight && devWeight > (oWeight * GAMMA)) {
                        System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" 吞吐下降,nWeight:"+nWeight+" oWeight:"+oWeight+" devWeight:"+devWeight+" rate:"+(devWeight / oWeight));
                        this.weight = nWeight;
                    }

                    if (now > weightThreshold) {
                        System.out.println(LocalDateTime.now().toString() + " bucket:" + bucket + " 时间到期,nWeight:" + nWeight + " oWeight:" + oWeight + " weightTran" + weightTran);
                        this.weight = nWeight;
                        this.rise.set(1);
                        this.weightThreshold = now + REFRESH;
                    }

                    System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" collect data current,weight:"+getWeight()+" maxWeight:"+weight+" throughputRate:"+throughputRate);
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

    public void reset() {
        if (calc.compareAndSet(false, true)) {
            this.throughput.set(0);
            this.threshold = System.currentTimeMillis() + interval;
            calc.compareAndSet(true, false);
        }
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
    }
}
