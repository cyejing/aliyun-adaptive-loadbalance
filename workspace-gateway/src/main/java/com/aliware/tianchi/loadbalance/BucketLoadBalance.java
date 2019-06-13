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
                    List<String> sort = InvokerStats.getInstance().getSort();
                    for (String key : sort) {
                        Bucket bucket = map.get(key);
                        DataCollector dc = InvokerStats.getInstance().getDataCollector(key);
                        String s = String.format(
                                LocalDateTime.now().toString() +
                                        " bucket weight key:%s, active:%d, breaking:%s Succeed:%d, SucceedWindow:%d.",
                                key, bucket.getActive(), String.valueOf(bucket.isBreaking()),
                                dc.getSucceedRequestCount(), dc.getSucceedRequestCountInWindow());

                        log.info(s);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000 * 10, 600);

    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        List<String> sort = InvokerStats.getInstance().getSort();
        List<Invoker<T>> selects = new ArrayList<>(invokers);
        for (String key : sort) {
            Bucket bucket = map.get(key);
            if (bucket == null) {
                map.putIfAbsent(key, new Bucket());
                bucket = map.get(key);
            }
            if (bucket.isBreaking()) {
                selects.removeIf(i -> i.getUrl().toIdentityString().equals(key));
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
        String key = invoker.getUrl().toIdentityString();
        Bucket bucket = map.get(key);
        if (bucket == null) {
            map.putIfAbsent(key, new Bucket());
            bucket = map.get(key);
        }
        bucket.increment();
    }

    public void decrement(Invoker invoker) {
        map.get(invoker.getUrl().toIdentityString()).decrement();
    }

    public void breaking(Invoker invoker) {
        map.get(invoker.getUrl().toIdentityString()).breaking();
    }

    static class Bucket {

        private AtomicInteger active = new AtomicInteger(0);
        private volatile boolean breaking = false;

        public Bucket() {

        }

        public int increment() {
            return active.incrementAndGet();
        }

        public int decrement() {
            int i = active.decrementAndGet();
            breaking = false;
            return i;
        }

        public void breaking() {
            breaking = true;
        }

        public boolean isNotBreaking() {
            return !breaking;
        }

        public boolean isBreaking() {
            return breaking;
        }

        public int getActive() {
            return active.get();
        }

    }
}
