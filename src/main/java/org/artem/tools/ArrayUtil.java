package org.artem.tools;

import java.util.Arrays;
import java.util.Random;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class ArrayUtil {

    private Random random = new Random();

    public int[] randperm(int length) {
        int[] res = new int[length];
        for (int i = 0; i < length; i++) res[i] = i+1;
        for (int i = length; i > 1; i--) {
            int j = random.nextInt(i);
            int temp = res[j];
            res[j] = res[i-1];
            res[i-1] = temp;
        }
        return res;
    }

    public static int getIndexOfMax(double[] arr) {
        double max = -Double.MAX_VALUE;
        int res = 0;
        for (int i = 0; i < arr.length; i++)
            if (arr[i] > max) {
                max = arr[i];
                res = i;
            }
        return res;
    }

    public <T> T[] selectIndexes(T[] array, int[] indexes) {
        T[] res = Arrays.copyOfRange(array, 0, indexes.length);
        for (int i = 0; i < indexes.length; i++) {
            res[i] = array[indexes[i]];
        }
        return res;
    }
}
