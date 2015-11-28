package org.artem.apps.mnist;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import org.artem.tools.regression.Classification;

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

    public static void main(String[] args) throws IOException {
        HandwrittenDigitsRecognizer instance = new HandwrittenDigitsRecognizer();
        instance.readTrainedClassification("./src/main/resources/lineraRegressionTheta.mat");
    }

    private void readTrainedClassification(String file) throws IOException {
        Map<String, MLArray> arrayMap = new MatFileReader().read(new File(file));
        classification = new Classification();
        classification.fromMLData("", arrayMap);
    }

}
