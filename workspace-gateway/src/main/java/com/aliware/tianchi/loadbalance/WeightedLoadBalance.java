package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.support.MockInvoker;

public class WeightedLoadBalance extends BasicWeightedLoadBalance {

    private static final Logger log = LoggerFactory.getLogger(WeightedLoadBalance.class);

    private ConcurrentMap<String, WeightedRoundRobin> map = new ConcurrentHashMap<>();

    @Override
    protected WeightedRoundRobin getWeightedRoundRobin(Invoker invoker) {
        String key = invoker.getUrl().toIdentityString();
        WeightedRoundRobin weightedRoundRobin = map.get(key);
        if (weightedRoundRobin == null) {
            map.putIfAbsent(key, new WeightedRoundRobin(key, DEFAULT_WEIGHT));
            weightedRoundRobin = map.get(key);
        }

        DataCollector dc = InvokerStats.getInstance().getDataCollector(key);
        weightedRoundRobin.setWeight(dc.getWeight());
        return weightedRoundRobin;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> select = super.select(invokers, url, invocation);
        if (select == null) {
            log.error("不应该出现的位置");
            select = invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }
        return select;
    }

    public static void main(String[] args) throws InterruptedException {
        URL url = new URL("doubb", "12.0.0.1", 8090);
        Invoker<WeightedLoadBalance> i1  = new MockInvoker<>(new URL("doubb", "12.0.0.1", 8090).addParameter("weight",10), WeightedLoadBalance.class);
        Invoker<WeightedLoadBalance> i2  = new MockInvoker<>(new URL("doubb", "12.0.0.1", 8091).addParameter("weight",20), WeightedLoadBalance.class);
        Invoker<WeightedLoadBalance> i3 = new MockInvoker<>(new URL("doubb", "12.0.0.1", 8092).addParameter("weight",30), WeightedLoadBalance.class);

        Map<Invoker, AtomicInteger> map = new ConcurrentHashMap<>();
        map.put(i1, new AtomicInteger(0));
        map.put(i2, new AtomicInteger(0));
        map.put(i3, new AtomicInteger(0));
        List<Invoker<WeightedLoadBalance>> invokers = Arrays.asList(i1, i2, i3);
        WeightedLoadBalance loadBalance = new WeightedLoadBalance();

        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000000; i++) {
            executorService.submit(() -> {
                Invoker<WeightedLoadBalance> select = loadBalance.select(invokers, url, new RpcInvocation());
                map.get(select).incrementAndGet();
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.DAYS);
        for (Entry<Invoker, AtomicInteger> entry : map.entrySet()) {
            System.out.println(entry.getKey().getUrl().toIdentityString() + ": " + entry.getValue().get());
        }
    }
}
