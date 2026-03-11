package view;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;

// Une classe pour afficher une image d'arrière-plan à la fenêtre principale.
public class BackgroundPanel extends JPanel {
    // Une constante pour gérer le zoom dans l'image
    private static final double BACKGROUND_ZOOM = 1.035;

    private final Image backgroundImage;

    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    // Une méthode permettant l'affichage de l'image dans l'interface
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // On définit les dimensions de l'image
        if (backgroundImage != null) {
            int drawWidth = (int) (getWidth() * BACKGROUND_ZOOM);
            int drawHeight = (int) (getHeight() * BACKGROUND_ZOOM);
            int drawX = (getWidth() - drawWidth) / 2;
            int drawY = (getHeight() - drawHeight) / 2;

            g.drawImage(backgroundImage, drawX, drawY, drawWidth, drawHeight, this);
        }
    }
}