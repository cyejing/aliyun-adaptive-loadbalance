package com.aliware.tianchi.loadbalance;


import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

public class BucketLoadBalance implements LoadBalance {

    private static final Logger log = LoggerFactory.getLogger(BucketLoadBalance.class);

    private Timer logTimer = new Timer();

    private ConcurrentMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
    private int time = 0;

    private final LoadBalance loadBalance;

    public BucketLoadBalance(LoadBalance loadBalance) {
        System.out.println("make by Born");
        this.loadBalance = loadBalance;
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ConcurrentMap<String, DataCollector> dcm = InvokerStats.getInstance()
                            .getDataCollectors();
                    for (Entry<String, DataCollector> e : dcm.entrySet()) {
                        String key = e.getKey();
                        DataCollector dc = e.getValue();
                        String s = String.format(
                                LocalDateTime.now().toString() +
                                        " bucket key:%s, active:%d, limit:%d, mean:%f, qps:%d. limitCount:%d",
                                key, dc.getActive(), dc.getBucket(), dc.getMean(), dc.getQPS(), map.get(key).get());

                        log.info(s);
                    }
                    time++;
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000, 500);

    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        List<Invoker<T>> selects = new ArrayList<>(invokers);
        for (Invoker invoker : invokers) {
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            int limit = dc.getBucket();
            int active = dc.getActive();
            if (active < limit) {
                String key = invoker.getUrl().toIdentityString();
                AtomicInteger a = map.get(key);
                if (a == null) {
                    map.putIfAbsent(key, new AtomicInteger(0));
                    a = map.get(key);
                }
                if (time > 20) {
                    a.incrementAndGet();
                }
                selects.add(invoker);
            }

        }

        if (CollectionUtils.isEmpty(selects)) {
            selects = invokers;
        }

        Invoker<T> select = loadBalance.select(selects, url, invocation);
        return select;
    }

}
