package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private AtomicInteger activeRequests = new AtomicInteger(0);
    private AtomicInteger failedRequests = new AtomicInteger(0);
    private BucketRate bucketRate = new BucketRate(1000 * 3, 100, 50);
    private MeasuredRate qps = new MeasuredRate(500);
    private DistributionRate distributionRate = new DistributionRate(500);


    public void incrementRequests() {
        activeRequests.incrementAndGet();
    }

    public void incrementFailedRequests() {
        failedRequests.incrementAndGet();
    }

    public void decrementRequests() {
        activeRequests.decrementAndGet();
    }

    public int getActive() {
        return activeRequests.get();
    }


    public void succeedRequest() {
        qps.increment();
    }

    public int getQPS() {
        return qps.getCount();
    }

    public int getMaxQPS() {
        return qps.getMaxCount();
    }

    public void setBucket(int i) {
        bucketRate.setBucket(i);
    }

    public int getAvgBucket() {
        return bucketRate.getAvgBucket();
    }

    public void noteValue(long i) {
        distributionRate.noteValue(i);
    }

    public double getMean() {
        return distributionRate.getMean();
    }

    public int getFailed() {
        return failedRequests.get();
    }

    public int getOneQPS() {
        return (new Double(1000 / getMean()).intValue()) *10;
    }
}
