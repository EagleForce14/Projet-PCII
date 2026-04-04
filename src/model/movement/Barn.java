package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Représente la grange, un obstacle fixe situé en haut du champ.
 * Les entités (Unité, Ennemis) ne peuvent pas la traverser.
 */
public class Barn {
    private static final String ASSET_PATH = "/assets/barn.png";
    private static final double SPRITE_SCALE = 0.30;
    private static final double HITBOX_WIDTH_RATIO = 0.92;
    private static final double HITBOX_HEIGHT_RATIO = 0.8;
    private static final double HITBOX_BOTTOM_INSET_RATIO = 0.10;
    private static final int HORIZONTAL_SHIFT_RIGHT = 108;
    private static final int DEFAULT_TILE_SIZE = 54;
    private static final int LEFT_SHIFT_COLUMNS = 0;
    private static final Dimension SPRITE_SIZE = BuildingGeometry.loadSpriteSize(
            Barn.class,
            ASSET_PATH,
            new Dimension(967, 967)
    );
    private static volatile int currentTileSize = DEFAULT_TILE_SIZE;

    // La taille affichée suit désormais les dimensions réelles du PNG
    // pour éviter d'écraser une image plus large ou plus plate.
    public static final int WIDTH = Math.max(1, (int) Math.round(SPRITE_SIZE.width * SPRITE_SCALE));
    public static final int HEIGHT = Math.max(1, (int) Math.round(SPRITE_SIZE.height * SPRITE_SCALE));
    
    // Position x dans le repère logique du champ.
    // On garde ici le placement historique de référence.
    private static final int BASE_X = (-WIDTH / 2) + HORIZONTAL_SHIFT_RIGHT;
    // On la garde un peu dégagée du bord haut, mais plus basse qu'avant.
    public static final int Y = -400;

    public static int getDrawX() {
        return BASE_X - (LEFT_SHIFT_COLUMNS * currentTileSize);
    }

    public static int getHorizontalTileShiftColumns() {
        return LEFT_SHIFT_COLUMNS;
    }

    public static void updateTileSize(int tileSize) {
        if (tileSize > 0) {
            currentTileSize = tileSize;
        }
    }

    /**
     * Retourne la zone de collision (hitbox).
     * Celle-ci reste plus étroite et plus basse que l'image entière :
     * le joueur et les lapins ne doivent pas heurter le toit ou le vide autour du sprite.
     *
     * @return Rectangle représentant les limites infranchissables de la grange
     */
    public static Rectangle getCollisionBounds() {
        return BuildingGeometry.buildCollisionBounds(
                new Rectangle(getDrawX(), Y, WIDTH, HEIGHT),
                HITBOX_WIDTH_RATIO,
                HITBOX_HEIGHT_RATIO,
                HITBOX_BOTTOM_INSET_RATIO
        );
    }

    /**
     * Vérifie si une entité centrée sur (centerX, centerY) recoupe la hitbox de la grange.
     * Les coordonnées reçues sont dans le repère logique partagé par le joueur et les lapins.
     */
    public static boolean collidesWithCenteredBox(double centerX, double centerY, int width, int height) {
        return BuildingGeometry.collidesWithCenteredBox(getCollisionBounds(), centerX, centerY, width, height);
    }

    /**
     * Vérifie qu'une entité centrée sur la position donnée ne touche pas la grange.
     */
    public static boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        return !collidesWithCenteredBox(centerX, centerY, width, height);
    }
}
