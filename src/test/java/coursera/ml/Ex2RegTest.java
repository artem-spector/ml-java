package coursera.ml;

import org.artem.tools.file.DataLoadUtil;
import org.artem.tools.display.ContourFunction;
import org.artem.tools.display.DisplayUtil;
import org.artem.tools.display.GridDimension;
import org.artem.tools.regression.*;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.SimpleMatrixFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.math.plot.Plot2DPanel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/27/15
 */
public class Ex2RegTest {

    private static DisplayUtil disp = new DisplayUtil();
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
    public void plotInitialData() throws IOException {
        disp.saveImage(plotTrainingData(), "./target/Ex2RegTrainingData.png");
    }

    @Test
    public void testPolynomialFeatures() {
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, new PolynomialFeatures(new int[]{0, 1}, 6)))
                .setMatrixFactory(factory);
        int numFeatures = train.getThetaSize();
        System.out.printf("After adding polynomial features of 6 degree, the number of features is %d%n", numFeatures);
        assertEquals(28, numFeatures);

        Trainer trainer = new Trainer(train, y);
        double initialCost = trainer.getInitialCost();
        System.out.printf("Cost at initial theta (zeros): %f %n", initialCost);
        assertEquals(0.693147, initialCost, 1e-6);
    }

    @Test
    public void testRegularization() throws IOException {
        PolynomialFeatures polynomialFeatures = new PolynomialFeatures(new int[]{0, 1}, 6);
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, polynomialFeatures))
                .setRegularization(1)
                .setMatrixFactory(factory);
        Predictor predictor = minimize(train, y);

        double accuracy = predictor.getPredictionAccuracy(x -> x >= 0.5 ? 1 : 0, X, y) * 100;
        System.out.printf("Train accuracy %f %n", accuracy);
        assertEquals(83.050847, accuracy, 1e-6);

        Plot2DPanel panel = plotTrainingData();
        plotDecisionBoundary(predictor, panel);
        disp.saveImage(panel, "./target/Ex2RegDecisionBoundary.png");
    }

    private Predictor minimize(TrainingSet trainingSet, Matrix y) {
        return new Trainer(trainingSet, y).train(false);
    }

    private Plot2DPanel plotTrainingData() {
        Matrix passed = allData.selectRows(row -> row[2] == 1);
        Matrix notPassed = allData.selectRows(row -> row[2] == 0);

        Plot2DPanel panel = disp.createPlotPanel("Test 1", "Test 2", null);
        panel.addScatterPlot("y=1", passed.getColumn(0).asArray(), passed.getColumn(1).asArray());
        panel.addScatterPlot("y=0", notPassed.getColumn(0).asArray(), notPassed.getColumn(1).asArray());

        return panel;
    }

    private void plotDecisionBoundary(Predictor predictor, Plot2DPanel panel) {
        GridDimension gridDimension = new GridDimension(-1, 1.5, 800);
        ContourFunction f = (x, y) -> Math.abs(predictor.predict(null, x, y) - 0.5) <= 1e-3;
        double[][] contourPoints = disp.getContourPoints(gridDimension, gridDimension, f);
        panel.addLinePlot("Decision boundary", contourPoints);
    }
}
