package org.artem.tools.regression;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLInt64;
import org.artem.tools.file.MLExternalizable;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.Vector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/18/15
 */
public class Classification implements MLExternalizable {

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
        Matrix labelPredictions = getLabelPredictions(X);
        int[] res = new int[X.numRows()];
        for (int i = 0; i < res.length; i++) {
            Vector row = labelPredictions.getRow(i);
            int labelIdx = row.idxMax();
            res[i] = row.get(labelIdx) > 0.5 ? trainedLabels[labelIdx] : -1;
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

    public Map<Integer, Double> getRawLabelPredictions(double[] imageData) {
        Map<Integer, Double> res = new HashMap<>();
        for (int i = 0; i < trainedLabels.length; i++) {
            res.put(trainedLabels[i], trainedPredictors[i].predict(null, imageData));
        }
        return res;
    }

    private Matrix getLabelPredictions(Matrix X) {
        assert trained;

        Matrix res = matrixFactory.createMatrix(X.numRows(), trainedLabels.length);
        for (int j = 0; j < trainedLabels.length; j++) {
            Matrix labelPredictions = trainedPredictors[j].predict(null, X);
            for (int i = 0; i < res.numRows(); i++)
                res.set(i, j, labelPredictions.get(i, 0));
        }
        return res;
    }

    private Predictor trainLabel(TrainingSet trainingSet, Matrix y, int label) {
        y = y.applyFunction(x -> x == label ? 1 : 0);
        Trainer trainer = new Trainer(trainingSet, y);
        Predictor res = trainer.train(false);
        Matrix h = trainingSet.getHypothesis(res.getTheta()).applyFunction(p -> p >= 0.5 ? 1 : 0);
        Vector yColumn = y.getColumn(0);
        double correctPredictions = h.multiplyElements(yColumn).getColumn(0).sum();

        System.out.println("Label " + label
                        + " initial cost: " + trainer.getInitialCost()
                        + "; final cost: " + trainer.getFinalCost()
                        + "; training accuracy: " + correctPredictions / yColumn.sum() * 100
        );
        return res;
    }

    @Override
    public void toMLData(String prefix, Collection<MLArray> out) {
        assert trained;

        out.add(new MLChar(prefix + "matrixFactory", matrixFactory.getClass().getName()));

        long[] ints = new long[trainedLabels.length];
        for (int i = 0; i < ints.length; i++) ints[i] = trainedLabels[i];
        out.add(new MLInt64(prefix + "trainedLabels", new long[][]{ints}));

        for (int i = 0; i < trainedPredictors.length; i++) {
            trainedPredictors[i].toMLData(prefix + "trainedPredictors:" + i + ":", out);
        }
    }

    @Override
    public void fromMLData(String prefix, Map<String, MLArray> in) throws IOException {
        MLArray arr;
        try {
            trained = true;

            arr = in.get(prefix + "matrixFactory");
            String className = ((MLChar) arr).getString(0);
            matrixFactory = (MatrixFactory) Class.forName(className).newInstance();

            arr = in.get(prefix + "trainedLabels");
            trainedLabels = new Integer[arr.getN()];
            for (int i = 0; i < trainedLabels.length; i++) trainedLabels[i] = ((MLInt64) arr).get(i).intValue();

            trainedPredictors = new Predictor[trainedLabels.length];
            for (int i = 0; i < trainedPredictors.length; i++) {
                trainedPredictors[i] = new Predictor(matrixFactory);
                trainedPredictors[i].fromMLData(prefix + "trainedPredictors:" + i + ":", in);
            }
        } catch (Throwable e) {
            throw new IOException("Failed to read Classification from Matlab data file.", e);
        }
    }
}
