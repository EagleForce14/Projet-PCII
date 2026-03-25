package view.shop;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Scrollbar pour la boutique.
 * On veut surtout le comportement de scroll, pas l'apparence Swing par defaut.
 */
public class ShopScrollBarUI extends BasicScrollBarUI {
    @Override
    protected void configureScrollBarColors() {
        thumbColor = new Color(214, 186, 111, 170);
        trackColor = new Color(55, 40, 28, 120);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(trackColor);
        g2d.fillRoundRect(trackBounds.x + 3, trackBounds.y, trackBounds.width - 6, trackBounds.height, 10, 10);
        g2d.dispose();
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(thumbColor);
        g2d.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 10, 10);
        g2d.dispose();
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createStubButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createStubButton();
    }

    private JButton createStubButton() {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        return button;
    }
}
