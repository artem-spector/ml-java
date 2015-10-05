package org.artem.tools.matrix;

import org.artem.tools.regression.PolynomialFeatures;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/2/15
 */
public class PolynomsTest {

    @Test
    public void testNumPolynoms() {
        PolynomialFeatures polynomialFeatures = new PolynomialFeatures(new int[]{0, 1}, 6);
        System.out.println(polynomialFeatures);
        assertEquals(25, polynomialFeatures.numPolynoms());
    }

    @Test
    public void testCalculatePolynoms() {
        PolynomialFeatures polynomialFeatures = new PolynomialFeatures(new int[]{0, 1}, 2);
        assertEquals(3, polynomialFeatures.numPolynoms());
        double[] src = new double[] {1, 2};
        double[] target = new double[src.length + polynomialFeatures.numPolynoms()];
        System.arraycopy(src, 0, target, 0, src.length);
        polynomialFeatures.calculatePolynomialFeatures(target, 0, src.length);
        assertEquals(src[0], target[0], 0);
        assertEquals(src[1], target[1], 0);
        assertEquals(src[0]*src[0], target[2], 0);
        assertEquals(src[0]*src[1], target[3], 0);
        assertEquals(src[1]*src[1], target[4], 0);
    }
}
