package org.artem.apps.mnist;

import org.artem.tools.regression.LogisticModel;
import org.artem.tools.regression.TrainingSet;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;

import java.io.IOException;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/5/15
 */
public class Trainer {

    private long timerStart;

    private TrainingSet trainingSet;
    private Matrix labels;

    public static void main(String[] args) throws IOException {
        Trainer trainer = new Trainer();
        trainer.resetTimer();
        trainer.loadTrainingData();

        trainer.printDuration("Loading training data");
    }

    private void loadTrainingData() throws IOException {
        IDXFileReader trainingLabelsFile = new IDXFileReader("src/main/resources/mnist/train-labels-idx1-ubyte.gz", 2049, 1);
        byte[] trainingLabels = trainingLabelsFile.readAllData(byte[].class);
        System.out.println("Training labels: " + trainingLabels.length);

        IDXFileReader trainingImagesFile = new IDXFileReader("src/main/resources/mnist/train-images-idx3-ubyte.gz", 2051, 3);
        byte[][][] trainingImages = trainingImagesFile.readAllData(byte[][][].class);
        System.out.println("Training images: " + trainingImages.length + " of " + trainingImages[0].length + "*" + trainingImages[0][0].length);

        trainingLabelsFile.close();

        createTrainingSet(trainingImages, trainingLabels);
    }

    private void createTrainingSet(byte[][][] images, byte[] labels) {
        int imgRows = images[0].length;
        int imgCols = images[0][0].length;
        double[][] imgArray = new double[images.length][imgRows * imgCols];

        for (int i = 0; i < images.length; i++)
            for (int j = 0; j < imgRows; j++)
                    for (int k = 0; k < imgCols; k++)
                            imgArray[i][j * imgCols + k] = images[i][j][k];

        MatrixFactory matrixFactory = new SimpleMatrixFactory();

        trainingSet = new TrainingSet()
                .setMatrixFactory(matrixFactory)
                .setModelCalculator(new LogisticModel())
                .setX(matrixFactory.createMatrix(imgArray))
                .setXTransformations(true, false, null);

        double[][] labelArray = new double[labels.length][1];
        for (int i = 0; i < labels.length; i++)
            labelArray[i][0] = labels[i];

        this.labels = matrixFactory.createMatrix(labelArray);
    }

    private void resetTimer() {
        timerStart = System.currentTimeMillis();
    }

    private void printDuration(String title) {
        long duration = System.currentTimeMillis() - timerStart;
        System.out.println(title + ": " + duration / 1000 + "." + duration % 1000 + "s.");
    }
}
