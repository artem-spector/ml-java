package coursera.ml;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import org.artem.tools.ArrayUtil;
import org.artem.tools.display.DisplayUtil;
import org.artem.tools.regression.*;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class TestEx3 {

    private static DisplayUtil displayUtil = new DisplayUtil();
    private static MatrixFactory factory = new SimpleMatrixFactory();

    private static Matrix X;
    private static Matrix y;

    @BeforeClass
    public static void loadData() throws Exception {
        MatFileReader reader = new MatFileReader("machine-learning-ex3/ex3/ex3data1.mat");
        Map<String, MLArray> content = reader.getContent();
        Assert.assertNotNull(content);

        X = factory.createMatrix(((MLDouble) content.get("X")).getArray());
        y = factory.createMatrix(((MLDouble) content.get("y")).getArray());
        System.out.println("X: " + X.numRows() + "*" + X.numColumns());
        System.out.println("y: " + y.numRows() + "*" + y.numColumns());
        Assert.assertEquals(X.numRows(), y.numRows());
        Assert.assertEquals(1, y.numColumns());

        double[][] images = new double[100][];
        int[] randIndexes = new ArrayUtil().randperm(X.numRows());
        for (int i = 0; i < images.length; i++) images[i] = X.getRow(randIndexes[i]).asArray();
        JPanel panel = displayUtil.createImageGrid(images, 20, 20, 2);
        displayUtil.saveImage(panel, "./target/Ex3TrainingSample.png");
    }

    @Test
    public void testOneVsAll() {
        int numLabels = 10;
        Predictor[] predictors = new Predictor[numLabels];

        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, null))
                .setRegularization(0.1)
                .setMatrixFactory(factory);
        for (int i = 0; i < numLabels; i++) {
            predictors[i] = trainLabel(i + 1, trainingSet);
        }

        Matrix[] labelPredictions = new Matrix[numLabels];
        for (int i = 0; i < numLabels; i++)
            labelPredictions[i] = predictors[i].predict(null, X);

        double[] predictedLabels = new double[X.numRows()];
        for (int i = 0; i < predictedLabels.length; i++) {
            double maxProbability = 0;
            predictedLabels[i] = -1;
            for (int j = 0; j < numLabels; j++) {
                double p = labelPredictions[j].get(i, 0);
                if (p > maxProbability) {
                    maxProbability = p;
                    predictedLabels[i] = j + 1;
                }
            }
        }

        double correctPredictions = factory.createMatrix(predictedLabels.length, 1, predictedLabels).subtract(y)
                .applyFunction(v -> v == 0 ? 1 : 0).getColumn(0).sum();
        double accuracy = correctPredictions / X.numRows() * 100;
        System.out.println("Training set accuracy: " + accuracy);
    }

    private Predictor trainLabel(int label, TrainingSet trainingSet) {
        System.out.println("Training label " + label);
        Trainer trainer = new Trainer(trainingSet, y.applyFunction(val -> val == label ? 1 : 0));
        Predictor predictor = trainer.train(false);
        System.out.println("Cost: " + trainer.getFinalCost());
        return predictor;
    }
}
