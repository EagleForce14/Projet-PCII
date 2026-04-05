package view;

import model.management.Money;
import model.management.MoneyRewardEffect;
import model.environment.TreeInstance;
import model.environment.TreeGeometry;
import model.environment.TreeFellingEffect;
import model.environment.TreeManager;
import model.environment.WoodRewardEffect;
import model.workshop.WorkshopConstructionManager;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Vue dédiée à l'affichage des éléments fixes de l'environnement,
 * comme la grange et les arbres.
 */
public class EnvironmentView extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    private static final Color TREE_GROUND_SHADOW_COLOR = new Color(7, 10, 4, 62);
    private static final Color TREE_GLOBAL_SHADOW_COLOR = new Color(6, 8, 4, 52);
    private static final Color WORKSHOP_PROGRESS_FRAME = new Color(31, 21, 18, 235);
    private static final Color WORKSHOP_PROGRESS_BACKGROUND = new Color(54, 67, 84, 228);
    private static final Color WORKSHOP_PROGRESS_FILL = new Color(105, 184, 219, 240);
    private static final Color WORKSHOP_PROGRESS_HIGHLIGHT = new Color(220, 246, 255, 210);
    private static final Color WORKSHOP_PROGRESS_TEXT = new Color(255, 247, 227);
    private static final Color WORKSHOP_PROGRESS_TEXT_SHADOW = new Color(24, 17, 13, 220);
    private static final Color TREE_PROGRESS_FRAME = new Color(33, 21, 12, 235);
    private static final Color TREE_PROGRESS_BACKGROUND = new Color(76, 54, 31, 228);
    private static final Color TREE_PROGRESS_FILL = new Color(197, 145, 76, 240);
    private static final Color TREE_PROGRESS_HIGHLIGHT = new Color(255, 229, 169, 210);
    private static final Color TREE_PROGRESS_TEXT = new Color(255, 246, 223);
    private static final Color TREE_PROGRESS_TEXT_SHADOW = new Color(30, 18, 9, 220);
    private static final Color TREE_FELLING_FLASH_OUTER = new Color(224, 178, 93);
    private static final Color TREE_FELLING_FLASH_INNER = new Color(255, 231, 173);
    private static final Color TREE_FELLING_DEBRIS = new Color(112, 74, 36);
    private static final Color WOOD_REWARD_GLOW = new Color(255, 229, 149, 120);
    private static final Color WOOD_REWARD_PARACHUTE = new Color(255, 247, 225, 220);
    private static final Color MONEY_REWARD_GLOW = new Color(255, 206, 108, 125);

    private final FieldPanel fieldPanel;
    private final TreeManager treeManager;
    private final WorkshopConstructionManager workshopConstructionManager;
    private final Money playerMoney;
    private final TopBarPanel topBarPanel;
    private final Image barnImage;
    private final Image workshopImage;
    private final Image bridgeImage;
    private final Image woodImage;
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
    private final Font progressFont;

    // Le constructeur de la classe
    public EnvironmentView(
            FieldPanel fieldPanel,
            TreeManager treeManager,
            WorkshopConstructionManager workshopConstructionManager,
            Money playerMoney,
            TopBarPanel topBarPanel
    ) {
        this.fieldPanel = fieldPanel;
        this.treeManager = treeManager;
        this.workshopConstructionManager = workshopConstructionManager;
        this.playerMoney = playerMoney;
        this.topBarPanel = topBarPanel;
        this.barnImage = ImageLoader.load("/assets/barn.png");
        this.workshopImage = ImageLoader.load("/assets/menuiserie.png");
        this.bridgeImage = ImageLoader.load("/assets/bridge.png");
        this.woodImage = ImageLoader.load("/assets/wood.png");
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
        this.progressFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
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
        drawPlacedBridges(g2);

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

        drawWorkshopConstructionProgress(g2);
        drawTreeCuttingProgress(g2);
        drawTreeFellingEffects(g2);
        drawWoodRewardEffects(g2);
        drawMoneyRewardEffects(g2);
        g2.dispose();
    }

    /**
     * Les ponts posés deviennent des éléments fixes du décor.
     * On les dessine donc ici, dans la même couche que les autres éléments
     * environnementaux, tout en laissant le joueur passer visuellement au-dessus.
     */
    private void drawPlacedBridges(Graphics2D g2) {
        if (bridgeImage == null || fieldPanel == null) {
            return;
        }

        for (Point bridgeAnchorCell : fieldPanel.getGrilleCulture().getBridgeAnchorCells()) {
            Rectangle bridgeBounds = fieldPanel.getBridgeScreenBounds(bridgeAnchorCell.x, bridgeAnchorCell.y);
            if (bridgeBounds == null) {
                continue;
            }

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(
                    bridgeImage,
                    bridgeBounds.x,
                    bridgeBounds.y,
                    bridgeBounds.width,
                    bridgeBounds.height,
                    this
            );
        }
    }

    /**
     * La progression est rendue dans la vue d'environnement pour rester visuellement liée
     * à la menuiserie, au-dessus du bâtiment et devant le décor.
     */
    private void drawWorkshopConstructionProgress(Graphics2D g2) {
        if (workshopConstructionManager == null || !workshopConstructionManager.isConstructionInProgress()) {
            return;
        }

        Rectangle workshopBounds = fieldPanel.getWorkshopScreenBounds();
        if (workshopBounds == null) {
            return;
        }

        int barWidth = Math.max(72, workshopBounds.width - 36);
        int barHeight = 9;
        int barX = workshopBounds.x + ((workshopBounds.width - barWidth) / 2);
        int barY = Math.max(26, workshopBounds.y - 18);
        int iconMaxSide = 28;
        int iconWidth = iconMaxSide;
        int iconHeight = iconMaxSide;
        if (bridgeImage != null) {
            int imageWidth = bridgeImage.getWidth(this);
            int imageHeight = bridgeImage.getHeight(this);
            if (imageWidth > 0 && imageHeight > 0) {
                if (imageWidth >= imageHeight) {
                    iconHeight = Math.max(1, (iconMaxSide * imageHeight) / imageWidth);
                } else {
                    iconWidth = Math.max(1, (iconMaxSide * imageWidth) / imageHeight);
                }
            }
        }

        int iconX = workshopBounds.x + ((workshopBounds.width - iconWidth) / 2);
        int iconY = Math.max(4, barY - iconHeight - 18);
        String timeText = WorkshopConstructionManager.formatDuration(workshopConstructionManager.getRemainingConstructionMs());
        int textY = barY - 6;

        if (bridgeImage != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(bridgeImage, iconX, iconY, iconWidth, iconHeight, this);
        }

        g2.setFont(progressFont);
        java.awt.FontMetrics fontMetrics = g2.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(timeText);
        int textX = workshopBounds.x + ((workshopBounds.width - textWidth) / 2);

        drawShadowedText(g2, timeText, textX, textY, WORKSHOP_PROGRESS_TEXT, WORKSHOP_PROGRESS_TEXT_SHADOW);

        HudProgressBarPainter.paint(
                g2,
                barX,
                barY,
                barWidth,
                barHeight,
                workshopConstructionManager.getConstructionProgressRatio(),
                WORKSHOP_PROGRESS_FRAME,
                WORKSHOP_PROGRESS_BACKGROUND,
                WORKSHOP_PROGRESS_FILL,
                WORKSHOP_PROGRESS_HIGHLIGHT
        );
    }

    /**
     * Affiche l'avancement de coupe directement au-dessus de chaque arbre entamé.
     * On réutilise volontairement le même gabarit de barre que pour d'autres objets du jeu
     * afin que le joueur lise immédiatement la progression.
     */
    private void drawTreeCuttingProgress(Graphics2D g2) {
        if (treeManager == null) {
            return;
        }

        int requiredImpactCount = treeManager.getRequiredCutImpactCount();
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            if (tree == null || !tree.hasCutProgress()) {
                continue;
            }

            Rectangle cellBounds = fieldPanel.getCellBounds(tree.getGridX(), tree.getGridY());
            Rectangle drawBounds = getRenderedTreeBounds(tree, cellBounds);
            if (drawBounds == null) {
                continue;
            }

            int barWidth = Math.max(34, Math.min(68, drawBounds.width / 2));
            int barHeight = 8;
            int barX = drawBounds.x + ((drawBounds.width - barWidth) / 2);
            int barY = Math.max(4, drawBounds.y - 14);
            String progressText = tree.getCutImpactCount() + "/" + requiredImpactCount;
            int textX = drawBounds.x + ((drawBounds.width - g2.getFontMetrics(progressFont).stringWidth(progressText)) / 2);
            int textY = barY - 4;

            g2.setFont(progressFont);
            drawShadowedText(g2, progressText, textX, textY, TREE_PROGRESS_TEXT, TREE_PROGRESS_TEXT_SHADOW);

            HudProgressBarPainter.paint(
                    g2,
                    barX,
                    barY,
                    barWidth,
                    barHeight,
                    tree.getCutProgressRatio(requiredImpactCount),
                    TREE_PROGRESS_FRAME,
                    TREE_PROGRESS_BACKGROUND,
                    TREE_PROGRESS_FILL,
                    TREE_PROGRESS_HIGHLIGHT
            );
        }
    }

    /**
     * La disparition de l'arbre n'est pas sèche :
     * on garde un flash court et quelques éclats,
     * dans le même esprit lisible que la casse des clôtures.
     */
    private void drawTreeFellingEffects(Graphics2D g2) {
        if (treeManager == null) {
            return;
        }

        long now = System.currentTimeMillis();
        for (TreeFellingEffect effect : treeManager.getActiveFellingEffects()) {
            drawTreeFellingEffect(g2, effect, now);
        }
    }

    /**
     * L'icône de bois suit une trajectoire rapide et arquée jusqu'à l'inventaire.
     * La petite canopée blanche sert juste à suggérer l'idée d'un "parachute".
     */
    private void drawWoodRewardEffects(Graphics2D g2) {
        if (treeManager == null) {
            return;
        }

        Rectangle fieldBoundsInView = getFieldBoundsInView();
        Rectangle woodSlotBounds = InventoryStatusOverlay.computeSlotBounds(
                InventoryStatusOverlay.getWoodSlotIndex(),
                fieldBoundsInView,
                getWidth(),
                getHeight()
        );
        if (woodSlotBounds == null) {
            return;
        }

        int destinationX = woodSlotBounds.x + (woodSlotBounds.width / 2);
        int destinationY = woodSlotBounds.y + (woodSlotBounds.height / 2) - 4;
        long now = System.currentTimeMillis();

        for (WoodRewardEffect effect : treeManager.getActiveWoodRewardEffects()) {
            drawWoodRewardEffect(g2, effect, destinationX, destinationY, now);
        }
    }

    /**
     * Les gains d'argent sont volontairement plus simples que le bois :
     * une grosse pièce pixel-art qui remonte directement
     * vers le compteur d'argent du HUD.
     */
    private void drawMoneyRewardEffects(Graphics2D g2) {
        if (playerMoney == null || topBarPanel == null) {
            return;
        }

        Rectangle moneyBounds = topBarPanel.getMoneyLabelBoundsIn(this);
        if (moneyBounds == null) {
            return;
        }

        Rectangle fieldBoundsInView = getFieldBoundsInView();
        long now = System.currentTimeMillis();
        int destinationX = moneyBounds.x + Math.max(12, moneyBounds.width / 2);
        int destinationY = moneyBounds.y + (moneyBounds.height / 2);

        for (MoneyRewardEffect effect : playerMoney.getActiveRewardEffects()) {
            drawMoneyRewardEffect(g2, effect, fieldBoundsInView, destinationX, destinationY, now);
        }
    }

    // Pour dessiner plusieurs arbres
    private void drawTrees(Graphics2D g2) {
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle cellBounds = fieldPanel.getCellBounds(tree.getGridX(), tree.getGridY());
            if (cellBounds == null) {
                continue;
            }

            Rectangle drawBounds = getRenderedTreeBounds(tree, cellBounds);
            Image displayedTreeImage = getDisplayedTreeImage(tree);
            if (tree.isMature()) {
                drawTreeShadow(g2, getTreeGroundShadowImage(tree), cellBounds, drawBounds, true, tree.usesWeepingWillowSprite());
                drawGlobalTreeShadow(g2, getTreeShadowImage(tree), drawBounds, true);
            } else {
                drawTreeShadow(g2, getTrunkGroundShadowImage(tree), cellBounds, drawBounds, false, tree.usesWeepingWillowSprite());
                drawGlobalTreeShadow(g2, getTrunkShadowImage(tree), drawBounds, false);
            }
            drawTree(g2, displayedTreeImage, drawBounds);
        }
    }

    private Rectangle getRenderedTreeBounds(TreeInstance tree, Rectangle cellBounds) {
        if (tree == null || cellBounds == null) {
            return null;
        }

        if (tree.isMature()) {
            return computeMatureTreeBounds(getMatureTreeImage(tree), cellBounds, tree.usesWeepingWillowSprite());
        }

        return computeTreeBounds(getTrunkImage(tree), cellBounds, TreeGeometry.TRUNK_TILE_SCALE, 0.50, 0.50);
    }

    private Image getDisplayedTreeImage(TreeInstance tree) {
        return tree != null && tree.isMature() ? getMatureTreeImage(tree) : getTrunkImage(tree);
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
        Point cellCenter = getBoundsCenter(cellBounds);
        int centerX = cellCenter.x;
        int centerY = cellCenter.y;
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

    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    /**
     * Valide en un seul endroit les prérequis communs aux effets
     * ancrés sur une case du terrain.
     */
    private Rectangle resolveGridEffectCellBounds(int gridX, int gridY, double progress) {
        if (progress >= 1.0) {
            return null;
        }

        return fieldPanel.getCellBounds(gridX, gridY);
    }

    private void drawTreeFellingEffect(Graphics2D g2, TreeFellingEffect effect, long now) {
        if (effect == null) {
            return;
        }

        double progress = effect.getProgress(now);
        Rectangle cellBounds = resolveGridEffectCellBounds(effect.getGridX(), effect.getGridY(), progress);
        if (cellBounds == null) {
            return;
        }

        Graphics2D effectGraphics = (Graphics2D) g2.create();
        Point cellCenter = getBoundsCenter(cellBounds);
        int centerX = cellCenter.x;
        int centerY = cellCenter.y;
        int baseRadius = Math.max(10, cellBounds.width / 2);
        int outerRadius = baseRadius + (int) Math.round(progress * 18);
        int innerRadius = Math.max(5, (int) Math.round(baseRadius * (0.95 - (progress * 0.48))));
        int outerAlpha = (int) Math.round(170 * (1.0 - progress));
        int innerAlpha = (int) Math.round(220 * (1.0 - progress));
        int debrisDistance = baseRadius + (int) Math.round(progress * 22);
        int debrisSize = Math.max(3, baseRadius / 3);

        drawCenteredGlow(effectGraphics, centerX, centerY, outerRadius * 2, TREE_FELLING_FLASH_OUTER, outerAlpha);
        drawCenteredGlow(effectGraphics, centerX, centerY, innerRadius * 2, TREE_FELLING_FLASH_INNER, innerAlpha);
        drawStandardBurstShards(effectGraphics, centerX, centerY, debrisDistance, debrisSize, progress);
        effectGraphics.dispose();
    }

    private void drawWoodRewardEffect(Graphics2D g2, WoodRewardEffect effect, int destinationX, int destinationY, long now) {
        if (effect == null) {
            return;
        }

        double progress = effect.getProgress(now);
        Rectangle cellBounds = resolveGridEffectCellBounds(effect.getGridX(), effect.getGridY(), progress);
        if (cellBounds == null) {
            return;
        }

        Point start = getBoundsCenter(cellBounds);
        Point current = computeArcPosition(start.x, start.y, destinationX, destinationY, progress, 52.0);
        int iconSize = 22;
        int iconX = current.x - (iconSize / 2);
        int iconY = current.y - (iconSize / 2);

        Graphics2D rewardGraphics = (Graphics2D) g2.create();
        rewardGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        drawCenteredGlow(rewardGraphics, current.x, current.y, 28, WOOD_REWARD_GLOW, (int) Math.round(110 * (1.0 - (progress * 0.45))));
        drawWoodRewardIcon(rewardGraphics, iconX, iconY, iconSize);
        drawParachuteCanopy(rewardGraphics, current.x, iconY - 2, progress);
        rewardGraphics.dispose();
    }

    private void drawMoneyRewardEffect(
            Graphics2D g2,
            MoneyRewardEffect effect,
            Rectangle fieldBoundsInView,
            int destinationX,
            int destinationY,
            long now
    ) {
        if (effect == null) {
            return;
        }

        double progress = effect.getProgress(now);
        if (progress >= 1.0) {
            return;
        }

        int startX = effect.hasExplicitSource()
                ? fieldBoundsInView.x + (fieldBoundsInView.width / 2) + effect.getSourceWorldX()
                : fieldBoundsInView.x + 36;
        int startY = effect.hasExplicitSource()
                ? fieldBoundsInView.y + (fieldBoundsInView.height / 2) + effect.getSourceWorldY()
                : fieldBoundsInView.y + fieldBoundsInView.height - 36;
        Point current = computeArcPosition(startX, startY, destinationX, destinationY, progress, 46.0);
        int pixelSize = 6;
        int iconWidth = ProductPixelArt.getCoinArtWidth(pixelSize);
        int iconHeight = ProductPixelArt.getCoinArtHeight(pixelSize);
        int iconX = current.x - (iconWidth / 2);
        int iconY = current.y - (iconHeight / 2);

        Graphics2D rewardGraphics = (Graphics2D) g2.create();
        drawCenteredGlow(rewardGraphics, current.x, current.y, 36, MONEY_REWARD_GLOW, (int) Math.round(105 * (1.0 - (progress * 0.35))));
        ProductPixelArt.drawCoinResource(rewardGraphics, iconX, iconY, pixelSize);
        rewardGraphics.dispose();
    }

    private void drawTreeShard(Graphics2D g2, int centerX, int centerY, int directionX, int directionY, int distance, int size, double progress) {
        int drawX = centerX + (directionX * distance) - (size / 2);
        int drawY = centerY + (directionY * distance) - (size / 2);
        int alpha = (int) Math.round(205 * (1.0 - progress));
        g2.setColor(withAlpha(TREE_FELLING_DEBRIS, alpha));
        g2.fillRect(drawX, drawY, size, size);
    }

    private void drawStandardBurstShards(Graphics2D g2, int centerX, int centerY, int debrisDistance, int debrisSize, double progress) {
        drawTreeShard(g2, centerX, centerY, -1, -1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 1, -1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, -1, 1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 1, 1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 0, -1, debrisDistance + 5, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 0, 1, debrisDistance + 5, debrisSize, progress);
    }

    private void drawParachuteCanopy(Graphics2D g2, int centerX, int canopyBaseY, double progress) {
        int canopyWidth = 14;
        int canopyHeight = 7;
        int canopyX = centerX - (canopyWidth / 2);
        int canopyY = canopyBaseY - 9 - (int) Math.round(Math.sin(progress * Math.PI) * 4.0);

        g2.setColor(withAlpha(WOOD_REWARD_PARACHUTE, (int) Math.round(230 * (1.0 - (progress * 0.35)))));
        g2.fillArc(canopyX, canopyY, canopyWidth, canopyHeight, 0, 180);
        g2.drawLine(centerX - 4, canopyY + canopyHeight - 1, centerX - 2, canopyBaseY);
        g2.drawLine(centerX + 4, canopyY + canopyHeight - 1, centerX + 2, canopyBaseY);
    }

    private void drawWoodRewardIcon(Graphics2D g2, int iconX, int iconY, int iconSize) {
        if (woodImage != null) {
            g2.drawImage(woodImage, iconX, iconY, iconSize, iconSize, this);
            return;
        }

        ProductPixelArt.drawWoodResource(g2, iconX, iconY, 4);
    }

    private void drawCenteredGlow(Graphics2D g2, int centerX, int centerY, int diameter, Color color, int alpha) {
        g2.setColor(withAlpha(color, alpha));
        g2.fillOval(centerX - (diameter / 2), centerY - (diameter / 2), diameter, diameter);
    }

    private void drawShadowedText(Graphics2D g2, String text, int x, int y, Color textColor, Color shadowColor) {
        g2.setColor(shadowColor);
        g2.drawString(text, x + 1, y + 1);
        g2.setColor(textColor);
        g2.drawString(text, x, y);
    }

    private Point getBoundsCenter(Rectangle bounds) {
        return new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
    }

    private Point computeArcPosition(int startX, int startY, int destinationX, int destinationY, double progress, double arcHeight) {
        double easedProgress = easeOutCubic(progress);
        int currentX = (int) Math.round(lerp(startX, destinationX, easedProgress));
        int currentY = (int) Math.round(lerp(startY, destinationY, easedProgress) - (Math.sin(progress * Math.PI) * arcHeight));
        return new Point(currentX, currentY);
    }

    private Color withAlpha(Color color, int alpha) {
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampedAlpha);
    }

    private double lerp(double start, double end, double progress) {
        return start + ((end - start) * progress);
    }

    private double easeOutCubic(double progress) {
        double inverse = 1.0 - progress;
        return 1.0 - (inverse * inverse * inverse);
    }
}
