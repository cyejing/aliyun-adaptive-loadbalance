package com.aliware.tianchi;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;

public class CompletableFutureWrapper<T> extends CompletableFuture<T> {
    private static final Logger log = LoggerFactory.getLogger(CompletableFutureWrapper.class);

    private final  CompletableFuture<T> completableFuture;

    public CompletableFutureWrapper(CompletableFuture completableFuture) {
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
       return  super.handle((a,t) -> {
            log.info("完成拦截");
            return a;
        }).whenComplete(action);
    }




}
