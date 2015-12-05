package org.artem.tools.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 12/5/15
 */
public class FreehandDrawingPanel extends JPanel implements MouseMotionListener {

    private boolean drawingOn;
    private GeneralPath shape = new GeneralPath();

    public FreehandDrawingPanel() {
        addMouseMotionListener(this);
    }

    public void setDrawingOn(boolean drawingOn, int x, int y) {
        this.drawingOn = drawingOn;
        if (drawingOn)
            shape.moveTo(x, y);
    }

    public boolean isDrawingOn() {
        return drawingOn;
    }

    public void clear() {
        Color background = getBackground();
        Graphics2D graphics = (Graphics2D) getGraphics();
        graphics.setColor(background);
        graphics.fillRect(0, 0, getWidth(), getHeight());
        shape = new GeneralPath();
    }

    public BufferedImage getImage(int width, int height) {
        Rectangle rectangle = shape.getBounds();
        double scaleX = calculateScale(rectangle.getWidth(), width);
        double scaleY = calculateScale(rectangle.getHeight(), height);
        double scale = Math.min(scaleX, scaleY);

        AffineTransform af = new AffineTransform();
        double shiftX = ((double) width - rectangle.getWidth() * scale) / 2 - rectangle.getX() * scale;
        double shiftY = ((double) height - rectangle.getHeight() * scale) / 2 - rectangle.getY() * scale;

        af.translate(shiftX, shiftY);
        af.scale(scale, scale);
        Shape transformedShape = af.createTransformedShape(shape);

        BufferedImage img = new BufferedImage(width, height, TYPE_BYTE_GRAY);
        Graphics2D graphics = (Graphics2D) img.getGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.draw(transformedShape);

        return img;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!drawingOn) return;

        Point2D previousPoint = shape.getCurrentPoint();
        shape.lineTo(e.getX(), e.getY());
        Point2D currentPoint = shape.getCurrentPoint();

        Graphics2D graphics = (Graphics2D) getGraphics();
        graphics.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.drawLine((int) previousPoint.getX(), (int) previousPoint.getY(), (int) currentPoint.getX(), (int) currentPoint.getY());
    }

    private double calculateScale(double srcValue, int targetValue) {
        double var = targetValue;
        double res = ((double) targetValue) / srcValue;
        while (res * srcValue > targetValue)
            res = ((double) --var) / srcValue;
        return res;
    }
}
