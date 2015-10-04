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
        if (fill.length == m * n) {
            double[] data = new double[fill.length];
            System.arraycopy(fill, 0, data, 0, fill.length);
            return new MatrixImpl(m, n, data, true);
        } else {
            MatrixImpl x = new MatrixImpl(m, n);
            if (fill.length > 0) x.fill(fill);
            return x;
        }
    }

    @Override
    public Matrix createMatrix(double[][] data) {
        int m = data.length;
        int n = data[0].length;

        double[] res = new double[m * n];
        for (int i = 0; i < m; i++)
            System.arraycopy(data[i], 0, res, i * n, n);
        return new MatrixImpl(m, n, res, true);
    }

    @Override
    public Matrix eye(int m) {
        return MatrixImpl.eye(m);
    }
}
