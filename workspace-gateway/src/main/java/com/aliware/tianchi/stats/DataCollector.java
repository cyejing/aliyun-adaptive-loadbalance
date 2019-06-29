package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    public static final double ALPHA = 0.962;
    public static final double GAMMA = 1;


    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private DistributionRate distributionRate = new DistributionRate(3000, 100);

    public DataCollector() {
    }


    public void incrementRequests() {
        activeRequests.incrementAndGet();
    }

    public void incrementFailedRequests() {
    }

    public void decrementRequests() {
        activeRequests.decrementAndGet();
    }

    public void setBucket(int i) {
        this.bucket = i;
    }

    public void noteValue(long i) {
        distributionRate.calc(i);
    }

    public void succeedRequest() {
    }


    public int getActive() {
        return activeRequests.get();
    }

    public int getBucket() {
        return bucket;
    }

    public double getMean() {
        return distributionRate.getMean();
    }


    public int getWeight() {
        double mean = distributionRate.getMean();
        double curr = distributionRate.getCurr();
        double r = Math.pow(1000 / mean, GAMMA) * ((Math.pow(bucket, ALPHA) + curr));
        return new Double(r).intValue();
    }

    public double getCurr() {
        return distributionRate.getCurr();
    }
}
