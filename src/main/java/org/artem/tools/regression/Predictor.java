package org.artem.tools.regression;

import org.artem.tools.FunctionEvaluator;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/17/15
 */
public class Predictor {

    private MatrixFactory matrixFactory;
    private XTransformation xTransformation;
    private Matrix theta;
    private RegressionModel model;

    public Predictor(MatrixFactory matrixFactory, RegressionModel model, XTransformation xTransformation, Matrix theta) {
        this.matrixFactory = matrixFactory;
        this.xTransformation = xTransformation;
        this.theta = theta;
        this.model = model;
    }

    public Matrix getTheta() {
        return theta;
    }

    public double predict(FunctionEvaluator f, double... x) {
        return predict(f, matrixFactory.createMatrix(new double[][]{x})).get(0, 0);
    }

    public Matrix predict(FunctionEvaluator f, Matrix X) {
        Matrix transformed = xTransformation.transform(X);
        assert transformed.numColumns() == theta.numRows();

        Matrix h = model.calculateHypothesis(theta, transformed);
        return f == null ? h : h.applyFunction(f);
    }

    public double getPredictionAccuracy(FunctionEvaluator f, Matrix X, Matrix expectedY) {
        Matrix predictedY = predict(f, X);
        double correctPredictions = predictedY.subtract(expectedY).applyFunction(x -> x == 0 ? 1 : 0).getColumn(0).sum();
        return correctPredictions / X.numRows();
    }
}
