package org.artem.apps.mnist;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

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

    private final ColorModel colorModel;
    private BufferedImage original;
    private BufferedImage resized;
    private double[] data;

    public MNISTImageTransformation(BufferedImage original) {
        this.original = original;
        colorModel = original.getColorModel();
    }

    public void transform() {
        int minX = original.getWidth() - 1;
        int maxX = 0;
        int minY = original.getHeight() - 1;
        int maxY = 0;
        for (int i = 0; i < original.getWidth(); i++)
            for (int j = 0; j < original.getHeight(); j++)
                if (grayScale(original.getRGB(i, j)) < 255) {
                    minX = Math.min(minX, i);
                    maxX = Math.max(maxX, i);
                    minY = Math.min(minY, j);
                    maxY = Math.max(maxY, j);
                }
        Rectangle origRect = new Rectangle(minX, minY, maxX - minX +1, maxY - minY + 1);

        double scaleX = 20d / origRect.getWidth();
        double scaleY = 20d / origRect.getHeight();
        double scale = Math.min(scaleX, scaleY);

        resized = new BufferedImage(20, 20, original.getType());
        Graphics2D g = resized.createGraphics();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, resized.getWidth(), resized.getHeight());

        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1f));
        g.drawImage(original, 0, 0, (int) Math.round((origRect.getWidth() + 1) * scale), (int) Math.round((origRect.getHeight() + 1) * scale), minX, minY, maxX, maxY, null);
        g.dispose();

        data = new double[20 * 20];
        for (int i = 0; i < 20; i++)
            for (int j = 0; j < 20; j++)
                data[i * 20 + j] = grayScale(resized.getRGB(i, j));
    }

    public BufferedImage getResizedImage() {
        return resized;
    }

    public double[] getData() {
        return data;
    }

    private int grayScale(int rgb) {
        int red = colorModel.getRed(rgb);
        int green = colorModel.getGreen(rgb);
        int blue = colorModel.getBlue(rgb);

        assert red == green && red == blue;
        return red;
    }

}
