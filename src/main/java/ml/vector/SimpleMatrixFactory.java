package ml.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/2/15
 */
public class SimpleMatrixFactory implements MatrixFactory {

    @Override
    public Matrix createMatrix(int m, int n, double... fill) {
        MatrixImpl x = new MatrixImpl(m, n);
        if (fill.length > 0) x.fill(fill);
        return x;
    }

    @Override
    public Matrix createMatrix(double[][] data) {
        return new MatrixImpl(data);
    }

    @Override
    public Matrix eye(int m) {
        return MatrixImpl.eye(m);
    }
}
