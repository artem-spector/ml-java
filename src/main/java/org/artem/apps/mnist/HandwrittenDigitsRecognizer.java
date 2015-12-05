package org.artem.apps.mnist;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import org.artem.tools.display.GrayScaleImage;
import org.artem.tools.regression.Classification;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 11/28/15
 */
public class HandwrittenDigitsRecognizer {

    private Classification classification;
    private RecognizerUI ui;

    public static void main(String[] args) throws IOException {
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
        int width = 20;
        int height = 20;
        int xMargin = 2;
        int yMargin = 2;

        BufferedImage image = ui.getDrawingImage(width, height);
        ui.showResultImage(image);

        int resWidth = width + xMargin * 2;
        int resHeight = height + yMargin * 2;
        double[] imgData = new double[resWidth * resHeight];
        double blackRGB = -1.6777216E7; // that's the black color value I got via debugger. voodoo..
        for (int i = 0; i < resWidth; i++)
            for (int j = 0; j < resHeight; j++) {
                int imgX = i >= xMargin && i < width + xMargin ? i - xMargin : -1;
                int imgY = j >= yMargin && j < height + yMargin ? j - yMargin : -1;

                imgData[i * resHeight + j] = imgX == -1 || imgY == -1 ? blackRGB : image.getRGB(imgX, imgY);
            }

        GrayScaleImage grayScaleImage = new GrayScaleImage(imgData, resWidth, resHeight, 4);


        ui.showResultImage(grayScaleImage);
    }
}
