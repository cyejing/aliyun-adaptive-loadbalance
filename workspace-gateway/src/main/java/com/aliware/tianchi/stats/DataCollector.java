package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private AtomicInteger activeRequests = new AtomicInteger(0);
    private AtomicInteger failedRequests = new AtomicInteger(0);
    private BucketRate bucketRate = new BucketRate(3000, 100, 50);
    private QPSRate qps = new QPSRate(3000,1000,3);
    private DistributionRate distributionRate = new DistributionRate(3000, 100);


    public void incrementRequests() {
        activeRequests.incrementAndGet();
    }

    public void incrementFailedRequests() {
        failedRequests.incrementAndGet();
    }

    public void decrementRequests() {
        activeRequests.decrementAndGet();
    }

    public void noteBucket(int i) {
        bucketRate.noteValue(i);
    }

    public void noteValue(long i) {
        distributionRate.calc(i);
    }

    public void succeedRequest() {
        qps.note();
    }


    public int getActive() {
        return activeRequests.get();
    }

    public int getQPS() {
        return qps.getAvgQPS();
    }

    public int getAvgBucket() {
        return bucketRate.getAvgBucket();
    }

    public double getMean() {
        return distributionRate.getMean();
    }

    public int getFailed() {
        return failedRequests.get();
    }

    public int getOneQPS() {
        return distributionRate.getOneQPS();
    }

    public int getWeight() {
        if (getQPS() == 0) {
            return getOneQPS();
        }else{
            return  getOneQPS() * getActive();
        }
    }

}
