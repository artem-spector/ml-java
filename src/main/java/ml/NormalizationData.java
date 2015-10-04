package ml;

/**
 * http://www.johndcook.com/blog/standard_deviation/
 *
 * @author artem
 *         Date: 9/15/15
 */
public class NormalizationData {

    private double mean;
    private double variance;
    private double std;
    private int count;

    private boolean stopped;

    public void addValue(double v) {
        if (count++ == 0) {
            mean = v;
        } else {
            double oldMean = mean;
            mean += (v - mean) / count;
            variance += (v - oldMean) * (v - mean);
        }
    }

    public void stop() {
        variance /= count - 1;
        std = Math.sqrt(variance);
        stopped = true;
    }

    public double apply(double val) {
        assert stopped;
        return (val - mean)/std;
    }

    public double getMean() {
        assert stopped;
        return mean;
    }

    public double getVariance() {
        assert stopped;
        return variance;
    }

    public double getStd() {
        assert stopped;
        return std;
    }

    public int getCount() {
        assert stopped;
        return count;
    }
}
