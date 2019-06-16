package com.aliware.tianchi.loadbalance;


import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
                        AtomicInteger a = map.get(key);
                        String s = String.format(
                                "%s bucket key:%s, active:%d, limit:%d, bucket:%d, mean:%f, one:%d, qps:%d, failed:%d limitCount:%d",
                                LocalDateTime.now().toString(), key, dc.getActive(),dc.getLimit(), dc.getMaxBucket(), dc.getMean(),dc.getOneQPS(),
                                dc.getQPS(), dc.getFailed(), a==null?0:a.get());

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
            int bucket = dc.getMaxBucket();
            int limit = dc.incrementLimitRequests();
            if (limit < bucket) {
                selects.add(invoker);
            }else{
                String key = invoker.getUrl().toIdentityString();
                AtomicInteger a = map.get(key);
                if (a == null) {
                    map.putIfAbsent(key, new AtomicInteger(0));
                    a = map.get(key);
                }
                a.incrementAndGet();
            }
            dc.decrementLimitRequests();
        }

        if (CollectionUtils.isEmpty(selects)) {
            log.error("全部熔断");
            selects = invokers;
        }

        Invoker<T> select = loadBalance.select(selects, url, invocation);
        return select;
    }

}
