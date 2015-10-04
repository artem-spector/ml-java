package ml.profiler;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import edu.stanford.nlp.optimization.QNMinimizer;
import ml.regression.LogisticModel;
import ml.regression.TrainingSet;
import ml.vector.Matrix;
import ml.vector.MatrixFactory;
import ml.vector.SimpleMatrixFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/4/15
 */
public class MatrixProfilerTest {

    private static MatrixFactory factory = new SimpleMatrixFactory();

    @Test
    public void testMatrixCreation() {
        long start = System.currentTimeMillis();

        MatrixFactory f = createFactoryProxyAndStartProfiling();
        Matrix a = f.createMatrix(2, 3, 1, 2, 3, 4, 5, 6);
        a.multiply(f.eye(3));

        long duration = System.currentTimeMillis() - start;
        Map<String, MethodStatistics> statisticsMap = stopProfiling();
        assertNotNull(statisticsMap);
        System.out.println(Profiler.getInstance().getReportStr());
    }

    @Test
    public void testMultiplication() {
        MatrixFactory f = createFactoryProxyAndStartProfiling();
        Matrix X = f.createMatrix(1000, 400, 1, 2, 3);
        Matrix theta = f.createMatrix(400, 1, 0.5);
        Matrix res = X.multiply(theta);

        Map<String, MethodStatistics> statisticsMap = stopProfiling();
        assertNotNull(statisticsMap);
        System.out.println(Profiler.getInstance().getReportStr());
    }

    @Test
    public void testLogisticRegression() throws IOException {
        MatFileReader reader = new MatFileReader("machine-learning-ex3/ex3/ex3data1.mat");
        Map<String, MLArray> content = reader.getContent();
        Assert.assertNotNull(content);

        MatrixFactory f = MatrixProfilingProxy.createMatrixFactoryProxy(factory);
        Matrix X = f.createMatrix(((MLDouble) content.get("X")).getArray());
        Matrix y = f.createMatrix(((MLDouble) content.get("y")).getArray());

        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setY(y.applyFunction(val -> val == 1 ? 1 : 0))
                .setXTransformations(true, false, null).setRegularization(0.1)
                .setMatrixFactory(f);

        double[] initTheta = new double[trainingSet.getThetaSize()];

        QNMinimizer minimizer = new QNMinimizer(10, true);
        minimizer.shutUp();

        Profiler profiler = Profiler.getInstance();
        profiler.startCollecting();

        minimizer.minimize(trainingSet, 1e-5, initTheta);

        profiler.stopCollecting();
        System.out.println(profiler.getReportStr());
    }

    private MatrixFactory createFactoryProxyAndStartProfiling() {
        Profiler.getInstance().startCollecting();
        return MatrixProfilingProxy.createMatrixFactoryProxy(factory);
    }

    private Map<String, MethodStatistics> stopProfiling() {
        Profiler profiler = Profiler.getInstance();
        profiler.stopCollecting();
        return profiler.getStatistics();
    }

}
