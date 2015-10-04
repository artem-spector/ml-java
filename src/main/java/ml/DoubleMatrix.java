package ml;

import ml.regression.PolynomialFeatures;
import ml.vector.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

/**
 * Two dimensional matrix with double values
 *
 * @author artem
 *         Date: 9/14/15
 */
public class DoubleMatrix implements Matrix {

    private final double data[][];
    public final int m;
    public final int n;

    public static DoubleMatrix ones(int m, int n) {
        double[][] ones = new double[m][n];
        for (int i = 0; i < m; i++) Arrays.fill(ones[i], 1);
        return new DoubleMatrix(ones);
    }

    public static DoubleMatrix zeros(int m, int n) {
        double[][] zeros = new double[m][n];
        for (int i = 0; i < m; i++) Arrays.fill(zeros[i], 0);
        return new DoubleMatrix(zeros);
    }

    public static DoubleMatrix eye(int m) {
        double[][] eye = new double[m][m];
        for (int i = 0; i < m; i++) {
            Arrays.fill(eye[i], 0);
            eye[i][i] = 1;
        }
        return new DoubleMatrix(eye);
    }

    public DoubleMatrix(double[][] data) {
        this.data = data;
        m = data.length;
        n = data[0].length;
    }

    @Override
    public int numRows() {
        return m;
    }

    @Override
    public int numColumns() {
        return n;
    }

    public double get(int i, int j) {
        return data[i][j];
    }

    public void set(int i, int j, double v) {
        data[i][j] = v;
    }

    @Override
    public Vector getColumn(int col) {
        return new StandaloneVector(Vector.VectorType.COLUMN, getColumnData(col));
    }

    @Override
    public Vector getRow(int row) {
        return new StandaloneVector(Vector.VectorType.ROW, data[row]);
    }

    @Override
    public Matrix multiply(Matrix other) {
        return multiply((DoubleMatrix) other);
    }

    @Override
    public Matrix multiplyElements(Vector column) {
        assert column.type() == Vector.VectorType.COLUMN && column.length() == m;
        return multiplyElements(column.asArray());
    }

    @Override
    public Matrix subtract(Matrix other) {
        return subtract((DoubleMatrix) other);
    }

    @Override
    public Matrix add(Matrix other) {
        return add((DoubleMatrix) other);
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
        Matrix res = new DoubleMatrix(new double[m][n]);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res.set(i, j, columnStatistics[j].normalizeFunction.eval(get(i, j)));
        return res;
    }

