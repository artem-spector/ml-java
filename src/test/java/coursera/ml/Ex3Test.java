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
import java.util.concurrent.Executors;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class Ex3Test {

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
        String[] labels = new String[images.length];
        for (int i = 0; i < labels.length; i++) labels[i] = String.valueOf((int) y.get(randIndexes[i], 0));

        JPanel panel = displayUtil.createImageGrid(images, 20, 20, 2, labels);
        displayUtil.saveImage(panel, "./target/Ex3TrainingSample.png");
    }

    @Test
    public void testOneVsAll() {
        TrainingSet trainingSet = new TrainingSet()
                .setModelCalculator(new LogisticModel())
                .setX(X)
                .setXTransformation(new XTransformation(true, null, null))
                .setRegularization(0.1)
                .setMatrixFactory(factory);

        Classification classifier = new Classification();
        classifier.trainOneVsAll(trainingSet, y, Executors.newFixedThreadPool(5));
        ClassificationAccuracy accuracy = classifier.getAccuracy(X, y);
        System.out.println("Training set accuracy: " + accuracy.getCrossLabelAccuracy() * 100);
    }

}
