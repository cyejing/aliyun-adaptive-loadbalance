package com.aliware.tianchi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.SimpleAsyncRpcResult;

public class CompletableFutureWrapper extends CompletableFuture<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CompletableFutureWrapper.class);

    public static final int RETRY_FLAG = -1111;


    private final CompletableFuture<Integer> completableFuture;
    Function<? super Integer, ? extends CompletionStage<Integer>> retry;


    public CompletableFutureWrapper(CompletableFuture completableFuture,
            Function<? super Integer, ? extends CompletionStage<Integer>> fn) {
        this.retry = fn;
        this.completableFuture = completableFuture;
        this.completableFuture.whenComplete((a, t) -> {
            if (t != null) {
                CompletableFutureWrapper.this.completeExceptionally(t);
            }
            CompletableFutureWrapper.this.complete(a);
        });
    }

    @Override
    public CompletableFuture whenComplete(BiConsumer action) {
        return super.exceptionally(t -> RETRY_FLAG)
                .thenCompose(retry)
                .exceptionally(t -> RETRY_FLAG)
                .thenCompose(retry)
                .whenComplete(action);
    }


}
