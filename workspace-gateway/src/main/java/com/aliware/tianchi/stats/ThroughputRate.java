package com.aliware.tianchi.stats;

import static com.aliware.tianchi.stats.DataCollector.ALPHA;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Born
 */
public class ThroughputRate {

    AtomicInteger throughput = new AtomicInteger(0);

    private volatile double throughputRate;
    private volatile double weight;
    //    private volatile AtomicInteger rise = new AtomicInteger(0);
    private volatile AtomicReference<Rise> rise = new AtomicReference<>();

    private long interval;
    private volatile long threshold;

    class Rise{

        private double weight;
        private boolean rise;

        public Rise(double weight, boolean rise) {
            this.weight = weight;
            this.rise = rise;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Rise)) {
                return false;
            }
            Rise rise1 = (Rise) o;
            return Double.compare(rise1.weight, weight) == 0 &&
                    rise == rise1.rise;
        }

        @Override
        public int hashCode() {
            return Objects.hash(weight, rise);
        }
    }

    public ThroughputRate(long interval) {
        this.interval = interval;
        this.threshold = System.currentTimeMillis() + interval;
    }

    public int note() {
        checkAndSet();
        return throughput.incrementAndGet();
    }

    public double getThroughputRate() {
        checkAndSet();
        return throughputRate;
    }

    public double getWeight() {
        checkAndSet();
        return weight;
    }

    public void checkAndSet() {
        int i = this.throughput.get();
        long now = System.currentTimeMillis();
        long t = this.threshold;
        if (now > t) {
            double oWeight = this.weight;
            double nWeight = i * (1000D / (now - t + interval));
            this.throughputRate = nWeight;
            double devWeight = Math.abs(nWeight - oWeight);
            nWeight = oWeight * (1 - ALPHA) + nWeight * ALPHA;

            if (nWeight > oWeight || devWeight > 1200) {
                this.weight = nWeight;
                this.rise.set(new Rise(nWeight, true));
                System.out.println(LocalDateTime.now().toString() + "设置时间上升" + nWeight);
            }else{
                this.weight = oWeight;
                if (this.rise.compareAndSet(new Rise(oWeight, true), new Rise(oWeight, false))) {
                    System.out.println(LocalDateTime.now().toString() + " 设置时间下降" + oWeight);
                }
            }


            this.throughput.set(0);
            this.threshold = now + interval;
        }
    }

    public boolean isRise() {
        if (rise.get().rise) {
            return true;
        }
        return false;
    }
}
