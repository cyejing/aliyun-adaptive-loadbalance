package com.aliware.tianchi;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

public class CompletableFutureWrapper extends CompletableFuture<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CompletableFutureWrapper.class);

    private final CompletableFuture<Integer> completableFuture;

    private Runnable calcResponseTime;

    private Function<Throwable, Integer> exceptionally;

    private Function<Throwable, Integer> exceptionally1;
    private Function<Integer, CompletionStage<Integer>> retry1;

    private Function<Throwable, Integer> exceptionally2;
    private Function<Integer, CompletionStage<Integer>> retry2;


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
                .exceptionally(exceptionally)
                .thenCompose(retry1)
                .exceptionally(exceptionally1)
                .thenCompose(retry2)
                .exceptionally(exceptionally2)
                .whenComplete(action)
                .thenRunAsync(calcResponseTime);
    }

    public void setCalcResponseTime(Runnable calcResponseTime) {
        this.calcResponseTime = calcResponseTime;
    }

    public void setExceptionally(Function<Throwable, Integer> exceptionally) {
        this.exceptionally = exceptionally;
    }

    public void setExceptionally1(Function<Throwable, Integer> exceptionally1) {
        this.exceptionally1 = exceptionally1;
    }

    public void setRetry1(
            Function<Integer, CompletionStage<Integer>> retry1) {
        this.retry1 = retry1;
    }

    public void setExceptionally2(Function<Throwable, Integer> exceptionally2) {
        this.exceptionally2 = exceptionally2;
    }

    public void setRetry2(
            Function<Integer, CompletionStage<Integer>> retry2) {
        this.retry2 = retry2;
    }
}
