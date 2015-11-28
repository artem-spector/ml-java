package org.artem.apps.mnist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 11/28/15
 */
public class RecognizerUI implements MouseListener, MouseMotionListener {

    private JFrame frame;
    private JPanel contentPanel = new JPanel();
    private JPanel instructionsPanel = new JPanel();
    private JPanel controlsPanel = new JPanel();
    private JButton okButton = new JButton("OK");
    private JPanel drawingPanel = new JPanel();

    private boolean drawing;
    private Point previousPoint;

    public RecognizerUI() {
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(instructionsPanel, BorderLayout.NORTH);
        contentPanel.add(controlsPanel, BorderLayout.SOUTH);
        contentPanel.add(drawingPanel, BorderLayout.CENTER);

        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.add(okButton, BorderLayout.CENTER);

        instructionsPanel.setLayout(new BorderLayout());
        instructionsPanel.add(new JLabel("Click to toggle drawing. Press OK when done."), BorderLayout.CENTER);

        drawingPanel.setPreferredSize(new Dimension(500, 500));
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseListener(this);
        drawingPanel.addMouseMotionListener(this);
    }

    public void show() {
        frame = new JFrame("Draw a digit");
        frame.setContentPane(contentPanel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    public void close() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        drawing = !drawing;
        previousPoint = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (drawing) {
            Graphics graphics = drawingPanel.getGraphics();
            ((Graphics2D) graphics).setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics.drawLine(previousPoint.x, previousPoint.y, e.getX(), e.getY());
            previousPoint = e.getPoint();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }
}
