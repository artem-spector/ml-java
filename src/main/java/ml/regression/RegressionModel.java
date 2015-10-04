package ml.regression;

import ml.DoubleMatrix;
import ml.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public interface RegressionModel {

    Matrix calculateHypothesis(Matrix theta, Matrix X);

    double calculateCost(Matrix theta, Matrix X, Matrix y, Matrix h);
}
