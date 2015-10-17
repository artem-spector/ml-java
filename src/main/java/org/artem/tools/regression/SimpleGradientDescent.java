package org.artem.tools.regression;

import org.artem.tools.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class SimpleGradientDescent {

    private TrainingSet trainingSet;
    private Matrix y;

    private double[] costHistory;

    public SimpleGradientDescent(TrainingSet trainingSet, Matrix y) {
        this.trainingSet = trainingSet;
        this.y = y;
    }

    public Matrix train(double alpha, int maxIter, boolean keepCostHistory, Matrix initialTheta) {
        Matrix theta = initialTheta;
        costHistory = keepCostHistory ? new double[maxIter] : null;

        for (int i = 0; i < maxIter; i++) {
            Matrix gradient = trainingSet.getGradient(theta, y);
            theta = theta.subtract(gradient.applyFunction(x -> x * alpha));

            if (keepCostHistory) {
                double cost = trainingSet.getCost(theta, y);
                costHistory[i] = cost;
            }
        }

        return theta;
    }

    public double[] getCostHistory() {
        return costHistory;
    }
}
