package org.artem.tools.display;

import org.artem.tools.FunctionEvaluator;
import org.math.plot.Plot2DPanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/5/15
 */
public class DisplayUtil {

    public Plot2DPanel createPlotPanel(String xLabel, String yLabel, Dimension preferredSize) {
        Plot2DPanel plot2DPanel = new Plot2DPanel("North");
        plot2DPanel.setPreferredSize(preferredSize != null ? preferredSize : new Dimension(600, 400));
        if (xLabel != null) {
            plot2DPanel.setAxisLabel(0, xLabel);
        }
        if (yLabel != null) {
            plot2DPanel.setAxisLabel(1, yLabel);
        }
        return plot2DPanel;
    }

    public JPanel createImageGrid(double[][] data, int imgWidth, int imgHeight, int pixelSize) {
        int numCells = data.length;
        int numRows = (int) Math.sqrt(numCells);
        int numCols = numCells / numRows;
        if (numRows * numCols < numCells) numRows++;

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(numRows, numCols, 2, 2));
        for (double[] sample : data) {
            panel.add(new GrayScaleImage(sample, imgWidth, imgHeight, pixelSize));
        }

        return panel;
    }

    public double[][] getFunctionPoints(double fromX, double toX, int numPoints, FunctionEvaluator function) {
        double step = (toX - fromX) / numPoints;

        double[][] res = new double[numPoints][2];

        for (int i = 0; i < numPoints; i++) {
            res[i][0] = fromX + step * i;
            res[i][1] = function.eval(res[i][0]);
        }

        return res;
    }

    public double[][] getContourPoints(GridDimension xGrid, GridDimension yGrid, ContourFunction f) {
        double stepX = (xGrid.max - xGrid.min) / xGrid.numSteps;
        double stepY = (yGrid.max - yGrid.min) / yGrid.numSteps;

        java.util.List<double[]> contourPoints = new ArrayList<>();
        for (int i = 0; i < xGrid.numSteps; i++) {
            double x = xGrid.min + stepX * i;
            for (int j = 0; j < yGrid.numSteps; j++) {
                double y = yGrid.min + stepY * j;
                if (f.isOnContour(x, y))
                    contourPoints.add(new double[]{x, y});
            }
        }

        ArrayList<double[]> sorted = new ArrayList<>(contourPoints.size());
        sorted.add(contourPoints.remove(0));
        boolean forwardDirection = true;
        double maxGap = 0;
        while (!contourPoints.isEmpty()) {
            double[] curr = forwardDirection ? sorted.get(sorted.size() - 1) : sorted.get(0);

            Collections.sort(contourPoints, (double[] p1, double[] p2) -> {
                double res = distance(curr, p1) - distance(curr, p2);
                return res == 0 ? 0 : res < 0 ? -1 : 1;
            });

            double gap = distance(curr, contourPoints.get(0));
            if (maxGap == 0 || gap <= maxGap) {
                maxGap = gap;
            } else {
                maxGap = gap;
                forwardDirection = !forwardDirection;
                continue;
            }

            double[] point = contourPoints.remove(0);
            if (forwardDirection)
                sorted.add(point);
            else
                sorted.add(0, point);
        }

        return sorted.toArray(new double[sorted.size()][2]);
    }

    private double distance(double[] p1, double[] p2) {
        return Math.pow((p1[0] - p2[0]), 2) + Math.pow((p1[1] - p2[1]), 2);
    }

    public void pause() {
        System.out.print("Press Enter to continue:");
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            //
        }
    }

    public JFrame showFrame(String name, JPanel contentPanel) {
        JFrame frame = new JFrame(name);
        frame.setContentPane(contentPanel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        return frame;
    }

    public void closeFrame(JFrame frame) {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void saveImage(JPanel contentPanel, String file) throws IOException {
        JFrame frame = new JFrame("");
        frame.setContentPane(contentPanel);
        frame.pack();

        BufferedImage image = new BufferedImage(contentPanel.getWidth(), contentPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        contentPanel.printAll(graphics);
        graphics.dispose();

        ImageIO.write(image, "png", new File(file));
    }
}
