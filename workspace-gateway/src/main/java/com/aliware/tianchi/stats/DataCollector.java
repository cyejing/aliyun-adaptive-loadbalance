package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private AtomicInteger activeRequests = new AtomicInteger(0);
    private AtomicInteger failedRequests = new AtomicInteger(0);
    private BucketRate bucketRate = new BucketRate(3000, 100, 50);
    private QPSRate qps = new QPSRate(3000,50,50);
    private RTTRate rttRate = new RTTRate(5000);


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
        qps.note();
    }

    public int getQPS() {
        return qps.getAvgQPS();
    }

    public void setBucket(int i) {
        bucketRate.noteValue(i);
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
        if (getQPS() == 0) {
            return getOneQPS();
        }else{
            return getQPS() * getOneQPS();
        }
    }

}
