package com.aliware.tianchi.loadbalance;

import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;

public class DynamicWeightedLoadBalance extends BasicWeightedLoadBalance {

    private static final Logger log = LoggerFactory.getLogger(DynamicWeightedLoadBalance.class);

    public static final int TRIPPED_DECREASE_WEIGHT = 50;
    public static final int REGAIN_DECREASE_WEIGHT = 60;

    private Timer timer = new Timer();
    private Timer logTimer = new Timer();

    private final WeightedLoadBalance weightedLoadBalance;

    public DynamicWeightedLoadBalance(WeightedLoadBalance weightedLoadBalance) {
        this.weightedLoadBalance = weightedLoadBalance;

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    for (WeightedRoundRobin wrr : getMap().values()) {
                        wrr.increaseWeight(REGAIN_DECREASE_WEIGHT);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 0, 200);

        logTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Set<Entry<String, WeightedRoundRobin>> entries = getMap().entrySet();
                    for (Entry<String, WeightedRoundRobin> e : entries) {
                        String key = e.getKey();
                        WeightedRoundRobin wrr = e.getValue();

                        DataCollector dc = InvokerStats.getInstance().getDataCollector(key);
                        String s = String.format(
                                "adjustment weight key:%s, weight:%d, current:%d, Succeed:%d, SucceedWindow:%d .",
                                wrr.getKey(), wrr.getWeight(), wrr.getCurrent(), dc.getSucceedRequestCount(),
                                dc.getSucceedRequestCountInWindow());
                        log.info(s);
                    }
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        }, 0, 600);
    }

    private ConcurrentMap<String, WeightedRoundRobin> map = new ConcurrentHashMap<>();

    public void tripped(Invoker invoker) {
        String identifyString = invoker.getUrl().toIdentityString();
        WeightedRoundRobin weightedRoundRobin = map.get(identifyString);
        weightedRoundRobin.decreaseWeight(TRIPPED_DECREASE_WEIGHT);
    }

    @Override
    protected ConcurrentMap<String, WeightedRoundRobin> getMap() {
        return this.map;
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        Invoker<T> select = super.select(invokers, url, invocation);
        if (select == null) {
            select = weightedLoadBalance.select(invokers, url, invocation);
        }
        return select;
    }
}
