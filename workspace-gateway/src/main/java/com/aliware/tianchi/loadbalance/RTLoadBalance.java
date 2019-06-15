package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;

public class RTLoadBalance extends BasicWeightedLoadBalance {

    private static final Logger log = LoggerFactory.getLogger(RTLoadBalance.class);

    private Timer timer = new Timer();

    public RTLoadBalance() {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (WeightedRoundRobin wrr : getMap().values()) {
                        DataCollector dc = InvokerStats.getInstance().getDataCollector(wrr.getKey());
                        wrr.setWeight(dc.getOneQPS());
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 1000, 100);
    }

    private ConcurrentMap<String, WeightedRoundRobin> map = new ConcurrentHashMap<>();

    protected ConcurrentMap<String, WeightedRoundRobin> getMap() {
        return this.map;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> select = super.select(invokers, url, invocation);
        if (select == null) {
            log.error("不应该出现的位置");
            select = invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
        }
        return select;
    }
}
