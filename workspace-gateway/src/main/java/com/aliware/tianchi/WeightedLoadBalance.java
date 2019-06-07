package com.aliware.tianchi;

import com.aliware.tianchi.stats.Distribution;
import com.aliware.tianchi.stats.InvokerStats;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
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

public class WeightedLoadBalance implements LoadBalance {
    private static final Logger log = LoggerFactory.getLogger(WeightedLoadBalance.class);

    public static final int DEFAULT_WEIGHT = 1000;
    public static final int TRIPPED_DECREASE_WEIGHT = 50;
    public static final int REGAIN_DECREASE_WEIGHT = 25;

    private Timer timer = new Timer();



    public WeightedLoadBalance() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Collection<WeightedRoundRobin> values = map.values();
                if (values != null && values.size() > 0) {
                    for (WeightedRoundRobin wrr : values) {
                        wrr.increaseWeight(REGAIN_DECREASE_WEIGHT);
                        log.info("adjustment weight key:" + wrr.getKey() + ", weight:" + wrr.getWeight() + ", current"
                                + wrr.getCurrent());
                    }
                }
            }
        }, 0, 500);
    }

    private ConcurrentMap<String, WeightedRoundRobin> map = new ConcurrentHashMap<>();

    public void tripped(Invoker invoker) {
        String identifyString = invoker.getUrl().toIdentityString();
        WeightedRoundRobin weightedRoundRobin = map.get(identifyString);
        weightedRoundRobin.decreaseWeight(TRIPPED_DECREASE_WEIGHT);
    }



    protected static class WeightedRoundRobin {

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
            this.weight = new AtomicInteger();
        }

        public void increaseWeight(int i) {
            this.weight.updateAndGet(o -> o+i >= DEFAULT_WEIGHT ? DEFAULT_WEIGHT : o + i);
        }

        public void decreaseWeight(int i) {
            this.weight.updateAndGet(o -> o-i <= 0 ? 0 : o - i);
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


    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        Invoker<T> selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (Invoker<T> invoker : invokers) {
            String identifyString = invoker.getUrl().toIdentityString();
            WeightedRoundRobin weightedRoundRobin = map.get(identifyString);

            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin(identifyString,DEFAULT_WEIGHT);
                map.putIfAbsent(identifyString, weightedRoundRobin);
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

//        log.error("should not happen here. have not selectedInvoker");
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
