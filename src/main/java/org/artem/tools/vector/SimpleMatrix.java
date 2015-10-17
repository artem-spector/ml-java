package org.artem.tools.vector;

import org.artem.tools.FunctionEvaluator;
import org.artem.tools.regression.PolynomialFeatures;

import java.util.*;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/30/15
 */
public class SimpleMatrix implements Matrix {

    protected int m;
    protected int n;
    protected double[][] data;

    public static SimpleMatrix eye(int size) {
        SimpleMatrix res = new SimpleMatrix(size, size);
        for (int i = 0; i < size; i++) res.set(i, i, 1);
        return res;
    }

    public SimpleMatrix(int m, int n) {
        this(new double[m][n]);
    }

    SimpleMatrix(double[][] data) {
        this.m = data.length;
        this.n = data[0].length;
        this.data = data;
    }

    @Override
    public int numRows() {
        return m;
    }

    @Override
    public int numColumns() {
        return n;
    }

    public double get(int row, int col) {
        return data[row][col];
    }

    public void set(int row, int col, double val) {
        data[row][col] = val;
    }

    public Vector getRow(int row) {
        return new EmbeddedVector(Vector.VectorType.ROW, row, this);
    }

    public Vector getColumn(int col) {
        return new EmbeddedVector(Vector.VectorType.COLUMN, col, this);
    }

    @Override
    public Matrix selectRows(RowSelector rowSelector) {
        List<double[]> res = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            if (rowSelector.evaluate(data[i])) {
                double[] row = new double[n];
                System.arraycopy(data[i], 0, row, 0, n);
                res.add(row);
            }
        }
        return new SimpleMatrix(res.toArray(new double[res.size()][]));
    }

    @Override
    public Matrix selectColumns(int... colIdx) {
        SimpleMatrix res = new SimpleMatrix(m, colIdx.length);
        for (int j  = 0; j < colIdx.length; j++) {
            for (int i = 0; i < m; i++) res.data[i][j] = data[i][colIdx[j]];
        }
        return res;
    }

    public Matrix transpose() {
        SimpleMatrix res = new SimpleMatrix(n, m);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[j][i] = data[i][j];
        return res;
    }

    public Matrix multiply(Matrix other) {
        assert n == other.numRows();
        SimpleMatrix res = new SimpleMatrix(m, other.numColumns());
        for (int i = 0; i < res.m; i++) {
            for (int j = 0; j < res.n; j++) {
                double val = 0;
                for (int k = 0; k < n; k++) {
                    val += data[i][k] * ((SimpleMatrix) other).data[k][j];
                }
                res.data[i][j] = val;
            }
        }
        return res;
    }

    @Override
    public Matrix multiplyElements(Vector column) {
        assert column.type() == Vector.VectorType.COLUMN && column.length() == m;
        SimpleMatrix res = new SimpleMatrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[i][j] = data[i][j] * column.get(i);
        return res;
    }

    @Override
    public Matrix subtract(Matrix other) {
        assert m == other.numRows() && n == other.numColumns();
        SimpleMatrix res = new SimpleMatrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[i][j] = data[i][j] - ((SimpleMatrix) other).data[i][j];
        return res;
    }

    @Override
    public Matrix add(Matrix other) {
        assert m == other.numRows() && n == other.numColumns();
        SimpleMatrix res = new SimpleMatrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[i][j] = data[i][j] + ((SimpleMatrix) other).data[i][j];
        return res;
    }

    @Override
    public Matrix applyFunction(FunctionEvaluator function) {
        SimpleMatrix res = new SimpleMatrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[i][j] = function.eval(data[i][j]);
        return res;
    }

    @Override
    public Statistics[] calculateColumnStatistics() {
        Statistics[] res = new Statistics[n];
        for (int j = 0; j < n; j++)
            res[j] = getColumn(j).calculateStatistics();
        return res;
    }

    @Override
    public Matrix normalize(Statistics[] columnStatistics) {
        assert columnStatistics.length == n;
        SimpleMatrix res = new SimpleMatrix(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.data[i][j] = columnStatistics[j].normalizeFunction.eval(data[i][j]);
        return res;
    }

    @Override
    public Matrix addPlynomialFeatures(PolynomialFeatures pol) {
        SimpleMatrix res = new SimpleMatrix(m, n + pol.numPolynoms());
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res.data[i], 0, n);
            pol.calculatePolynomialFeatures(res.data[i], 0, n);
        }
        return res;
    }

    @Override
    public Matrix addOnesColumn() {
        SimpleMatrix res = new SimpleMatrix(m, n + 1);
        for (int i = 0; i < m; i++) {
            res.data[i][0] = 1;
            System.arraycopy(data[i], 0, res.data[i], 1, n);
        }
        return res;
    }

    @Override
    public String toString() {
        return toString("%f  ", 10, 10);
    }

    @Override
    public int hashCode() {
        return m * n;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof SimpleMatrix)) return false;

        SimpleMatrix that = (SimpleMatrix) obj;
        if (this.m != that.m || this.n != that.n)
            return false;
        else {
            for (int i = 0; i < m; i++)
                if (!Arrays.equals(this.data[i], that.data[i])) return false;
            return true;
        }
    }
}
