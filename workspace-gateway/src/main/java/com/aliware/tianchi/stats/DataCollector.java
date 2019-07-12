package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {


    public static final double ALPHA = 0.875;
    public static final double BETA = 0.23;
    public static final double GAMMA = 1.25;
    public static final double NEUTRON = 1.45;

    public static final int COLLECT = 300;

    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private ThroughputRate throughputRate = new ThroughputRate(COLLECT);

    private double rate = 1.0;

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


    public int getWeight() {
        double weight = this.throughputRate.getWeight();

        weight = weight * rate;

        return new Double(weight).intValue();
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public ThroughputRate getThroughputRate() {
        return throughputRate;
    }

}
