package ml.coursera;

import ml.DataLoadUtil;
import ml.display.DisplayUtil;
import ml.regression.LinearModel;
import ml.regression.SimpleGradientDescent;
import ml.regression.TrainingSet;
import ml.vector.Matrix;
import ml.vector.MatrixFactory;
import ml.vector.SimpleMatrixFactory;
import ml.vector.Statistics;
import org.junit.BeforeClass;
import org.junit.Test;
import org.math.plot.Plot2DPanel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/23/15
 */
public class Ex1MultiTest {

    private static MatrixFactory factory = new SimpleMatrixFactory();

    private static DataLoadUtil dataLoadUtil = new DataLoadUtil();
    private static DisplayUtil displayUtil = new DisplayUtil();

    private static Matrix X;
    private static Matrix y;

    @BeforeClass
    public static void loadData() {
        Matrix allData = dataLoadUtil.readCSV("machine-learning-ex1/ex1/ex1data2.txt", factory);
        X = allData.selectColumns(0, 1);
        y = allData.selectColumns(2);
    }

    @Test
    public void testNormalizedRegression() {

        System.out.println("First 10 examples from initial dataset:");
        for (int i = 0; i < 10; i++) {
            double[] row = X.getRow(i).asArray();
            for (int j = 0; j < X.numColumns(); j++) {
                System.out.printf("%.0f\t", row[j]);
            }
            System.out.printf("%.0f%n", y.getRow(i).get(0));
        }

        System.out.println("Normalizing data...");
        Statistics[] normalization = X.calculateColumnStatistics();
        System.out.printf("mu0=%f sigma0=%f %n", normalization[0].mean, normalization[0].std);
        System.out.printf("mu1=%f sigma1=%f %n", normalization[1].mean, normalization[1].std);

        Matrix normX = X.normalize(normalization);
        System.out.println("First 10 examples from normalized dataset:");
        for (int i = 0; i < 10; i++) {
            double[] row = X.getRow(i).asArray();
            for (int j = 0; j < X.numColumns(); j++) {
                System.out.printf("%+.4e\t", row[j]);
            }
            System.out.printf("%.0f%n", y.get(i, 0));
        }

        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LinearModel())
                .setX(X).setXTransformations(true, true, null)
                .setY(y)
                .setMatrixFactory(factory);
        SimpleGradientDescent regression = new SimpleGradientDescent(train);
        Matrix finalTheta = regression.train(0.01, 400, false, factory.createMatrix(normX.numColumns() + 1, 1));
        System.out.println("Theta computed from gradient descent: " + finalTheta);

        double size = 1650;
        double bedrooms = 3;
        Matrix inputData = factory.createMatrix(1, 2, size, bedrooms);
        System.out.println("Input data: " + inputData);
        Matrix normInputData = inputData.normalize(normalization).addOnesColumn();
        System.out.println("Normalized input data with bias column: " + normInputData);
        double predictedPrice = train.predictSingle(finalTheta, new double[]{size, bedrooms});
        System.out.printf("Predicted price of a %.0f sq-ft %.0f br house : $%.4f", size, bedrooms, predictedPrice);
        assertEquals(289314.620338, predictedPrice, 1e-5);
    }

    @Test
    public void testDifferentAlpha() throws IOException {
        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LinearModel())
                .setX(X).setY(y)
                .setXTransformations(true, true, null)
                .setMatrixFactory(factory);

        Plot2DPanel panel = displayUtil.createPlotPanel("iterations", "cost", null);
        train(0.01, 80, trainingSet, panel);
        train(0.03, 80, trainingSet, panel);
        train(0.1, 80, trainingSet, panel);
        train(0.3, 80, trainingSet, panel);
        displayUtil.saveImage(panel, "./target/Ex1MultiAlpha.png");
    }

    private void train(double alpha, int numIter, TrainingSet trainingSet, Plot2DPanel panel) {
        SimpleGradientDescent regression = new SimpleGradientDescent(trainingSet);
        regression.train(alpha, numIter, true, factory.createMatrix(trainingSet.getThetaSize(), 1));
        panel.addLinePlot("alpha=" + alpha, regression.getCostHistory());
    }

}
