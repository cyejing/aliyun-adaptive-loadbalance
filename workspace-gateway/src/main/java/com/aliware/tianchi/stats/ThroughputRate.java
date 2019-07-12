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
    private volatile AtomicBoolean devRise = new AtomicBoolean(false);

    private long interval;
    private volatile long threshold;

    private int bucket;


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
                double t = threshold;
                if (now > t) {
                    double oWeight = this.weight;
                    double nWeight = i * (1000D / (now - t + interval));
                    this.throughputRate = nWeight;
                    double devWeight = Math.abs(nWeight - oWeight);
                    double weightTran = oWeight * (1 - ALPHA) + nWeight * ALPHA;

                    if (nWeight > oWeight || devWeight > (oWeight * BETA)) {
                        if (devWeight > (oWeight * BETA)) {
                            devRise.compareAndSet(false, true);
                            System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" 方差变化,nWeight:"+nWeight+" oWeight:"+oWeight+" devWeight:"+devWeight+" rate:"+(devWeight / oWeight));
                        }else{
                            System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" 吞吐上升,nWeight:"+nWeight+" oWeight:"+oWeight);
                        }
                        this.weight = nWeight;
                        this.rise.set(1);
                    } else {
                        this.weight = oWeight;
                    }

                    System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+" 收集数据,nWeight:"+nWeight+" oWeight:"+oWeight);
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

    public AtomicBoolean getDevRise() {
        return devRise;
    }

    public void reset() {
        if (calc.compareAndSet(false, true)) {
            this.throughput.set(0);
            this.threshold = System.currentTimeMillis() + interval;
            calc.compareAndSet(true, false);
            System.out.println(LocalDateTime.now().toString()+" bucket:"+bucket+"重置计算weight:" + weight);
        }
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
    }
}
