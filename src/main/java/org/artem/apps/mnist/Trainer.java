package org.artem.apps.mnist;

import org.artem.tools.regression.TrainingSet;

import java.io.IOException;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/5/15
 */
public class Trainer {

    private byte[] trainingLabels;
    private byte[][][] trainingImages;
    private TrainingSet trainingSet;

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        Trainer trainer = new Trainer();
        trainer.loadTrainingData();

        long duration = System.currentTimeMillis() - start;
        System.out.println("Finished in " + duration / 1000 + "." + duration % 1000 + "s.");
    }

    private void loadTrainingData() throws IOException {
        IDXFileReader trainingLabelsFile = new IDXFileReader("src/main/resources/mnist/train-labels-idx1-ubyte.gz", 2049, 1);
        trainingLabels = trainingLabelsFile.readAllData(byte[].class);
        System.out.println("Training labels: " + this.trainingLabels.length);

        IDXFileReader trainingImagesFile = new IDXFileReader("src/main/resources/mnist/train-images-idx3-ubyte.gz", 2051, 3);
        trainingImages = trainingImagesFile.readAllData(byte[][][].class);
        System.out.println("Training images: " + trainingImages.length + " of " + trainingImages[0].length + "*" + trainingImages[0][0].length);

        trainingLabelsFile.close();
    }

    private void createTrainingSet() {

        trainingSet = new TrainingSet().setXTransformations(true, false, null).setX(null);
    }
}
