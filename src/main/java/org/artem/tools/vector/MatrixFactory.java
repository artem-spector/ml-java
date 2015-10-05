package org.artem.tools.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/2/15
 */
public interface MatrixFactory {

    Matrix createMatrix(int m, int n, double... fill);

    Matrix createMatrix(double[][] data);

    Matrix eye(int m);

}
