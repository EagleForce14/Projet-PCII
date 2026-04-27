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
 * comme la boutique principale (à droite) et les arbres.
 */
public class EnvironmentView extends JPanel {
    // Police pixel utilisée pour les barres de progression dans le décor.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    // Couleur de l'ombre plaquée au sol sous les arbres.
    private static final Color TREE_GROUND_SHADOW_COLOR = new Color(7, 10, 4, 62);
    // Couleur de l'ombre générale projetée par les arbres.
    private static final Color TREE_GLOBAL_SHADOW_COLOR = new Color(6, 8, 4, 52);
    // Couleur du cadre de la barre de progression de menuiserie.
    private static final Color WORKSHOP_PROGRESS_FRAME = new Color(31, 21, 18, 235);
    // Couleur du fond de la barre de progression de menuiserie.
    private static final Color WORKSHOP_PROGRESS_BACKGROUND = new Color(54, 67, 84, 228);
    // Couleur du remplissage de la barre de progression de menuiserie.
    private static final Color WORKSHOP_PROGRESS_FILL = new Color(105, 184, 219, 240);
    // Couleur du reflet léger ajouté sur la barre de progression de menuiserie.
    private static final Color WORKSHOP_PROGRESS_HIGHLIGHT = new Color(220, 246, 255, 210);
    // Couleur du texte de durée affiché près de la menuiserie.
    private static final Color WORKSHOP_PROGRESS_TEXT = new Color(255, 247, 227);
    // Ombre du texte de durée affiché près de la menuiserie.
    private static final Color WORKSHOP_PROGRESS_TEXT_SHADOW = new Color(24, 17, 13, 220);
    // Couleur du cadre de la barre de progression d'abattage.
    private static final Color TREE_PROGRESS_FRAME = new Color(33, 21, 12, 235);
    // Couleur du fond de la barre de progression d'abattage.
    private static final Color TREE_PROGRESS_BACKGROUND = new Color(76, 54, 31, 228);
    // Couleur du remplissage de la barre de progression d'abattage.
    private static final Color TREE_PROGRESS_FILL = new Color(197, 145, 76, 240);
    // Couleur du reflet léger ajouté sur la barre de progression d'abattage.
    private static final Color TREE_PROGRESS_HIGHLIGHT = new Color(255, 229, 169, 210);
    // Couleur du texte affiché au-dessus d'un arbre en cours de coupe.
    private static final Color TREE_PROGRESS_TEXT = new Color(255, 246, 223);
    // Ombre du texte affiché au-dessus d'un arbre en cours de coupe.
    private static final Color TREE_PROGRESS_TEXT_SHADOW = new Color(30, 18, 9, 220);
    // Couleur extérieure du flash quand un arbre tombe.
    private static final Color TREE_FELLING_FLASH_OUTER = new Color(224, 178, 93);
    // Couleur interne du flash quand un arbre tombe.
    private static final Color TREE_FELLING_FLASH_INNER = new Color(255, 231, 173);
    // Couleur des petits débris projetés quand un arbre tombe.
    private static final Color TREE_FELLING_DEBRIS = new Color(112, 74, 36);
    // Halo lumineux utilisé pendant la récompense en bois.
    private static final Color WOOD_REWARD_GLOW = new Color(255, 229, 149, 120);
    // Couleur du parachute stylisé utilisé pendant la récompense en bois.
    private static final Color WOOD_REWARD_PARACHUTE = new Color(255, 247, 225, 220);
    // Halo lumineux utilisé pendant la récompense en argent.
    private static final Color MONEY_REWARD_GLOW = new Color(255, 206, 108, 125);
    // Ressources partagées entre toutes les instances de cette vue.
    private static SharedAssets sharedAssets;

