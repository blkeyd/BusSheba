package com.bussheba.ui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * A JPanel that paints a background image "scale-to-cover" — the image
 * always fills the entire panel with no stretching/distortion, cropping
 * whichever dimension overflows (like CSS's background-size: cover).
 *
 * Loads the image once from the classpath (src/main/resources/...), so it
 * works both when running inside the IDE and when packaged into a jar.
 *
 * Usage: give it a null LayoutManager child hierarchy on top, or a
 * FlowLayout/GridBagLayout with transparent children, so the background
 * shows through around whatever you place on it (e.g. a floating card).
 */
public class BackgroundPanel extends JPanel {

    private BufferedImage backgroundImage;

    public BackgroundPanel(String classpathImagePath) {
        loadImage(classpathImagePath);
        setLayout(new GridBagLayout()); // centers a single child by default
    }

    private void loadImage(String classpathImagePath) {
        try {
            URL imageUrl = getClass().getResource(classpathImagePath);
            if (imageUrl == null) {
                System.err.println("BackgroundPanel: image not found on classpath: " + classpathImagePath);
                return;
            }
            backgroundImage = ImageIO.read(imageUrl);
        } catch (IOException e) {
            System.err.println("BackgroundPanel: failed to load image: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage == null) {
            // Fallback so the UI doesn't look broken if the image failed to load.
            g.setColor(new Color(24, 24, 27));
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int imgWidth = backgroundImage.getWidth();
        int imgHeight = backgroundImage.getHeight();

        double panelRatio = (double) panelWidth / panelHeight;
        double imgRatio = (double) imgWidth / imgHeight;

        int drawWidth;
        int drawHeight;

        if (imgRatio > panelRatio) {
            // Image is relatively wider than the panel — match height, crop width.
            drawHeight = panelHeight;
            drawWidth = (int) (drawHeight * imgRatio);
        } else {
            // Image is relatively taller than the panel — match width, crop height.
            drawWidth = panelWidth;
            drawHeight = (int) (drawWidth / imgRatio);
        }

        int x = (panelWidth - drawWidth) / 2;
        int y = (panelHeight - drawHeight) / 2;

        g2.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
        g2.dispose();
    }
}
