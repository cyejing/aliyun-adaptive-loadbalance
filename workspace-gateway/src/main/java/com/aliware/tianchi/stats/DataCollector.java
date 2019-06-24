package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private static final double ALPHA = 1.005;
    private static final double GAMMA = 1.25;


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

    public void noteBucket(int i) {
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

    public int getQPS() {
        return 0;
    }

    public int getAvgBucket() {
        return bucket;
    }

    public double getMean() {
        return distributionRate.getMean();
    }

    public int getFailed() {
        return 0;
    }

    public int getOneQPS() {
        return distributionRate.getOneQPS();
    }

    public int getWeight() {
        double mean = distributionRate.getMean();
        double r = Math.pow(Math.pow(1000 / mean, GAMMA) * bucket, ALPHA);
        return new Double(r).intValue();
    }

    public static void main(String[] args) {
        System.out.println(Math.pow(19,1.45));
        System.out.println(Math.pow(25,1.45));
    }
}
