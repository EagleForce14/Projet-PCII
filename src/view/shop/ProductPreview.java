package view.shop;

import model.shop.Product;
import view.ProductPixelArt;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Petite preview d'un produit dans la colonne de droite.
 * Elle reste volontairement sobre: un cadre, puis l'illustration pixel-art.
 */
public class ProductPreview extends JPanel {
    private static final Color PREVIEW_FILL = new Color(56, 40, 28, 210);
    private static final Color PREVIEW_BORDER = new Color(138, 110, 73, 180);

    private Product product;

    public ProductPreview() {
        setOpaque(false);
        setPreferredSize(new Dimension(88, 88));
        setMinimumSize(new Dimension(88, 88));
    }

    public void setProduct(Product product) {
        this.product = product;
        repaint();
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

        if (product != null) {
            int pixelSize = 8;
            int artSize = 5 * pixelSize;
            int artX = (getWidth() - artSize) / 2;
            int artY = (getHeight() - artSize) / 2;
            ProductPixelArt.drawProduct(g2d, product, artX, artY, pixelSize);
        }

        g2d.dispose();
    }
}
