package view.shop;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Bouton-toggle utilisé pour les filtres de la colonne de gauche.
 * Le composant reste volontairement petit:
 * il sert juste à lire vite les catégories et à changer de vue.
 */
public class ShopFilterChip extends JToggleButton {
    private static final Color TEXT_PRIMARY = new Color(255, 248, 226);
    private static final Color BORDER_SOFT = new Color(138, 110, 73, 180);
    private static final Color ACCENT = new Color(216, 181, 96);

    // Le constructeur de la vue
    public ShopFilterChip(String label, Font font) {
        super(label);
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(TEXT_PRIMARY);
        setFont(font);
        setHorizontalAlignment(SwingConstants.LEFT);
        setMargin(new java.awt.Insets(0, 0, 0, 0));
        // Toute la largeur est cliquable, mais on garde une hauteur stable
        // pour que la pile de filtres reste bien rythmee.
        setPreferredSize(new Dimension(0, 42));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ButtonModel model = getModel();
        Color fill = isSelected()
                ? new Color(94, 68, 44, 238)
                : model.isRollover() ? new Color(74, 54, 38, 230) : new Color(55, 39, 29, 210);

        g2d.setColor(fill);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

        g2d.setColor(isSelected() ? ACCENT : BORDER_SOFT);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

        super.paintComponent(g2d);
        g2d.dispose();
    }
}
