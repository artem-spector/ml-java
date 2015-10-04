package ml.regression;

import ml.DoubleMatrix;
import ml.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class LinearModel implements RegressionModel {

    @Override
    public Matrix calculateHypothesis(Matrix theta, Matrix X) {
        return X.multiply(theta);
    }

    @Override
    public double calculateCost(Matrix theta, Matrix X, Matrix y, Matrix h) {
        Matrix diff = h.subtract(y);
        double squareSum = diff.applyFunction(arg -> arg * arg).getColumn(0).sum();
        return squareSum / (2 * y.numRows());
    }
}
