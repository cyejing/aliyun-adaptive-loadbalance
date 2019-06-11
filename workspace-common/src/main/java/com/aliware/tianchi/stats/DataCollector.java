package com.aliware.tianchi.stats;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Born
 */
public class DataCollector {

    private AtomicInteger activeRequestsCount = new AtomicInteger(0);
    private AtomicInteger failedRequestsCount = new AtomicInteger(0);
    private MeasuredRate requestCountInWindow = new MeasuredRate(1000 );
    private MeasuredRate failedRequestCountInWindow = new MeasuredRate(1000 );
    private Distribution distribution = new Distribution();

    public void incrementFailedRequests() {
        failedRequestsCount.incrementAndGet();
        failedRequestCountInWindow.increment();
    }

    public void incrementRequests() {
        activeRequestsCount.incrementAndGet();
        requestCountInWindow.increment();
    }

    public void decrementRequests() {
        if (activeRequestsCount.decrementAndGet() < 0) {
            activeRequestsCount.set(0);
        }
    }

    public int getSucceedRequestCount() {
        return activeRequestsCount.get() - failedRequestsCount.get();
    }
    public int getSucceedRequestCountInWindow() {
        return requestCountInWindow.getMaxCount() - failedRequestCountInWindow.getMaxCount();
    }

    public int getFailedRequestCount() {
        return failedRequestsCount.get();
    }

    public int getFailedRequestCountInWindow() {
        return failedRequestCountInWindow.getCount();
    }

    public int getActiveRequestsCount() {
        return activeRequestsCount.get();
    }

    public int getRequestCountInWindow() {
        return requestCountInWindow.getCount();
    }

    public int getMaxActive() {
       return requestCountInWindow.getMaxCount();
    }


    public Distribution getDistribution() {
        return distribution;
    }
}
