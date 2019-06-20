package com.aliware.tianchi.loadbalance;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

public abstract class BasicWeightedLoadBalance implements LoadBalance {

    public static final int DEFAULT_WEIGHT = 1000;

    private static final Logger log = LoggerFactory.getLogger(BasicWeightedLoadBalance.class);

    abstract protected WeightedRoundRobin getWeightedRoundRobin(Invoker invoker);

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        Invoker<T> selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (Invoker<T> invoker : invokers) {
            WeightedRoundRobin weightedRoundRobin = getWeightedRoundRobin(invoker);

            int weight = weightedRoundRobin.getWeight();
            long cur = weightedRoundRobin.increaseCurrent();
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInvoker = invoker;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }

        if (selectedInvoker != null && totalWeight != 0) {
            selectedWRR.decreaseCurrent(totalWeight);
            return selectedInvoker;
        }

        return null;
    }

    static class WeightedRoundRobin {

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
}
