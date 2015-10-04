package ml.coursera;

import edu.stanford.nlp.optimization.QNMinimizer;
import ml.DataLoadUtil;
import ml.display.ContourFunction;
import ml.display.DisplayUtil;
import ml.display.GridDimension;
import ml.regression.LogisticModel;
import ml.regression.PolynomialFeatures;
import ml.regression.TrainingSet;
import ml.vector.Matrix;
import ml.vector.MatrixFactory;
import ml.vector.SimpleMatrixFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class Ex2RegTest {

    private static MatrixFactory factory = new SimpleMatrixFactory();
    private static Matrix allData;

    private static Matrix X;
    private static Matrix y;

    @BeforeClass
    public static void loadData() {
        DataLoadUtil dataLoadUtil = new DataLoadUtil();
        allData = dataLoadUtil.readCSV("machine-learning-ex2/ex2/ex2data2.txt", factory);
        X = allData.selectColumns(0, 1);
        y = allData.selectColumns(2);
    }

    @Test
    public void plotInitialData() {
        DisplayUtil disp = new DisplayUtil();
        plotTrainingData(disp);
        disp.saveImage("./target/Ex2RegTrainingData.png");
    }

    @Test
    public void testPolynomialFeatures() {
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, new PolynomialFeatures(new int[]{0, 1}, 6))
                .setY(y)
                .setMatrixFactory(factory);
        int numFeatures = train.getThetaSize();
        System.out.printf("After adding polynomial features of 6 degree, the number of features is %d%n", numFeatures);
        assertEquals(28, numFeatures);

        Matrix initialTheta = factory.createMatrix(numFeatures, 1);
        double initialCost = train.getCost(initialTheta);
        System.out.printf("Cost at initial theta (zeros): %f %n", initialCost);
        assertEquals(0.693147, initialCost, 1e-6);
    }

    @Test
    public void testRegularization() {
        PolynomialFeatures polynomialFeatures = new PolynomialFeatures(new int[]{0, 1}, 6);
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, polynomialFeatures)
                .setRegularization(1)
                .setY(y)
                .setMatrixFactory(factory);
        Matrix initialTheta = factory.createMatrix(train.getThetaSize(), 1);
        Matrix finalTheta = minimize(train, initialTheta);

        Matrix p = train.predict(finalTheta, X).applyFunction(x -> x >= 0.5 ? 1 : 0);
        double correct = p.subtract(y).applyFunction(x -> x == 0 ? 1 : 0).getColumn(0).sum();
        double accuracy = (correct / y.numRows()) * 100;
        System.out.printf("Train accuracy %f %n", accuracy);
        assertEquals(83.050847, accuracy, 1e-6);

        DisplayUtil disp = new DisplayUtil();
        plotTrainingData(disp);
        plotDecisionBoundary(train, finalTheta, disp);
        disp.saveImage("./target/Ex2RegDecisionBoundary.png");
    }

    private Matrix minimize(TrainingSet trainingSet, Matrix initialTheta) {
        QNMinimizer minimizer = new QNMinimizer(10, true);
        minimizer.shutUp();
        double[] minTheta = minimizer.minimize(trainingSet, 1e-5, initialTheta.getColumn(0).asArray(), 400);
        return factory.createMatrix(minTheta.length, 1, minTheta);
    }

    private void plotTrainingData(DisplayUtil disp) {
        Matrix passed = allData.selectRows(row -> row[2] == 1);
        Matrix notPassed = allData.selectRows(row -> row[2] == 0);

        disp.createPlotPanel("Test 1", "Test 2");
        disp.addPlotScatter("y=1", passed.getColumn(0).asArray(), passed.getColumn(1).asArray());
        disp.addPlotScatter("y=0", notPassed.getColumn(0).asArray(), notPassed.getColumn(1).asArray());
    }

    private void plotDecisionBoundary(TrainingSet trainingSet, Matrix theta, DisplayUtil disp) {
        GridDimension gridDimension = new GridDimension(-1, 1.5, 800);
        ContourFunction f = (x, y) -> Math.abs(trainingSet.predictSingle(theta, new double[]{x,y}) - 0.5) <= 1e-3 ;
        disp.addPlotContour("Decision boundary", gridDimension, gridDimension, f);
    }
}
