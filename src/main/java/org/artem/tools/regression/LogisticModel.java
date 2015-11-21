package org.artem.tools.regression;

import org.artem.tools.FunctionEvaluator;
import org.artem.tools.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class LogisticModel implements RegressionModel {

    private static final FunctionEvaluator SIGMOID = x -> 1d / (1d + Math.exp(-x));

    @Override
    public Matrix calculateHypothesis(Matrix theta, Matrix X) {
        return X.multiply(theta).applyFunction(SIGMOID);
    }

    @Override
    public double calculateCost(Matrix y, Matrix h) {
        double maxCost = Double.MAX_VALUE / y.numRows();
        double sum = 0;
        for (int i = 0; i < y.numRows(); i++) {
            double yi = y.get(i, 0);
            double hi = h.get(i, 0);
            double costI = yi == 0 ? -Math.log(1 - hi) : -Math.log(hi);
            if (costI > maxCost) costI = maxCost;
            sum += costI;
        }
        return sum / y.numRows();
    }
}
