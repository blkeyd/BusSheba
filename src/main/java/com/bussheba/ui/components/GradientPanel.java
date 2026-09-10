package com.bussheba.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel filled with a diagonal (top-left to bottom-right) two-color
 * gradient. Used for the "info" side of split cards (e.g. LoginFrame's
 * left half) to match BusSheba's purple/indigo accent color.
 */
public class GradientPanel extends JPanel {

    private final Color startColor;
    private final Color endColor;

    public GradientPanel(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gradient = new GradientPaint(0, 0, startColor, getWidth(), getHeight(), endColor);
        g2.setPaint(gradient);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
        // Deliberately no super.paintComponent(g) call here — we've already
        // painted every pixel ourselves. Calling it would let the default
        // JPanel painting overwrite our gradient with a flat background fill.
    }
}
