package view.shop;

import javax.swing.ButtonModel;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Dessine le fond du bouton, sa bordure et s'assure que le texte reste centré.
 */
public class ShopPixelButton extends JButton {
    // Couleur du texte quand le bouton est désactivé.
    private static final Color TEXT_MUTED = new Color(169, 151, 124);

    // Couleur principale du bouton au repos.
    private final Color baseColor;
    // Couleur utilisée au survol.
    private final Color hoverColor;
    // Couleur de bordure du bouton.
    private final Color borderColor;
    // Couleur normale du texte.
    private final Color textColor;

    /**
     * On prépare un bouton visuel de boutique avec ses couleurs et sa typographie.
     */
    public ShopPixelButton(String text, Font font, Color baseColor, Color hoverColor, Color borderColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
        this.borderColor = borderColor;
        this.textColor = textColor;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(textColor);
        setFont(font);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new java.awt.Insets(10, 14, 10, 14));
    }

    /**
     * On dessine le fond, la bordure puis le texte centré selon l'état courant du bouton.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ButtonModel model = getModel();
        Color fill = !isEnabled()
                ? new Color(72, 58, 45, 160)
                : model.isPressed() ? baseColor.darker() : model.isRollover() ? hoverColor : baseColor;

        g2d.setColor(fill);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);

        g2d.setColor(isEnabled() ? borderColor : new Color(90, 77, 62));
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

        g2d.setColor(isEnabled() ? textColor : TEXT_MUTED);
        g2d.setFont(getFont());

        String text = getText();
        if (text != null && !text.isEmpty()) {
            java.awt.FontMetrics metrics = g2d.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(text)) / 2;
            int textY = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();
            g2d.drawString(text, textX, textY);
        }

        g2d.dispose();
    }
}
