package com.aliware.tianchi.stats;

/**
 * @author Born
 */
public class RTTRate {

    //    α = 0.125，β = 0.25， μ = 1，∂ = 4；
    private static final double α = 0.125;
    private static final double β = 0.75;
    private static final double μ = 1;
    private static final double e = 1;

    double srtt = 100;
    double devRtt = 1;

    private long delayThreshold;

    public RTTRate(long delay) {
        this.delayThreshold = System.currentTimeMillis() + delay;
    }

    public static void main(String[] args) {
        RTTRate rttRate = new RTTRate(0);
        for (int i = 0; i < 10; i++) {
            int jsize = 10;
            for (int j = 0; j < jsize; j++) {
                System.out.println(i * jsize + j + ":   " + rttRate.calc(50) + ", " + rttRate.srtt + ", " + rttRate.devRtt);
            }
            System.out.println(i * jsize + "f:   " + rttRate.calc(70) + ", " + rttRate.srtt + ", " + rttRate.devRtt);
            System.out.println(i * jsize + "f:   " + rttRate.calc(70) + ", " + rttRate.srtt + ", " + rttRate.devRtt);

        }


    }

    public double calc(double rtt) {
        double osrtt = srtt;
        double odevRtt = devRtt;
        devRtt = (1 - β) * odevRtt + β * (Math.abs(rtt - osrtt));
        srtt = osrtt + α * (rtt - osrtt);
        return μ * srtt + e * devRtt;
    }

    public double getRTO() {
        return μ * srtt + e * devRtt;
    }

    public int getOneQPS() {
        if (getRTO() == 0) {
            return 1000;
        }
        return (new Double(10000D / getRTO()).intValue());
    }

}
