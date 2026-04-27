package view.grotte;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Permet de dessiner un rectangle sur la vue de la grotte
 * (permettant via d'autres classes un retour visuel si l'on est touché par un projectile par exemple).
 * Ici, on affiche juste une vignette rectangulaire sur les 4 bords de l'écran et rien de plus.
 */
public final class GrotteOverlayPainter {
    private GrotteOverlayPainter() {
    }

    /**
     * On dessine une vignette rectangulaire simple sur les quatre bords de l'écran.
     */
    public static void drawScreenVignette(Graphics2D g2, int width, int height, int band, Color color) {
        if (g2 == null || width <= 0 || height <= 0 || band <= 0 || color == null) {
            return;
        }

        g2.setColor(color);
        g2.fillRect(0, 0, width, band);
        g2.fillRect(0, height - band, width, band);
        g2.fillRect(0, 0, band, height);
        g2.fillRect(width - band, 0, band, height);
    }
}
