package ml;

import ml.vector.Matrix;
import ml.vector.MatrixFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/15/15
 */
public class DataLoadUtil {

    public Matrix readCSV(String fileName, MatrixFactory factory) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(fileName));

            int m = 0;
            int n = 0;
            double[] data = new double[0];

            String line = in.readLine();
            if (line == null) return null;
            String delimiters = ", \t";
            StringTokenizer tokenizer = new StringTokenizer(line, delimiters);
            while(tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
                n++;
            }

            if (n == 0) return null;
            do {
                double[] row = parseRow(new StringTokenizer(line, delimiters), n);
                double[] temp = new double[data.length + n];
                System.arraycopy(data, 0, temp, 0, data.length);
                System.arraycopy(row, 0, temp, data.length, n);
                data = temp;
                m++;
                line = in.readLine();
            } while (line != null);

            return factory.createMatrix(m, n, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private double[] parseRow(StringTokenizer tokenizer, int n) {
        double[] row =new double[n];
        for (int j = 0; j < n; j++) row[j] = Double.parseDouble(tokenizer.nextToken());
        return row;
    }
}