    // Panneau du champ utilisé pour projeter correctement le décor à l'écran.
    private final FieldPanel fieldPanel;
    // Gestionnaire des arbres affichés dans l'environnement.
    private final TreeManager treeManager;
    // Gestionnaire de construction de la menuiserie.
    private final WorkshopConstructionManager workshopConstructionManager;
    // Compteur d'argent lu pour les animations de récompense.
    private final Money playerMoney;
    // Barre supérieure utilisée comme destination visuelle des pièces.
    private final TopBarPanel topBarPanel;
    // Image du bâtiment principal de la ferme.
    private final Image barnImage;
    // Image de l'échoppe.
    private final Image stallImage;
    // Image de la menuiserie.
    private final Image workshopImage;
    // Image du pont construit sur la rivière.
    private final Image bridgeImage;
    // Image du morceau de bois utilisé dans les récompenses.
    private final Image woodImage;
    // Image de l'arbre principal.
    private final Image treeImage;
    // Variante visuelle du deuxième arbre.
    private final Image alternateTreeImage;
    // Image du saule pleureur.
    private final Image weepingWillowImage;
    // Image du tronc clair.
    private final Image trunkImage;
    // Image du tronc sombre.
    private final Image darkTrunkImage;
    // Ombre au sol du premier arbre.
    private final BufferedImage treeGroundShadowImage;
    // Ombre au sol du deuxième arbre.
    private final BufferedImage alternateTreeGroundShadowImage;
    // Ombre au sol du saule pleureur.
    private final BufferedImage weepingWillowGroundShadowImage;
    // Ombre au sol du tronc clair.
    private final BufferedImage trunkGroundShadowImage;
    // Ombre au sol du tronc sombre.
    private final BufferedImage darkTrunkGroundShadowImage;
    // Ombre générale du premier arbre.
    private final BufferedImage treeShadowImage;
    // Ombre générale du deuxième arbre.
    private final BufferedImage alternateTreeShadowImage;
    // Ombre générale du saule pleureur.
    private final BufferedImage weepingWillowShadowImage;
    // Ombre générale du tronc clair.
    private final BufferedImage trunkShadowImage;
    // Ombre générale du tronc sombre.
    private final BufferedImage darkTrunkShadowImage;
    // Police utilisée pour les compteurs et durées flottants.
    private final Font progressFont;

    /**
     * On prépare la vue du décor avec toutes les images partagées déjà chargées.
     */
    public EnvironmentView(
            FieldPanel fieldPanel,
            TreeManager treeManager,
            WorkshopConstructionManager workshopConstructionManager,
            Money playerMoney,
            TopBarPanel topBarPanel
    ) {
        SharedAssets assets = getSharedAssets();
        this.fieldPanel = fieldPanel;
        this.treeManager = treeManager;
        this.workshopConstructionManager = workshopConstructionManager;
        this.playerMoney = playerMoney;
        this.topBarPanel = topBarPanel;
        this.barnImage = assets.barnImage;
        this.stallImage = assets.stallImage;
        this.workshopImage = assets.workshopImage;
        this.bridgeImage = assets.bridgeImage;
        this.woodImage = assets.woodImage;
        this.treeImage = assets.treeImage;
        this.alternateTreeImage = assets.alternateTreeImage;
        this.weepingWillowImage = assets.weepingWillowImage;
        this.trunkImage = assets.trunkImage;
        this.darkTrunkImage = assets.darkTrunkImage;
        this.treeGroundShadowImage = assets.treeGroundShadowImage;
        this.alternateTreeGroundShadowImage = assets.alternateTreeGroundShadowImage;
        this.weepingWillowGroundShadowImage = assets.weepingWillowGroundShadowImage;
        this.trunkGroundShadowImage = assets.trunkGroundShadowImage;
        this.darkTrunkGroundShadowImage = assets.darkTrunkGroundShadowImage;
        this.treeShadowImage = assets.treeShadowImage;
        this.alternateTreeShadowImage = assets.alternateTreeShadowImage;
        this.weepingWillowShadowImage = assets.weepingWillowShadowImage;
        this.trunkShadowImage = assets.trunkShadowImage;
        this.darkTrunkShadowImage = assets.darkTrunkShadowImage;
        this.progressFont = assets.progressFont;
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    /**
     * Force le chargement des images partagées de décor avant le premier affichage.
     */
    public static void warmupSharedAssets() {
        getSharedAssets();
    }

    /**
     * Renvoie le paquet d'images et de polices communes à toutes les instances de cette vue.
     */
    private static synchronized SharedAssets getSharedAssets() {
        if (sharedAssets == null) {
            sharedAssets = new SharedAssets();
        }
        return sharedAssets;
    }

    /**
     * Dessine les éléments fixes du monde :
     * arbres, bâtiments, ponts et différentes animations visuelles liées au décor.
     */
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
        if (stallImage != null) {
            Rectangle stallBounds = fieldPanel.getStallScreenBounds();
            if (stallBounds != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(
                        stallImage,
                        stallBounds.x,
                        stallBounds.y,
                        stallBounds.width,
                        stallBounds.height,
                        null
                );
            }
        }

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
     * Dessine tous les ponts déjà placés sur la carte.
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
     * Dessine la barre de progression de construction du pont au-dessus de la menuiserie.
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
     * Dessine une barre de progression au-dessus de chaque arbre en train d'être coupé.
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
     * Dessine les petits effets lumineux et les éclats qui apparaissent
     * quand un arbre vient d'être abattu.
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
     * Dessine les animations des morceaux de bois qui volent jusqu'à l'inventaire.
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
     * Dessine les animations des pièces qui montent jusqu'au compteur d'argent.
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

    /**
     * Dessine tous les arbres visibles avec leur image et leurs ombres.
     */
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

    /**
     * Calcule le rectangle exact à l'écran occupé par un arbre ou un tronc.
     */
    private Rectangle getRenderedTreeBounds(TreeInstance tree, Rectangle cellBounds) {
        if (tree == null || cellBounds == null) {
            return null;
        }

        if (tree.isMature()) {
            return computeMatureTreeBounds(getMatureTreeImage(tree), cellBounds, tree.usesWeepingWillowSprite());
        }

        return computeTreeBounds(getTrunkImage(tree), cellBounds, TreeGeometry.TRUNK_TILE_SCALE, 0.50, 0.50);
    }

    /**
     * Renvoie l'image à afficher pour un arbre :
     * arbre adulte ou tronc selon son état.
     */
    private Image getDisplayedTreeImage(TreeInstance tree) {
        return tree != null && tree.isMature() ? getMatureTreeImage(tree) : getTrunkImage(tree);
    }

    /**
     * Calcule la zone d'affichage d'un arbre adulte dans sa case.
     */
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
     * Calcule où une image doit être dessinée dans une case,
     * avec son échelle et son point d'ancrage.
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

    /**
     * Dessine une image d'arbre ou de tronc dans le rectangle prévu.
     */
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
     * Dessine l'ombre aplatie d'un arbre directement au sol.
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
     * Dessine une ombre légère derrière tout l'arbre pour mieux le détacher du décor.
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

    /**
     * Choisit l'image correcte pour un arbre adulte selon sa variante visuelle.
     */
    private Image getMatureTreeImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowImage != null) {
            return weepingWillowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeImage != null) {
            return alternateTreeImage;
        }

