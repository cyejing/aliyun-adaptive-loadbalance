package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private AtomicInteger activeRequests = new AtomicInteger(0);
    private AtomicInteger failedRequests = new AtomicInteger(0);
    private BucketRate bucketRate = new BucketRate(3000, 100, 50);
    private MeasuredRate qps = new MeasuredRate(300);
//    private DistributionRate distributionRate = new DistributionRate(3000, 50);
    private RTTRate rttRate = new RTTRate(3000);


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
        rttRate.calc(i);
    }

    public double getMean() {
        return rttRate.getRTO();
    }

    public int getFailed() {
        return failedRequests.get();
    }

    public int getOneQPS() {
        return rttRate.getOneQPS();
    }

    public int getWeight() {
        return getOneQPS();
    }

}
