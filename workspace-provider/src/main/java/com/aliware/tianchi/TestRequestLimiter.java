package com.aliware.tianchi;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.remoting.exchange.Request;
import org.apache.dubbo.remoting.transport.RequestLimiter;

/**
 * @author daofeng.xjf
 *
 * 服务端限流
 * 可选接口
 * 在提交给后端线程池之前的扩展，可以用于服务端控制拒绝请求
 */
public class TestRequestLimiter implements RequestLimiter {

    private static final Logger log = LoggerFactory.getLogger(TestRequestLimiter.class);


    /**
     * @param request 服务请求
     * @param activeCount 服务端对应线程池的活跃线程数
     * @return  false 不提交给服务端业务线程池直接返回，客户端可以在 Filter 中捕获 RpcException
     *          true 不限流
     */
    @Override
    public boolean tryAcquire(Request request, int activeCount) {
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        Map<String, Object> executors = dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executors.values().iterator().next();
        int max = executor.getMaximumPoolSize();
        if (activeCount+10 >= max) {
            log.warn("服务器线程已满,开始限制流量.activeTaskCount" + activeCount + ",max" + max);
            return false;
        }
        return true;
    }

}
