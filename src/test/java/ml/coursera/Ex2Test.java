package ml.coursera;

import edu.stanford.nlp.optimization.QNMinimizer;
import ml.DataLoadUtil;
import ml.DoubleMatrixFactory;
import ml.FunctionEvaluator;
import ml.display.DisplayUtil;
import ml.regression.LogisticModel;
import ml.regression.SimpleGradientDescent;
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
 *         Date: 9/24/15
 */
public class Ex2Test {

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
    public void testPlotAdmittedNotAdmitted() {
        assertEquals(X.numRows(), admitted.numRows() + notAdmitted.numRows());
        DisplayUtil disp = new DisplayUtil();
        disp.createPlotPanel("Exam 1 score", "Exam 2 score");
        disp.addPlotScatter("Admitted", admitted.getColumn(0).asArray(), admitted.getColumn(1).asArray());
        disp.addPlotScatter("Not admitted", notAdmitted.getColumn(0).asArray(), notAdmitted.getColumn(1).asArray());
        disp.saveImage("./target/Ex2admissionData.png");
    }

    @Test
    public void testSimpleVsQNMinimizer() {
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, null)
                .setY(y)
                .setMatrixFactory(factory);
        Matrix initialTheta = factory.createMatrix(train.getThetaSize(), 1);
        double initialCost = train.getCost(initialTheta);
        System.out.printf("Cost at initial theta (zeros) %f %n", initialCost);
        System.out.println("Gradient at initial theta: " + train.getGradient(initialTheta));
        assertEquals(0.69314, initialCost, 1e-5);

        SimpleGradientDescent simple = new SimpleGradientDescent(train);

        int maxIter = 50000;
        Matrix finalTheta = simple.train(0.001, maxIter, true, initialTheta);
        System.out.println("Cost at final theta found by simple regression after " + maxIter + " iterations: "
                + ": " + train.getCost(finalTheta));

        QNMinimizer minimizer = new QNMinimizer(10, true);
        double[] min = minimizer.minimize(train, 1e-6, initialTheta.getColumn(0).asArray(), 400);
        finalTheta = factory.createMatrix(min.length, 1, min);
        System.out.println("Cost at final theta found by QNMinimizer: " + train.getCost(finalTheta));

        double prediction = train.predictSingle(finalTheta, new double[]{45, 85});
        System.out.printf("For a student with scores %d and %d, we predict an admission probability of %.6f %n", 45, 85,
                prediction);
        assertEquals(0.776289, prediction, 1e-6);
    }

    @Test
    public void testPlotDecisionBoundary() {
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, null)
                .setY(y)
                .setMatrixFactory(factory);
        double[] theta = minimizeTheta(train);

        // decision boundary has probablility 0.5, the argument of sigmoid function must be 0.
        FunctionEvaluator f = x1 -> (theta[0] + theta[1] * x1) / -theta[2];
        double[] v1 = new double[]{30, 60, 100}; // two points would be enough for a straight line, use 3 just for fun
        double[] v2 = new double[]{f.eval(v1[0]), f.eval(v1[1]), f.eval(v1[2])};

        DisplayUtil disp = new DisplayUtil();
        disp.createPlotPanel("exam 1 score", "exam 2 score");
        disp.addPlotScatter("Admitted", admitted.getColumn(0).asArray(), admitted.getColumn(1).asArray());
        disp.addPlotScatter("Not admitted", notAdmitted.getColumn(0).asArray(), notAdmitted.getColumn(1).asArray());
        disp.addPlotLine("Decision boundary", v1, v2);
        disp.saveImage("./target/Ex2AdmissionAndDecisionBoundary.png");
    }

    @Test
    public void testAccuracy() {
        TrainingSet train = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformations(true, false, null)
                .setY(y)
                .setMatrixFactory(factory);
        Matrix theta = factory.createMatrix(train.getThetaSize(), 1, minimizeTheta(train));
        Matrix h = train.getHypothesis(theta).applyFunction(x -> x >= 0.5 ? 1 : 0);

        FunctionEvaluator f = v -> v == 0 ? 1 : 0;
        double correctPredisctions = y.subtract(h).applyFunction(f).getColumn(0).sum();
        double accuracy = (correctPredisctions / y.numRows()) * 100;
        System.out.println("Train accuracy: " + accuracy);
        assertEquals(89.00, accuracy, 1e-5);
    }

    private double[] minimizeTheta(TrainingSet trainingSet) {
        QNMinimizer minimizer = new QNMinimizer(10, true);
        minimizer.shutUp();
        return minimizer.minimize(trainingSet, 1e-6, new double[trainingSet.getThetaSize()], 400);
    }

}
