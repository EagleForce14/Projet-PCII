package view.shop;

import model.shop.Product;
import view.ProductPixelArt;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Carte visuelle d'un produit dans le catalogue central.
 */
public class ProductCardView extends JPanel {
    private static final Color TEXT_PRIMARY = new Color(255, 248, 226);
    private static final Color TEXT_SECONDARY = new Color(216, 199, 164);
    private static final Color PANEL_SHADOW = new Color(0, 0, 0, 90);
    private static final Color CARD_FILL = new Color(67, 49, 34, 238);
    private static final Color CARD_FILL_HOVER = new Color(83, 61, 42, 244);
    private static final Color CARD_FILL_SELECTED = new Color(102, 72, 46, 248);
    private static final Color BORDER_SOFT = new Color(138, 110, 73, 180);
    private static final Color ACCENT = new Color(216, 181, 96);

    private final Product product;
    private final String categoryLabel;
    private final int remainingStock;
    private final int cartQuantity;
    private final boolean selected;
    private final Image woodTexture;
    private final Font labelFont;
    private final Font priceFont;
    private final Font bodyFont;
    private final Runnable onSelect;

    private boolean hovered;

    // La carte reste un composant purement visuel:
    // toute la logique de panier ou de filtre reste volontairement dans ShopOverlay.
    public ProductCardView(
            Product product,
            String categoryLabel,
            int remainingStock,
            int cartQuantity,
            boolean selected,
            Image woodTexture,
            Font labelFont,
            Font priceFont,
            Font bodyFont,
            Runnable onSelect
    ) {
        this.product = product;
        this.categoryLabel = categoryLabel;
        this.remainingStock = remainingStock;
        this.cartQuantity = cartQuantity;
        this.selected = selected;
        this.woodTexture = woodTexture;
        this.labelFont = labelFont;
        this.priceFont = priceFont;
        this.bodyFont = bodyFont;
        this.onSelect = onSelect;

        setOpaque(false);
        setPreferredSize(new Dimension(270, 154));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // On ne garde qu'un retour visuel minimal:
        // survol pour donner envie de cliquer, puis callback de selection.
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (onSelect != null) {
                    onSelect.run();
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = selected ? CARD_FILL_SELECTED : hovered ? CARD_FILL_HOVER : CARD_FILL;

        // La carte superpose ombre, fond et bordure.
        // Le rendu reste simple pour laisser respirer le pixel art au centre.
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 20, 20);

        g2d.setColor(fill);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        if (woodTexture != null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.08f));
            g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        g2d.setColor(selected ? ACCENT : BORDER_SOFT);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Le cartouche sombre derriere l'illustration aide a garder
        // une lecture propre meme quand la texture du bois est visible.
        g2d.setColor(new Color(92, 68, 46, 220));
        g2d.fillRoundRect(16, 16, 88, 88, 18, 18);

        int pixelSize = 7;
        int artSize = 5 * pixelSize;
        int artX = 16 + ((88 - artSize) / 2);
        int artY = 16 + ((88 - artSize) / 2);
        ProductPixelArt.drawProduct(g2d, product, artX, artY, pixelSize);

        g2d.setFont(labelFont);
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString(categoryLabel, 120, 30);

        g2d.setFont(priceFont);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(product.getName(), 120, 58);

        g2d.setFont(bodyFont);
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString(product.getPrice() + " EUR", 120, 86);
        g2d.drawString("Stock : " + remainingStock, 120, 110);

        if (cartQuantity > 0) {
            // Le badge ne s'affiche que si l'article est deja dans le panier.
            // Ca evite d'ajouter du bruit sur toutes les cartes en permanence.
            String badgeText = "Panier " + cartQuantity;
            java.awt.FontMetrics badgeMetrics = g2d.getFontMetrics();
            int badgeWidth = badgeMetrics.stringWidth(badgeText) + 18;
            int badgeX = getWidth() - badgeWidth - 14;
            int badgeY = getHeight() - 38;

            g2d.setColor(new Color(45, 75, 42, 220));
            g2d.fillRoundRect(badgeX, badgeY, badgeWidth, 24, 12, 12);
            g2d.setColor(new Color(126, 190, 110));
            g2d.drawRoundRect(badgeX, badgeY, badgeWidth, 24, 12, 12);
            g2d.setColor(TEXT_PRIMARY);
            int badgeTextX = badgeX + ((badgeWidth - badgeMetrics.stringWidth(badgeText)) / 2);
            int badgeTextY = badgeY + ((24 - badgeMetrics.getHeight()) / 2) + badgeMetrics.getAscent();
            g2d.drawString(badgeText, badgeTextX, badgeTextY);
        }

        g2d.dispose();
    }
}
