package view;

import model.environment.TreeInstance;
import model.environment.TreeGeometry;
import model.environment.TreeManager;

import javax.swing.JPanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Vue dédiée à l'affichage des éléments fixes de l'environnement,
 * comme la grange et les arbres.
 */
public class EnvironmentView extends JPanel {
    private static final Color TREE_GROUND_SHADOW_COLOR = new Color(7, 10, 4, 62);
    private static final Color TREE_GLOBAL_SHADOW_COLOR = new Color(6, 8, 4, 52);

    private final FieldPanel fieldPanel;
    private final TreeManager treeManager;
    private final Image barnImage;
    private final Image workshopImage;
    private final Image treeImage;
    private final Image alternateTreeImage;
    private final Image weepingWillowImage;
    private final Image trunkImage;
    private final Image darkTrunkImage;
    private final BufferedImage treeGroundShadowImage;
    private final BufferedImage alternateTreeGroundShadowImage;
    private final BufferedImage weepingWillowGroundShadowImage;
    private final BufferedImage trunkGroundShadowImage;
    private final BufferedImage darkTrunkGroundShadowImage;
    private final BufferedImage treeShadowImage;
    private final BufferedImage alternateTreeShadowImage;
    private final BufferedImage weepingWillowShadowImage;
    private final BufferedImage trunkShadowImage;
    private final BufferedImage darkTrunkShadowImage;

    // Le constructeur de la classe
    public EnvironmentView(FieldPanel fieldPanel, TreeManager treeManager) {
        this.fieldPanel = fieldPanel;
        this.treeManager = treeManager;
        this.barnImage = ImageLoader.load("/assets/barn.png");
        this.workshopImage = ImageLoader.load("/assets/menuiserie.png");
        this.treeImage = ImageLoader.load("/assets/arbre.png");
        this.alternateTreeImage = ImageLoader.load("/assets/arbre2.png");
        this.weepingWillowImage = ImageLoader.load("/assets/Saule pleureur.png");
        this.trunkImage = ImageLoader.load("/assets/tronc_arbre.png");
        this.darkTrunkImage = ImageLoader.load("/assets/tronc_sombre.png");
        this.treeGroundShadowImage = createShadowImage(treeImage, TREE_GROUND_SHADOW_COLOR);
        this.alternateTreeGroundShadowImage = createShadowImage(alternateTreeImage, TREE_GROUND_SHADOW_COLOR);
        this.weepingWillowGroundShadowImage = createShadowImage(weepingWillowImage, TREE_GROUND_SHADOW_COLOR);
        this.trunkGroundShadowImage = createShadowImage(trunkImage, TREE_GROUND_SHADOW_COLOR);
        this.darkTrunkGroundShadowImage = createShadowImage(darkTrunkImage, TREE_GROUND_SHADOW_COLOR);
        this.treeShadowImage = createShadowImage(treeImage, TREE_GLOBAL_SHADOW_COLOR);
        this.alternateTreeShadowImage = createShadowImage(alternateTreeImage, TREE_GLOBAL_SHADOW_COLOR);
        this.weepingWillowShadowImage = createShadowImage(weepingWillowImage, TREE_GLOBAL_SHADOW_COLOR);
        this.trunkShadowImage = createShadowImage(trunkImage, TREE_GLOBAL_SHADOW_COLOR);
        this.darkTrunkShadowImage = createShadowImage(darkTrunkImage, TREE_GLOBAL_SHADOW_COLOR);
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

        Rectangle barnBounds = fieldPanel.getBarnScreenBounds();
        if (workshopImage != null) {
            Rectangle workshopBounds = fieldPanel.getWorkshopScreenBounds();
            if (workshopBounds != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(
                        workshopImage,
                        workshopBounds.x,
                        workshopBounds.y,
                        workshopBounds.width,
                        workshopBounds.height,
                        null
                );
            }
        }

        if (barnImage != null && barnBounds != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
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

            Rectangle drawBounds;
            if (tree.isMature()) {
                Image matureTreeImage = getMatureTreeImage(tree);
                BufferedImage matureTreeGroundShadowImage = getTreeGroundShadowImage(tree);
                BufferedImage matureTreeShadowImage = getTreeShadowImage(tree);
                drawBounds = computeMatureTreeBounds(
                        matureTreeImage,
                        cellBounds,
                        tree.usesWeepingWillowSprite()
                );
                drawTreeShadow(g2, matureTreeGroundShadowImage, cellBounds, drawBounds, true, tree.usesWeepingWillowSprite());
                drawGlobalTreeShadow(g2, matureTreeShadowImage, drawBounds, true);
                drawTree(g2, matureTreeImage, drawBounds);
            } else {
                Image trunkVariant = getTrunkImage(tree);
                BufferedImage trunkGroundShadowVariant = getTrunkGroundShadowImage(tree);
                BufferedImage trunkShadowVariant = getTrunkShadowImage(tree);
                drawBounds = computeTreeBounds(trunkVariant, cellBounds, TreeGeometry.TRUNK_TILE_SCALE, 0.50, 0.50);
                drawTreeShadow(g2, trunkGroundShadowVariant, cellBounds, drawBounds, false, tree.usesWeepingWillowSprite());
                drawGlobalTreeShadow(g2, trunkShadowVariant, drawBounds, false);
                drawTree(g2, trunkVariant, drawBounds);
            }
        }
    }

