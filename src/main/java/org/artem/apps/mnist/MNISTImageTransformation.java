package org.artem.apps.mnist;

import org.artem.tools.display.Drawing;

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

        for (int i = 0; i < 28; i++)
            for (int j = 0; j < 28; j++)
                if (i >= 4 && i < 24 && j >= 4 && j < 24)
                    data[i][j] = pixels[i - 4][j - 4];
                else
                    data[i][j] = 0;
    }


    public double[] getData() {
        double[] res = new double[WIDTH * HEIGHT];
        for (int i = 0; i < WIDTH; i++)
            for (int j = 0; j < HEIGHT; j++)
                res[i * HEIGHT + j] = data[i][j];
        return res;
    }

}
