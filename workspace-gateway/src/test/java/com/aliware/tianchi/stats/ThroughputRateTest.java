package com.aliware.tianchi.stats;

import static org.junit.Assert.*;

import com.alibaba.fastjson.JSON;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;

public class ThroughputRateTest {

    private ThroughputRate throughputRate = new ThroughputRate(100);


    ExecutorService executorService = Executors.newFixedThreadPool(2000);
    int totalPermit = 1000;
    InternalSemaphore permit = new InternalSemaphore(totalPermit);

    @Test
    public void test() throws Exception {
        String path;

//        path = "/Users/cyejing/Downloads/log/234508_744025_dxEXNs4AHbZF";
        path = "/Users/cyejing/Downloads/log/default";


        GlobalConf conf = JSON.parseObject(FileConfTest.loadJson(path), GlobalConf.class);

        for (int i = 0; i < 10; i++) {
            System.out.println("next avg_rtt:" + conf.small.get(i).avg_rtt);
            calc(conf.small.get(i).avg_rtt, conf.small.get(i).max_concurrent, new CountDownLatch(4000));

        }

    }

    private void calc(double avg_rtt, int curr, CountDownLatch latch) throws InterruptedException {
        int permitChange = totalPermit - curr;
        if (permitChange != 0) {
            if (permitChange > 0) {
                permit.reducePermit(permitChange);
            } else {
                permit.addPermit(Math.abs(permitChange));
            }
        }
        this.totalPermit = curr;
        long size = latch.getCount();
        for (int j = 0; j < size; j++) {

            executorService.submit(() -> {
                try {
                    permit.acquire();
                    long rtt = nextRTT(avg_rtt);
                    Thread.sleep(rtt);
                    synchronized (DistributionRateTest.class){
                        throughputRate.note();
                        latch.countDown();
                        if (latch.getCount() % 100 == 0) {
                            System.out.println("throughputRate:" + throughputRate.getThroughputRate());
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    permit.release();
                }
            });
        }
        latch.await();
    }

    Random random = new Random(2019);

    private long nextRTT(double avg_rtt) {
        double u = random.nextDouble();
        int x = 0;
        double cdf = 0;
        while (u >= cdf) {
            x++;
            cdf = 1 - Math.exp(-1.0D * 1 / avg_rtt * x);
        }
        return x;
    }
}
