package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Représente la menuiserie affichée à droite de la boutique principale.
 * Son placement et sa collision restent alignés avec la géométrie réelle du sprite.
 */
public final class Workshop {
    // Chemin du sprite de la menuiserie.
    private static final String ASSET_PATH = "/assets/menuiserie.png";
    // Hauteur affichée de la menuiserie par rapport à celle de la boutique.
    private static final double HEIGHT_RATIO_TO_BARN = 1.08;
    // Espace horizontal conservé entre la boutique et la menuiserie.
    private static final double GAP_RATIO_TO_BARN_WIDTH = 0.08;
    // Marge minimale conservée avec le bord de l'écran.
    private static final int MIN_SCREEN_MARGIN = 18;
    // Ratio de largeur de la hitbox.
    private static final double HITBOX_WIDTH_RATIO = 0.88;
    // Ratio de hauteur de la hitbox.
    private static final double HITBOX_HEIGHT_RATIO = 0.62;
    // Décalage vertical de la hitbox depuis le bas du sprite.
    private static final double HITBOX_BOTTOM_INSET_RATIO = 0.02;
    // Dimensions natives du sprite de menuiserie.
    private static final Dimension SPRITE_SIZE = BuildingGeometry.loadSpriteSize(
            Workshop.class,
            ASSET_PATH,
            new Dimension(1024, 1226)
    );

    /**
     * On empêche toute instanciation de cette classe utilitaire.
     */
    private Workshop() {}

    /**
     * Place la menuiserie à droite de la boutique principale en gardant sa base sur le même sol.
     * Les coordonnées renvoyées sont dans le repère logique centré du champ.
     */
    public static Rectangle getDrawBounds(Rectangle fieldLogicalBounds) {
        if (fieldLogicalBounds == null || fieldLogicalBounds.width <= 0 || fieldLogicalBounds.height <= 0) {
            return null;
        }

        Rectangle barnDrawBounds = new Rectangle(Barn.getDrawX(), Barn.Y, Barn.WIDTH, Barn.HEIGHT);
        return BuildingGeometry.buildBarnSideBuildingDrawBounds(
                fieldLogicalBounds,
                barnDrawBounds,
                SPRITE_SIZE,
                HEIGHT_RATIO_TO_BARN,
                GAP_RATIO_TO_BARN_WIDTH,
                MIN_SCREEN_MARGIN,
                false
        );
    }

    /**
     * On renvoie la hitbox compacte de la menuiserie à partir de sa zone de dessin.
     */
    public static Rectangle getCollisionBounds(Rectangle fieldLogicalBounds) {
        return BuildingGeometry.buildCollisionBounds(
                getDrawBounds(fieldLogicalBounds),
                HITBOX_WIDTH_RATIO,
                HITBOX_HEIGHT_RATIO,
                HITBOX_BOTTOM_INSET_RATIO
        );
    }
}
