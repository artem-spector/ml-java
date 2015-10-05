package org.artem.tools.display;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 9/28/15
 */
public class GridDimension {

    public final double min;
    public final double max;
    public final int numSteps;

    public GridDimension(double min, double max, int numSteps) {
        this.min = min;
        this.max = max;
        this.numSteps = numSteps;
    }
}
