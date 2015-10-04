package ml.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/30/15
 */
public class EmbeddedVector implements Vector {

    private VectorType type;
    private MatrixImpl matrix;
    private int idx;

    EmbeddedVector(VectorType type, int idx, MatrixImpl matrix) {
        assert idx >= 0;
        assert type == VectorType.ROW && idx < matrix.m || type == VectorType.COLUMN && idx < matrix.n;

        this.matrix = matrix;
        this.type = type;
        this.idx = idx;
    }

    @Override
    public VectorType type() {
        return type;
    }

    @Override
    public int length() {
        return type == VectorType.ROW ? matrix.n : matrix.m;
    }

    @Override
    public double get(int pos) {
        return matrix.data[dataIndex(pos)];
    }

    @Override
    public void set(int pos, double value) {
        matrix.data[dataIndex(pos)] = value;
    }

    @Override
    public double[] asArray() {
        double[] res = new double[length()];

        if (type == VectorType.ROW && matrix.orderedByRows)
            System.arraycopy(matrix.data, idx * matrix.n, res, 0, matrix.n);
        else if (type == VectorType.COLUMN && !matrix.orderedByRows)
            System.arraycopy(matrix.data, idx * matrix.m, res, 0, matrix.m);
        else
            for (int i = 0; i < res.length; i++) res[i] = matrix.data[dataIndex(i)];

        return res;
    }

    private int dataIndex(int pos) {
        assert pos >= 0 && pos < length();
        int row = type == VectorType.ROW ? this.idx : pos;
        int col = type == VectorType.ROW ? pos : this.idx;
        return matrix.indexOf(row, col);
    }
}

