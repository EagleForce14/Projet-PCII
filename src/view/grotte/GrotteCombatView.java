package view.grotte;

import model.grotte.combat.CaveInventoryRewardEffect;
import model.grotte.combat.CaveCombatModel;
import model.grotte.combat.CaveProjectile;
import model.grotte.combat.CaveProjectileOwner;
import model.grotte.drop.CaveDrop;
import model.grotte.drop.CaveDropDefinition;
import view.CustomFontLoader;
import view.HudProgressBarPainter;
import view.InventoryStatusOverlay;
import view.ProductPixelArt;
import view.RewardAnimationUtils;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Couche d'interface dédiée au combat de la grotte.

 * Elle affiche uniquement des informations de gameplay :
 * projectiles, flash de dégâts et petite barre de vie du joueur.
 */
public final class GrotteCombatView extends JPanel {
    // Police pixel commune aux petits overlays de grotte.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    // Halo des projectiles du joueur.
    private static final Color PLAYER_PROJECTILE_GLOW = new Color(72, 214, 120, 96);
    // Noyau des projectiles du joueur.
    private static final Color PLAYER_PROJECTILE_CORE = new Color(156, 255, 188, 255);
    // Halo des projectiles ennemis.
    private static final Color ENEMY_PROJECTILE_GLOW = new Color(255, 88, 88, 88);
    // Noyau des projectiles ennemis.
    private static final Color ENEMY_PROJECTILE_CORE = new Color(255, 146, 146, 255);
    // Voile rouge affiché quand le joueur est touché.
    private static final Color HIT_VIGNETTE = new Color(170, 20, 20, 150);
    // Fond de la petite barre de vie du joueur.
    private static final Color PLAYER_HEALTH_BACKGROUND = new Color(28, 14, 16, 196);
    // Remplissage de la petite barre de vie du joueur.
    private static final Color PLAYER_HEALTH_FILL = new Color(212, 74, 74, 235);
    // Bordure de la petite barre de vie du joueur.
    private static final Color PLAYER_HEALTH_BORDER = new Color(245, 208, 184, 214);
    // Ombre des drops posés au sol.
    private static final Color DROP_SHADOW = new Color(8, 6, 10, 104);
    // Léger halo des drops ramassables.
    private static final Color DROP_GLOW = new Color(255, 233, 176, 62);
    // Halo des récompenses qui volent vers l'inventaire.
    private static final Color REWARD_GLOW = new Color(255, 229, 149, 120);
    // Couleur du petit parachute décoratif des récompenses.
    private static final Color REWARD_PARACHUTE = new Color(255, 247, 225, 220);
    // Ombre discrète du popup d'aide.
    private static final Color SHOOT_HINT_SHADOW = new Color(0, 0, 0, 86);
    // Fond principal du popup d'aide.
    private static final Color SHOOT_HINT_BACKGROUND = new Color(33, 24, 19, 238);
    // Bordure fixe du popup d'aide.
    private static final Color SHOOT_HINT_BORDER = new Color(236, 203, 132, 255);
    // Couleur du texte principal.
    private static final Color SHOOT_HINT_TEXT = new Color(255, 245, 225, 255);
    // Couleur du texte secondaire.
    private static final Color SHOOT_HINT_MUTED = new Color(226, 214, 192, 236);
    // Fond de la capsule "ESPACE".
    private static final Color SHOOT_HINT_KEY_BACKGROUND = new Color(91, 67, 39, 245);
    // Bordure de la capsule "ESPACE".
    private static final Color SHOOT_HINT_KEY_BORDER = new Color(255, 229, 170, 245);
    // Fond du bouton de fermeture.
    private static final Color SHOOT_HINT_CLOSE_BACKGROUND = new Color(72, 48, 29, 240);
    // Couleur de la croix de fermeture.
    private static final Color SHOOT_HINT_CLOSE_TEXT = new Color(255, 243, 218, 255);

    // Modèle de combat dont on lit l'état à afficher.
    private final CaveCombatModel combatModel;
    // Carte de grotte utilisée pour convertir les cases et repères logiques.
    private final GrotteFieldPanel mapPanel;
    // Police du petit titre.
    private final Font shootHintTitleFont;
    // Police de la consigne.
    private final Font shootHintBodyFont;
    // Police de la capsule clavier.
    private final Font shootHintKeyFont;
    // Le hint est refermé localement mais réouvert à chaque nouvelle entrée.
    private boolean shootHintVisible;
    // Zone cliquable de la croix de fermeture.
    private Rectangle shootHintCloseBounds;

