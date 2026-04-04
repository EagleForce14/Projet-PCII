package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Représente la menuiserie affichée à droite de la grange.
 * Son placement et sa collision restent alignés avec la géométrie réelle du sprite.
 */
public final class Workshop {
    private static final String ASSET_PATH = "/assets/menuiserie.png";
    private static final double HEIGHT_RATIO_TO_BARN = 1.08;
    private static final double GAP_RATIO_TO_BARN_WIDTH = 0.08;
    private static final int MIN_SCREEN_MARGIN = 18;
    private static final double HITBOX_WIDTH_RATIO = 0.88;
    private static final double HITBOX_HEIGHT_RATIO = 0.62;
    private static final double HITBOX_BOTTOM_INSET_RATIO = 0.02;
    private static final Dimension SPRITE_SIZE = BuildingGeometry.loadSpriteSize(
            Workshop.class,
            ASSET_PATH,
            new Dimension(1024, 1226)
    );

    private Workshop() {}

    /**
     * Place la menuiserie à droite de la grange en gardant sa base sur le même sol.
     * Les coordonnées renvoyées sont dans le repère logique centré du champ.
     */
    public static Rectangle getDrawBounds(Rectangle fieldLogicalBounds) {
        if (fieldLogicalBounds == null || fieldLogicalBounds.width <= 0 || fieldLogicalBounds.height <= 0) {
            return null;
        }

        Rectangle barnDrawBounds = new Rectangle(Barn.getDrawX(), Barn.Y, Barn.WIDTH, Barn.HEIGHT);
        int targetHeight = Math.max(1, (int) Math.round(barnDrawBounds.height * HEIGHT_RATIO_TO_BARN));
        double scale = (double) targetHeight / SPRITE_SIZE.height;
        int drawWidth = Math.max(1, (int) Math.round(SPRITE_SIZE.width * scale));
        int drawHeight = Math.max(1, (int) Math.round(SPRITE_SIZE.height * scale));
        int gap = Math.max(18, (int) Math.round(barnDrawBounds.width * GAP_RATIO_TO_BARN_WIDTH));
        int maxX = fieldLogicalBounds.x + fieldLogicalBounds.width - MIN_SCREEN_MARGIN - drawWidth;
        int drawX = Math.min(maxX, barnDrawBounds.x + barnDrawBounds.width + gap);
        int drawY = barnDrawBounds.y + barnDrawBounds.height - drawHeight;

        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    public static Rectangle getCollisionBounds(Rectangle fieldLogicalBounds) {
        return BuildingGeometry.buildCollisionBounds(
                getDrawBounds(fieldLogicalBounds),
                HITBOX_WIDTH_RATIO,
                HITBOX_HEIGHT_RATIO,
                HITBOX_BOTTOM_INSET_RATIO
        );
    }
}
