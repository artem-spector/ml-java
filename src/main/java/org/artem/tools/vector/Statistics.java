package org.artem.tools.vector;

import org.artem.tools.FunctionEvaluator;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/1/15
 */
public class Statistics {

    public final double mean;
    public final double variance;
    public final double std;
    public final int count;
    public final FunctionEvaluator normalizeFunction;

    public Statistics(double mean, double variance, double std, int count) {
        this.mean = mean;
        this.variance = variance;
        this.std = std;
        this.count = count;

        normalizeFunction = x -> (x - mean) / std;
    }
}
