package view;

import model.objective.GestionnaireObjectifs;
import model.runtime.Jour;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Overlay qui recouvre la zone de jeu en cas de défaite.
 */
public class GameOverOverlay extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    private final Jour jour;

    public GameOverOverlay(Jour jour) {
        this.jour = jour;

        setOpaque(false);
        setAlignmentX(0.5f);
        setAlignmentY(0.5f);
    }

    /** Méthode qui dessine l'overlay de fin de partie */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (!jour.isPartieTerminee()) {
            return;
        }

        // On utilise Graphics2D pour de meilleurs rendus (antialiasing).
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(12, 8, 6, 200));
        g2d.fillRect(0, 0, width, height);

        int cardWidth = Math.min(620, width - 80);
        int cardHeight = 230;
        int cardX = (width - cardWidth) / 2;
        int cardY = (height - cardHeight) / 2;

        g2d.setColor(new Color(45, 26, 18, 240));
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);

        g2d.setColor(new Color(214, 159, 126, 255));
        g2d.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);

        Font titleFont = CustomFontLoader.loadFont(FONT_PATH, 26.0f);
        Font bodyFont = CustomFontLoader.loadFont(FONT_PATH, 13.0f);
        Font hintFont = CustomFontLoader.loadFont(FONT_PATH, 10.0f);

        String title = "PARTIE PERDUE";
        GestionnaireObjectifs gestionnaire = jour.getGestionnaireObjectifs();
        int minimum = gestionnaire.getNombreObjectifsAValiderEffectif();
        int atteints = gestionnaire.getNombreObjectifsAtteints();
        String body = "Objectifs valides : " + atteints + " / " + minimum;
        String hint = "Le jeu est en pause. Lance une nouvelle partie pour continuer.";

        g2d.setColor(new Color(255, 225, 201));
        g2d.setFont(titleFont);
        drawCenteredLine(g2d, title, width, cardY + 70);

        g2d.setColor(new Color(246, 215, 188));
        g2d.setFont(bodyFont);
        drawCenteredLine(g2d, body, width, cardY + 125);

        g2d.setColor(new Color(214, 188, 166));
        g2d.setFont(hintFont);
        drawCenteredLine(g2d, hint, width, cardY + 170);

        g2d.dispose();
    }

    /** Dessine une ligne centrée sur le composant */
    private void drawCenteredLine(Graphics2D g2d, String text, int fullWidth, int baselineY) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (fullWidth - metrics.stringWidth(text)) / 2;
        g2d.drawString(text, x, baselineY);
    }
}
