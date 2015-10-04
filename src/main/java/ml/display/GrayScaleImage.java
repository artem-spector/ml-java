package ml.display;

import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.util.Arrays;

/**
 * Presents an array of double values as an image of given dimensions in gray scale,
 * so that the minimal value is shown white, and the maximal value is shown black.
 *
 * @author artem
 *         Date: 9/13/15
 */
public class GrayScaleImage extends JPanel {

    private static final ColorSpace linearRGB = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

    private float pixels[][][];
    private int width;
    private int height;
    private int pixelSize;

    public GrayScaleImage(double[] values, int width, int height, int pixelSize) {
        this.width = width;
        this.height = height;
        this.pixelSize = pixelSize;
        setMinimumSize(new Dimension(width * this.pixelSize, height * this.pixelSize));
        setPreferredSize(getMinimumSize());

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double value : values) {
            min = Math.min(min, value);
            max = Math.max(max, value);
        }
        double scale = max - min;

        pixels = new float[width][height][3];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                float intensity = 1f - (float) ((values[i * width + j] - min) / scale);
                Arrays.fill(pixels[i][j], intensity);
            }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Shape pixelShape = new Rectangle(i * pixelSize, j * pixelSize, pixelSize, pixelSize);
                Color gray = new Color(linearRGB, pixels[i][j], 1f);
                g.setColor(gray);
                g2.fill(pixelShape);
            }
        }
    }
}
