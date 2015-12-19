package org.artem.tools.display;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 12/5/15
 */
public class FreehandDrawingPanel extends JPanel implements MouseMotionListener {

    private boolean drawingOn;
    private Drawing drawing;

    public FreehandDrawingPanel() {
        addMouseMotionListener(this);
    }

    public void setDrawingOn(boolean drawingOn, int x, int y) {
        this.drawingOn = drawingOn;
        if (drawingOn)
            drawing.jumpTo(x, y);
    }

    public boolean isDrawingOn() {
        return drawingOn;
    }

    public void clear() {
        Graphics2D graphics = (Graphics2D) getGraphics();
        graphics.setColor(getBackground());
        graphics.fillRect(0, 0, getWidth(), getHeight());
        graphics.setColor(getForeground());
        graphics.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        drawing = new Drawing();
        drawing.starRecording(graphics);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!drawingOn) return;
        drawing.scripeTo(e.getX(), e.getY());
    }

    public Drawing getDrawing() {
        return drawing;
    }
}
