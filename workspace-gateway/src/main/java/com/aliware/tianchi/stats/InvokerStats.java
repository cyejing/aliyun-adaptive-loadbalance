package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;
import static com.aliware.tianchi.stats.DataCollector.BETA;
import static com.aliware.tianchi.stats.DataCollector.GAMMA;

import com.aliware.tianchi.stats.DataCollector.DataCollectorCopy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
        System.out.println("make by Born. ALPHA: " + ALPHA + ", BETA: " + BETA + ", GAMMA: " + GAMMA);
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Map<String, DataCollector> dcm = InvokerStats.getInstance().getDataCollectors();
                    List<DataCollectorCopy> copyList = new ArrayList<>();
                    double totalWeight = 0;
                    for (DataCollector dc : dcm.values()) {
                        DataCollectorCopy copy = dc.copy();
                        copyList.add(copy);
                        totalWeight += copy.getWeight();
                    }


                    for (DataCollectorCopy dc : copyList) {
                        String s = String.format(
                                "%s bucket active:%d, bucket:%d, weight:%f, throughput:%d.",
                                LocalDateTime.now().toString(), dc.getActive(), dc.getBucket(),
                                dc.getWeight() / totalWeight, dc.getWeight());

                        log.info(s);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000, 200);

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
