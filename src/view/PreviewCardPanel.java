package view;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Base commune des petits aperçus encadrés affichés dans les overlays.
 * Le cadre reste toujours le même ; seules l'illustration et sa position changent
 * selon le type d'aperçu affiché.
 */
public abstract class PreviewCardPanel extends JPanel {
    // Fond chaud utilisé derrière les mini visuels.
    private static final Color PREVIEW_FILL = new Color(56, 40, 28, 210);
    // Bordure légère qui détache l'aperçu du fond de l'overlay.
    private static final Color PREVIEW_BORDER = new Color(138, 110, 73, 180);
    // Arrondi partagé par tous les cadres d'aperçu.
    private static final int PREVIEW_ARC = 18;

    /**
     * Le fond et la bordure sont gérés ici une seule fois.
     * Les classes filles se contentent ensuite de dessiner leur contenu propre dans la zone utile.
     */
    @Override
    protected final void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        ComponentPaintContext paintContext = ComponentPaintContext.create(graphics, this);
        if (paintContext == null) {
            return;
        }

        Graphics2D g2d = paintContext.graphics();
        try {
            int width = paintContext.width();
            int height = paintContext.height();

            g2d.setColor(PREVIEW_FILL);
            g2d.fillRoundRect(0, 0, width, height, PREVIEW_ARC, PREVIEW_ARC);
            g2d.setColor(PREVIEW_BORDER);
            g2d.drawRoundRect(0, 0, width - 1, height - 1, PREVIEW_ARC, PREVIEW_ARC);

            paintPreviewContent(g2d, width, height);
        } finally {
            paintContext.dispose();
        }
    }

    /**
     * Dessine le contenu spécifique de l'aperçu à l'intérieur du cadre commun.
     */
    protected abstract void paintPreviewContent(Graphics2D g2d, int width, int height);
}
