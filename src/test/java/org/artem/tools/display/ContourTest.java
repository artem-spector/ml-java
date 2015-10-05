package org.artem.tools.display;

import org.junit.Test;
import org.math.plot.Plot2DPanel;

import java.awt.*;
import java.io.IOException;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/28/15
 */
public class ContourTest {

    private static DisplayUtil disp = new DisplayUtil();

    @Test
    public void testContours() throws IOException {
        Plot2DPanel panel = disp.createPlotPanel("x", "y", new Dimension(600, 600));

        GridDimension gridDimension = new GridDimension(-1, 1, 1000);
        double[][] points = disp.getContourPoints(gridDimension, gridDimension, (x, y) -> Math.abs(x * x + y * y - 1) <= 5e-4);
        panel.addLinePlot("x^2 + y^2 = 1", points);

        gridDimension = new GridDimension(-2, 2, 1000);
        points = disp.getContourPoints(gridDimension, gridDimension, (x, y) -> Math.abs(x * x - y) <= 5e-4);
        panel.addLinePlot("y = x^2", points);

        gridDimension = new GridDimension(-2, 2, 1000);
        points = disp.getContourPoints(gridDimension, gridDimension, (x, y) -> Math.abs(-x * x - y) <= 5e-4);
        panel.addLinePlot("y = -x^2", points);

        gridDimension = new GridDimension(-2, 2, 1000);
        points = disp.getContourPoints(gridDimension, gridDimension, (x, y) -> Math.abs(y * y - x) <= 5e-4);
        panel.addLinePlot("x = y^2", points);

        gridDimension = new GridDimension(-2, 2, 1000);
        points = disp.getContourPoints(gridDimension, gridDimension, (x, y) -> Math.abs(-y * y - x) <= 5e-4);
        panel.addLinePlot("x = -y^2", points);

        disp.saveImage(panel, "./target/testContours.png");
    }

}
