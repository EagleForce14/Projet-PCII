package view.shop;

import model.shop.Product;
import view.ProductPixelArt;
import view.PreviewCardPanel;

import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Petite preview d'un produit dans la colonne de droite.
 * Elle reste volontairement sobre: un cadre, puis l'illustration pixel-art.
 */
public class ProductPreview extends PreviewCardPanel {
    // Produit actuellement affiché dans l'aperçu.
    private Product product;

    /**
     * On prépare un petit cadre fixe pour afficher un seul produit à la fois.
     */
    public ProductPreview() {
        setOpaque(false);
        setPreferredSize(new Dimension(88, 88));
        setMinimumSize(new Dimension(88, 88));
    }

    /**
     * On remplace le produit affiché puis on redessine l'aperçu.
     */
    public void setProduct(Product product) {
        this.product = product;
        repaint();
    }

    @Override
    protected void paintPreviewContent(Graphics2D g2d, int width, int height) {
        if (product != null) {
            int pixelSize = 8;
            int artWidth = ProductPixelArt.getProductArtWidth(product, pixelSize);
            int artHeight = ProductPixelArt.getProductArtHeight(product, pixelSize);
            int artX = (width - artWidth) / 2;
            int artY = (height - artHeight) / 2;
            ProductPixelArt.drawProduct(g2d, product, artX, artY, pixelSize);
        }
    }
}
