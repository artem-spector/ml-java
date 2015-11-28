package org.artem.tools.regression;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import org.artem.tools.FunctionEvaluator;
import org.artem.tools.file.MLExternalizable;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/17/15
 */
public class Predictor implements MLExternalizable {

    private MatrixFactory matrixFactory;
    private XTransformation xTransformation;
    private Matrix theta;
    private RegressionModel model;

    public Predictor(MatrixFactory matrixFactory, RegressionModel model, XTransformation xTransformation, Matrix theta) {
        this.matrixFactory = matrixFactory;
        this.xTransformation = xTransformation;
        this.theta = theta;
        this.model = model;
    }

    Predictor(MatrixFactory matrixFactory) {
        this(matrixFactory, null, null, null);
    }

    public Matrix getTheta() {
        return theta;
    }

    public double predict(FunctionEvaluator f, double... x) {
        return predict(f, matrixFactory.createMatrix(new double[][]{x})).get(0, 0);
    }

    public Matrix predict(FunctionEvaluator f, Matrix X) {
        Matrix transformed = xTransformation.transform(X);
        assert transformed.numColumns() == theta.numRows();

        Matrix h = model.calculateHypothesis(theta, transformed);
        return f == null ? h : h.applyFunction(f);
    }

    public double getPredictionAccuracy(FunctionEvaluator f, Matrix X, Matrix expectedY) {
        Matrix predictedY = predict(f, X);
        double correctPredictions = predictedY.subtract(expectedY).applyFunction(x -> x == 0 ? 1 : 0).getColumn(0).sum();
        return correctPredictions / X.numRows();
    }

    @Override
    public void toMLData(String prefix, Collection<MLArray> out) {
        out.add(new MLChar(prefix + "model", model.getClass().getName()));

        double[][] data = new double[theta.numRows()][theta.numColumns()];
        for (int i = 0; i < theta.numRows(); i++)
            for (int j = 0; j < theta.numColumns(); j++)
                data[i][j] = theta.get(i, j);
        out.add(new MLDouble(prefix + "theta", data));

        xTransformation.toMLData(prefix + "xTransformation:", out);
    }

    @Override
    public void fromMLData(String prefix, Map<String, MLArray> in) throws IOException {
        try {
            MLArray arr = in.get(prefix + "model");
            String className = ((MLChar) arr).getString(0);
            model = (RegressionModel) Class.forName(className).newInstance();

            arr = in.get(prefix + "theta");
            theta = matrixFactory.createMatrix(((MLDouble) arr).getArray());

            xTransformation = new XTransformation();
            xTransformation.fromMLData(prefix + "xTransformation:", in);
        } catch (Throwable e) {
            throw new IOException("Failed to read Predictor from Matlab data file", e);
        }
    }
}
