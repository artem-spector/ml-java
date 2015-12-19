package org.artem.apps.mnist;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import org.artem.tools.display.Drawing;
import org.artem.tools.display.GrayScaleImage;
import org.artem.tools.regression.Classification;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 11/28/15
 */
public class HandwrittenDigitsRecognizer {

    private Classification classification;
    private RecognizerUI ui;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        HandwrittenDigitsTrainer trainer = new HandwrittenDigitsTrainer();
        trainer.loadTestData();
        new HandwrittenDigitsRecognizer("./src/main/resources/linearRegressionTheta.mat").showUI();
    }

    public HandwrittenDigitsRecognizer(String classificationData) throws IOException {
        readTrainedClassification(classificationData);
        ui = new RecognizerUI(this);
    }

    public void showUI() {
        ui.showFrame();
    }

    private void readTrainedClassification(String file) throws IOException {
        Map<String, MLArray> arrayMap = new MatFileReader().read(new File(file));
        classification = new Classification();
        classification.fromMLData("", arrayMap);
    }

    public void analyzeDrawing(Drawing drawing) {
        MNISTImageTransformation mnist = new MNISTImageTransformation(drawing);
        mnist.transform();
        double[] data = mnist.getData();
        int label = classification.getLabel(data);

        GrayScaleImage grayScaleImage = new GrayScaleImage(data, MNISTImageTransformation.WIDTH, MNISTImageTransformation.HEIGHT, 3);
        grayScaleImage.setLabel(String.valueOf(label));

        ui.showResultImage(grayScaleImage);
    }

}
