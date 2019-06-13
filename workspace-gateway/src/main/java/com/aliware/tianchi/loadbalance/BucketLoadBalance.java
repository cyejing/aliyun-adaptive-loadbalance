package com.aliware.tianchi.loadbalance;


import com.aliware.tianchi.stats.InvokerStats;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

public class BucketLoadBalance  implements LoadBalance {

    private static final Logger log = LoggerFactory.getLogger(BucketLoadBalance.class);

    private ConcurrentMap<String, Bucket> map = new ConcurrentHashMap<>();

    private final LoadBalance loadBalance;

    public BucketLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        List<String> sort = InvokerStats.getInstance().getSort();
        List<Invoker<T>> selects = new ArrayList<>(invokers);
        for (String key : sort) {
            Bucket bucket = map.get(key);
            if (bucket.isBreaking()) {
                selects.removeIf(i -> i.getUrl().toIdentityString().equals(key));
            }
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

    public void breaking(Invoker invoker) {
        map.get(invoker.getUrl().toIdentityString()).breaking();
    }

    static class Bucket{

        private AtomicInteger active = new AtomicInteger(0);
        private volatile boolean breaking = false;
        private final Invoker invoker;

        public Bucket(Invoker invoker) {
            this.invoker = invoker;
        }

        public int increment() {
            return active.incrementAndGet();
        }

        public int decrement() {
            int i = active.decrementAndGet();
            breaking = true;
            return i;
        }

        public void breaking() {
            breaking = false;
        }

        public boolean isNotBreaking() {
            return !breaking;
        }

        public boolean isBreaking() {
            return breaking;
        }
    }
}
