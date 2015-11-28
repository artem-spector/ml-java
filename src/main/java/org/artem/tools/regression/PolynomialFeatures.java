package org.artem.tools.regression;

import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt64;
import org.artem.tools.file.MLExternalizable;

import java.io.IOException;
import java.util.*;

/**
 * Polynoms for the given features and degree. Don't include the features themselves.
 *
 * @author artem
 *         Date: 9/27/15
 */
public class PolynomialFeatures implements MLExternalizable {

    private int[] srcIdx;
    private int degree;
    private int[][] polynoms;

    public PolynomialFeatures(int[] srcIdx, int degree) {
        assert srcIdx.length >= 2 && degree >= srcIdx.length;
        this.srcIdx = srcIdx;
        this.degree = degree;

        List<int[]> res = new ArrayList<>();
        res.add(new int[srcIdx.length]);
        fillLastRow(res, 0, degree);

        Iterator<int[]> iter = res.iterator();
        while (iter.hasNext()) {
            int[] row = iter.next();
            int sum = 0;
            for (int pow : row) sum += pow;
            if (sum <= 1) iter.remove();
        }
        polynoms = res.toArray(new int[res.size()][]);
    }

    PolynomialFeatures() {
    }

    int[] getSrcIdx() {
        return srcIdx;
    }

    public int numPolynoms() {
        return polynoms.length;
    }

    private void fillLastRow(List<int[]> allRows, int fromIndex, int degree) {
        int[] lastRow = allRows.get(allRows.size() - 1);
        lastRow[fromIndex] = degree;

        for (int i = degree - 1; i >= 0; i--) {
            int[] newRow = new int[lastRow.length];
            System.arraycopy(lastRow, 0, newRow, 0, fromIndex);
            newRow[fromIndex] = i;
            allRows.add(newRow);
            if (fromIndex < lastRow.length - 1) {
                fillLastRow(allRows, fromIndex + 1, degree - i);
            }
        }
    }

    public void calculatePolynomialFeatures(double[] data, int srcStart, int srcLen) {
        for (int i = 0; i < polynoms.length; i++) {
            double val = 1;
            for (int j = 0; j < polynoms[i].length; j++) {
                val *= Math.pow(data[srcStart + srcIdx[j]], polynoms[i][j]);
            }
            data[srcStart + srcLen + i] = val;
        }
    }

    @Override
    public void toMLData(String prefix, Collection<MLArray> out) {
        long[][] longValues = new long[srcIdx.length][1];
        for (int i = 0; i < srcIdx.length; i++) longValues[i][0] = srcIdx[i];
        out.add(new MLInt64(prefix + "srcIdx", longValues));

        out.add(new MLInt64(prefix + "degree", new long[][]{{degree}}));

        longValues = new long[polynoms.length][polynoms[0].length];
        for (int i = 0; i < polynoms.length; i++)
            for (int j = 0; j < polynoms[i].length; j++)
                longValues[i][j] = polynoms[i][j];
        out.add(new MLInt64(prefix + "polynoms", longValues));
    }

    @Override
    public void fromMLData(String prefix, Map<String, MLArray> in) throws IOException {
        try {
            MLArray arr = in.get(prefix + "srcIdx");
            long[][] longValues = ((MLInt64) arr).getArray();
            srcIdx = new int[longValues.length];
            for (int i = 0; i < longValues.length; i++) srcIdx[i] = (int) longValues[i][0];

            arr = in.get(prefix + "degree");
            degree = (int) (long) ((MLInt64)arr).get(0, 0);

            arr = in.get(prefix + "polynoms");
            longValues = ((MLInt64)arr).getArray();
            polynoms = new int[longValues.length][longValues[0].length];
            for (int i = 0; i < polynoms.length; i++)
                for (int j = 0; j < polynoms[i].length; j++)
                    polynoms[i][j] = (int) longValues[i][j];
        } catch (Throwable e) {
            throw new IOException("Failed to read PolynomialFeatures from Matlab data file", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("Polynoms of ")
                .append(srcIdx.length).append(" features ").append(degree).append(" degree:\n");
        for (int[] row : polynoms) {
            for (int pow : row)
                res.append(pow).append("\t");
            res.append("\n");
        }

        return res.toString();
    }
}
