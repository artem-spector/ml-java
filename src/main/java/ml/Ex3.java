package ml;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import ml.display.DisplayUtil;

import java.io.IOException;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class Ex3 {

    public static void main (String args[]) throws IOException {
        ArrayUtil arrayUtil = new ArrayUtil();
        DisplayUtil displayUtil = new DisplayUtil();

        MatFileReader reader = new MatFileReader("machine-learning-ex3/ex3/ex3data1.mat");
        MLArray X = reader.getMLArray("X");
        MLArray y = reader.getMLArray("y");

        int[] selectionIdx = new int[100];
        System.arraycopy(arrayUtil.randperm(X.getM()), 0, selectionIdx, 0, selectionIdx.length);

/*
        double[][] selection = arrayUtil.selectIndexes(((MLDouble)X).getArray(), selectionIdx);
        displayUtil.displayImageGrid(selection);
        displayUtil.pause();

        displayUtil.plotFunction("Sigmoid", -15, 15, new Sigmoid());
        displayUtil.pause();

*/
        DoubleMatrix trainExamples = new DoubleMatrix(arrayUtil.selectIndexes(((MLDouble) X).getArray(), selectionIdx));
        DoubleMatrix labels = new DoubleMatrix(arrayUtil.selectIndexes(((MLDouble) y).getArray(), selectionIdx));
        DoubleMatrix zeroLabels = labels.applyFunction(new FunctionEvaluator() {
            public double eval(double x) {
                return x == 10 ? 1 : 0;
            }
        });

/*
        LogisticRegression zeroVsAll = new LogisticRegression(trainExamples, zeroLabels);
        for (int i = 0; i < 10; i++) {
            zeroVsAll.train(10);
            System.out.println(zeroVsAll.getIteration() + ": " + zeroVsAll.getCost());
        }
*/

        System.out.println("Done.");
        System.exit(0);
    }

}
