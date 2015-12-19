package org.artem.apps.mnist;

import org.artem.tools.display.Drawing;

import java.awt.geom.Point2D;

/**
 * Transformation to be applied to an image so that it would comply the MNIST dataset as described at http://yann.lecun.com/exdb/mnist/
 * <br/>
 * The original black and white (bilevel) images from NIST were size normalized to fit in a 20x20 pixel box while preserving their aspect ratio.
 * The resulting images contain grey levels as a result of the anti-aliasing technique used by the normalization algorithm.
 * the images were centered in a 28x28 image by computing the center of mass of the pixels,
 * and translating the image so as to position this point at the center of the 28x28 field.
 * <br/>
 * Pixels are organized row-wise. Pixel values are 0 to 255. 0 means background (white), 255 means foreground (black).
 *
 * @author artem
 *         Date: 12/12/15
 */
public class MNISTImageTransformation {

    public static final int WIDTH = 28;
    public static final int HEIGHT = 28;

    private Drawing drawing;
    private double[][] data;

    public MNISTImageTransformation(Drawing drawing) {
        this.drawing = drawing;
    }

    public void transform() {
        data = new double[WIDTH][HEIGHT];
        double[][] pixels = drawing.toPixels(20, 20);
        Point2D.Double massCenter = calculateMassCenter(pixels);
        int shiftX = 10 - (int) Math.round(massCenter.x) + 4;
        int shiftY = 10 - (int) Math.round(massCenter.y) + 4;

        for (int i = 0; i < 28; i++) {
            int srcX = i - shiftX;
            for (int j = 0; j < 28; j++) {
                int srcY = j - shiftY;
                data[i][j] = (srcX >= 0 && srcX < 20 && srcY >= 0 && srcY < 20) ? pixels[srcX][srcY] : 0;
            }
        }
    }


    public double[] getData() {
        double[] res = new double[WIDTH * HEIGHT];
        for (int i = 0; i < WIDTH; i++)
            for (int j = 0; j < HEIGHT; j++)
                res[i * HEIGHT + j] = data[i][j];
        return res;
    }

    private Point2D.Double calculateMassCenter(double[][] pixels) {
        double totalMass = 0;
        Point2D.Double res = new Point2D.Double(0, 0);
        for (int i = 0; i < pixels.length; i++)
            for (int j = 0; j < pixels[0].length; j++) {
                double pixelMass = pixels[i][j];
                totalMass += pixelMass;
                res.setLocation(res.x + pixelMass * i, res.y + pixelMass * j);
            }

        res.setLocation(res.x / totalMass, res.y / totalMass);
        return res;
    }

}
