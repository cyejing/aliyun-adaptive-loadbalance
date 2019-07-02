package com.aliware.tianchi.stats;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {


    public static final double ALPHA = 0.125;
    public static final double BETA = 1;
    public static final double GAMMA = 1;


    private volatile int bucket = 1000;
    private AtomicInteger activeRequests = new AtomicInteger(0);
    private ThroughputRate throughputRate = new ThroughputRate(300);
    private volatile double weight;

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

        double rate = this.throughputRate.getThroughputRate();
        if (this.weight == 0) {
            this.weight = rate;
        }
        
        this.weight = this.weight * ALPHA + rate * (1 - ALPHA);


            if (ThreadLocalRandom.current().nextInt(2000) < bucket) {
                this.weight = this.weight + (bucket/10);
            }
        
        return new Double(this.weight).intValue();
    }


    public DataCollectorCopy copy() {
        return new DataCollectorCopy(getActive(), getBucket(), getWeight());
    }

    public static class DataCollectorCopy{

        public DataCollectorCopy(int active, int bucket, int weight) {
            this.active = active;
            this.bucket = bucket;
            this.weight = weight;
        }

        private int active;
        private int bucket;
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

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}
