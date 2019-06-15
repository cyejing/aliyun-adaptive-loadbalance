package com.aliware.tianchi.loadbalance;


import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private ConcurrentMap<String, Bucket> map = new ConcurrentHashMap<>();

    private final LoadBalance loadBalance;

    public BucketLoadBalance(LoadBalance loadBalance) {
        System.out.println("make by Born");
        this.loadBalance = loadBalance;
        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Set<Entry<String, Bucket>> entries = map.entrySet();
                    for (Entry<String, Bucket> e : entries) {
                        String key = e.getKey();
                        Bucket bucket = e.getValue();
                        DataCollector dc = InvokerStats.getInstance().getDataCollector(key);
                        String s = String.format(
                                LocalDateTime.now().toString() +
                                        " bucket1 weight key:%s, active:%d, limit:%d, Active:%d, Succeed:%d, SucceedWindow:%d.",
                                key, bucket.getActive(), dc.getBucket(), dc.getActive(), dc.getQPS(), dc.getMaxQPS());

                        log.info(s);
                    }

                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 0, 600);

    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        List<Invoker<T>> selects = new ArrayList<>(invokers);
        for (Invoker invoker : invokers) {
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            int limit = dc.getBucket();
            String key = invoker.getUrl().toIdentityString();
            Bucket bucket = map.get(key);
            if (bucket == null) {
                map.putIfAbsent(key, new Bucket());
                bucket = map.get(key);
            }
            if (bucket.getActive() < limit) {
                selects.add(invoker);
            }

        }

        if (CollectionUtils.isEmpty(selects)) {
            selects = invokers;
        }

        Invoker<T> select = loadBalance.select(selects, url, invocation);
        increment(select);
        return select;
    }

    public void increment(Invoker invoker) {
        map.get(invoker.getUrl().toIdentityString()).increment();
    }

    public void decrement(Invoker invoker) {
        map.get(invoker.getUrl().toIdentityString()).decrement();
    }

    static class Bucket {

        private AtomicInteger active = new AtomicInteger(0);

        public Bucket() {

        }

        public int increment() {
            return active.incrementAndGet();
        }

        public int decrement() {
           return active.decrementAndGet();
        }

        public int getActive() {
            return active.get();
        }

    }
}
