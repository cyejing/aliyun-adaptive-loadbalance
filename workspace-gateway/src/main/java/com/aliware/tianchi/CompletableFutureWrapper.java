package com.aliware.tianchi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

public class CompletableFutureWrapper extends CompletableFuture<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CompletableFutureWrapper.class);


    private final CompletableFuture<Integer> completableFuture;


    private BiFunction<Integer, Throwable, Integer> handle;

    private BiFunction<Integer, Throwable, Integer> handle1;
    private Function<Integer, CompletionStage<Integer>> retry1;



    public CompletableFutureWrapper(CompletableFuture<Integer> completableFuture) {
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

        return super
                .handle(handle)
                .thenCompose(retry1)
                .handle(handle1)
                .whenComplete(action);
    }

    public void setHandle(BiFunction<Integer, Throwable, Integer> handle) {
        this.handle = handle;
    }

    public void setHandle1(BiFunction<Integer, Throwable, Integer> handle1) {
        this.handle1 = handle1;
    }

    public void setRetry1(
            Function<Integer, CompletionStage<Integer>> retry1) {
        this.retry1 = retry1;
    }

}
