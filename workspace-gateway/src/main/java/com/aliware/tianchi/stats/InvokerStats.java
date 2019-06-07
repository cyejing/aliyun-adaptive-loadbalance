package com.aliware.tianchi.stats;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    private ConcurrentMap<String, Distribution> statsMap = new ConcurrentHashMap();

    public void noteResponseTime(Invoker invoker, long sec) {
        String key = invoker.getUrl().toIdentityString();
        Distribution distribution = statsMap.get(key);
        if (distribution == null) {
            statsMap.put(key, new Distribution());
            distribution = statsMap.get(key);
        }
        distribution.noteValue(sec);
    }

    public Distribution getStats(String key) {
        return statsMap.get(key);
    }

    public ConcurrentMap<String, Distribution> getStatsMap() {
        return statsMap;
    }
}
