package org.artem.tools.regression;

import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class TrainingSet {

    MatrixFactory matrixFactory;

    private Matrix X;
    private Matrix transposedX;

    private boolean istransformed;
    XTransformation xTransformation;

    RegressionModel modelCalculator;
    private double lambda;

    public TrainingSet setMatrixFactory(MatrixFactory matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public TrainingSet setX(Matrix X) {
        this.X = X;
        attemptTransform();
        return this;
    }

    public TrainingSet setModelCalculator(RegressionModel calculator) {
        this.modelCalculator = calculator;
        return this;
    }

    public TrainingSet setRegularization(double lambda) {
        this.lambda = lambda;
        return this;
    }

    public TrainingSet setXTransformation(XTransformation transformation) {
        this.xTransformation = transformation;
        attemptTransform();
        return this;
    }

    private void attemptTransform() {
        assert !istransformed;
        if (X != null && xTransformation != null) {
            X = xTransformation.transform(X);
            transposedX = X.transpose();
            istransformed = true;
        }
    }

    public int getThetaSize() {
        return X.numColumns();
    }

    public Matrix getHypothesis(Matrix theta) {
        return modelCalculator.calculateHypothesis(theta, X);
    }

    public double getCost(Matrix theta, Matrix y) {
        double cost = modelCalculator.calculateCost(y, getHypothesis(theta));
        if (lambda != 0) cost += costRegularization(theta);
        return cost;
    }

    public Matrix getGradient(Matrix theta, Matrix y) {
        Matrix diff = getHypothesis(theta).subtract(y);
        Matrix gradient = transposedX.multiply(diff).applyFunction(x -> x / X.numRows());
        if (lambda != 0) gradient = gradient.add(gradientRegularization(theta));
        return gradient;
    }

    private double costRegularization(Matrix theta) {
        if (lambda == 0) return 0;
        double sum = 0;
        for (int i = 1; i < theta.numRows(); i++) sum += Math.pow(theta.get(i, 0), 2);
        return sum * lambda / (2 * X.numRows());
    }

    private Matrix gradientRegularization(Matrix theta) {
        Matrix res = matrixFactory.createMatrix(theta.numRows(), theta.numColumns(), 0);
        if (lambda != 0) {
            for (int i = 1; i < theta.numRows(); i++)
                res.set(i, 0, theta.get(i, 0) * lambda / X.numRows());
        }
        return res;
    }
}
