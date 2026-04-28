package view.workshop;

import view.ProductPixelArt;
import view.PreviewCardPanel;

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Aperçu dédié à la ressource bois dans la menuiserie.
 */
public final class WoodPreviewPanel extends PreviewCardPanel {
    /**
     * On prépare un aperçu fixe pour afficher la ressource bois dans la menuiserie.
     */
    public WoodPreviewPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(118, 102));
        setMinimumSize(new Dimension(118, 102));
        setMaximumSize(new Dimension(118, 102));
    }

    @Override
    protected void paintPreviewContent(Graphics2D g2d, int width, int height) {
        int pixelSize = 10;
        int artWidth = ProductPixelArt.getWoodArtWidth(pixelSize);
        int artHeight = ProductPixelArt.getWoodArtHeight(pixelSize);
        int artX = (width - artWidth) / 2;
        int artY = (height - artHeight) / 2;
        ProductPixelArt.drawWoodResource(g2d, artX, artY, pixelSize);
    }
}
