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

}
