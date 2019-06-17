package com.aliware.tianchi;

import com.aliware.tianchi.stats.DataCollector;
import com.aliware.tianchi.stats.InvokerStats;
import com.aliware.tianchi.stats.Stopwatch;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.qos.server.handler.HttpProcessHandler;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

/**
 * @author daofeng.xjf
 *
 * 客户端过滤器
 * 可选接口
 * 用户可以在客户端拦截请求和响应,捕获 rpc 调用时产生、服务端返回的已知异常。
 */
@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(TestClientFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try{
            DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
            dc.incrementRequests();
            invocation.getAttachments().put("active", String.valueOf(dc.getActive()));
            Result result = invoker.invoke(invocation);
            return result;
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        DataCollector dc = InvokerStats.getInstance().getDataCollector(invoker);
        if (result.hasException()) {
            String active = invocation.getAttachment("active");
            dc.setBucket(Integer.valueOf(active));
            dc.decrementRequests();
            dc.incrementFailedRequests();
        }else{
            dc.decrementRequests();
            dc.succeedRequest();
        }
        return result;
    }



}
