package org.artem.tools.matrix;

import org.artem.tools.vector.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/30/15
 */
public class MatrixTest {

    private static MatrixFactory factory = new SimpleMatrixFactory();

    @Test
    public void testEmptyMatrix() {
        int m = 2;
        int n = 3;
        Matrix emptyMatrix = factory.createMatrix(m, n);
        System.out.println(emptyMatrix);

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) assertEquals(0.0, emptyMatrix.get(i, j), 0);

        Matrix transposed = emptyMatrix.transpose();
        assertNotSame(emptyMatrix, transposed);
        assertFalse(emptyMatrix.equals(transposed));

        transposed = transposed.transpose();
        assertNotSame(emptyMatrix, transposed);
        assertEquals(emptyMatrix, transposed);
    }

    @Test
    public void testTranspose() {
        int m = 2;
        int n = 3;
        Matrix x = factory.createMatrix(m, n);
        int count = 1;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) x.set(i, j, count++);
        checkTranspose(x);

        checkTranspose(SimpleMatrix.eye(5));
        checkTranspose(new SimpleMatrix(4, 4));
    }

    @Test
    public void testEye() {
        int m = 3;
        int n = 5;
        Matrix x = factory.createMatrix(m, n);
        int count = 1;
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++) x.set(i, j, count++);

        Matrix res = x.multiply(factory.eye(n));
        assertEquals(x, res);
    }

    @Test
    public void testAddColumn() {
        Matrix x = factory.createMatrix(2, 3, 11, 12, 13, 14, 15, 16);
        addOnesAndCheck(x);
        addOnesAndCheck(x.transpose());
    }

    @Test
    public void testGetVector() {
        Matrix x = factory.createMatrix(2, 3, 11, 12, 13, 14, 15, 16);
        getVectorAndCheck(x, Vector.VectorType.ROW, 1);
        getVectorAndCheck(x, Vector.VectorType.COLUMN, 2);

        x = x.transpose();
        getVectorAndCheck(x, Vector.VectorType.ROW, 0);
        getVectorAndCheck(x, Vector.VectorType.COLUMN, 1);
    }

    private void getVectorAndCheck(Matrix x, Vector.VectorType type, int idx) {
        Vector v;
        if (type == Vector.VectorType.COLUMN) {
            v = x.getColumn(idx);
            for (int i = 1; i < v.length(); i++)
                assertEquals(x.get(i, idx), v.get(i), 0);
        } else {
            v = x.getRow(idx);
            for (int i = 1; i < v.length(); i++)
                assertEquals(x.get(idx, i), v.get(i), 0);
        }

        double[] values = v.asArray();
        for (int i = 0; i < values.length; i++)
            assertEquals(v.get(i), values[i], 0);
    }

    private void checkTranspose(Matrix x) {
        Matrix t = x.transpose();
        for (int i = 0; i < x.numRows(); i++)
            for (int j = 0; j < x.numColumns(); j++)
                assertEquals(x.get(i, j), t.get(j, i), 0);

        if (x.numRows() == x.numColumns()) {
            boolean elementsEqual = true;
            for (int i = 0; i < x.numRows(); i++)
                for (int j = 0; j < x.numColumns(); j++)
                    if (x.get(i, j) != t.get(i, j)) {
                        elementsEqual = false;
                        break;
                    }

            assertEquals(elementsEqual, x.equals(t));
        }

        t = t.transpose();
        for (int i = 0; i < x.numRows(); i++)
            for (int j = 0; j < x.numColumns(); j++)
                assertEquals(x.get(i, j), t.get(i, j), 0);
        assertEquals(x, t);
    }

    private void addOnesAndCheck(Matrix src) {
        int m = src.numRows();
        int n = src.numColumns();

        Matrix res = src.addOnesColumn();
        System.out.println("Source: " + src.toString("%.0f\t"));
        System.out.println("Res: " + res.toString("%.0f\t"));

        assertEquals(m, res.numRows());
        assertEquals(n + 1, res.numColumns());

        for (int i = 0; i < m; i++)
            for (int j = 0; j < n + 1; j++)
                assertEquals(j == 0 ? 1 : src.get(i, j - 1), res.get(i, j), 0);
    }
}
