package view;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

/**
 * Vue dédiée à l'affichage des éléments fixes de l'environnement,
 * comme la grange.
 */
public class EnvironmentView extends JPanel {
    private final FieldPanel fieldPanel;
    private final Image barnImage;

    // Le constructeur de la vue
    public EnvironmentView(FieldPanel fieldPanel) {
        this.fieldPanel = fieldPanel;
        this.barnImage = ImageLoader.load("/assets/barn.png");
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (barnImage != null && fieldPanel != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            
            // On calcule précisément la position du centre du champ dans ce composant plein écran
            Rectangle fieldBounds = SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
            int centerX = fieldBounds.x + (fieldBounds.width / 2);
            int centerY = fieldBounds.y + (fieldBounds.height / 2);

            // On récupère les coordonnées logiques de la grange
            int drawX = centerX + model.Barn.X;
            int drawY = centerY + model.Barn.Y;

            // On dessine l'image de la grange
            g2.drawImage(barnImage, drawX, drawY, model.Barn.WIDTH, model.Barn.HEIGHT, null);
            
            g2.dispose();
        }
    }
}