    @Override
    public Matrix addPlynomialFeatures(PolynomialFeatures pol) {
        double[][] res = new double[m][n + pol.numPolynoms()];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, data[i].length);
            pol.calculatePolynomialFeatures(res[i], 0, n);
        }
        return new DoubleMatrix(res);
    }

    @Override
    public Matrix addOnesColumn() {
        return addBiasColumn();
    }

    private double[] getColumnData(int col) {
        double[] res = new double[m];
        for (int i = 0; i < m; i++) res[i] = data[i][col];
        return res;
    }

    public Matrix selectRows(RowSelector rowSelector) {
        List<double[]> res = new ArrayList<>();
        for (int i = 0; i < m; i++)
            if (rowSelector.evaluate(data[i])) res.add(data[i]);
        return new DoubleMatrix(res.toArray(new double[res.size()][]));
    }

    @Override
    public Matrix selectColumns(int... colIdx) {
        double[][] res = new double[m][colIdx.length];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < colIdx.length; j++)
                res[i][j] = data[i][colIdx[j]];

        return new DoubleMatrix(res);
    }

    public DoubleMatrix select(int[] rows, int[] columns) {
        if (rows == null) {
            rows = new int[m];
            for (int i = 0; i < m; i++) rows[i] = i;
        }
        if (columns == null) {
            columns = new int[n];
            for (int j = 0; j < n; j++) columns[j] = j;
        }

        double[][] res = new double[rows.length][columns.length];
        for (int i = 0; i < rows.length; i++)
            for (int j = 0; j < columns.length; j++)
                res[i][j] = data[rows[i]][columns[j]];

        return new DoubleMatrix(res);
    }

    public double[] getRowData(int row) {
        double[] res = new double[n];
        System.arraycopy(data[row], 0, res, 0, n);
        return res;
    }

    public DoubleMatrix addBiasColumn() {
        double[][] res = new double[m][n + 1];
        for (int i = 0; i < m; i++) {
            res[i][0] = 1;
            System.arraycopy(data[i], 0, res[i], 1, n);
        }
        return new DoubleMatrix(res);
    }

    public DoubleMatrix transpose() {
        double[][] trans = new double[n][m];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                trans[j][i] = data[i][j];
        return new DoubleMatrix(trans);
    }

    public DoubleMatrix multiply(DoubleMatrix that) {
        if (this.n != that.m)
            throw new IllegalArgumentException("Cannot calculate (" + m + "*" + n + ") * (" + that.m + "*" + that.n + ")");

        double[][] res = new double[this.m][that.n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < that.n; j++) {
                double sum = 0;
                for (int k = 0; k < this.n; k++) {
                    sum += this.data[i][k] * that.data[k][j];
                }
                res[i][j] = sum;
            }
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix subtract(DoubleMatrix that) {
        if (this.m != that.m || this.n != that.n)
            throw new IllegalArgumentException("Cannot calculate (" + m + "*" + n + ") - (" + that.m + "*" + that.n + ")");

        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] -= that.data[i][j];
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix add(DoubleMatrix that) {
        if (this.m != that.m || this.n != that.n)
            throw new IllegalArgumentException("Cannot calculate (" + m + "*" + n + ") + (" + that.m + "*" + that.n + ")");

        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] += that.data[i][j];
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix minus() {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res[i][j] = -data[i][j];

        return new DoubleMatrix(res);
    }

    public DoubleMatrix multiply(double val) {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] *= val;
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix divide(double val) {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] /= val;
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix subtract(double val) {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] -= val;
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix add(double val) {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++) {
            System.arraycopy(data[i], 0, res[i], 0, n);
            for (int j = 0; j < n; j++)
                res[i][j] += val;
        }

        return new DoubleMatrix(res);
    }

    public DoubleMatrix applyFunction(FunctionEvaluator function) {
        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res[i][j] += function.eval(data[i][j]);

        return new DoubleMatrix(res);
    }

    public DoubleMatrix sumRows() {
        double[][] res = new double[1][n];
        Arrays.fill(res[0], 0);
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res[0][j] += data[i][j];

        return new DoubleMatrix(res);
    }

    public DoubleMatrix multiplyElements(double[] column) {
        if (m != column.length)
            throw new IllegalArgumentException("Cannot multiply matrix " + m + "*" + n + " by column " + column.length);
        double res[][] = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res[i][j] += data[i][j] * column[i];

        return new DoubleMatrix(res);
    }

    public NormalizationData[] calculateNormalization() {
        NormalizationData[] res = new NormalizationData[n];
        for (int j = 0; j < n; j++) {
            res[j] = new NormalizationData();
            for (int i = 0; i < m; i++)
                res[j].addValue(data[i][j]);
            res[j].stop();
        }
        return res;
    }

    public DoubleMatrix normalize(NormalizationData[] norm) {
        if (norm.length != n)
            throw new IllegalArgumentException("Cannot normalize " + m + "*" + n + " matrix with " + norm.length + " norm data.");

        double[][] res = new double[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                res[i][j] = norm[j].apply(data[i][j]);

        return new DoubleMatrix(res);
    }

    @Override
    public String toString() {
        return toString("%f  ");
    }

    public String toString(String format) {
        StringBuilder line = new StringBuilder().append("Matrix ").append(m).append(" by ").append(n);
        Formatter formatter = new Formatter(line);
        for (int i = 0; i < m; i++) {
            formatter.format("%n");
            for (int j = 0; j < n; j++)
                formatter.format(format, data[i][j]);
        }
        return line.toString();
    }

    @Override
    public int hashCode() {
        return m * n;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || !(obj instanceof DoubleMatrix)) return false;

        DoubleMatrix that = (DoubleMatrix) obj;
        if (this.m != that.m || this.n != that.n)
            return false;
        for (int i = 0; i < m; i++) {
            if (!Arrays.equals(data[i], that.data[i]))
                return false;
        }
        return true;
    }
}
