package com.aliware.tianchi.loadbalance;

import static com.aliware.tianchi.loadbalance.BasicWeightedLoadBalance.DEFAULT_WEIGHT;

import com.aliware.tianchi.stats.InvokerStats;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Born
 */
public class WeightedRoundRobin {

    private final String key;
    private final Invoker invoker;
    private AtomicInteger weight;
    private AtomicLong current = new AtomicLong(0);

    public WeightedRoundRobin(String key, Invoker invoker, int weight) {
        this.key = key;
        this.invoker = invoker;
        this.weight = new AtomicInteger(weight);
    }

    public int getWeight() {
        return weight.get();
    }

    public void setWeight(int weight) {
        int max = weight == 0 ? DEFAULT_WEIGHT : weight;
        this.weight.set(max);
    }

    public void increaseWeight(int i) {
        int srcw = InvokerStats.getInstance().getDataCollector(invoker).getQPS();
        int srcMax = InvokerStats.getInstance().getDataCollector(invoker).getMaxQPS();
        int max;
        if (srcw > 0) {
            max = srcw;
        } else if (srcMax > 0) {
            max = srcMax;
        } else {
            max = DEFAULT_WEIGHT;
        }

        this.weight.updateAndGet(o -> o + i >= max ? max : o + i);
    }

    public void decreaseWeight(int i) {
        this.weight.updateAndGet(o -> o - i <= 0 ? 0 : o - i);
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

    public Invoker getInvoker() {
        return invoker;
    }
}
