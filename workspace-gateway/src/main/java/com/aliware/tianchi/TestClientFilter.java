package com.aliware.tianchi;

import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import com.aliware.tianchi.stats.Stopwatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.CallbackService;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器 可选接口 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TestClientFilter.class);
//    private ConcurrentMap<Invocation, Stopwatch> map = new ConcurrentHashMap<>(1024 * 1024);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if (invoker.getInterface().isAssignableFrom(CallbackService.class)) {
            return invoker.invoke(invocation);
        }
        try {
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            dc.incrementRequests();
            invocation.getAttachments().put("st", String.valueOf(System.nanoTime()));
            Result result = invoker.invoke(invocation);
            return result;
        } catch (Exception e) {
            log.error("",e);
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        if (invoker.getInterface().isAssignableFrom(CallbackService.class)) {
            return result;
        }
        try {
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            Long startTime = Long.valueOf(invocation.getAttachment("st"));
            Stopwatch stopwatch = Stopwatch.createStarted(startTime);
            if (result.hasException()) {
                dc.incrementFailedRequests();
            } else {
                dc.succeedRequest();
            }
            dc.decrementRequests();
            dc.noteValue(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            log.error("", e);
        }
        return result;
    }


}
