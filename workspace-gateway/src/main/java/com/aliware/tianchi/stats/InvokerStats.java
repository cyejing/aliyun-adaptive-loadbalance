package com.aliware.tianchi.stats;

import java.time.LocalDateTime;
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
import java.util.concurrent.TimeUnit;
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
    private Timer logTimer = new Timer();


    public InvokerStats() {
        System.out.println("make by Born");
        long start = System.currentTimeMillis();
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    String i;
                    long d = TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
                    if (d < 35) {
                        i = "preheat";
                    } else if (d < 50) {
                        i = "1stage";
                    }else if (d < 65) {
                        i = "2stage";
                    } else if ( d < 80) {
                        i = "3stage";
                    } else if ( d < 95) {
                        i = "4stage";
                    }else{
                        i = "over";
                    }
                    ConcurrentMap<String, DataCollector> dcm = InvokerStats.getInstance()
                            .getDataCollectors();
                    for (Entry<String, DataCollector> e : dcm.entrySet()) {
                        String key = e.getKey();
                        DataCollector dc = e.getValue();
                        String s = String.format(
                                "%s %s bucket key:%s, active:%d, bucket:%d, weight:%d, mean:%f, qps:%d, failed:%d.",
                                LocalDateTime.now().toString(), i, key, dc.getActive(), dc.getAvgBucket(),
                                dc.getWeight(), dc.getMean(), dc.getQPS(), dc.getFailed());

                        log.info(s);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000, 500);

    }

    private static class InvokerStatsBuilder {
        private static InvokerStats INSTANCE = new InvokerStats();
    }

    public static InvokerStats getInstance() {
        return InvokerStatsBuilder.INSTANCE;
    }

    private ConcurrentMap<String, DataCollector> dataMap = new ConcurrentHashMap();



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

    public ConcurrentMap<String, DataCollector> getDataCollectors() {
        return dataMap;
    }

}
