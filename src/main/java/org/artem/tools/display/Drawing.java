package org.artem.tools.display;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 12/19/15
 */
public class Drawing {

    private static class DrawingMotion {
        private enum MotionType {JUMP, SCRIPE}

        public final long timeMillis;
        public final MotionType type;
        public final int x;
        public final int y;

        public DrawingMotion(long timeMillis, MotionType type, int x, int y) {
            this.timeMillis = timeMillis;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    private List<DrawingMotion> motions;

    private boolean isRecording;
    private long recordingStartTime;
    private Graphics2D graphics;
    private int minX, maxX, minY, maxY;

    public void starRecording(Graphics2D garphics) {
        assert !isRecording;
        isRecording = true;
        recordingStartTime = System.currentTimeMillis();
        this.graphics = garphics;
        motions = new ArrayList<>();
        minX = Integer.MAX_VALUE;
        maxX = 0;
        minY = Integer.MAX_VALUE;
        maxY = 0;
    }

    public void jumpTo(int x, int y) {
        assert isRecording;
        motions.add(new DrawingMotion(System.currentTimeMillis() - recordingStartTime, DrawingMotion.MotionType.JUMP, x, y));
        updateMinMax(x, y);
    }

    public void scripeTo(int x, int y) {
        assert isRecording;
        assert !motions.isEmpty();
        motions.add(new DrawingMotion(System.currentTimeMillis() - recordingStartTime, DrawingMotion.MotionType.SCRIPE, x, y));
        updateMinMax(x, y);
        if (graphics != null) {
            DrawingMotion last = motions.get(motions.size() - 1);
            DrawingMotion prev = motions.get(motions.size() - 2);
            graphics.drawLine(prev.x, prev.y, last.x, last.y);
        }
    }

    private void updateMinMax(int x, int y) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
    }

    public void stopRecording() {
        assert isRecording;
        isRecording = false;
        graphics = null;
    }

    public double[][] toPixels(int width, int height) {
        assert !isRecording;
        double scale = Math.max((double) (maxX - minX + 1) / width, (double) (maxY - minY + 1) / height);


        double[][] res = new double[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++) {
                Rectangle2D.Double rectangle = new Rectangle2D.Double(minX + scale * i, minY + scale * j, scale, scale);
                res[i][j] = crosses(rectangle) ? 255 : 0;
            }
        return antiAlias(res);
    }

    private boolean crosses(Rectangle2D rectangle) {
        for (int i = 0; i < motions.size(); i++) {
            DrawingMotion curr = motions.get(i);
            if (curr.type == DrawingMotion.MotionType.SCRIPE) {
                DrawingMotion prev = motions.get(i - 1);
                if (rectangle.intersectsLine(prev.x, prev.y, curr.x, curr.y))
                    return true;
            }
        }
        return false;
    }

    private double[][] antiAlias(double[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        double[][] res = new double[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixels[i][j] != 0d) {
                    res[i][j] = pixels[i][j];
                } else {
                    double bordersSum = 0;
                    if (i > 0) bordersSum += pixels[i - 1][j];
                    if (i < width - 1) bordersSum += pixels[i + 1][j];
                    if (j > 0) bordersSum += pixels[i][j - 1];
                    if (j < height - 1) bordersSum += pixels[i][j + 1];

                    res[i][j] = bordersSum / 4;
                }
            }
        }
        return res;
    }
}
