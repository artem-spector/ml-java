package org.artem.tools.regression;

import edu.stanford.nlp.optimization.DiffFunction;
import edu.stanford.nlp.optimization.QNMinimizer;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/17/15
 */
public class Trainer implements DiffFunction {

    private TrainingSet trainingSet;
    private Matrix y;
    private final MatrixFactory matrixFactory;

    private double[] initTheta;
    private double[] currentTheta;

    public Trainer(TrainingSet trainingSet, Matrix y) {
        this.trainingSet = trainingSet;
        matrixFactory = trainingSet.matrixFactory;
        this.y = y;
        initTheta = new double[trainingSet.getThetaSize()];
        currentTheta = new double[initTheta.length];
    }

    public Predictor train(boolean verbouse) {
        QNMinimizer minimizer = new QNMinimizer(10, true);
        if (!verbouse) minimizer.shutUp();

        minimizer.minimize(this, 1e-5, currentTheta);

        Matrix theta = matrixFactory.createMatrix(trainingSet.getThetaSize(), 1, currentTheta);
        return new Predictor(matrixFactory, trainingSet.modelCalculator, trainingSet.xTransformation, theta);
    }

    public double getInitialCost() {
        return valueAt(initTheta);
    }

    public double getFinalCost() {
        return valueAt(currentTheta);
    }

    @Override
    public double[] derivativeAt(double[] theta) {
        return trainingSet.getGradient(createTheta(theta), y).getColumn(0).asArray();
    }

    @Override
    public double valueAt(double[] theta) {
        return trainingSet.getCost(createTheta(theta), y);
    }

    @Override
    public int domainDimension() {
        return trainingSet.getThetaSize();
    }

    private Matrix createTheta(double[] theta) {
        return matrixFactory.createMatrix(new double[][]{theta}).transpose();
    }
}
