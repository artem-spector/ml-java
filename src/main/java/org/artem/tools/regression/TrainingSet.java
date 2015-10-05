package org.artem.tools.regression;

import edu.stanford.nlp.optimization.DiffFunction;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.Statistics;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class TrainingSet implements DiffFunction {

    private MatrixFactory matrixFactory;

    private Matrix X;
    private Matrix transposedX;
    private boolean istransformed;

    private boolean transformationsDefined;
    private boolean addBiasColumn;
    private boolean doNormalize;
    private Statistics[] normalization;
    private PolynomialFeatures polynomialFeatures;

    private Matrix y;
    private RegressionModel calculator;
    private double lambda;

    private Matrix cachedTheta;
    private Matrix cachedHypothesis;

    public TrainingSet setMatrixFactory(MatrixFactory matrixFactory) {
        this.matrixFactory = matrixFactory;
        return this;
    }

    public TrainingSet setX(Matrix X) {
        this.X = X;
        attemptTransform();
        return this;
    }

    public TrainingSet setY(Matrix y) {
        this.y = y;
        return this;
    }

    public TrainingSet setModelCalculator(RegressionModel calculator) {
        this.calculator = calculator;
        return this;
    }

    public TrainingSet setRegularization(double lambda) {
        this.lambda = lambda;
        return this;
    }

    public TrainingSet setXTransformations(boolean addBiasColumn, boolean normalize, PolynomialFeatures polynomialFeatures) {
        transformationsDefined = true;
        this.addBiasColumn = addBiasColumn;
        this.doNormalize = normalize;
        this.polynomialFeatures = polynomialFeatures;
        attemptTransform();
        return this;
    }

    private void attemptTransform() {
        assert !istransformed;
        if (X != null && transformationsDefined) {
            if (doNormalize) normalization = X.calculateColumnStatistics();
            X = transform(X);
            transposedX = X.transpose();
            istransformed = true;
        }
    }

    private Matrix transform(Matrix X) {
        Matrix res = X;
        if (polynomialFeatures != null) res = res.addPlynomialFeatures(polynomialFeatures);
        if (doNormalize) res = res.normalize(normalization);
        if (addBiasColumn) res = res.addOnesColumn();
        return res;
    }

    public int getThetaSize() {
        return X.numColumns();
    }

    public Matrix getHypothesis(Matrix theta) {
        if (!theta.equals(cachedTheta)) {
            cachedHypothesis = calculator.calculateHypothesis(theta, X);
            cachedTheta = theta;
        }
        return cachedHypothesis;
    }

    public double getCost(Matrix theta) {
        double cost = calculator.calculateCost(theta, X, y, getHypothesis(theta));
        if (lambda != 0) cost += costRegularization(theta);
        return cost;
    }

    public Matrix getGradient(Matrix theta) {
        Matrix diff = getHypothesis(theta).subtract(y);
        Matrix gradient = transposedX.multiply(diff).applyFunction(x -> x / X.numRows());
        if (lambda != 0) gradient = gradient.add(gradientRegularization(theta));
        return gradient;
    }

    public double predictSingle(Matrix theta, double[] x) {
        Matrix X = matrixFactory.createMatrix(1, x.length, x);
        X = transform(X);
        return calculator.calculateHypothesis(theta, X).get(0, 0);
    }

    public Matrix predict(Matrix theta, Matrix X) {
        X = transform(X);
        return calculator.calculateHypothesis(theta, X);
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

    @Override
    public double[] derivativeAt(double[] x) {
        return getGradient(createTheta(x)).getColumn(0).asArray();
    }

    @Override
    public double valueAt(double[] x) {
        return getCost(createTheta(x));
    }

    @Override
    public int domainDimension() {
        return getThetaSize();
    }

    private Matrix createTheta(double[] x) {
        return matrixFactory.createMatrix(x.length, 1, x);
    }
}