    /**
     * On prépare la couche d'affichage du combat de grotte.
     */
    public GrotteCombatView(CaveCombatModel combatModel, GrotteFieldPanel mapPanel) {
        this.combatModel = combatModel;
        this.mapPanel = mapPanel;
        this.shootHintTitleFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
        this.shootHintBodyFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
        this.shootHintKeyFont = CustomFontLoader.loadFont(FONT_PATH, 7.0f);
        this.shootHintVisible = true;
        this.shootHintCloseBounds = new Rectangle();
        setOpaque(false);
        setDoubleBuffered(true);
    }

    /**
     * Réaffiche explicitement l'aide quand le joueur revient dans la grotte.
     */
    public void showShootHint() {
        shootHintVisible = true;
        repaint();
    }

    /**
     * Ferme le popup seulement si le clic tombe sur la croix.
     */
    public boolean handleShootHintCloseClick(Point clickPoint) {
        if (!shootHintVisible || clickPoint == null || !shootHintCloseBounds.contains(clickPoint)) {
            return false;
        }

        shootHintVisible = false;
        repaint();
        return true;
    }

    /**
     * On convertit ici les bornes du terrain de grotte dans le repère de cette vue de combat.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(mapPanel.getMapComponent(), mapPanel.getFieldBounds(), this);
    }

    /**
     * On dessine les éléments de combat superposés à la grotte dans le bon ordre visuel.
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle fieldBounds = getFieldBoundsInView();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        drawDrops(g2d, centerX, centerY);
        drawProjectiles(g2d, centerX, centerY);
        drawInventoryRewardEffects(g2d, centerX, centerY, fieldBounds);
        drawPlayerHitVignette(g2d);
        drawPlayerHealthBar(g2d, centerX, centerY);
        drawShootHint(g2d);

        g2d.dispose();
    }

    /**
     * Le hint reste compact et haut perché pour informer sans masquer la zone de jeu.
     */
    private void drawShootHint(Graphics2D g2d) {
        if (!shootHintVisible || getWidth() <= 0 || getHeight() <= 0) {
            shootHintCloseBounds.setBounds(0, 0, 0, 0);
            return;
        }

        int cardWidth = Math.min(292, Math.max(214, getWidth() - 32));
        int cardHeight = 52;
        int cardX = (getWidth() - cardWidth) / 2;
        int cardY = 14;
        int closeSize = 18;
        int closeX = cardX + cardWidth - closeSize - 10;
        int closeY = cardY + 8;
        shootHintCloseBounds.setBounds(closeX, closeY, closeSize, closeSize);

        g2d.setColor(SHOOT_HINT_SHADOW);
        g2d.fillRoundRect(cardX + 3, cardY + 4, cardWidth, cardHeight, 18, 18);

        g2d.setColor(SHOOT_HINT_BORDER);
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 18, 18);
        g2d.setColor(SHOOT_HINT_BACKGROUND);
        g2d.fillRoundRect(cardX + 1, cardY + 1, cardWidth - 2, cardHeight - 2, 16, 16);

        g2d.setColor(SHOOT_HINT_CLOSE_BACKGROUND);
        g2d.fillRoundRect(closeX, closeY, closeSize, closeSize, 8, 8);
        g2d.setFont(shootHintBodyFont);
        g2d.setColor(SHOOT_HINT_CLOSE_TEXT);
        java.awt.FontMetrics closeMetrics = g2d.getFontMetrics();
        int closeTextX = closeX + ((closeSize - closeMetrics.stringWidth("x")) / 2);
        int closeTextY = closeY + ((closeSize - closeMetrics.getHeight()) / 2) + closeMetrics.getAscent();
        g2d.drawString("x", closeTextX, closeTextY);

        int textX = cardX + 24;
        g2d.setFont(shootHintTitleFont);
        g2d.setColor(SHOOT_HINT_TEXT);
        g2d.drawString("Tir dans la grotte", textX, cardY + 19);

        String prefix = "Appuyez sur";
        String keyLabel = "ESPACE";
        String suffix = "pour tirer";
        int baselineY = cardY + 37;

        g2d.setFont(shootHintBodyFont);
        g2d.setColor(SHOOT_HINT_MUTED);
        g2d.drawString(prefix, textX, baselineY);

