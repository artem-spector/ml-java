package ml.coursera;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import edu.stanford.nlp.optimization.QNMinimizer;
import ml.ArrayUtil;
import ml.display.DisplayUtil;
import ml.regression.LogisticModel;
import ml.regression.TrainingSet;
import ml.vector.Matrix;
import ml.vector.MatrixFactory;
import ml.vector.SimpleMatrixFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class TestEx3 {

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
        DisplayUtil displayUtil = new DisplayUtil();
        displayUtil.displayImageGrid(images, 20, 20, 2);
        displayUtil.saveImage("./target/Ex3TrainingSample.png");
    }

    @Test
    public void testOneVsAll() {
        int numLabels = 10;
        double[][] allTheta = new double[numLabels][];

        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, null).setRegularization(0.1)
                .setMatrixFactory(factory);
        for (int i = 0; i < numLabels; i++) {
            allTheta[i] = trainLabel(i + 1, trainingSet);
        }

        double[] predictedLabels = new double[X.numRows()];
        Matrix predictions = X.addOnesColumn().multiply(factory.createMatrix(allTheta).transpose());
        for (int i = 0; i < predictions.numRows(); i++) {
            predictedLabels[i] = ArrayUtil.getIndexOfMax(predictions.getRow(i).asArray()) + 1;
        }
        double correctPredictions = factory.createMatrix(predictedLabels.length, 1, predictedLabels).subtract(y)
                .applyFunction(v -> v == 0 ? 1 : 0).getColumn(0).sum();
        double accuracy = correctPredictions / X.numRows() * 100;
        System.out.println("Training set accuracy: " + accuracy);
    }

    private double[] trainLabel(int label, TrainingSet trainingSet) {
        System.out.println("Training label " + label);
        trainingSet.setY(y.applyFunction(val -> val == label ? 1 : 0));
        double[] initTheta = new double[trainingSet.getThetaSize()];

        QNMinimizer minimizer = new QNMinimizer(10, true);
        minimizer.shutUp();
        double[] minTheta = minimizer.minimize(trainingSet, 1e-5, initTheta);
        System.out.println("Cost: " + trainingSet.getCost(factory.createMatrix(minTheta.length, 1, minTheta)));
        return minTheta;
    }
}
