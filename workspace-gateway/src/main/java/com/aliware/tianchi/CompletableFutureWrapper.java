package com.aliware.tianchi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

public class CompletableFutureWrapper extends CompletableFuture<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CompletableFutureWrapper.class);

    private final CompletableFuture<Integer> completableFuture;
    private final Function<Integer,CompletionStage<Integer>> retry;
    private final Function<Throwable, Integer> exHandler ;

    public CompletableFutureWrapper(CompletableFuture completableFuture,
            Function<Throwable, Integer> exHandler,
            Function<Integer, CompletionStage<Integer>> fn) {
        this.exHandler = exHandler;
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
        return super.exceptionally(exHandler)
                .thenCompose(retry)
                .exceptionally(exHandler)
                .thenCompose(retry)
                .whenComplete(action);
    }


}
