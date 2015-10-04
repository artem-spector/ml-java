package ml.profiler;

import java.lang.reflect.Method;
import java.util.Formatter;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class MethodStatistics {

    private static final String LINE_FORMAT = "%30s\t|\t%20s\t|\t%5d\t|\t%4d\t|\t%6.2f\t|\t%5.2f";
    private static final String HEAD_FORMAT = "%30s\t|\t%20s\t|\t%5s\t|\t%4s\t|\t%6s\t|\t%5s";

    public final Class targetClass;
    public final Method method;
    private boolean counting;

    private int count;
    private double mean;
    private double variance;
    private double std;
    private long cumulativeTime;

    public MethodStatistics(Class targetClass, Method method) {
        this.targetClass = targetClass;
        this.method = method;
        counting = true;
    }

    public synchronized void reportCall(long durationMillis) {
        assert counting;

        cumulativeTime += durationMillis;

        if (count++ == 0) {
            mean = durationMillis;
        } else {
            double oldMean = mean;
            mean += (durationMillis - mean) / count;
            variance += (durationMillis - oldMean) * (durationMillis - mean);
        }
    }

    public void stopCounting() {
        assert counting;
        counting = false;
        variance /= count - 1;
        std = Math.sqrt(variance);
    }

    public String toFormattedString() {
        assert !counting;
        StringBuilder str = new StringBuilder();
        new Formatter(str).format(LINE_FORMAT, targetClass.getSimpleName(), method.getName(), cumulativeTime, count, mean, std);
        return str.toString();
    }

    public static String getHeadStr() {
        StringBuilder str = new StringBuilder();
        new Formatter(str).format(HEAD_FORMAT, "Class", "Method", "Total", "Count", "Mean", "Std");
        return str.toString();
    }

    public long getCumulativeTime() {
        return cumulativeTime;
    }
}
