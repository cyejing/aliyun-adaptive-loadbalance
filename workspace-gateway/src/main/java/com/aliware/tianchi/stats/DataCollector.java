package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    public static final double ALPHA = 1;
    public static final double GAMMA = 1.15;


    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private DistributionRate distributionRate = new DistributionRate(3000, 200, 400);

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

    public void setBucket(int bucket) {
        this.bucket = bucket;
        this.distributionRate.setBucket(bucket);
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
        double r = Math.pow(1000 / mean, GAMMA) * Math.pow(curr, ALPHA);
        return new Double(r).intValue();
    }

    public double getCurr() {
        return distributionRate.getCurr();
    }

    public DataCollectorCopy copy() {
        return new DataCollectorCopy(getActive(), getBucket(), getMean(), getCurr(), getWeight());
    }

    public static class DataCollectorCopy{

        public DataCollectorCopy(int active, int bucket, double mean, double curr, int weight) {
            this.active = active;
            this.bucket = bucket;
            this.mean = mean;
            this.curr = curr;
            this.weight = weight;
        }

        private int active;
        private int bucket;
        private double mean;
        private double curr;
        private int weight;

        public int getActive() {
            return active;
        }

        public void setActive(int active) {
            this.active = active;
        }

        public int getBucket() {
            return bucket;
        }

        public void setBucket(int bucket) {
            this.bucket = bucket;
        }

        public double getMean() {
            return mean;
        }

        public void setMean(double mean) {
            this.mean = mean;
        }

        public double getCurr() {
            return curr;
        }

        public void setCurr(double curr) {
            this.curr = curr;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}
