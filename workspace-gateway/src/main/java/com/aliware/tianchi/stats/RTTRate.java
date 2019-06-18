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

    private double srtt = 100;
    private double devRtt = 100;

    public synchronized double calc(double rtt) {
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