        int prefixWidth = g2d.getFontMetrics().stringWidth(prefix);
        int keyX = textX + prefixWidth + 8;
        int keyY = baselineY - 11;
        int keyWidth = g2d.getFontMetrics(shootHintKeyFont).stringWidth(keyLabel) + 12;
        int keyHeight = 16;

        g2d.setColor(SHOOT_HINT_KEY_BACKGROUND);
        g2d.fillRoundRect(keyX, keyY, keyWidth, keyHeight, 8, 8);
        g2d.setFont(shootHintKeyFont);
        g2d.setColor(SHOOT_HINT_TEXT);
        java.awt.FontMetrics keyMetrics = g2d.getFontMetrics();
        int keyTextX = keyX + ((keyWidth - keyMetrics.stringWidth(keyLabel)) / 2);
        int keyTextY = keyY + ((keyHeight - keyMetrics.getHeight()) / 2) + keyMetrics.getAscent();
        g2d.drawString(keyLabel, keyTextX, keyTextY);

        int suffixX = keyX + keyWidth + 8;
        g2d.setFont(shootHintBodyFont);
        g2d.setColor(SHOOT_HINT_MUTED);
        g2d.drawString(suffix, suffixX, baselineY);
    }

    /**
     * Les drops restent visuellement modestes :
     * une petite ombre pour les ancrer au sol, un léger flottement,
     * puis le sprite de l'objet centré dans la case.
     */
    private void drawDrops(Graphics2D g2d, int centerX, int centerY) {
        long now = System.currentTimeMillis();

        for (CaveDrop drop : combatModel.getDrops()) {
            Rectangle logicalCellBounds = mapPanel.getLogicalCellBounds(drop.getGridX(), drop.getGridY());
            if (logicalCellBounds == null) {
                continue;
            }

            int cellCenterX = centerX + logicalCellBounds.x + (logicalCellBounds.width / 2);
            int cellCenterY = centerY + logicalCellBounds.y + (logicalCellBounds.height / 2);
            int cellBottomY = centerY + logicalCellBounds.y + logicalCellBounds.height;
            int pixelSize = Math.max(2, Math.min(logicalCellBounds.width, logicalCellBounds.height) / 9);
            int bobOffset = (int) Math.round(Math.sin((now / 170.0) + (drop.getGridX() * 0.9) + (drop.getGridY() * 0.45)) * 2.0);

            CaveDropDefinition definition = drop.getDefinition();
            int artWidth = getDropArtWidth(definition, pixelSize);
            int artHeight = getDropArtHeight(definition, pixelSize);
            int drawX = cellCenterX - (artWidth / 2);
            int drawY = cellCenterY - (artHeight / 2) - Math.max(2, logicalCellBounds.height / 10) + bobOffset;

            int shadowWidth = Math.max(12, Math.min(logicalCellBounds.width - 6, Math.max(artWidth - 6, artWidth / 2)));
            int shadowHeight = Math.max(5, shadowWidth / 4);
            int shadowX = cellCenterX - (shadowWidth / 2);
            int shadowY = cellBottomY - shadowHeight - Math.max(3, logicalCellBounds.height / 8);
            g2d.setColor(DROP_SHADOW);
            g2d.fillOval(shadowX, shadowY, shadowWidth, shadowHeight);

            g2d.setColor(DROP_GLOW);
            g2d.fillOval(drawX - 2, drawY + artHeight - Math.max(4, pixelSize), artWidth + 4, Math.max(6, pixelSize + 2));
            drawDropArt(g2d, definition, drawX, drawY, pixelSize);
        }
    }

    /**
     * On dessine tous les projectiles actifs avec leur petite traînée lumineuse.
     */
    private void drawProjectiles(Graphics2D g2d, int centerX, int centerY) {
        for (CaveProjectile projectile : combatModel.getProjectiles()) {
            double projectileX = centerX + projectile.getX();
            double projectileY = centerY + projectile.getY();
            double previousX = centerX + projectile.getPreviousX();
            double previousY = centerY + projectile.getPreviousY();
            double directionX = projectileX - previousX;
            double directionY = projectileY - previousY;
            if (Math.abs(directionX) < 0.0001 && Math.abs(directionY) < 0.0001) {
                directionX = 1.0;
                directionY = 0.0;
            }

            double rotationRadians = Math.atan2(directionY, directionX);
            int baseSize = Math.max(projectile.getWidth(), projectile.getHeight());
            int trailLength = Math.max(10, baseSize + 12);
            int coreLength = Math.max(8, baseSize + 8);
            /*
             * On garde volontairement une épaisseur commune joueur/ennemis
             * pour que les tirs ennemis n'aient pas un rendu visuellement "lourd".
             */
            int coreThickness = 4;
            int glowThickness = coreThickness + 4;

            boolean playerProjectile = projectile.getOwner() == CaveProjectileOwner.PLAYER;
            g2d.setColor(playerProjectile ? PLAYER_PROJECTILE_GLOW : ENEMY_PROJECTILE_GLOW);
            g2d.setStroke(new BasicStroke(
                    Math.max(2.0f, glowThickness / 2.0f),
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g2d.drawLine(
                    (int) Math.round(previousX),
                    (int) Math.round(previousY),
                    (int) Math.round(projectileX),
                    (int) Math.round(projectileY)
            );

            Graphics2D projectileGraphics = (Graphics2D) g2d.create();
            projectileGraphics.translate(projectileX, projectileY);
            projectileGraphics.rotate(rotationRadians);
            projectileGraphics.setColor(playerProjectile ? PLAYER_PROJECTILE_GLOW : ENEMY_PROJECTILE_GLOW);
            projectileGraphics.fillRoundRect(
                    -(trailLength / 2),
                    -(glowThickness / 2),
                    trailLength,
                    glowThickness,
                    glowThickness,
                    glowThickness
            );

            projectileGraphics.setColor(playerProjectile ? PLAYER_PROJECTILE_CORE : ENEMY_PROJECTILE_CORE);
            projectileGraphics.fillRoundRect(
                    -(coreLength / 2),
                    -(coreThickness / 2),
                    coreLength,
                    coreThickness,
                    coreThickness,
                    coreThickness
            );
            projectileGraphics.dispose();
        }
    }

    /**
     * Réutilise la même logique visuelle que les récompenses de bois :
     * trajectoire arquée + petit parachute blanc.
     */
    private void drawInventoryRewardEffects(Graphics2D g2d, int centerX, int centerY, Rectangle fieldBounds) {
        long now = System.currentTimeMillis();

        for (CaveInventoryRewardEffect effect : combatModel.getActiveInventoryRewardEffects()) {
            if (effect == null) {
                continue;
            }

            int targetSlotIndex = resolveTargetSlotIndex(effect.getDefinition());
            if (targetSlotIndex < 0) {
                continue;
            }

            Rectangle destinationSlotBounds = InventoryStatusOverlay.computeSlotBounds(
                    targetSlotIndex,
                    fieldBounds,
                    getWidth(),
                    getHeight()
            );
            if (destinationSlotBounds == null) {
                continue;
            }

            Rectangle sourceCellBounds = mapPanel.getLogicalCellBounds(effect.getSourceGridX(), effect.getSourceGridY());
            if (sourceCellBounds == null) {
                continue;
            }

            double progress = effect.getProgress(now);
            int sourceX = centerX + sourceCellBounds.x + (sourceCellBounds.width / 2);
            int sourceY = centerY + sourceCellBounds.y + (sourceCellBounds.height / 2);
            int destinationX = destinationSlotBounds.x + (destinationSlotBounds.width / 2);
            int destinationY = destinationSlotBounds.y + (destinationSlotBounds.height / 2) - 4;
            java.awt.Point current = RewardAnimationUtils.computeArcPosition(
                    sourceX,
                    sourceY,
                    destinationX,
                    destinationY,
                    progress,
                    52.0
            );

            int pixelSize = 4;
            int iconWidth = getDropArtWidth(effect.getDefinition(), pixelSize);
            int iconHeight = getDropArtHeight(effect.getDefinition(), pixelSize);
            int iconX = current.x - (iconWidth / 2);
            int iconY = current.y - (iconHeight / 2);

            RewardAnimationUtils.drawCenteredGlow(
                    g2d,
                    current.x,
                    current.y,
                    28,
                    REWARD_GLOW,
                    (int) Math.round(110 * (1.0 - (progress * 0.45)))
            );
            drawDropArt(g2d, effect.getDefinition(), iconX, iconY, pixelSize);
            RewardAnimationUtils.drawParachuteCanopy(g2d, current.x, iconY - 2, progress, REWARD_PARACHUTE);
        }
    }

    /**
     * On associe un type de drop au slot d'inventaire qu'il doit rejoindre visuellement.
     */
    private int resolveTargetSlotIndex(CaveDropDefinition definition) {
        if (definition == null) {
            return -1;
        }
        if (definition.isWood()) {
            return InventoryStatusOverlay.getWoodSlotIndex();
        }
        if (definition.isSeed()) {
            return InventoryStatusOverlay.getSeedSlotIndex(definition.getSeedType());
        }
        if (definition.isFacility()) {
            return InventoryStatusOverlay.getFacilitySlotIndex(definition.getFacilityType());
        }
        return -1;
    }

    /**
     * On dessine le flash rouge périphérique qui signale un coup reçu par le joueur.
     */
    private void drawPlayerHitVignette(Graphics2D g2d) {
        double flashRatio = combatModel.getPlayerHitFlashRatio();
        if (flashRatio <= 0.0) {
            return;
        }

        int alpha = (int) Math.round(HIT_VIGNETTE.getAlpha() * flashRatio);
        Color vignetteColor = new Color(HIT_VIGNETTE.getRed(), HIT_VIGNETTE.getGreen(), HIT_VIGNETTE.getBlue(), alpha);
        int band = Math.max(34, Math.min(getWidth(), getHeight()) / 8);
        GrotteOverlayPainter.drawScreenVignette(g2d, getWidth(), getHeight(), band, vignetteColor);
    }

    /**
     * La barre suit directement la tête du joueur pour garder l'information
     * au centre de l'action au lieu de la déplacer dans un coin de l'écran.
     */
    private void drawPlayerHealthBar(Graphics2D g2d, int centerX, int centerY) {
        if (!combatModel.shouldDisplayPlayerHealthBar()) {
            return;
        }

        int barWidth = 38;
        int barHeight = 6;
        int drawX = (int) Math.round(centerX + combatModel.getPlayerX() - (barWidth / 2.0));
        int drawY = centerY + combatModel.getPlayerY() - 28;
        HudProgressBarPainter.paint(
                g2d,
                drawX,
                drawY,
                barWidth,
                barHeight,
                combatModel.getPlayerHealthRatio(),
                PLAYER_HEALTH_BORDER,
                PLAYER_HEALTH_BACKGROUND,
                PLAYER_HEALTH_FILL,
                new Color(255, 186, 186, 205)
        );
    }

    /**
     * On renvoie la largeur du pixel art à dessiner pour ce drop.
     */
    private int getDropArtWidth(CaveDropDefinition definition, int pixelSize) {
        if (definition == null) {
            return 0;
        }
        if (definition.isSeed()) {
            return ProductPixelArt.getMatureCultureArtWidth(definition.getSeedType(), pixelSize);
        }
        if (definition.isFacility()) {
            return ProductPixelArt.getFacilityArtWidth(definition.getFacilityType(), pixelSize);
        }
        if (definition.isWood()) {
            return ProductPixelArt.getWoodArtWidth(pixelSize);
        }
        return 0;
    }

    /**
     * On renvoie la hauteur du pixel art à dessiner pour ce drop.
     */
    private int getDropArtHeight(CaveDropDefinition definition, int pixelSize) {
        if (definition == null) {
            return 0;
        }
        if (definition.isSeed()) {
            return ProductPixelArt.getMatureCultureArtHeight(definition.getSeedType(), pixelSize);
        }
        if (definition.isFacility()) {
            return ProductPixelArt.getFacilityArtHeight(definition.getFacilityType(), pixelSize);
        }
        if (definition.isWood()) {
            return ProductPixelArt.getWoodArtHeight(pixelSize);
        }
        return 0;
    }

    /**
     * On dessine le bon pixel art selon la vraie nature du drop reçu.
     */
    private void drawDropArt(Graphics2D g2d, CaveDropDefinition definition, int drawX, int drawY, int pixelSize) {
        if (definition == null) {
            return;
        }
        if (definition.isSeed()) {
            ProductPixelArt.drawMatureCulture(g2d, definition.getSeedType(), drawX, drawY, pixelSize);
            return;
        }
        if (definition.isFacility()) {
            ProductPixelArt.drawFacility(g2d, definition.getFacilityType(), drawX, drawY, pixelSize);
            return;
        }
        if (definition.isWood()) {
            ProductPixelArt.drawWoodResource(g2d, drawX, drawY, pixelSize);
        }
    }

}
