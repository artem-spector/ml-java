package org.artem.tools.regression;

import org.artem.tools.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public interface RegressionModel {

    Matrix calculateHypothesis(Matrix theta, Matrix X);

    double calculateCost(Matrix y, Matrix h);
}
