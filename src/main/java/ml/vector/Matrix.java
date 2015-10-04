package ml.vector;

import ml.FunctionEvaluator;
import ml.regression.PolynomialFeatures;

import java.util.Formatter;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/1/15
 */
public interface Matrix {

    int numRows();

    int numColumns();

    double get(int row, int col);

    void set(int row, int col, double val);

    Vector getRow(int row);

    Vector getColumn(int col);

    Matrix selectRows(RowSelector rowSelector);

    Matrix selectColumns(int... colIdx);

    Matrix transpose();

    Matrix multiply(Matrix other);

    Matrix multiplyElements(Vector column);

    Matrix subtract(Matrix other);

    Matrix add(Matrix other);

    Matrix applyFunction(FunctionEvaluator function);

    Statistics[] calculateColumnStatistics();

    Matrix normalize(Statistics[] columnStatistics);

    Matrix addPlynomialFeatures(PolynomialFeatures pol);

    Matrix addOnesColumn();

    default void fill(double... values) {
        assert values.length > 0;
        int k = 0;
        for (int i = 0; i < numRows(); i++)
            for (int j = 0; j < numColumns(); j++) {
                set(i, j, values[k++ % values.length]);
            }
    }

    default String toString(String format) {
        int m = numRows();
        int n = numColumns();
        StringBuilder line = new StringBuilder().append("Matrix ").append(m).append(" by ").append(n);
        Formatter formatter = new Formatter(line);
        for (int i = 0; i < m; i++) {
            formatter.format("%n");
            for (int j = 0; j < n; j++)
                formatter.format(format, get(i, j));
        }
        return line.toString();
    }


}