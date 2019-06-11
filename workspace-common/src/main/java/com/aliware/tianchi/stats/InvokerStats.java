package com.aliware.tianchi.stats;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Born
 */
public class InvokerStats {


    private InvokerStats() {}

    private static class InvokerStatsBuilder {
        private static InvokerStats INSTANCE = new InvokerStats();
    }

    public static InvokerStats getInstance() {
        return InvokerStatsBuilder.INSTANCE;
    }

    private ConcurrentMap<String, DataCollector> dataMap = new ConcurrentHashMap();


    public void incrementFailedRequests(Invoker invoker) {
        getDataCollector(invoker).incrementFailedRequests();
    }

    public void incrementRequests(Invoker invoker) {
        getDataCollector(invoker).incrementRequests();
    }

    public void decrementRequests(Invoker invoker) {
        getDataCollector(invoker).decrementRequests();
    }

    public DataCollector getDataCollector(String key) {
        DataCollector dataCollector = dataMap.get(key);
        if (dataCollector == null) {
            dataMap.put(key, new DataCollector());
            dataCollector = dataMap.get(key);
        }
        return dataCollector;
    }

    public DataCollector getDataCollector(Invoker invoker) {
        String key = invoker.getUrl().toIdentityString();
        return getDataCollector(key);
    }

    public static void main(String[] args) throws Exception{
        MeasuredRate measuredRate = new MeasuredRate(1000);
        for (int i = 0; i < 100; i++) {
            Thread.sleep(350);
            measuredRate.increment();
            measuredRate.increment();
            measuredRate.increment();
            System.out.println(measuredRate.getCount()+","+measuredRate.getMaxCount());
        }



    }

}
