package ml.vector;

import ml.FunctionEvaluator;
import ml.regression.PolynomialFeatures;

import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/30/15
 */
public class MatrixImpl implements Matrix {

    protected int m;
    protected int n;
    protected double[] data;
    protected boolean orderedByRows;

    public static MatrixImpl eye(int size) {
        MatrixImpl res = new MatrixImpl(size, size);
        for (int i = 0; i < size; i++) res.set(i, i, 1);
        return res;
    }

    public MatrixImpl(int m, int n) {
        this(m, n, new double[m * n], true);
    }

    MatrixImpl(int m, int n, double[] data, boolean orderedByRows) {
        this.m = m;
        this.n = n;
        this.data = data;
        this.orderedByRows = orderedByRows;
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
        return data[indexOf(row, col)];
    }

    public void set(int row, int col, double val) {
        data[indexOf(row, col)] = val;
    }

    public Vector getRow(int row) {
        return new EmbeddedVector(Vector.VectorType.ROW, row, this);
    }

    public Vector getColumn(int col) {
        return new EmbeddedVector(Vector.VectorType.COLUMN, col, this);
    }

    @Override
    public Matrix selectRows(RowSelector rowSelector) {
        double[] res = new double[0];
        for (int i = 0; i < m; i++) {
            double[] rowData = getRow(i).asArray();
            if (rowSelector.evaluate(rowData)) {
                double[] tmp = new double[res.length + n];
                System.arraycopy(res, 0, tmp, 0, res.length);
                System.arraycopy(rowData, 0, tmp, res.length, rowData.length);
                res = tmp;
            }
        }
        return new MatrixImpl(res.length / n, n, res, true);
    }

    @Override
    public Matrix selectColumns(int... colIdx) {
        double[] res = new double[0];
        for (int col : colIdx) {
            double[] rowData = getColumn(col).asArray();
            double[] tmp = new double[res.length + m];
            System.arraycopy(res, 0, tmp, 0, res.length);
            System.arraycopy(rowData, 0, tmp, res.length, rowData.length);
            res = tmp;
        }
        return new MatrixImpl(m, colIdx.length, res, false);
    }

    public Matrix transpose() {
        return new MatrixImpl(n, m, cloneData(), !orderedByRows);
    }

    public Matrix multiply(Matrix other) {
        assert n == other.numRows();
        MatrixImpl res = new MatrixImpl(m, other.numColumns());

        VectorDataCache otherColumnsCache = new VectorDataCache(key -> other.getColumn(key).asArray());

        for (int i = 0; i < res.m; i++) {
            int rowStart = indexOf(i, 0);
            for (int j = 0; j < res.n; j++) {
                double[] otherColumn = otherColumnsCache.get(j);
                double val = 0;
                for (int k = 0; k < n; k++) {
                    double rowElement = orderedByRows ? data[rowStart + k] : get(i, k);
                    val += rowElement * otherColumn[k];
                }
                res.set(i, j, val);
            }
        }
        return res;
    }

    @Override
    public Matrix multiplyElements(Vector column) {
        assert column.type() == Vector.VectorType.COLUMN && column.length() == m;
        MatrixImpl res = new MatrixImpl(m, n, cloneData(), orderedByRows);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.set(i, j, res.get(i, j) * column.get(i));
        return res;
    }

    @Override
    public Matrix subtract(Matrix other) {
        assert m == other.numRows() && n == other.numColumns();
        Matrix res = new MatrixImpl(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.set(i, j, get(i, j) - other.get(i, j));
        return res;
    }

    @Override
    public Matrix add(Matrix other) {
        assert m == other.numRows() && n == other.numColumns();
        Matrix res = new MatrixImpl(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.set(i, j, get(i, j) + other.get(i, j));
        return res;
    }

    @Override
    public Matrix applyFunction(FunctionEvaluator function) {
        MatrixImpl res = new MatrixImpl(m, n, cloneData(), orderedByRows);
        for (int i = 0; i < data.length; i++)
            res.data[i] = function.eval(res.data[i]);
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
        Matrix res = new MatrixImpl(m, n);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.set(i, j, columnStatistics[j].normalizeFunction.eval(get(i, j)));
        return res;
    }

    @Override
    public Matrix addPlynomialFeatures(PolynomialFeatures pol) {
        MatrixImpl res = new MatrixImpl(m, n + pol.numPolynoms());
        for (int i = 0; i < m; i++) {
            System.arraycopy(getRow(i).asArray(), 0, res.data, res.indexOf(i, 0), n);
            pol.calculatePolynomialFeatures(res.data, res.indexOf(i, 0), n);
        }
        return res;
    }

    @Override
    public Matrix addOnesColumn() {
        MatrixImpl res = new MatrixImpl(m, n + 1, new double[data.length + m], orderedByRows);
        if (orderedByRows) {
            for (int i = 0; i < m; i++) {
                int resIdx = res.indexOf(i, 0);
                res.data[resIdx] = 1;
                System.arraycopy(data, indexOf(i, 0), res.data, resIdx + 1, n);
            }
        } else {
            Arrays.fill(res.data, 0, m, 1);
            System.arraycopy(data, 0, res.data, m, data.length);
        }
        return res;
    }

    int indexOf(int row, int col) {
        assert row >= 0 && row < m && col >= 0 && col < n;
        return orderedByRows ? row * n + col : col * m + row;
    }

    private double[] cloneData() {
        double[] copy = new double[data.length];
        System.arraycopy(data, 0, copy, 0, data.length);
        return copy;
    }

    @Override
    public String toString() {
        return toString("%f  ");
    }

    @Override
    public int hashCode() {
        return m * n;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof MatrixImpl)) return false;

        MatrixImpl that = (MatrixImpl) obj;
        if (this.m != that.m || this.n != that.n)
            return false;
        else if (this.orderedByRows == that.orderedByRows)
            return Arrays.equals(this.data, that.data);
        else {
            for (int i = 0; i < m; i++)
                for (int j = 0; j < n; j++)
                    if (get(i, j) != that.get(i, j)) return false;
            return true;
        }
    }
}
