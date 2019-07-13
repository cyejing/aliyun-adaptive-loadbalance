package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;
import static com.aliware.tianchi.stats.DataCollector.BETA;
import static com.aliware.tianchi.stats.DataCollector.COLLECT;
import static com.aliware.tianchi.stats.DataCollector.GAMMA;
import static com.aliware.tianchi.stats.DataCollector.NEUTRON;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invoker;

/**
 * @author Born
 */
public class InvokerStats {

    private static final Logger log = LoggerFactory.getLogger(InvokerStats.class);
    private Timer fire = new Timer();


    public InvokerStats() {
        System.out.println("make by Born. ALPHA: " + ALPHA + ", BETA: " + BETA + ", GAMMA: " + GAMMA);

        fire.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Collection<DataCollector> values = InvokerStats.getInstance().getDataCollectors().values();
                    for (DataCollector dc : values) {
                        if (dc.getThroughputRate().isRise()) {

                            dc.getThroughputRate().reset();
                            if (dc.getThroughputRate().getDevRise().compareAndSet(true, false)) {
                                dc.setRate(NEUTRON);
                            } else {
                                dc.setRate(GAMMA);
                            }
                            System.out.println(LocalDateTime.now().toString()+" bucket:"+dc.getBucket()+" 施压探测,weight: "+dc.getWeight()+" b:" + dc.getThroughputRate().getDevRise().get());
                            Thread.sleep(COLLECT);
                            dc.setRate(1.0);
                            dc.getThroughputRate().decrementRise();
                        }
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 3000, 50);
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
            synchronized (this) {
                dataMap.put(key, new DataCollector());
                dataCollector = dataMap.get(key);
            }
        }
        return dataCollector;
    }

    public DataCollector getDataCollector(Invoker invoker) {
        String key = invoker.getUrl().toIdentityString();
        return getDataCollector(key);
    }

    public Map<String, DataCollector> getDataCollectors() {
        return Collections.unmodifiableMap(dataMap);
    }

    public void putBucket(String port, int max) {
        for (Entry<String, DataCollector> entry : dataMap.entrySet()) {
            if (entry.getKey().contains(port)) {
                entry.getValue().setBucket(max);
                break;
            }
        }
    }

}
