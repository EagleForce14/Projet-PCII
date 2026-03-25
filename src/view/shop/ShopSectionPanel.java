package view.shop;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * Panneau de base pour le background des différentes zones de la boutique.
 */
public class ShopSectionPanel extends JPanel {
    private static final Color BORDER = new Color(230, 214, 157, 215);
    private static final Color BORDER_SOFT = new Color(138, 110, 73, 180);
    private static final Color PANEL_TINT = new Color(44, 31, 22, 232);
    private static final Color PANEL_SHADOW = new Color(0, 0, 0, 90);

    private final Image woodTexture;

    public ShopSectionPanel(Image woodTexture) {
        this.woodTexture = woodTexture;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // La structure du panneau reste tres simple:
        // une ombre, une masse sombre, puis une double bordure.
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 22, 22);

        g2d.setColor(PANEL_TINT);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

        if (woodTexture != null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.12f));
            g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        g2d.setColor(BORDER);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

        g2d.setColor(BORDER_SOFT);
        g2d.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
        g2d.dispose();
    }
}
