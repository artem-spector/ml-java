package coursera.ml;

import org.artem.tools.DataLoadUtil;
import org.artem.tools.display.DisplayUtil;
import org.artem.tools.regression.LinearModel;
import org.artem.tools.regression.SimpleGradientDescent;
import org.artem.tools.regression.TrainingSet;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.math.plot.Plot2DPanel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/23/15
 */
public class Ex1Test {

    private static DataLoadUtil dataLoadUtil = new DataLoadUtil();
    private static DisplayUtil displayUtil = new DisplayUtil();

    private static Matrix allData;
    private static MatrixFactory matrixFactory = new SimpleMatrixFactory();

    @BeforeClass
    public static void loadData() {
        allData = dataLoadUtil.readCSV("machine-learning-ex1/ex1/ex1data1.txt", matrixFactory);
    }

    @Test
    public void testEye() {
        int size = 5;
        Matrix eye = matrixFactory.eye(size);
        System.out.println(eye);
        assertEquals(size, eye.numRows());
        assertEquals(size, eye.numColumns());
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                double v = eye.get(i, j);
                assertTrue(i == j ? v == 1 : v == 0);
            }
    }

    @Test
    public void testPlotTrainingData() throws IOException {
        Matrix X = allData.selectColumns(0);
        Matrix y = allData.selectColumns(1);

        Plot2DPanel panel = displayUtil.createPlotPanel("Population in 10,000s", "Profit in $10,000s", null);
        panel.addScatterPlot("Profit vs. population", X.getColumn(0).asArray(), y.getColumn(0).asArray());
        displayUtil.saveImage(panel, "./target/Ex1load.png");
    }

    @Test
    public void testSimpleGradientDescent() throws IOException {
        Matrix X = allData.selectColumns(0);
        Matrix y = allData.selectColumns(1);

        TrainingSet train = new TrainingSet().setX(X).setXTransformations(true, false, null).setY(y).setModelCalculator(new LinearModel());
        Matrix initialTheta = matrixFactory.createMatrix(train.getThetaSize(), 1);
        double initialCost = train.getCost(initialTheta);
        System.out.println("Cost at initial theta (zeros): " + initialCost);
        assertEquals("Initial cost as expected", 32.073, initialCost, 1e-3);
        System.out.println("Gradient at initial theta (zeros): " + train.getGradient(initialTheta));

        SimpleGradientDescent simpleRegression = new SimpleGradientDescent(train);
        Matrix finalTheta = simpleRegression.train(0.01, 1500, true, initialTheta);
        double finalCost = train.getCost(finalTheta);
        System.out.println("Cost at final theta: " + finalCost);
        System.out.println("Final theta: " + finalTheta);
        assertEquals(-3.630291, finalTheta.get(0, 0), 1e-6);
        assertEquals(1.166362, finalTheta.get(1, 0), 1e-6);

        Plot2DPanel panel = displayUtil.createPlotPanel("Population in 10,000s", "Profit in $10,000s", null);
        double[] x = X.getColumn(0).asArray();
        panel.addScatterPlot("Profit vs. population", x, y.getColumn(0).asArray());
        panel.addLinePlot("Linear regression", x, train.getHypothesis(finalTheta).getColumn(0).asArray());
        displayUtil.saveImage(panel, "./target/Ex1Regression.png");
    }
}
