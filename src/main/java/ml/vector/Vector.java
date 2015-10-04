package ml.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/1/15
 */
public interface Vector {

    enum VectorType {ROW, COLUMN}

    VectorType type();

    int length();

    double get(int pos);

    void set(int pos, double value);

    double[] asArray();

    default double multiply(Vector v) {
        assert type() == VectorType.ROW && v.type() == VectorType.COLUMN && length() == v.length();
        double res = 0;
        for (int i = 0; i < length(); i++) res += get(i) * v.get(i);
        return res;
    }

    default double sum() {
        double res = 0;
        for (int i = 0; i < length(); i++)
            res += get(i);
        return res;
    }

    default int idxMin() {
        double minValue = Double.MAX_VALUE;
        int res = 0;
        for (int i = 0; i < length(); i++) {
            double value = get(i);
            if (value < minValue) {
                minValue = value;
                res = i;
            }
        }
        return res;
    }

    /**
     *
     * @return
     */
    default int idxMax() {
        double maxValue = -Double.MAX_VALUE;
        int res = 0;
        for (int i = 0; i < length(); i++) {
            double value = get(i);
            if (value > maxValue) {
                maxValue = value;
                res = i;
            }
        }
        return res;
    }

    /**
     * Statistics for this vector
     * calculated in one pass, as described at <a href="http://www.johndcook.com/blog/standard_deviation/">John D. Cook's blog<a/>
     */
    default Statistics calculateStatistics() {
        int count = 0;
        double mean = 0;
        double variance = 0;

        for (int i =0; i < length(); i++) {
            double v = get(i);
            if (count++ == 0) {
                mean = v;
            } else {
                double oldMean = mean;
                mean += (v - mean) / count;
                variance += (v - oldMean) * (v - mean);
            }
        }
        variance /= count - 1;
        double std = Math.sqrt(variance);

        return new Statistics(mean, variance, std, count);
    }
}
