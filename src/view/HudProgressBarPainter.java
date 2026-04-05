package view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

/**
 * Renderer partagé pour les petites barres de progression du HUD.
 * L'objectif est double :
 * - garantir exactement le même rendu partout dans l'interface ;
 * - éviter de recopier la même logique de dessin dans plusieurs vues.
 */
public final class HudProgressBarPainter {
    private HudProgressBarPainter() {
        // Classe utilitaire : aucune instance n'a de sens ici.
    }

    /**
     * Dessine une barre de progression arrondie.
     * La méthode ne dépend d'aucun composant Swing précis, ce qui permet de la
     * réutiliser aussi bien dans un JPanel classique que dans un renderer overlay.
     */
    public static void paint(
            Graphics2D g2,
            int x,
            int y,
            int width,
            int height,
            double progressRatio,
            Color frameColor,
            Color backgroundColor,
            Color fillColor,
            Color highlightColor
    ) {
        double clampedProgressRatio = Math.max(0.0, Math.min(1.0, progressRatio));
        int frameArc = height + 2;
        int innerPadding = 2;
        int innerWidth = Math.max(1, width - (innerPadding * 2));
        int innerHeight = Math.max(1, height - (innerPadding * 2));
        int innerArc = Math.max(4, frameArc - 2);
        int fillWidth = (int) Math.round(innerWidth * clampedProgressRatio);

        g2.setColor(frameColor);
        g2.fillRoundRect(x, y, width, height, frameArc, frameArc);
        fillRoundedLayer(g2, x + innerPadding, y + innerPadding, innerWidth, innerHeight, innerArc, backgroundColor);

        /*
         * Le remplissage ne démarre que si la progression est strictement positive.
         * Cela évite d'avoir un faux pixel lumineux quand le chrono vient juste de repartir.
         */
        if (fillWidth > 0) {
            fillRoundedLayer(g2, x + innerPadding, y + innerPadding, fillWidth, innerHeight, innerArc, fillColor);
            drawFillHighlight(g2, x + innerPadding, y + innerPadding, fillWidth, innerHeight, innerArc, highlightColor);
        }
    }

    private static void fillRoundedLayer(Graphics2D g2, int x, int y, int width, int height, int arc, Color fillColor) {
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, width, height, arc, arc);
    }

    /**
     * La surbrillance supérieure doit rester strictement enfermée dans la forme arrondie
     * du remplissage.
     */
    private static void drawFillHighlight(Graphics2D g2, int x, int y, int width, int height, int arc, Color highlightColor) {
        if (width <= 2 || height <= 2) {
            return;
        }

        Graphics2D highlightGraphics = (Graphics2D) g2.create();
        Shape previousClip = highlightGraphics.getClip();
        Shape fillShape = new RoundRectangle2D.Float(x, y, width, height, arc, arc);
        highlightGraphics.clip(fillShape);
        highlightGraphics.setColor(highlightColor);
        highlightGraphics.fillRect(x + 1, y + 1, Math.max(1, width - 2), 1);
        highlightGraphics.setClip(previousClip);
        highlightGraphics.dispose();
    }
}
