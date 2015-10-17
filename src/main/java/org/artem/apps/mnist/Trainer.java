package org.artem.apps.mnist;

import edu.stanford.nlp.optimization.QNMinimizer;
import org.artem.tools.ArrayUtil;
import org.artem.tools.regression.LogisticModel;
import org.artem.tools.regression.TrainingSet;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/5/15
 */
public class Trainer {

    public static final MatrixFactory MATRIX_FACTORY = new SimpleMatrixFactory();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(3);

    private long timerStart;

    private Matrix trainingImages;
    private Matrix trainingLabels;
    private Byte[] uniqueLabels;
    private Matrix allTheta;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        Trainer trainer = new Trainer();
        trainer.resetTimer();
        trainer.loadTrainingData();
        trainer.printDuration("Loading training data");

        trainer.resetTimer();
        trainer.train();
        trainer.printDuration("Training");

        Matrix predictedTrainingLabels = trainer.predict(trainer.trainingImages);
        double correctPredictions = predictedTrainingLabels.subtract(trainer.trainingLabels).applyFunction(x -> x == 0 ? 1 : 0).getColumn(0).sum();
        double accuracy = correctPredictions / trainer.trainingLabels.numRows() * 100;
        System.out.println("Training set accuracy: " + accuracy);

        threadPool.shutdown();
    }

    private void loadTrainingData() throws IOException, ExecutionException, InterruptedException {
        Future<Void> loadImages = threadPool.submit(() -> {
            trainingImages = readImageMatrix("src/main/resources/mnist/train-images-idx3-ubyte.gz", 2051);
            System.out.println("Training images: " + trainingImages.numRows());
            return null;
        });

        Future<Void> loadLabels = threadPool.submit(() -> {
            trainingLabels = readLabelMatrix("src/main/resources/mnist/train-labels-idx1-ubyte.gz", 2049);
            Set<Byte> labelSet = new HashSet<>();
            for (int i = 0; i < trainingLabels.numRows(); i++) labelSet.add((byte) trainingLabels.get(i, 0));
            uniqueLabels = labelSet.toArray(new Byte[labelSet.size()]);
            System.out.println("Training labels: " + trainingLabels.numRows());
            return null;
        });

        loadLabels.get();
        loadImages.get();
    }

    private Matrix readImageMatrix(String imageFile, int magicNumber) throws IOException {
        IDXFileReader reader = new IDXFileReader(imageFile, magicNumber, 3);
        byte[][][] images = reader.readAllData(byte[][][].class);
        reader.close();

        int imgRows = images[0].length;
        int imgCols = images[0][0].length;
        double[][] imgArray = new double[images.length][imgRows * imgCols];

        for (int i = 0; i < images.length; i++)
            for (int j = 0; j < imgRows; j++)
                for (int k = 0; k < imgCols; k++)
                    imgArray[i][j * imgCols + k] = images[i][j][k];

        return MATRIX_FACTORY.createMatrix(imgArray);
    }

    private Matrix readLabelMatrix(String labelFile, int magicNumber) throws IOException {
        IDXFileReader reader = new IDXFileReader(labelFile, magicNumber, 1);
        byte[] labels = reader.readAllData(byte[].class);
        reader.close();

        double[][] labelArray = new double[labels.length][1];
        for (int i = 0; i < labels.length; i++) labelArray[i][0] = labels[i];

        return MATRIX_FACTORY.createMatrix(labelArray);
    }


    private void train() throws ExecutionException, InterruptedException {
        int numLabels = uniqueLabels.length;
        Future<double[]>[] trainLabelTasks = new Future[numLabels];
        for (int i = 0; i < numLabels; i++) {
            byte label = uniqueLabels[i];
            trainLabelTasks[i] = threadPool.submit(() -> trainLabel(label, 0));
        }

        double[][] thetas = new double[numLabels][];
        for (int i = 0; i < numLabels; i++) thetas[i] = trainLabelTasks[i].get();

        allTheta = MATRIX_FACTORY.createMatrix(thetas).transpose();
    }

    private double[] trainLabel(byte label, double regularization) {
        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(trainingImages)
                .setXTransformations(true, false, null)
                .setRegularization(regularization)
                .setY(trainingLabels.applyFunction(x -> x == label ? 1 : 0))
                .setMatrixFactory(MATRIX_FACTORY);
        double[] initTheta = new double[trainingSet.getThetaSize()];

        QNMinimizer minimizer = new QNMinimizer(10, true);
        minimizer.shutUp();
        double[] minTheta = minimizer.minimize(trainingSet, 1e-5, initTheta);
        System.out.println("Label " + label + " cost: " + trainingSet.getCost(MATRIX_FACTORY.createMatrix(minTheta.length, 1, minTheta)));
        return minTheta;
    }

    private Matrix predict(Matrix x) {
        assert x.numColumns() + 1 == allTheta.numRows();

        Matrix predictions = x.addOnesColumn().multiply(allTheta);
        double[] predictedLabels = new double[predictions.numRows()];
        for (int i = 0; i < predictions.numRows(); i++) {
            double[] probabilities = predictions.getRow(i).asArray();
            int indexOfMax = ArrayUtil.getIndexOfMax(probabilities);
            if (probabilities[indexOfMax] < 0.5) {
                System.out.println("warn: max probability is " + probabilities[indexOfMax]);
            }
            predictedLabels[i] = uniqueLabels[indexOfMax];
        }
        return MATRIX_FACTORY.createMatrix(predictedLabels.length, 1, predictedLabels);
    }

    private void resetTimer() {
        timerStart = System.currentTimeMillis();
    }

    private void printDuration(String title) {
        long duration = System.currentTimeMillis() - timerStart;
        System.out.println(title + ": " + duration / 1000 + "." + duration % 1000 + "s.");
    }
}
