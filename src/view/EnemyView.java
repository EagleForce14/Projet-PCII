package view;

import model.EnemyModel;
import model.EnemyUnit;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Vue pour afficher les ennemis (IA ennemie).
 */
public class EnemyView extends JPanel {
    private final EnemyModel model;
    private final FieldPanel fieldPanel;
    
    public EnemyView(EnemyModel model, FieldPanel fieldPanel) {
        this.model = model;
        this.fieldPanel = fieldPanel;
        this.setOpaque(false);
        this.setDoubleBuffered(true); // Évite les clignotements
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getWidth() > 0 && getHeight() > 0) {
            model.setViewportSize(getWidth(), getHeight());
        }

        List<EnemyUnit> enemies = model.getEnemyUnits();
        Rectangle fieldBounds = SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
        model.setFieldSize(fieldBounds.width, fieldBounds.height);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (EnemyUnit enemy : enemies) {
            int drawX = centerX + enemy.getX();
            int drawY = centerY + enemy.getY();
            int radius = 10;

            g2d.setColor(Color.WHITE);
            g2d.fillOval(drawX - radius, drawY - radius, radius * 2, radius * 2);

            g2d.setColor(Color.GRAY);
            g2d.drawOval(drawX - radius, drawY - radius, radius * 2, radius * 2);
        }
    }
}
