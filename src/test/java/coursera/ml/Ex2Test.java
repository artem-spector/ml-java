package coursera.ml;

import org.artem.tools.DataLoadUtil;
import org.artem.tools.FunctionEvaluator;
import org.artem.tools.display.DisplayUtil;
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
 *         Date: 9/24/15
 */
public class Ex2Test {

    private static DisplayUtil disp = new DisplayUtil();
    private static MatrixFactory factory = new SimpleMatrixFactory();

    private static Matrix X;
    private static Matrix y;
    private static Matrix admitted;
    private static Matrix notAdmitted;

    @BeforeClass
    public static void loadData() {
        DataLoadUtil dataLoadUtil = new DataLoadUtil();

        Matrix allData = dataLoadUtil.readCSV("machine-learning-ex2/ex2/ex2data1.txt", factory);
        X = allData.selectColumns(0, 1);
        y = allData.selectColumns(2);

        admitted = allData.selectRows(row -> row[2] == 1);
        notAdmitted = allData.selectRows(row -> row[2] == 0);
    }

    @Test
    public void testPlotAdmittedNotAdmitted() throws IOException {
        assertEquals(X.numRows(), admitted.numRows() + notAdmitted.numRows());
        Plot2DPanel panel = disp.createPlotPanel("Exam 1 score", "Exam 2 score", null);
        panel.addScatterPlot("Admitted", admitted.getColumn(0).asArray(), admitted.getColumn(1).asArray());
        panel.addScatterPlot("Not admitted", notAdmitted.getColumn(0).asArray(), notAdmitted.getColumn(1).asArray());
        disp.saveImage(panel, "./target/Ex2admissionData.png");
    }

    @Test
    public void testSimpleVsQNMinimizer() {
        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, null))
                .setMatrixFactory(factory);
        Matrix initialTheta = factory.createMatrix(trainingSet.getThetaSize(), 1);
        double initialCost = trainingSet.getCost(initialTheta, y);
        System.out.printf("Cost at initial theta (zeros) %f %n", initialCost);
        System.out.println("Gradient at initial theta: " + trainingSet.getGradient(initialTheta, y));
        assertEquals(0.69314, initialCost, 1e-5);

        SimpleGradientDescent simple = new SimpleGradientDescent(trainingSet, y);
        int maxIter = 50000;
        Matrix finalTheta = simple.train(0.001, maxIter, true, initialTheta);
        System.out.println("Cost at final theta found by simple regression after " + maxIter + " iterations: "
                + ": " + trainingSet.getCost(finalTheta, y));

        Trainer trainer = new Trainer(trainingSet, y);
        Predictor predictor = trainer.train(true);
        System.out.println("Cost at final theta found by QNMinimizer: " + trainer.getFinalCost());

        double prediction = predictor.predict(null, 45, 85);
        System.out.printf("For a student with scores %d and %d, we predict an admission probability of %.6f %n", 45, 85,
                prediction);
        assertEquals(0.776289, prediction, 1e-6);
    }

    @Test
    public void testPlotDecisionBoundary() throws IOException {
        Predictor predictor = train();

        // decision boundary has probablility 0.5, the argument of sigmoid function must be 0.
        double[] theta = predictor.getTheta().getColumn(0).asArray();
        FunctionEvaluator f = x1 -> (theta[0] + theta[1] * x1) / -theta[2];
        double[] v1 = new double[]{30, 60, 100}; // two points would be enough for a straight line, use 3 just for fun
        double[] v2 = new double[]{f.eval(v1[0]), f.eval(v1[1]), f.eval(v1[2])};

        Plot2DPanel panel = disp.createPlotPanel("exam 1 score", "exam 2 score", null);
        panel.addScatterPlot("Admitted", admitted.getColumn(0).asArray(), admitted.getColumn(1).asArray());
        panel.addScatterPlot("Not admitted", notAdmitted.getColumn(0).asArray(), notAdmitted.getColumn(1).asArray());
        panel.addLinePlot("Decision boundary", v1, v2);
        disp.saveImage(panel, "./target/Ex2AdmissionAndDecisionBoundary.png");
    }

    @Test
    public void testAccuracy() {
        double accuracy = train().getPredictionAccuracy(x -> x >= 0.5 ? 1 : 0, X, y) * 100;
        System.out.println("Train accuracy: " + accuracy);
        assertEquals(89.00, accuracy, 1e-5);
    }

    private Predictor train() {
        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, null))
                .setMatrixFactory(factory);

        Trainer trainer = new Trainer(trainingSet, y);
        return trainer.train(false);
    }

}
