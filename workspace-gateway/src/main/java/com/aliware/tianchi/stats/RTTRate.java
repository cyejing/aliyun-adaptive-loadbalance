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
        for (int i = 0; i < 1000; i++) {
            RTTRate rttRate = new RTTRate(0);
            System.out.println(rttRate.calc(45));;
            if (i == 300) {
                System.out.println("fuck");
                System.out.println(rttRate.calc(70));
            }
        }

    }

    public synchronized double calc(double rtt) {
        if (delayThreshold > System.currentTimeMillis()) {
            return 0;
        }
        srtt = srtt + α * (rtt - srtt);
        devRtt = (1 - β) * devRtt + β * (Math.abs(rtt - srtt));
        return μ * srtt + e * devRtt;
    }

    public double getRTO() {
        return μ * srtt + e * devRtt;
    }

    public int getOneQPS() {
        return (new Double(1000 / getRTO()).intValue());
    }

}
