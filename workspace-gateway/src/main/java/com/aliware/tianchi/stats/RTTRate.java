package com.aliware.tianchi.stats;

/**
 * @author Born
 */
public class RTTRate {

    //    α = 0.125，β = 0.25， μ = 1，∂ = 4；
    private static final double α = 0.125;
    private static final double β = 0.25;
    private static final double μ = 1;
    private static final double e = 4;

    private double srtt = 0;
    private double devRtt = 0;

    private long delayThreshold;

    public RTTRate(long delay) {

        this.delayThreshold = System.currentTimeMillis() + delay;
    }

    public static void main(String[] args) {
        RTTRate rttRate = new RTTRate(0);
        for (int i = 0; i < 10; i++) {
            System.out.println(rttRate.calc(45));
            System.out.println(rttRate.calc(70));
            System.out.println(rttRate.calc(60));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(50));
            System.out.println(rttRate.calc(70));
            System.out.println(rttRate.calc(70));
        }

    }

    public synchronized double calc(double rtt) {
        if (delayThreshold > System.currentTimeMillis()) {
            return 0;
        }
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
        return (new Double(100000 / getRTO()).intValue());
    }

}
