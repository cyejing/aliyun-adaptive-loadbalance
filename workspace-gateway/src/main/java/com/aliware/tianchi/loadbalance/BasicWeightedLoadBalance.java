package com.aliware.tianchi.loadbalance;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
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

    abstract protected ConcurrentMap<String, WeightedRoundRobin> getMap();

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        Invoker<T> selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (Invoker<T> invoker : invokers) {
            String identifyString = invoker.getUrl().toIdentityString();
            WeightedRoundRobin weightedRoundRobin = getMap().get(identifyString);

            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin(identifyString, invoker, DEFAULT_WEIGHT);
                getMap().putIfAbsent(identifyString, weightedRoundRobin);
            }

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
}
