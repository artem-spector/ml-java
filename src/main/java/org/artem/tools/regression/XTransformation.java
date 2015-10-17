package org.artem.tools.regression;

import org.artem.tools.vector.Matrix;
import org.artem.tools.vector.MatrixFactory;
import org.artem.tools.vector.Statistics;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines the transformations to be done to the X matrix before feeding it to regression or prediction
 *
 * @author artem
 *         Date: 10/17/15
 */
public class XTransformation {

    private boolean addBiasColumn;
    private Statistics[] normalization;
    private PolynomialFeatures polynomialFeatures;

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
}
