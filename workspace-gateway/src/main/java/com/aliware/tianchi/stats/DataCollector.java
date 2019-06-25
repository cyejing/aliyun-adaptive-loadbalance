package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    public static final double ALPHA = 1.015;
    public static final double GAMMA = 1.32;


    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private DistributionRate distributionRate = new DistributionRate(3000, 100);

    public DataCollector() {
    }


    public void incrementRequests() {
//        activeRequests.incrementAndGet();
    }

    public void incrementFailedRequests() {
    }

    public void decrementRequests() {
//        activeRequests.decrementAndGet();
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

    public int getAvgBucket() {
        return bucket;
    }

    public double getMean() {
        return distributionRate.getMean();
    }


    public int getOneQPS() {
        return distributionRate.getOneQPS();
    }

    public int getWeight() {
        double mean = distributionRate.getMean();
        double r = Math.pow(1000 / mean, GAMMA) * Math.pow(bucket, ALPHA);
        return new Double(r).intValue();
    }

    public static void main(String[] args) {
        System.out.println(Math.pow(19,1.32));
        System.out.println(Math.pow(25,1.32));
        System.out.println(Math.pow(650,1.015));
        System.out.println(Math.pow(450,1.015));
        System.out.println(Math.pow(200,1.015));
        System.out.println(Math.pow(15000,1.005));
        System.out.println(Math.pow(25000,1.005));
        System.out.println(Math.pow(35000,1.005));
    }
}
