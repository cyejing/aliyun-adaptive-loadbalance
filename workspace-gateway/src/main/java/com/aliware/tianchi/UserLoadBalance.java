package com.aliware.tianchi;

import com.aliware.tianchi.loadbalance.BucketLoadBalance;
import com.aliware.tianchi.loadbalance.WeightedLoadBalance;
import java.util.List;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance {
    private static final Logger log = LoggerFactory.getLogger(UserLoadBalance.class);

    private static LoadBalance loadBalance = new BucketLoadBalance(new WeightedLoadBalance());

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        return new InvokerWrapper<>(invokers, url, invocation, loadBalance);
    }
}
