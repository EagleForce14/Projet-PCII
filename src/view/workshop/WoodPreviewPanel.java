package view.workshop;

import view.ProductPixelArt;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Aperçu dédié à la ressource bois dans la menuiserie.
 */
public final class WoodPreviewPanel extends JPanel {
    private static final Color PREVIEW_FILL = new Color(56, 40, 28, 210);
    private static final Color PREVIEW_BORDER = new Color(138, 110, 73, 180);

    public WoodPreviewPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(118, 102));
        setMinimumSize(new Dimension(118, 102));
        setMaximumSize(new Dimension(118, 102));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(PREVIEW_FILL);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2d.setColor(PREVIEW_BORDER);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

        int pixelSize = 10;
        int artWidth = ProductPixelArt.getWoodArtWidth(pixelSize);
        int artHeight = ProductPixelArt.getWoodArtHeight(pixelSize);
        int artX = (getWidth() - artWidth) / 2;
        int artY = (getHeight() - artHeight) / 2;
        ProductPixelArt.drawWoodResource(g2d, artX, artY, pixelSize);

        g2d.dispose();
    }
}
