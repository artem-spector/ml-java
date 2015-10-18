package org.artem.tools.regression;

import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/18/15
 */
public class Classification {

    boolean trained;

    private Integer[] trainedLabels;
    private Predictor[] trainedPredictors;
    private MatrixFactory matrixFactory;

    public void trainOneVsAll(TrainingSet trainingSet, Matrix trainingLabels,
                              ExecutorService threadPool, Integer... labelsToTrain) {
        assert !trained;
        matrixFactory = trainingSet.matrixFactory;

        // if the labels to train for are provided as input params -  use them.
        // otherwise extract unique labels from the training labels matrix
        if (labelsToTrain != null && labelsToTrain.length > 0) {
            trainedLabels = labelsToTrain;
        } else {
            Set<Integer> labelSet = new HashSet<>();
            for (int i = 0; i < trainingLabels.numRows(); i++) labelSet.add((int) trainingLabels.get(i, 0));
            trainedLabels = labelSet.toArray(new Integer[labelSet.size()]);
        }
        int numLabels = trainedLabels.length;

        // train each unique label,
        // if a thread pool was provided as input param - submit each label training to that pool
        trainedPredictors = new Predictor[numLabels];
        Future<Predictor>[] trainLabelTasks = new Future[numLabels];

        for (int i = 0; i < numLabels; i++) {
            int label = trainedLabels[i];
            if (threadPool != null)
                trainLabelTasks[i] = threadPool.submit(() -> trainLabel(trainingSet, trainingLabels, label));
            else
                trainedPredictors[i] = trainLabel(trainingSet, trainingLabels, label);
        }

        try {
            if (threadPool != null)
                for (int i = 0; i < numLabels; i++)
                    trainedPredictors[i] = trainLabelTasks[i].get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        trained = true;
    }

    public int[] getLabels(Matrix X) {
        Matrix probabilities = getLabelProbabilities(X);
        int[] res = new int[X.numRows()];
        for (int i = 0; i < res.length; i++) {
            Vector row = probabilities.getRow(i);
            int labelIdx = row.idxMax();
            res[i] =row.get(labelIdx) >= 0.5 ? trainedLabels[labelIdx] : -1;
        }
        return res;
    }

    public int getLabel(double... data) {
        return getLabels(matrixFactory.createMatrix(new double[][]{data}))[0];
    }

    public ClassificationAccuracy getAccuracy(Matrix data, Matrix expectedLabels) {
        assert data.numRows() == expectedLabels.numRows();

        ClassificationAccuracy res = new ClassificationAccuracy(trainedLabels);
        int[] actualLabels = getLabels(data);
        for (int i = 0; i < data.numRows(); i++) {
            res.add(data.getRow(i), (int) expectedLabels.get(i, 0), actualLabels[i]);
        }
        return res;
    }

    private Matrix getLabelProbabilities(Matrix X) {
        assert trained;

        Matrix res = matrixFactory.createMatrix(X.numRows(), trainedLabels.length);
        for (int j = 0; j < trainedLabels.length; j++) {
            Matrix labelProbablities = trainedPredictors[j].predict(null, X);
            for (int i = 0; i < res.numRows(); i++)
                res.set(i, j, labelProbablities.get(i, 0));
        }
        return res;
    }

    private Predictor trainLabel(TrainingSet trainingSet, Matrix y, int label) {
        y = y.applyFunction(x -> x == label ? 1 : 0);
        Trainer trainer = new Trainer(trainingSet, y);
        Predictor res = trainer.train(false);
        System.out.println("Label " + label + " initial cost: " + trainer.getInitialCost() + "; final cost: " + trainer.getFinalCost());
        return res;
    }


}
