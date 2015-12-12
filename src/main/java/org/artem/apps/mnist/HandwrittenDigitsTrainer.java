package org.artem.apps.mnist;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import org.artem.tools.ArrayUtil;
import org.artem.tools.display.DisplayUtil;
import org.artem.tools.regression.*;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
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

    private static List<String> MODES = Arrays.asList("train", "test");

    public static final MatrixFactory MATRIX_FACTORY = new SimpleMatrixFactory();
    private static ExecutorService threadPool = Executors.newFixedThreadPool(4);

    private long timerStart;

    private Matrix trainingImages;
    private Matrix trainingLabels;
    private Classification classification;

    private Matrix testImages;
    private Matrix testLabels;

    private DisplayUtil displayUtil = new DisplayUtil();

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        String mode = (args != null && args.length == 1) ? args[0] : null;
        if (mode == null || !MODES.contains(mode)) {
            System.out.println("usage: HandwrittenDigitsTrainer [train|test]");
            System.exit(1);
        }

        HandwrittenDigitsTrainer instance = new HandwrittenDigitsTrainer();
//        instance.delme(100);

        instance.resetTimer();
        instance.loadTrainingData();
        instance.printImages(instance.trainingImages, "./target/MNIST_training_image_0.png", 0, 1, 2, 3);
        instance.printDuration("Loading training data");

        if (mode.equals("train")) {
            instance.resetTimer();
            instance.train();
            instance.printDuration("Training");
            instance.saveClassification("./src/main/resources/linearRegressionTheta.mat");
        } else {
            instance.resetTimer();
            instance.readClassification("./src/main/resources/linearRegressionTheta.mat");
            instance.printDuration("Load trained classification");
        }

        instance.checkTrainingAccuracy();

        instance.loadTestData();
        instance.checkTestAccuracy();

        threadPool.shutdown();
    }

    private void saveClassification(String file) throws IOException {
        Collection<MLArray> mlArrays = new ArrayList<>();
        classification.toMLData("", mlArrays);
        new MatFileWriter().write(file, mlArrays);
    }

    private void readClassification(String file) throws IOException {
        Map<String, MLArray> arrayMap = new MatFileReader().read(new File(file));
        classification = new Classification();
        classification.fromMLData("", arrayMap);
    }

    public static JPanel getTestImages(int numImages) {
        try {
            Matrix imageMatrix = readImageMatrix("src/main/resources/mnist/t10k-images-idx3-ubyte.gz", 2051);
            Matrix labelMatrix = readLabelMatrix("src/main/resources/mnist/t10k-labels-idx1-ubyte.gz", 2049);

            String[] labels = new String[numImages];
            double[][] images = new double[numImages][];

            for (int i = 0; i < numImages; i++) {
                images[i] = imageMatrix.getRow(i).asArray();
                labels[i] = String.valueOf((int) labelMatrix.get(i, 0));
            }
            return new DisplayUtil().createImageGrid(images, 28, 28, 2, labels);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void delme(int numImages) throws IOException {
        testImages = readImageMatrix("src/main/resources/mnist/t10k-images-idx3-ubyte.gz", 2051);
        testLabels = readLabelMatrix("src/main/resources/mnist/t10k-labels-idx1-ubyte.gz", 2049);

        String[] labels = new String[numImages];
        double[][] images = new double[numImages][];

        for (int i = 0; i < numImages; i++) {
            images[i] = testImages.getRow(i).asArray();
            labels[i] = String.valueOf((int) testLabels.get(i, 0));
        }
        JPanel panel = displayUtil.createImageGrid(images, 28, 28, 2, labels);
        JFrame frame = displayUtil.showFrame("", panel);
        displayUtil.pause();
        displayUtil.closeFrame(frame);
    }

    public JPanel getRandomTestImages(int numImages) throws IOException {
        int[] randIndexes = new ArrayUtil().randperm(testImages.numRows());
        int[] indexes = new int[numImages];
        System.arraycopy(randIndexes, 0, indexes, 0, indexes.length);
        double[][] imageData = new double[indexes.length][];
        for (int i = 0; i < indexes.length; i++) imageData[i] = testImages.getRow(indexes[i]).asArray();
        return displayUtil.createImageGrid(imageData, 28, 28, 2, null);
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

    private void printImages(Matrix images, String file, int... indexes) throws IOException {
        double[][] imageData = new double[indexes.length][];
        for (int i = 0; i < indexes.length; i++) imageData[i] = images.getRow(indexes[i]).asArray();
        JPanel panel = displayUtil.createImageGrid(imageData, 28, 28, 2, null);
        displayUtil.saveImage(panel, file);
    }

    public void loadTestData() throws IOException, ExecutionException, InterruptedException {
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

    private static Matrix readImageMatrix(String imageFile, int magicNumber) throws IOException {
        IDXFileReader reader = new IDXFileReader(imageFile, magicNumber, 3);
        int[] dimensions = reader.getDimensions();

        int numImages = dimensions[0];
        int imgWidth = dimensions[1];
        int imgHeight = dimensions[2];

        double[][] img = new double[numImages][imgHeight * imgWidth];
        for (int i = 0; i < numImages; i++)
            for (int j = 0; j < imgWidth; j++)
                for (int k = 0; k < imgHeight; k++)
                    img[i][j + k * imgWidth] = reader.readByte() & 0xFF;

        reader.close();
        return MATRIX_FACTORY.createMatrix(img);
    }

    private static Matrix readLabelMatrix(String labelFile, int magicNumber) throws IOException {
        IDXFileReader reader = new IDXFileReader(labelFile, magicNumber, 1);
        int numLabels = reader.getDimensions()[0];

        double[][] labels = new double[numLabels][1];
        for (int i = 0; i < numLabels; i++)
            labels[i][0] = reader.readByte();
        reader.close();

        return MATRIX_FACTORY.createMatrix(labels);
    }


    private void train() throws ExecutionException, InterruptedException {
        TrainingSet trainingSet = new TrainingSet()
                .setMatrixFactory(MATRIX_FACTORY)
                .setModelCalculator(new LogisticModel())
                .setX(trainingImages)
                .setXTransformation(new XTransformation(true, null, null))
                .setRegularization(0);

        classification = new Classification();
        classification.trainOneVsAll(trainingSet, trainingLabels, threadPool);
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
