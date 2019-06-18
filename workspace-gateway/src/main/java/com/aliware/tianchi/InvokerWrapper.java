package com.aliware.tianchi;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
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

/**
 * @author Born
 */
public class InvokerWrapper<T> implements Invoker<T> {

    private static final Logger log = LoggerFactory.getLogger(InvokerWrapper.class);

    public static final int RETRY_FLAG = -1111;

    private final List<Invoker<T>> invokers;
    private final URL url;
    private final Invocation invocation;
    private final LoadBalance loadBalance;


    private AtomicReference<Invoker> invoker = new AtomicReference<>();

    public InvokerWrapper(List<Invoker<T>> invokers, URL url, Invocation invocation, LoadBalance loadBalance) {
        this.invokers = invokers;
        this.url = url;
        this.invocation = invocation;
        this.loadBalance = loadBalance;
        this.invoker.set(loadBalance.select(invokers, url, invocation));
    }

    public Invoker getInvoker() {
        return invoker.get();
    }

    Invoker select() {
        List<Invoker<T>> r = invokers.stream().filter(i -> !i.equals(getInvoker())).collect(Collectors.toList());
        return loadBalance.select(r, url, invocation);
    }

    @Override
    public Class<T> getInterface() {
        return getInvoker().getInterface();
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return getInvoker().isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        Result result = getInvoker().invoke(invocation);
        if (result instanceof SimpleAsyncRpcResult) {
            CompletableFuture<Integer> valueFuture = ((SimpleAsyncRpcResult) result).getValueFuture();
            CompletableFutureWrapper cfw = new CompletableFutureWrapper(valueFuture);

            cfw.setHandle((a, t) -> {
                if (t != null) {
                    return RETRY_FLAG;
                }
                return a;
            });

            cfw.setRetry1((a) -> {
                if (a == RETRY_FLAG) {
                    this.invoker.set(select());
                    Result retry = getInvoker().invoke(invocation);
                    if (retry instanceof SimpleAsyncRpcResult) {
                        return ((SimpleAsyncRpcResult) retry).getValueFuture();
                    }
                }
                return CompletableFuture.supplyAsync(() -> a);
            });

            cfw.setHandle1((a, t) -> {
                if (t != null) {
                    return RETRY_FLAG;
                }
                return a;
            });

            RpcContext.getContext().setFuture(cfw);
        }
        return result;
    }


    @Override
    public void destroy() {
        getInvoker().destroy();
    }
}
