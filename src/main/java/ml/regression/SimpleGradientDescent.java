package ml.regression;

import ml.vector.Matrix;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class SimpleGradientDescent {

    private TrainingSet trainingSet;

    private double[] costHistory;

    public SimpleGradientDescent(TrainingSet trainingSet) {
        this.trainingSet = trainingSet;
    }

    public Matrix train(double alpha, int maxIter, boolean keepCostHistory, Matrix initialTheta) {
        Matrix theta = initialTheta;
        costHistory = keepCostHistory ? new double[maxIter] : null;

        for (int i = 0; i < maxIter; i++) {
            Matrix gradient = trainingSet.getGradient(theta);
            theta = theta.subtract(gradient.applyFunction(x -> x * alpha));

            if (keepCostHistory) {
                double cost = trainingSet.getCost(theta);
                costHistory[i] = cost;
            }
        }

        return theta;
    }

    public double[] getCostHistory() {
        return costHistory;
    }
}
