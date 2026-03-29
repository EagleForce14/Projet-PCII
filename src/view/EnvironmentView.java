package view;

import model.environment.TreeInstance;
import model.environment.TreeGeometry;
import model.environment.TreeManager;

import javax.swing.JPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Vue dédiée à l'affichage des éléments fixes de l'environnement,
 * comme la grange et les arbres.
 */
public class EnvironmentView extends JPanel {
    private final FieldPanel fieldPanel;
    private final TreeManager treeManager;
    private final Image barnImage;
    private final Image treeImage;
    private final Image trunkImage;

    // Le constructeur de la vue
    public EnvironmentView(FieldPanel fieldPanel, TreeManager treeManager) {
        this.fieldPanel = fieldPanel;
        this.treeManager = treeManager;
        this.barnImage = ImageLoader.load("/assets/barn.png");
        this.treeImage = ImageLoader.load("/assets/arbre.png");
        this.trunkImage = ImageLoader.load("/assets/tronc_arbre.png");
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (fieldPanel == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        drawTrees(g2);

        if (barnImage != null) {
            Rectangle barnBounds = fieldPanel.getBarnScreenBounds();
            g2.drawImage(barnImage, barnBounds.x, barnBounds.y, barnBounds.width, barnBounds.height, null);
        }

        g2.dispose();
    }

    // Pour dessiner plusieurs arbres
    private void drawTrees(Graphics2D g2) {
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle cellBounds = fieldPanel.getCellBounds(tree.getGridX(), tree.getGridY());
            if (cellBounds == null) {
                continue;
            }

            if (tree.isMature()) {
                drawTree(g2, treeImage,
                        computeTreeBounds(
                                treeImage,
                                cellBounds,
                                TreeGeometry.MATURE_TREE_TILE_SCALE,
                                TreeGeometry.MATURE_TREE_ANCHOR_X_RATIO,
                                TreeGeometry.MATURE_TREE_ANCHOR_Y_RATIO
                        )
                );
            } else {
                drawTree(g2, trunkImage, computeTreeBounds(trunkImage, cellBounds, TreeGeometry.TRUNK_TILE_SCALE, 0.50, 0.50));
            }
        }
    }

    /**
     * Le tronc et l'arbre mature partagent le même centre de case,
     * mais l'arbre mature s'ancre plus haut pour que sa base reste sur cette case.
     */
    private Rectangle computeTreeBounds(
            Image image,
            Rectangle cellBounds,
            double tileScale,
            double anchorXRatio,
            double anchorYRatio
    ) {
        if (image == null) {
            return null;
        }

        int imageWidth = image.getWidth(this);
        int imageHeight = image.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return null;
        }

        int availableWidth = Math.max(1, (int) Math.round(cellBounds.width * tileScale));
        int availableHeight = Math.max(1, (int) Math.round(cellBounds.height * tileScale));
        double scale = Math.min(
                (double) availableWidth / imageWidth,
                (double) availableHeight / imageHeight
        );

        int drawWidth = (int) Math.round(imageWidth * scale);
        int drawHeight = (int) Math.round(imageHeight * scale);
        int centerX = cellBounds.x + (cellBounds.width / 2);
        int centerY = cellBounds.y + (cellBounds.height / 2);
        int drawX = (int) Math.round(centerX - (drawWidth * anchorXRatio));
        int drawY = (int) Math.round(centerY - (drawHeight * anchorYRatio));
        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    // Pour dessiner l'arbre
    private void drawTree(Graphics2D g2, Image image, Rectangle drawBounds) {
        if (image == null || drawBounds == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(
                image,
                drawBounds.x,
                drawBounds.y,
                drawBounds.width,
                drawBounds.height,
                this
        );
    }
}