    private Rectangle computeMatureTreeBounds(Image image, Rectangle cellBounds, boolean weepingWillow) {
        return computeTreeBounds(
                image,
                cellBounds,
                TreeGeometry.getMatureTreeTileScale(weepingWillow),
                TreeGeometry.getMatureTreeAnchorXRatio(weepingWillow),
                TreeGeometry.getMatureTreeAnchorYRatio(weepingWillow)
        );
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

    /**
     * Ombre au sol basée sur la silhouette de l'arbre, fortement aplatie.
     * Cela garde une forme proche du sprite tout en restant visiblement posée au sol.
     */
    private void drawTreeShadow(
            Graphics2D g2,
            Image shadowImage,
            Rectangle cellBounds,
            Rectangle drawBounds,
            boolean mature,
            boolean weepingWillow
    ) {
        if (shadowImage == null || cellBounds == null || drawBounds == null) {
            return;
        }

        Graphics2D shadowGraphics = (Graphics2D) g2.create();
        shadowGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int cellSize = Math.max(1, Math.min(cellBounds.width, cellBounds.height));
        int shadowWidth = Math.max(cellSize, (int) Math.round(drawBounds.width * (mature ? 0.92 : 0.98)));
        int shadowHeight = Math.max(
                Math.max(10, cellSize / 3),
                (int) Math.round(drawBounds.height * (mature ? 0.22 : 0.28))
        );

        int trunkCenterX = drawBounds.x + (int) Math.round(
                drawBounds.width * (mature ? TreeGeometry.getMatureTreeAnchorXRatio(weepingWillow) : 0.50)
        );
        int trunkCenterY = drawBounds.y + (int) Math.round(
                drawBounds.height * (mature ? TreeGeometry.getMatureTreeAnchorYRatio(weepingWillow) : 0.50)
        );

        shadowGraphics.drawImage(
                shadowImage,
                trunkCenterX - (shadowWidth / 2),
                trunkCenterY - (shadowHeight / 2),
                shadowWidth,
                shadowHeight,
                this
        );
        shadowGraphics.dispose();
    }

    /**
     * Ajoute une ombre portée très légère derrière tout le sprite.
     * L'offset reste faible pour éviter un effet trop dramatique.
     */
    private void drawGlobalTreeShadow(Graphics2D g2, Image shadowImage, Rectangle drawBounds, boolean mature) {
        if (shadowImage == null || drawBounds == null) {
            return;
        }

        int offsetX = Math.max(2, (int) Math.round(drawBounds.width * (mature ? 0.024 : 0.032)));
        int offsetY = Math.max(2, (int) Math.round(drawBounds.height * (mature ? 0.028 : 0.036)));

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(
                shadowImage,
                drawBounds.x + offsetX,
                drawBounds.y + offsetY,
                drawBounds.width,
                drawBounds.height,
                this
        );
    }

    private Image getMatureTreeImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowImage != null) {
            return weepingWillowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeImage != null) {
            return alternateTreeImage;
        }

        return treeImage;
    }

    private BufferedImage getTreeGroundShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowGroundShadowImage != null) {
            return weepingWillowGroundShadowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeGroundShadowImage != null) {
            return alternateTreeGroundShadowImage;
        }

        return treeGroundShadowImage;
    }

    private BufferedImage getTreeShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowShadowImage != null) {
            return weepingWillowShadowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeShadowImage != null) {
            return alternateTreeShadowImage;
        }

        return treeShadowImage;
    }

    private Image getTrunkImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkImage != null) {
            return darkTrunkImage;
        }

        return trunkImage;
    }

    private BufferedImage getTrunkGroundShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkGroundShadowImage != null) {
            return darkTrunkGroundShadowImage;
        }

        return trunkGroundShadowImage;
    }

    private BufferedImage getTrunkShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkShadowImage != null) {
            return darkTrunkShadowImage;
        }

        return trunkShadowImage;
    }

    private BufferedImage createShadowImage(Image image, Color shadowColor) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth(this);
        int height = image.getHeight(this);
        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage shadowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D shadowGraphics = shadowImage.createGraphics();
        shadowGraphics.drawImage(image, 0, 0, this);
        shadowGraphics.setComposite(AlphaComposite.SrcIn);
        shadowGraphics.setColor(shadowColor);
        shadowGraphics.fillRect(0, 0, width, height);
        shadowGraphics.dispose();
        return shadowImage;
    }
}
