package org.artem.apps.mnist;

import org.artem.tools.regression.*;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;

import java.io.IOException;
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
    private Classification classification;

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
        instance.loadTestData();
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
            System.out.println("Training labels: " + trainingLabels.numRows());
            return null;
        });

        loadLabels.get();
        loadImages.get();
    }

    private void loadTestData() throws IOException, ExecutionException, InterruptedException {
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

        classification = new Classification();
        classification.trainOneVsAll(trainingSet, trainingLabels, threadPool, 1);
    }

    private void checkTrainingAccuracy() {
        ClassificationAccuracy accuracy = classification.getAccuracy(trainingImages, trainingLabels);
        System.out.println("Training accuracy: " + accuracy.getCrossLabelAccuracy() * 100);
    }

    private void checkTestAccuracy() {
        ClassificationAccuracy accuracy = classification.getAccuracy(testImages, testLabels);
        System.out.println("Test set accuracy: " + accuracy.getCrossLabelAccuracy() * 100);
    }

    private void resetTimer() {
        timerStart = System.currentTimeMillis();
    }

    private void printDuration(String title) {
        long duration = System.currentTimeMillis() - timerStart;
        System.out.println(title + ": " + duration / 1000 + "." + duration % 1000 + "s.");
    }
}
