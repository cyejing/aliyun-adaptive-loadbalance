package com.aliware.tianchi;

import com.aliware.tianchi.stats.InvokerStats;
import com.aliware.tianchi.stats.Stopwatch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

/**
 * @author Born
 */
public class InvokerWrapper<T> implements Invoker<T> {
    private static final Logger log = LoggerFactory.getLogger(InvokerWrapper.class);

    public static final int RETRY_FLAG = -1111;

    private final List<Invoker<T>> invokers;
    private final URL url;
    private final Invocation invocation;
    private final WeightedLoadBalance loadBalance;


    private Invoker<T> invoker;

    public InvokerWrapper(List<Invoker<T>> invokers, URL url, Invocation invocation, WeightedLoadBalance loadBalance) {
        this.invokers = invokers;
        this.url = url;
        this.invocation = invocation;
        this.loadBalance = loadBalance;
        this.invoker = loadBalance.select(invokers, url, invocation);
    }

    Invoker select() {
        List<Invoker<T>> r = invokers.stream().filter(i -> !i.equals(invoker)).collect(Collectors.toList());
        return loadBalance.select(r, url, invocation);
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
            CompletableFuture<Integer> valueFuture = ((SimpleAsyncRpcResult) result).getValueFuture();
            CompletableFutureWrapper cfw = new CompletableFutureWrapper(valueFuture);

            AtomicReference<Invoker> realInvoke = new AtomicReference<>(this.invoker);

            cfw.setExceptionally( (t) -> {
                loadBalance.tripped(this.invoker);
                return RETRY_FLAG;
            });
            Invoker invoker1 = select();
            cfw.setRetry1((a) -> {
                if (a == RETRY_FLAG) {
                    log.error("重试请求");
                    realInvoke.set(invoker1);
                    Result retry = invoker1.invoke(invocation);
                    if (retry instanceof SimpleAsyncRpcResult) {
                        return ((SimpleAsyncRpcResult) retry).getValueFuture();
                    }
                }
                return CompletableFuture.supplyAsync(() -> a);
            });

            cfw.setExceptionally1( (t) -> {
                loadBalance.tripped(invoker1);
                return RETRY_FLAG;
            });
            Invoker invoker2 = select();
            cfw.setRetry2((a) -> {
                if (a == RETRY_FLAG) {
                    log.error("重试请求");
                    realInvoke.set(invoker2);
                    Result retry = invoker2.invoke(invocation);
                    if (retry instanceof SimpleAsyncRpcResult) {
                        return ((SimpleAsyncRpcResult) retry).getValueFuture();
                    }
                }
                return CompletableFuture.supplyAsync(() -> a);
            });

            cfw.setExceptionally2( (t) -> {
                loadBalance.tripped(invoker2);
                return RETRY_FLAG;
            });

//            Stopwatch stopwatch = Stopwatch.createStarted();
            cfw.setCalcResponseTime(() -> {
//                stopwatch.stop();
//                InvokerStats.getInstance().noteResponseTime(realInvoke.get(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            });

            RpcContext.getContext().setFuture(cfw);
        }
        return result;
    }


    @Override
    public void destroy() {
        invoker.destroy();
    }
}
