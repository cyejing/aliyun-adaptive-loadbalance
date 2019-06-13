package com.aliware.tianchi.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Born
 */
public class InvokerStats {


    private Timer timer = new Timer();

    private InvokerStats() {
        //TODO timer
    }

    private static class InvokerStatsBuilder {
        private static InvokerStats INSTANCE = new InvokerStats();
    }

    public static InvokerStats getInstance() {
        return InvokerStatsBuilder.INSTANCE;
    }

    private ConcurrentMap<String, DataCollector> dataMap = new ConcurrentHashMap();
    private List<String> sort = new ArrayList<>();


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

    public List<String> getSort() {
        return sort;
    }

}
