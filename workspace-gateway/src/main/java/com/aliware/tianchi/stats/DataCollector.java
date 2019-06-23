package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private static final int DEFAULT_BUCKET = 1000;


    private volatile int bucket = DEFAULT_BUCKET;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private AtomicInteger failedRequests = new AtomicInteger(0);
    private QPSRate qps = new QPSRate(3000, 1000, 3);
    private DistributionRate distributionRate = new DistributionRate(3000, 100);

    public DataCollector() {
    }


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
//        bucketRate.noteValue(i);
    }

    public void setBucket(int i) {
        this.bucket = i;
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
        return bucket;
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
        int oneQPS = getOneQPS();
        if (getActive() == 0) {
            return oneQPS;
        } else {
            double r = oneQPS * bucket + oneQPS * 100;
            return new Double(r).intValue();
        }
    }

}
