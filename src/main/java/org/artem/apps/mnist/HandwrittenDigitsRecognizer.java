package org.artem.apps.mnist;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import org.artem.tools.display.GrayScaleImage;
import org.artem.tools.regression.Classification;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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

    public void analyzeDrawing() {
        BufferedImage original = ui.getDrawingImage();
        MNISTImageTransformation mnist = new MNISTImageTransformation(original);
        mnist.transform();
        ui.showResultImage(mnist.getResizedImage());
    }

    public void analyzeDrawing_old() {
        int width = 20;
        int height = 20;
        int xMargin = 2;
        int yMargin = 2;

        BufferedImage image = ui.getDrawingImage(width, height);
        ui.showResultImage(image);

        int resWidth = width + xMargin * 2;
        int resHeight = height + yMargin * 2;
        double[] resData = new double[resWidth * resHeight];

        for (int i = 0; i < resWidth; i++)
            for (int j = 0; j < resHeight; j++) {
                int imgX = i >= xMargin && i < width + xMargin ? i - xMargin : -1;
                int imgY = j >= yMargin && j < height + yMargin ? j - yMargin : -1;

                double intencity = 0;
                if (imgX >= 0 && imgY >= 0) {
                    intencity = getGrayPixel(image, imgX, imgY);
                }
                resData[i * resHeight + j] = intencity;
            }

        int label = classification.getLabel(resData);
        GrayScaleImage grayScaleImage = new GrayScaleImage(resData, resWidth, resHeight, 4);
        grayScaleImage.setLabel(String.valueOf(label));
        ui.showResultImage(grayScaleImage);
    }

    double getGrayPixel(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        ColorModel colorModel = image.getColorModel();
        int red = colorModel.getRed(rgb);
        int green = colorModel.getGreen(rgb);
        int blue = colorModel.getBlue(rgb);
        return ((double) blue / 255 + (double) green / 255 + (double) red / 255) / 3;
    }

}
