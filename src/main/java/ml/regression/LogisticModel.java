package ml.regression;

import ml.FunctionEvaluator;
import ml.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class LogisticModel implements RegressionModel {

    private static final FunctionEvaluator SIGMOID = x -> 1d / (1d + Math.exp(-x));
    private static final FunctionEvaluator LOG_FUNCTION = x -> x == 1 ? 0 : x == -Double.MAX_VALUE ? 0 : Math.log(x);

    @Override
    public Matrix calculateHypothesis(Matrix theta, Matrix X) {
        return X.multiply(theta).applyFunction(SIGMOID);
    }

    @Override
    public double calculateCost(Matrix theta, Matrix X, Matrix y, Matrix h) {
        Matrix logH = h.applyFunction(LOG_FUNCTION);
        Matrix logOneMinusH = h.applyFunction(x -> 1 - x).applyFunction(LOG_FUNCTION);
        Matrix part1 = y.applyFunction(x -> -x).multiplyElements(logH.getColumn(0));
        Matrix part2 = y.applyFunction(x -> 1 - x).multiplyElements(logOneMinusH.getColumn(0));
        return part1.subtract(part2).getColumn(0).sum() / y.numRows();
    }
}
