package model.environment;

import java.awt.Rectangle;

/**
 * Regroupe les constantes géométriques des arbres.
 * Le rendu, le placement et les collisions s'appuient ainsi sur les mêmes ratios.
 */
public final class TreeGeometry {
    public static final double TRUNK_TILE_SCALE = 0.80;
    public static final double CLASSIC_MATURE_TREE_TILE_SCALE = 3.35;
    public static final double CLASSIC_MATURE_TREE_ANCHOR_X_RATIO = 0.50;
    public static final double CLASSIC_MATURE_TREE_ANCHOR_Y_RATIO = 0.76;

    /*
     * Variables de réglage du saule pleureur :
     * ce sont celles à ajuster si son rendu ou sa collision doivent être affinés.
     */
    public static final double WEEPING_WILLOW_TILE_SCALE = 3.35;
    public static final double WEEPING_WILLOW_ANCHOR_X_RATIO = 0.50;
    public static final double WEEPING_WILLOW_ANCHOR_Y_RATIO = 0.76;

    // Le tronc garde une collision compacte.
    private static final double TRUNK_HITBOX_WIDTH_RATIO = 0.82;
    private static final double TRUNK_HITBOX_HEIGHT_RATIO = 0.92;
    private static final int TRUNK_HITBOX_Y_OFFSET = 0;

    // Les cases bloquées sont gérées séparément :
    // la collision du grand arbre peut donc rester plus proche du tronc visible.
    private static final double CLASSIC_MATURE_HITBOX_WIDTH_RATIO = 2.0;
    private static final double CLASSIC_MATURE_HITBOX_HEIGHT_RATIO = 2.5;
    private static final int CLASSIC_MATURE_HITBOX_Y_OFFSET = -45;

    private static final double WEEPING_WILLOW_HITBOX_WIDTH_RATIO = 1.70;
    private static final double WEEPING_WILLOW_HITBOX_HEIGHT_RATIO = 2.65;
    private static final int WEEPING_WILLOW_HITBOX_Y_OFFSET = -50;

    private TreeGeometry() {
    }

    /**
     * Construit l'enveloppe maximale allouée au sprite dans le repère d'une case.
     * Le vrai sprite peut être un peu plus petit selon son ratio, mais jamais plus grand.
     */
    public static Rectangle buildCenteredScaledBounds(Rectangle cellBounds, double tileScale) {
        return buildAnchoredScaledBounds(cellBounds, tileScale, 0.50, 0.50);
    }

    public static Rectangle buildMatureTreeBounds(Rectangle cellBounds, boolean weepingWillow) {
        return buildAnchoredScaledBounds(
                cellBounds,
                getMatureTreeTileScale(weepingWillow),
                getMatureTreeAnchorXRatio(weepingWillow),
                getMatureTreeAnchorYRatio(weepingWillow)
        );
    }

    /**
     * Permet d'accrocher un grand sprite à la case qui lui sert de racine.
     * Pour l'arbre mature, on aligne ainsi le centre de son tronc sur le centre
     * de la case, au lieu de centrer toute la canopée.
     */
    public static Rectangle buildAnchoredScaledBounds(
            Rectangle cellBounds,
            double tileScale,
            double anchorXRatio,
            double anchorYRatio
    ) {
        if (cellBounds == null) {
            return null;
        }

        int width = Math.max(1, (int) Math.round(cellBounds.width * tileScale));
        int height = Math.max(1, (int) Math.round(cellBounds.height * tileScale));
        int centerX = cellBounds.x + (cellBounds.width / 2);
        int centerY = cellBounds.y + (cellBounds.height / 2);
        int x = (int) Math.round(centerX - (width * anchorXRatio));
        int y = (int) Math.round(centerY - (height * anchorYRatio));
        return new Rectangle(x, y, width, height);
    }

    /**
     * La hitbox est centrée comme l'arbre lui-même.
     */
    public static Rectangle buildTreeHitbox(Rectangle cellBounds, boolean mature, boolean weepingWillow) {
        if (cellBounds == null) {
            return null;
        }

        double widthRatio = mature
                ? getMatureHitboxWidthRatio(weepingWillow)
                : TRUNK_HITBOX_WIDTH_RATIO;
        double heightRatio = mature
                ? getMatureHitboxHeightRatio(weepingWillow)
                : TRUNK_HITBOX_HEIGHT_RATIO;
        int width = Math.max(1, (int) Math.round(cellBounds.width * widthRatio));
        int height = Math.max(1, (int) Math.round(cellBounds.height * heightRatio));
        int x = cellBounds.x + ((cellBounds.width - width) / 2);
        int y = cellBounds.y + ((cellBounds.height - height) / 2) + getHitboxYOffset(mature, weepingWillow);
        return new Rectangle(x, y, width, height);
    }

    public static double getMatureTreeTileScale(boolean weepingWillow) {
        return weepingWillow ? WEEPING_WILLOW_TILE_SCALE : CLASSIC_MATURE_TREE_TILE_SCALE;
    }

    public static double getMatureTreeAnchorXRatio(boolean weepingWillow) {
        return weepingWillow ? WEEPING_WILLOW_ANCHOR_X_RATIO : CLASSIC_MATURE_TREE_ANCHOR_X_RATIO;
    }

    public static double getMatureTreeAnchorYRatio(boolean weepingWillow) {
        return weepingWillow ? WEEPING_WILLOW_ANCHOR_Y_RATIO : CLASSIC_MATURE_TREE_ANCHOR_Y_RATIO;
    }

    private static double getMatureHitboxWidthRatio(boolean weepingWillow) {
        return weepingWillow ? WEEPING_WILLOW_HITBOX_WIDTH_RATIO : CLASSIC_MATURE_HITBOX_WIDTH_RATIO;
    }

    private static double getMatureHitboxHeightRatio(boolean weepingWillow) {
        return weepingWillow ? WEEPING_WILLOW_HITBOX_HEIGHT_RATIO : CLASSIC_MATURE_HITBOX_HEIGHT_RATIO;
    }

    private static int getHitboxYOffset(boolean mature, boolean weepingWillow) {
        if (!mature) {
            return TRUNK_HITBOX_Y_OFFSET;
        }

        return weepingWillow ? WEEPING_WILLOW_HITBOX_Y_OFFSET : CLASSIC_MATURE_HITBOX_Y_OFFSET;
    }
}
