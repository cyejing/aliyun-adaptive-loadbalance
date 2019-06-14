package com.aliware.tianchi.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Born
 */
public class InvokerStats {
    private static final Logger log = LoggerFactory.getLogger(InvokerStats.class);


    private Timer timer = new Timer();

    private InvokerStats() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Set<Entry<String, DataCollector>> entries = dataMap.entrySet();
                    sort = entries.stream()
                            .sorted((o1, o2) -> {
                                int x = o1.getValue().getSucceedQPS();
                                int y = o2.getValue().getSucceedQPS();
                                return (x > y) ? -1 : ((x == y) ? 0 : 1);
                            })
                            .map(Entry::getKey)
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        },0,600);
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
