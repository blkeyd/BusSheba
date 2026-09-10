package com.bussheba.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A JPanel whose entire subtree (background + children) is clipped to a
 * rounded rectangle. Used so a card made of two different-colored halves
 * (e.g. a gradient left side + solid dark right side) still reads as one
 * shape with soft rounded corners, instead of each half having its own
 * square corners.
 */
public class RoundedContainer extends JPanel {

    private final int arc;

    public RoundedContainer(int arc) {
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
        super.paint(g2);
        g2.dispose();
    }
}
