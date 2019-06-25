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

    private final LoadBalance loadBalance;

    public BucketLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        List<Invoker<T>> selects = new ArrayList<>();
        for (Invoker invoker : invokers) {
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            int bucket = dc.getAvgBucket();
            int limit = dc.getActive();
            if (limit + 10 < bucket) {
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
