package com.aliware.tianchi.stats;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.TimeUnit;

/**
 * @author Born
 */
public class Stopwatch {

    private long startTime;
    private long elapsedNanos;
    private boolean running;

    public static Stopwatch createUnStarted() {
        return new Stopwatch();
    }

    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }


    public static Stopwatch createStarted(long startTime) {
        return new Stopwatch().start(startTime);
    }


    public Stopwatch start(long startTime) {
        this.startTime = startTime;
        running = true;
        return this;
    }

    public Stopwatch start() {
        startTime = System.nanoTime();
        running = true;
        return this;
    }

    public Stopwatch stop() {
        elapsedNanos += System.nanoTime() - startTime;
        running = false;
        return this;
    }

    public Stopwatch reset() {
        elapsedNanos = 0;
        running = false;
        return this;
    }

    private long elapsedNanos() {
        return running ? System.nanoTime() - startTime + elapsedNanos : elapsedNanos;
    }

    public long elapsed(TimeUnit desiredUnit) {
        return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
    }


}
