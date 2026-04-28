package view;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Petit contexte partagé pour les composants Swing affichés.
 * Il prépare un Graphics2D antialiasé et vérifie que le composant a
 * une taille exploitable avant de lancer un dessin plus détaillé.
 */
final class ComponentPaintContext {
    private final Graphics2D graphics;
    private final int width;
    private final int height;

    private ComponentPaintContext(Graphics2D graphics, int width, int height) {
        this.graphics = graphics;
        this.width = width;
        this.height = height;
    }

    /**
     * Prépare un contexte prêt à dessiner pour le composant courant.
     * Si la surface n'a pas encore de taille visible, on libère tout de suite
     * le Graphics2D temporaire et on retourne null.
     */
    static ComponentPaintContext create(Graphics graphics, JComponent component) {
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = component.getWidth();
        int height = component.getHeight();
        if (width <= 0 || height <= 0) {
            graphics2D.dispose();
            return null;
        }

        return new ComponentPaintContext(graphics2D, width, height);
    }

    Graphics2D graphics() {
        return graphics;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    void dispose() {
        graphics.dispose();
    }
}
