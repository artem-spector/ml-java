package org.artem.apps.mnist;

import org.artem.tools.display.Drawing;
import org.artem.tools.display.FreehandDrawingPanel;
import org.artem.tools.display.GrayScaleImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;

/**
 * TODO: Document!
 *
 * @author artem
 *         Date: 11/28/15
 */
public class RecognizerUI implements MouseListener{

    private HandwrittenDigitsRecognizer recognizer;

    private JFrame frame;
    private JPanel contentPanel = new JPanel();
    private JPanel inputPanel = new JPanel();
    private JPanel controlsPanel = new JPanel();
    private JButton okButton = new JButton("OK");
    private JButton clearButton = new JButton("Clear");
    private FreehandDrawingPanel drawingPanel = new FreehandDrawingPanel();

    private JPanel resultPanel = new JPanel();
    private JPanel res2 = new JPanel();

    public RecognizerUI(HandwrittenDigitsRecognizer recognizer) {
        this.recognizer = recognizer;

        clearButton.addActionListener(e -> drawingPanel.clear());
        okButton.addActionListener(e -> {
            Drawing drawing = drawingPanel.getDrawing();
            drawing.stopRecording();
            this.recognizer.analyzeDrawing(drawing);
        });

        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(inputPanel, BorderLayout.CENTER);
        contentPanel.add(resultPanel, BorderLayout.EAST);

        inputPanel.setLayout(new BorderLayout());
        inputPanel.add(new JLabel("Click to toggle drawing. Press OK when done."), BorderLayout.NORTH);
        inputPanel.add(controlsPanel, BorderLayout.SOUTH);
        inputPanel.add(drawingPanel, BorderLayout.CENTER);

        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.add(okButton, BorderLayout.EAST);
        controlsPanel.add(clearButton, BorderLayout.WEST);

        drawingPanel.setPreferredSize(new Dimension(500, 500));
        drawingPanel.setBackground(Color.WHITE);
        drawingPanel.addMouseListener(this);

        resultPanel.setPreferredSize(new Dimension(500, 500));
        resultPanel.setLayout(new GridLayout(1, 2));
        resultPanel.add(res2);
    }

    public void showFrame() {
        frame = new JFrame("Draw a digit");
        frame.setContentPane(contentPanel);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        drawingPanel.clear();
    }

    public void closeFrame() {
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void showResultImage(GrayScaleImage grayScaleImage) {
        res2.setLayout(new BorderLayout());
        res2.add(grayScaleImage, BorderLayout.CENTER);
        res2.setPreferredSize(grayScaleImage.getSize());
        frame.pack();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        drawingPanel.setDrawingOn(!drawingPanel.isDrawingOn(), e.getX(), e.getY());
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

}
