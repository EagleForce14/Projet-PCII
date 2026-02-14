package view;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * Panneau d'affichage du champ, compose d'une grille d'images.
 */
public class FieldPanel extends JPanel {
    private static final int ROWS = 6;
    private static final int COLS = 10;
    private static final int PREF_WIDTH = 900;
    private static final int PREF_HEIGHT = 540;
    private static final int INNER_PADDING = 96;

    private final Image tileImage;

    /**
     * Initialise le champ et charge l'image d'une parcelle.
     */
    public FieldPanel() {
        this.tileImage = ImageLoader.load("/assets/Terre.png");
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
    }

    /**
     * Dessine la grille (avec les images).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int availableWidth = Math.max(0, getWidth() - 2 * INNER_PADDING);
        int availableHeight = Math.max(0, getHeight() - 2 * INNER_PADDING);
        int tileW = availableWidth / COLS;
        int tileH = availableHeight / ROWS;
        int tileSize = Math.min(tileW, tileH);
        int gridW = tileSize * COLS;
        int gridH = tileSize * ROWS;
        int startX = (getWidth() - gridW) / 2;
        int startY = (getHeight() - gridH) / 2;

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int x = startX + c * tileSize;
                int y = startY + r * tileSize;
                if (tileImage != null) {
                    g2.drawImage(tileImage, x, y, tileSize, tileSize, this);
                } else {
                    g2.setColor(new Color(200, 190, 170));
                    g2.fillRect(x, y, tileSize, tileSize);
                    g2.setColor(new Color(160, 150, 130));
                    g2.drawRect(x, y, tileSize, tileSize);
                }
            }
        }

        g2.dispose();
    }
}