        return treeImage;
    }

    /**
     * Choisit l'ombre au sol adaptée à la variante de l'arbre adulte.
     */
    private BufferedImage getTreeGroundShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowGroundShadowImage != null) {
            return weepingWillowGroundShadowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeGroundShadowImage != null) {
            return alternateTreeGroundShadowImage;
        }

        return treeGroundShadowImage;
    }

    /**
     * Choisit l'ombre générale adaptée à la variante de l'arbre adulte.
     */
    private BufferedImage getTreeShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && weepingWillowShadowImage != null) {
            return weepingWillowShadowImage;
        }

        if (tree.usesAlternateMatureSprite() && alternateTreeShadowImage != null) {
            return alternateTreeShadowImage;
        }

        return treeShadowImage;
    }

    /**
     * Choisit l'image de tronc adaptée à la variante de l'arbre.
     */
    private Image getTrunkImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkImage != null) {
            return darkTrunkImage;
        }

        return trunkImage;
    }

    /**
     * Choisit l'ombre au sol adaptée à un tronc.
     */
    private BufferedImage getTrunkGroundShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkGroundShadowImage != null) {
            return darkTrunkGroundShadowImage;
        }

        return trunkGroundShadowImage;
    }

    /**
     * Choisit l'ombre générale adaptée à un tronc.
     */
    private BufferedImage getTrunkShadowImage(TreeInstance tree) {
        if (tree.usesWeepingWillowSprite() && darkTrunkShadowImage != null) {
            return darkTrunkShadowImage;
        }

        return trunkShadowImage;
    }

    /**
     * Fabrique une silhouette colorée à partir d'une image existante.
     * Cette méthode sert à créer les différentes ombres des arbres.
     */
    private static BufferedImage createShadowImage(Image image, Color shadowColor) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage shadowImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D shadowGraphics = shadowImage.createGraphics();
        shadowGraphics.drawImage(image, 0, 0, null);
        shadowGraphics.setComposite(AlphaComposite.SrcIn);
        shadowGraphics.setColor(shadowColor);
        shadowGraphics.fillRect(0, 0, width, height);
        shadowGraphics.dispose();
        return shadowImage;
    }

    /**
     * Regroupe toutes les images de décor partagées entre les différentes parties.
     */
    private static final class SharedAssets {
        // Image du bâtiment principal de la ferme.
        private final Image barnImage = ImageLoader.load("/assets/barn.png");
        // Image de l'échoppe.
        private final Image stallImage = ImageLoader.load("/assets/echoppe.png");
        // Image de la menuiserie.
        private final Image workshopImage = ImageLoader.load("/assets/menuiserie.png");
        // Image du pont.
        private final Image bridgeImage = ImageLoader.load("/assets/bridge.png");
        // Image du morceau de bois.
        private final Image woodImage = ImageLoader.load("/assets/wood.png");
        // Image du premier arbre.
        private final Image treeImage = ImageLoader.load("/assets/arbre.png");
        // Image de la variante d'arbre secondaire.
        private final Image alternateTreeImage = ImageLoader.load("/assets/arbre2.png");
        // Image du saule pleureur.
        private final Image weepingWillowImage = ImageLoader.load("/assets/Saule pleureur.png");
        // Image du tronc clair.
        private final Image trunkImage = ImageLoader.load("/assets/tronc_arbre.png");
        // Image du tronc sombre.
        private final Image darkTrunkImage = ImageLoader.load("/assets/tronc_sombre.png");
        // Ombre au sol du premier arbre.
        private final BufferedImage treeGroundShadowImage = createShadowImage(treeImage, TREE_GROUND_SHADOW_COLOR);
        // Ombre au sol du deuxième arbre.
        private final BufferedImage alternateTreeGroundShadowImage = createShadowImage(alternateTreeImage, TREE_GROUND_SHADOW_COLOR);
        // Ombre au sol du saule pleureur.
        private final BufferedImage weepingWillowGroundShadowImage = createShadowImage(weepingWillowImage, TREE_GROUND_SHADOW_COLOR);
        // Ombre au sol du tronc clair.
        private final BufferedImage trunkGroundShadowImage = createShadowImage(trunkImage, TREE_GROUND_SHADOW_COLOR);
        // Ombre au sol du tronc sombre.
        private final BufferedImage darkTrunkGroundShadowImage = createShadowImage(darkTrunkImage, TREE_GROUND_SHADOW_COLOR);
        // Ombre générale du premier arbre.
        private final BufferedImage treeShadowImage = createShadowImage(treeImage, TREE_GLOBAL_SHADOW_COLOR);
        // Ombre générale du deuxième arbre.
        private final BufferedImage alternateTreeShadowImage = createShadowImage(alternateTreeImage, TREE_GLOBAL_SHADOW_COLOR);
        // Ombre générale du saule pleureur.
        private final BufferedImage weepingWillowShadowImage = createShadowImage(weepingWillowImage, TREE_GLOBAL_SHADOW_COLOR);
        // Ombre générale du tronc clair.
        private final BufferedImage trunkShadowImage = createShadowImage(trunkImage, TREE_GLOBAL_SHADOW_COLOR);
        // Ombre générale du tronc sombre.
        private final BufferedImage darkTrunkShadowImage = createShadowImage(darkTrunkImage, TREE_GLOBAL_SHADOW_COLOR);
        // Police partagée par les petites infos flottantes du décor.
        private final Font progressFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
    }

    /**
     * Convertit la zone du champ dans le repère de cette vue.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    /**
     * Renvoie la case écran d'un effet lié à une case du terrain,
     * ou `null` si l'effet est déjà terminé.
     */
    private Rectangle resolveGridEffectCellBounds(int gridX, int gridY, double progress) {
        if (progress >= 1.0) {
            return null;
        }

        return fieldPanel.getCellBounds(gridX, gridY);
    }

    /**
     * Dessine l'explosion visuelle très courte qui apparaît quand un arbre tombe.
     */
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

        RewardAnimationUtils.drawCenteredGlow(
                effectGraphics,
                centerX,
                centerY,
                outerRadius * 2,
                TREE_FELLING_FLASH_OUTER,
                outerAlpha
        );
        RewardAnimationUtils.drawCenteredGlow(
                effectGraphics,
                centerX,
                centerY,
                innerRadius * 2,
                TREE_FELLING_FLASH_INNER,
                innerAlpha
        );
        drawStandardBurstShards(effectGraphics, centerX, centerY, debrisDistance, debrisSize, progress);
        effectGraphics.dispose();
    }

    /**
     * Dessine l'animation d'un morceau de bois qui part d'un arbre et rejoint l'inventaire.
     */
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
        Point current = RewardAnimationUtils.computeArcPosition(start.x, start.y, destinationX, destinationY, progress, 52.0);
        int iconSize = 22;
        int iconX = current.x - (iconSize / 2);
        int iconY = current.y - (iconSize / 2);

        Graphics2D rewardGraphics = (Graphics2D) g2.create();
        rewardGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        RewardAnimationUtils.drawCenteredGlow(
                rewardGraphics,
                current.x,
                current.y,
                28,
                WOOD_REWARD_GLOW,
                (int) Math.round(110 * (1.0 - (progress * 0.45)))
        );
        drawWoodRewardIcon(rewardGraphics, iconX, iconY, iconSize);
        RewardAnimationUtils.drawParachuteCanopy(rewardGraphics, current.x, iconY - 2, progress, WOOD_REWARD_PARACHUTE);
        rewardGraphics.dispose();
    }

    /**
     * Dessine l'animation d'une récompense en argent qui rejoint le compteur du HUD.
     */
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
        Point current = RewardAnimationUtils.computeArcPosition(startX, startY, destinationX, destinationY, progress, 46.0);
        int pixelSize = 6;
        int iconWidth = ProductPixelArt.getCoinArtWidth(pixelSize);
        int iconHeight = ProductPixelArt.getCoinArtHeight(pixelSize);
        int iconX = current.x - (iconWidth / 2);
        int iconY = current.y - (iconHeight / 2);

        Graphics2D rewardGraphics = (Graphics2D) g2.create();
        RewardAnimationUtils.drawCenteredGlow(
                rewardGraphics,
                current.x,
                current.y,
                36,
                MONEY_REWARD_GLOW,
                (int) Math.round(105 * (1.0 - (progress * 0.35)))
        );
        ProductPixelArt.drawCoinResource(rewardGraphics, iconX, iconY, pixelSize);
        rewardGraphics.dispose();
    }

    /**
     * Dessine un petit débris carré projeté lors de la chute d'un arbre.
     */
    private void drawTreeShard(Graphics2D g2, int centerX, int centerY, int directionX, int directionY, int distance, int size, double progress) {
        int drawX = centerX + (directionX * distance) - (size / 2);
        int drawY = centerY + (directionY * distance) - (size / 2);
        int alpha = (int) Math.round(205 * (1.0 - progress));
        g2.setColor(RewardAnimationUtils.withAlpha(TREE_FELLING_DEBRIS, alpha));
        g2.fillRect(drawX, drawY, size, size);
    }

    /**
     * Dessine plusieurs débris autour du centre d'un effet de chute d'arbre.
     */
    private void drawStandardBurstShards(Graphics2D g2, int centerX, int centerY, int debrisDistance, int debrisSize, double progress) {
        drawTreeShard(g2, centerX, centerY, -1, -1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 1, -1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, -1, 1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 1, 1, debrisDistance, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 0, -1, debrisDistance + 5, debrisSize, progress);
        drawTreeShard(g2, centerX, centerY, 0, 1, debrisDistance + 5, debrisSize, progress);
    }

    /**
     * Dessine l'icône de bois utilisée pendant l'animation de récompense.
     */
    private void drawWoodRewardIcon(Graphics2D g2, int iconX, int iconY, int iconSize) {
        if (woodImage != null) {
            g2.drawImage(woodImage, iconX, iconY, iconSize, iconSize, this);
            return;
        }

        ProductPixelArt.drawWoodResource(g2, iconX, iconY, 4);
    }

    /**
     * Écrit un texte avec une petite ombre pour qu'il reste lisible sur le décor.
     */
    private void drawShadowedText(Graphics2D g2, String text, int x, int y, Color textColor, Color shadowColor) {
        g2.setColor(shadowColor);
        g2.drawString(text, x + 1, y + 1);
        g2.setColor(textColor);
        g2.drawString(text, x, y);
    }

    /**
     * Renvoie le point situé au centre d'un rectangle.
     */
    private Point getBoundsCenter(Rectangle bounds) {
        return new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
    }

}
