package com.aliware.tianchi.stats;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {


    public static final double ALPHA = 0.775;
    public static final double BETA = 0.25;
    public static final double GAMMA = 2.15;
    public static final double NEUTRON = 2.15;

    public static final int COLLECT = 100;
    public static final int REFRESH = 20;

    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private ThroughputRate throughputRate = new ThroughputRate(COLLECT);

    private double rate = 1.0;

    private AtomicBoolean calc = new AtomicBoolean(false);
    private long print = System.currentTimeMillis();

    public DataCollector() {
    }


    public void incrementRequests() {
        activeRequests.incrementAndGet();
    }

    public void failedRequest() {
    }

    public void decrementRequests() {
        activeRequests.decrementAndGet();
        throughputRate.note();
    }

    public void setBucket(int bucket) {
        this.bucket = bucket;
        throughputRate.setBucket(bucket);
    }

    public void succeedRequest() {
    }


    public int getActive() {
        return activeRequests.get();
    }

    public int getBucket() {
        return bucket;
    }


    public double getWeight() {
        double weight = this.throughputRate.getWeight();

        weight = weight * rate;

        long now = System.currentTimeMillis();
        if (calc.compareAndSet(false, true)) {
            if (print > now) {
                System.out.println(
                        LocalDateTime.now().toString() + " bucket:" + bucket + " collect data current,weight:" + weight
                                + " activeRequests:" + activeRequests.get() + " rate:" + rate);
                print = now + 500;
            }
            calc.compareAndSet(true, false);
        }
        return weight;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public ThroughputRate getThroughputRate() {
        return throughputRate;
    }

}
