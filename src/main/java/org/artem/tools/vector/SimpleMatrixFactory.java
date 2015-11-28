package org.artem.tools.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/2/15
 */
public class SimpleMatrixFactory implements MatrixFactory {

    @Override
    public Matrix createMatrix(int m, int n, double... fill) {
        SimpleMatrix x = new SimpleMatrix(m, n);
        if (fill.length > 0) x.fill(fill);
        return x;
    }

    @Override
    public Matrix createMatrix(double[][] data) {
        return new SimpleMatrix(data);
    }

    @Override
    public Matrix eye(int m) {
        return SimpleMatrix.eye(m);
    }

}
