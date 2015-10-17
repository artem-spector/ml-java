package org.artem.apps.mnist;

import org.artem.tools.regression.*;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/5/15
 */
public class HandwrittenDigitsTrainer {

    public static final MatrixFactory MATRIX_FACTORY = new SimpleMatrixFactory();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private long timerStart;

    private Matrix trainingImages;
    private Matrix trainingLabels;
    private Byte[] uniqueLabels;

    private Predictor[] labelPredictors;

    private Matrix testImages;
    private Matrix testLabels;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        HandwrittenDigitsTrainer instance = new HandwrittenDigitsTrainer();
        instance.resetTimer();
        instance.loadTrainingData();
        instance.printDuration("Loading training data");

        instance.resetTimer();
        instance.train();
        instance.printDuration("Training");
        instance.checkTrainingAccuracy();

        instance.resetTimer();
        instance.loadTtestData();
        instance.printDuration("Loading test data");

        instance.checkTestAccuracy();

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

    private void loadTtestData() throws IOException, ExecutionException, InterruptedException {
        Future<Void> loadImages = threadPool.submit(() -> {
            testImages = readImageMatrix("src/main/resources/mnist/t10k-images-idx3-ubyte.gz", 2051);
            System.out.println("Test images: " + testImages.numRows());
            return null;
        });

        Future<Void> loadLabels = threadPool.submit(() -> {
            testLabels = readLabelMatrix("src/main/resources/mnist/t10k-labels-idx1-ubyte.gz", 2049);
            System.out.println("Test labels: " + testLabels.numRows());
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
        TrainingSet trainingSet = new TrainingSet()
                .setMatrixFactory(MATRIX_FACTORY)
                .setModelCalculator(new LogisticModel())
                .setX(trainingImages)
                .setXTransformation(new XTransformation(true, null, null))
                .setRegularization(0);

        int numLabels = uniqueLabels.length;
        Future<Predictor>[] trainLabelTasks = new Future[numLabels];
        for (int i = 0; i < numLabels; i++) {
            byte label = uniqueLabels[i];
            trainLabelTasks[i] = threadPool.submit(() -> trainLabel(trainingSet, label));
        }

        labelPredictors = new Predictor[numLabels];
        for (int i = 0; i < numLabels; i++) labelPredictors[i] = trainLabelTasks[i].get();
    }

    private Predictor trainLabel(TrainingSet trainingSet, byte label) {
        Trainer trainer = new Trainer(trainingSet, trainingLabels.applyFunction(x -> x == label ? 1 : 0));
        Predictor predictor = trainer.train(false);
        System.out.println("Label " + label + " cost: " + trainer.getFinalCost());
        return predictor;
    }

    private void checkTrainingAccuracy() {
        System.out.println("Training accuracy");
        checkAccuracy("\t", trainingImages, trainingLabels);
    }

    private void checkTestAccuracy() {
        System.out.println("Test set accuracy");
        checkAccuracy("\t", testImages, testLabels);
    }

    private void checkAccuracy(String prefix, Matrix images, Matrix labels) {
        for (int i = 0; i < uniqueLabels.length; i++) {
            byte label = uniqueLabels[i];
            Matrix expected = labels.applyFunction(x -> x == label ? 1 : 0);
            double accuracy = labelPredictors[i].getPredictionAccuracy(p -> p > 0.5 ? 1 : 0, images, expected);
            System.out.println(prefix + label + ": " + accuracy * 100 + "%");
        }
    }

    private void resetTimer() {
        timerStart = System.currentTimeMillis();
    }

    private void printDuration(String title) {
        long duration = System.currentTimeMillis() - timerStart;
        System.out.println(title + ": " + duration / 1000 + "." + duration % 1000 + "s.");
    }
}
