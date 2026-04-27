package view;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;

// Classe permettant d'afficher une image en arrière-plan de la fenêtre.
public class BackgroundPanel extends JPanel {
    // On définit une constante indiquant le facteur de zoom dans l'image.
    private static final double BACKGROUND_ZOOM = 1.035;

    // Image de fond réellement dessinée dans le panneau.
    private final Image backgroundImage;

    /**
     * On prépare un panneau chargé d'afficher une image de fond unique.
     */
    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * On dessine l'image de fond légèrement zoomée avant tous les autres composants.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // On dessine le fond avant les autres composants du panneau.
        if (backgroundImage != null) {
            int drawWidth = (int) (getWidth() * BACKGROUND_ZOOM);
            int drawHeight = (int) (getHeight() * BACKGROUND_ZOOM);
            int drawX = (getWidth() - drawWidth) / 2;
            int drawY = (getHeight() - drawHeight) / 2;

            g.drawImage(backgroundImage, drawX, drawY, drawWidth, drawHeight, this);
        }
    }
}
