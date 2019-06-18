package com.aliware.tianchi.loadbalance;

import static com.aliware.tianchi.loadbalance.BasicWeightedLoadBalance.DEFAULT_WEIGHT;

import com.aliware.tianchi.stats.InvokerStats;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Born
 */
public class WeightedRoundRobin {

    private final String key;
    private AtomicInteger weight;
    private AtomicLong current = new AtomicLong(0);

    public WeightedRoundRobin(String key, int weight) {
        this.key = key;
        this.weight = new AtomicInteger(weight);
    }

    public int getWeight() {
        return weight.get();
    }

    public void setWeight(int weight) {
        int max = weight == 0 ? DEFAULT_WEIGHT : weight;
        this.weight.set(max);
    }

    public long increaseCurrent() {
        return current.addAndGet(getWeight());
    }

    public void decreaseCurrent(int total) {
        current.addAndGet(-1 * total);
    }

    public String getKey() {
        return key;
    }

    public long getCurrent() {
        return current.get();
    }

}
