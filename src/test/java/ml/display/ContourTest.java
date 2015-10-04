package ml.display;

import org.junit.Test;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/28/15
 */
public class ContourTest {

    @Test
    public void testContours() {
        DisplayUtil disp = new DisplayUtil();
        disp.createPlotPanel("x", "y");

        GridDimension gridDimension = new GridDimension(-1, 1, 1000);
        disp.addPlotContour("circle 1", gridDimension, gridDimension, (x, y) -> Math.abs(x * x + y * y - 1) <= 5e-4);

        gridDimension = new GridDimension(-2, 2, 1000);
        disp.addPlotContour("y=x^2", gridDimension, gridDimension, (x, y) -> Math.abs(x * x - y) <= 5e-4);

        gridDimension = new GridDimension(-2, 2, 1000);
        disp.addPlotContour("y=-x^2", gridDimension, gridDimension, (x, y) -> Math.abs(-x * x - y) <= 5e-4);

        gridDimension = new GridDimension(-2, 2, 1000);
        disp.addPlotContour("x=y^2", gridDimension, gridDimension, (x, y) -> Math.abs(y * y - x) <= 5e-4);

        gridDimension = new GridDimension(-2, 2, 1000);
        disp.addPlotContour("x=-y^2", gridDimension, gridDimension, (x, y) -> Math.abs(-y * y - x) <= 5e-4);

        disp.saveImage("./target/testContours.png");
    }

}
