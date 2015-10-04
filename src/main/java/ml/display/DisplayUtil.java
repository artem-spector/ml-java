package ml.display;

import ml.FunctionEvaluator;
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

    private JFrame frame;
    private JPanel panel;

    public void createPlotPanel(String xLabel, String yLabel) {
        Plot2DPanel plot2DPanel = new Plot2DPanel("North");
        plot2DPanel.setPreferredSize(new Dimension(600, 400));
        if (xLabel != null) {
            plot2DPanel.setAxisLabel(0, xLabel);
        }
        if (yLabel != null) {
            plot2DPanel.setAxisLabel(1, yLabel);
        }
        panel = plot2DPanel;
    }

    public void displayImageGrid(double[][] data, int imgWidth, int imgHeight, int pixelSize) {
        int numCells = data.length;
        int numRows = (int) Math.sqrt(numCells);
        int numCols = numCells / numRows;
        if (numRows * numCols < numCells) numRows++;

        panel = new JPanel();
        panel.setLayout(new GridLayout(numRows, numCols, 2, 2));
        for (double[] sample : data) {
            panel.add(new GrayScaleImage(sample, imgWidth, imgHeight, pixelSize));
        }
    }

    public void addPlotFunction(String name, double fromX, double toX, FunctionEvaluator function) {
        int numPoints = 1000;
        double step = (toX - fromX) / numPoints;

        double x[] = new double[numPoints];
        double y[] = new double[numPoints];

        for (int i = 0; i < numPoints; i++) {
            x[i] = i == 0 ? fromX : x[i - 1] + step;
            y[i] = function.eval(x[i]);
        }

        ((Plot2DPanel)panel).addLinePlot(name, x, y);
    }

    public void addPlotScatter(String name, double[] x, double[] y) {
        ((Plot2DPanel) panel).addScatterPlot(name, x, y);
    }

    public void addPlotLine(String name, double[] values) {
        ((Plot2DPanel)panel).addLinePlot(name, values);
    }

    public void addPlotLine(String name, double[] x, double[] y) {
        ((Plot2DPanel)panel).addLinePlot(name, x, y);
    }

    public void addPlotContour(String name, GridDimension xGrid, GridDimension yGrid, ContourFunction f) {
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

        ((Plot2DPanel)panel).addLinePlot(name, sorted.toArray(new double[sorted.size()][2]));
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

    public void showFrame(String name) {
        JFrame frame = new JFrame(name);
        frame.setContentPane(panel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        this.frame = frame;
    }

    public void closeFrame() {
        if (frame != null) {
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
            frame = null;
            panel = null;
        }
    }

    public void saveImage(String file) {
        frame = new JFrame("");
        frame.setContentPane(panel);
        frame.pack();

        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        panel.printAll(graphics);
        graphics.dispose();

        try {
            ImageIO.write(image, "png", new File(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            frame = null;
            panel = null;
        }
    }
}
