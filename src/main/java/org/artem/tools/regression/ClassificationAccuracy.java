package org.artem.tools.regression;

import org.artem.tools.vector.Vector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 10/18/15
 */
public class ClassificationAccuracy {

    private int count;
    private int correctCount;
    private Set<Integer> trainedLabels;

    public ClassificationAccuracy(Integer[] trainedLabels) {
        this.trainedLabels = new HashSet<>();
        this.trainedLabels.addAll(Arrays.asList(trainedLabels));
    }

    public void add(Vector x, int expectedLabel, int actualLabel) {
        if (trainedLabels.contains(expectedLabel)) {
            count++;
            if (expectedLabel == actualLabel) correctCount++;
        }
    }

    public double getCrossLabelAccuracy() {
        return (double)correctCount / count;
    }
}
