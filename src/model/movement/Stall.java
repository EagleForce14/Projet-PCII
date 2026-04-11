package model.movement;

import model.culture.GrilleCulture;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Représente l'échoppe affichée à gauche de la boutique.
 * Son placement repart du même alignement vertical que la grange,
 * puis est volontairement décalé d'une ligne vers le bas
 * pour mieux occuper la zone gauche du décor.
 */
public final class Stall {
    private static final int DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN = 2;
    private static final String ASSET_PATH = "/assets/echoppe.png";
    private static final double HEIGHT_RATIO_TO_BARN = 1.02;
    private static final double GAP_RATIO_TO_BARN_WIDTH = 0.26;
    private static final int MIN_SCREEN_MARGIN = 18;
    private static final int VERTICAL_SHIFT_ROWS = 1;
    private static final double HITBOX_WIDTH_RATIO = 0.82;
    private static final double HITBOX_HEIGHT_RATIO = 0.71;
    private static final double HITBOX_BOTTOM_INSET_RATIO = 0.04;
    private static final Dimension SPRITE_SIZE = BuildingGeometry.loadSpriteSize(
            Stall.class,
            ASSET_PATH,
            new Dimension(1412, 852)
    );

    private Stall() {}

    /**
     * Place l'échoppe à gauche de la grange.
     * On calcule d'abord une position "de base" alignée sur le bas de la grange,
     * puis on recentre le bâtiment dans la zone gauche et on l'abaisse d'une ligne.
     */
    public static Rectangle getDrawBounds(Rectangle fieldLogicalBounds) {
        if (fieldLogicalBounds == null || fieldLogicalBounds.width <= 0 || fieldLogicalBounds.height <= 0) {
            return null;
        }

        Rectangle barnDrawBounds = new Rectangle(Barn.getDrawX(), Barn.Y, Barn.WIDTH, Barn.HEIGHT);
        Rectangle preferredBounds = BuildingGeometry.buildBarnSideBuildingDrawBounds(
                fieldLogicalBounds,
                barnDrawBounds,
                SPRITE_SIZE,
                HEIGHT_RATIO_TO_BARN,
                GAP_RATIO_TO_BARN_WIDTH,
                MIN_SCREEN_MARGIN,
                true
        );
        if (preferredBounds == null) {
            return null;
        }

        int tileSize = Math.max(1, fieldLogicalBounds.width / GrilleCulture.LARGEUR_GRILLE);
        int leftmostBarnColumn = resolveLeftmostBarnColumn(fieldLogicalBounds, barnDrawBounds, tileSize);
        int riverColumn = Math.max(0, leftmostBarnColumn - DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN);
        int leftZoneMinX = fieldLogicalBounds.x + MIN_SCREEN_MARGIN;
        int leftZoneMaxX = fieldLogicalBounds.x + (riverColumn * tileSize) - preferredBounds.width;
        if (leftZoneMaxX < leftZoneMinX) {
            return shiftDown(preferredBounds, tileSize);
        }

        int centeredX = leftZoneMinX + ((leftZoneMaxX - leftZoneMinX) / 2);
        return shiftDown(new Rectangle(centeredX, preferredBounds.y, preferredBounds.width, preferredBounds.height), tileSize);
    }

    public static Rectangle getCollisionBounds(Rectangle fieldLogicalBounds) {
        return BuildingGeometry.buildCollisionBounds(
                getDrawBounds(fieldLogicalBounds),
                HITBOX_WIDTH_RATIO,
                HITBOX_HEIGHT_RATIO,
                HITBOX_BOTTOM_INSET_RATIO
        );
    }

    /**
     * Reprend la même idée que le layout prédéfini :
     * on cherche la première colonne réellement touchée par la grange
     * pour retrouver ensuite la zone gauche située avant la rivière décorative.
     */
    private static int resolveLeftmostBarnColumn(Rectangle fieldLogicalBounds, Rectangle barnDrawBounds, int tileSize) {
        if (fieldLogicalBounds == null || barnDrawBounds == null || tileSize <= 0) {
            return 0;
        }

        int relativeBarnLeft = barnDrawBounds.x - fieldLogicalBounds.x;
        int leftmostColumn = Math.floorDiv(relativeBarnLeft, tileSize);
        return Math.max(0, Math.min(leftmostColumn, GrilleCulture.LARGEUR_GRILLE - 1));
    }

    private static Rectangle shiftDown(Rectangle bounds, int tileSize) {
        if (bounds == null) {
            return null;
        }

        return new Rectangle(
                bounds.x,
                bounds.y + (VERTICAL_SHIFT_ROWS * Math.max(1, tileSize)),
                bounds.width,
                bounds.height
        );
    }
}
