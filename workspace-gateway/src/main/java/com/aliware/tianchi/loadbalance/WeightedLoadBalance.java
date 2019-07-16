package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.stats.InvokerStats;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

public class WeightedLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> selectedInvoker = null;
        double weightSoFar = 0.0;
        List<WeightedRoundRobin> weightedRoundRobins = new ArrayList<>();
        for (Invoker<T> invoker : invokers) {
            double weight = InvokerStats.getInstance().getDataCollector(invoker).getWeight();
            weightSoFar += weight;
            weightedRoundRobins.add(new WeightedRoundRobin(invoker, weightSoFar));
        }

        double randomWeight = ThreadLocalRandom.current().nextDouble() * weightSoFar;

        for (WeightedRoundRobin wrr : weightedRoundRobins) {
            double weight = wrr.getWeight();
            if (weight > randomWeight) {
                selectedInvoker = wrr.getInvoker();
                break;
            }
        }

        if (selectedInvoker != null) {
            return selectedInvoker;
        }

        return null;
    }


    static class WeightedRoundRobin {

        private final Invoker invoker;
        private double weight;

        public WeightedRoundRobin(Invoker invoker, double weight) {
            this.invoker = invoker;
            this.weight = weight;
        }

        public double getWeight() {
            return weight;
        }

        public Invoker getInvoker() {
            return invoker;
        }
    }
}
