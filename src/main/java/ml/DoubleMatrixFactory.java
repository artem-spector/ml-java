package ml;

import ml.vector.Matrix;
import ml.vector.MatrixFactory;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/2/15
 */
public class DoubleMatrixFactory implements MatrixFactory {
    @Override
    public Matrix createMatrix(int m, int n, double... fill) {
        DoubleMatrix res = new DoubleMatrix(new double[m][n]);
        if (fill != null && fill.length > 0) res.fill(fill);
        return res;
    }

    @Override
    public Matrix createMatrix(double[][] data) {
        return new DoubleMatrix(data);
    }

    @Override
    public Matrix eye(int m) {
        return DoubleMatrix.eye(m);
    }
}
