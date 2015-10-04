package ml.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/30/15
 */
public class EmbeddedVector implements Vector {

    private VectorType type;
    private SimpleMatrix matrix;
    private int idx;

    EmbeddedVector(VectorType type, int idx, SimpleMatrix matrix) {
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
        int row = type == VectorType.ROW ? idx : pos;
        int col = type == VectorType.ROW ? pos : idx;
        return matrix.data[row][col];
    }

    @Override
    public double[] asArray() {
        int length = length();
        double[] res = new double[length];
        for (int i = 0; i < length; i++) res[i] = get(i);
        return res;
    }
}

