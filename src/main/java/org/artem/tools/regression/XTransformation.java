package org.artem.tools.regression;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLInt64;
import org.artem.tools.file.MLExternalizable;
import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.Statistics;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines the transformations to be done to the X matrix before feeding it to regression or prediction
 *
 * @author artem
 *         Date: 10/17/15
 */
public class XTransformation implements MLExternalizable {

    private boolean addBiasColumn;
    private Statistics[] normalization;
    private PolynomialFeatures polynomialFeatures;

    XTransformation() {
    }

    public XTransformation(boolean addBiasColumn, Statistics[] normalization, PolynomialFeatures polynomialFeatures) {
        this.addBiasColumn = addBiasColumn;
        this.normalization = normalization;
        this.polynomialFeatures = polynomialFeatures;
    }

    public Matrix transform(Matrix X) {
        Matrix res = X;
        if (polynomialFeatures != null) {
            int[] sourceIndexes = polynomialFeatures.getSrcIdx();
            assert sourceIndexes.length <= X.numColumns();
            Set<Integer> repeating = new HashSet<>();
            for (int idx : sourceIndexes) {
                assert idx >= 0 && idx < X.numColumns();
                assert !repeating.contains(idx);
                repeating.add(idx);
            }

            res = res.addPlynomialFeatures(polynomialFeatures);
        }

        if (normalization != null) {
            assert normalization.length == X.numColumns();
            res = res.normalize(normalization);
        }

        if (addBiasColumn) res = res.addOnesColumn();
        return res;
    }

    public Matrix transform(MatrixFactory matrixFactory, double... x) {
        return transform(matrixFactory.createMatrix(new double[][]{x}));
    }

    @Override
    public void toMLData(String prefix, Collection<MLArray> out) {
        out.add(new MLChar(prefix + "bias", String.valueOf(addBiasColumn)));

        int normLength = normalization == null ? 0 : normalization.length;
        out.add(new MLInt64(prefix + "normLength", new long[][]{{normLength}}));
        for (int i = 0; i < normLength; i++)
            out.add(new MLDouble(prefix + "normalization:" + i,
                    new double[][]{{normalization[i].mean, normalization[i].variance, normalization[i].std, normalization[i].count}}));

        out.add(new MLChar(prefix + "polynomsNotNull", String.valueOf(polynomialFeatures != null)));
        if (polynomialFeatures != null)
            polynomialFeatures.toMLData(prefix + "polynomialFeatures", out);
    }

    @Override
    public void fromMLData(String prefix, Map<String, MLArray> in) throws IOException {
        try {
            MLArray arr = in.get(prefix + "bias");
            addBiasColumn = Boolean.parseBoolean(((MLChar)arr).getString(0));

            arr = in.get(prefix + "normLength");
            int normLength = ((MLInt64) arr).get(0).intValue();
            if (normLength > 0) {
                normalization = new Statistics[normLength];
                for (int i = 0; i < normLength; i++) {
                    MLDouble doubleArr = (MLDouble) in.get(prefix + "normalization:" + i);
                    normalization[i] = new Statistics(doubleArr.get(0), doubleArr.get(1), doubleArr.get(2), doubleArr.get(3).intValue());
                }
            }

            arr = in.get(prefix + "polynomsNotNull");
            boolean polynomsNotNull = Boolean.parseBoolean(((MLChar) arr).getString(0));
            if (polynomsNotNull) {
                polynomialFeatures = new PolynomialFeatures();
                polynomialFeatures.fromMLData(prefix + "polynomialFeatures", in);
            }

        } catch (Throwable e) {
            throw new IOException("Failed to read XTransformation from Matlab data file", e);
        }
    }
}
