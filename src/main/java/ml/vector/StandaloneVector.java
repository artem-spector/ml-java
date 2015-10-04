package ml.vector;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/1/15
 */
public class StandaloneVector implements Vector {

    private VectorType type;
    private double[] data;

    public StandaloneVector(VectorType type, double[] data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public VectorType type() {
        return type;
    }

    @Override
    public int length() {
        return data.length;
    }

    @Override
    public double get(int pos) {
        return data[pos];
    }

    @Override
    public void set(int pos, double value) {
        data[pos] = value;
    }

    @Override
    public double[] asArray() {
        return data;
    }
}
