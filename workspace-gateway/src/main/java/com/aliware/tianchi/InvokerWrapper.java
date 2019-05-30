package com.aliware.tianchi;

import static com.aliware.tianchi.CompletableFutureWrapper.RETRY_FLAG;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.SimpleAsyncRpcResult;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;

/**
 * @author Born
 */
public class InvokerWrapper<T> implements Invoker<T> {
    private static final Logger log = LoggerFactory.getLogger(InvokerWrapper.class);

    private final List<Invoker<T>> invokers;
    private final URL url;
    private final Invocation invocation;

    private Invoker<T> invoker;

    private LoadBalance loadBalance = new RandomLoadBalance();


    public InvokerWrapper(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        this.invokers = invokers;
        this.url = url;
        this.invocation = invocation;
        this.invoker = select();
    }

    Invoker select() {
        if (invoker != null) {
            List<Invoker<T>> r = invokers.stream().filter(i -> !i.equals(invoker)).collect(Collectors.toList());
            return loadBalance.select(r, url, invocation);
        }
        Invoker<T> invoker = loadBalance.select(invokers, url, invocation);
        return invoker;
    }

    @Override
    public Class<T> getInterface() {
        return invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return invoker.isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        if (result instanceof SimpleAsyncRpcResult) {
            CompletableFuture<Result> resultFuture = ((SimpleAsyncRpcResult) result).getValueFuture();
            CompletableFutureWrapper any = new CompletableFutureWrapper(resultFuture, (a)->{
                if (a == RETRY_FLAG) {
                    log.error("重试请求");
                    Result retry = select().invoke(invocation);
                    if (retry instanceof SimpleAsyncRpcResult) {
                        return ((SimpleAsyncRpcResult) retry).getValueFuture();
                    }
                }
                return CompletableFuture.supplyAsync(() -> a);
            });
            RpcContext.getContext().setFuture(any);
        }
        return result;
    }

    @Override
    public void destroy() {
        invoker.destroy();
    }
}
