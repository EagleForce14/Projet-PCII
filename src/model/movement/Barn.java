package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;

/**
 * Représente la boutique principale (à droite), un obstacle fixe situé en haut du champ.
 * Les entités (Unité, Ennemis) ne peuvent pas la traverser.
 */
public class Barn {
    // Chemin de l'image utilisée pour la boutique principale.
    private static final String ASSET_PATH = "/assets/barn.png";
    // Échelle d'affichage du sprite par rapport à sa taille native.
    private static final double SPRITE_SCALE = 0.30;
    // Ratio de largeur retenu pour la hitbox.
    private static final double HITBOX_WIDTH_RATIO = 0.92;
    // Ratio de hauteur retenu pour la hitbox.
    private static final double HITBOX_HEIGHT_RATIO = 0.8;
    // Décalage vertical qui remonte un peu la hitbox depuis le bas.
    private static final double HITBOX_BOTTOM_INSET_RATIO = 0.10;
    // Décalage horizontal historique de la boutique vers la droite.
    private static final int HORIZONTAL_SHIFT_RIGHT = 108;
    // Taille de tuile utilisée tant qu'aucune vraie taille n'a été fournie.
    private static final int DEFAULT_TILE_SIZE = 54;
    // Nombre de colonnes logiques de décalage appliquées vers la gauche.
    private static final int LEFT_SHIFT_COLUMNS = 0;
    // Dimensions natives connues du sprite de boutique.
    private static final Dimension SPRITE_SIZE = BuildingGeometry.loadSpriteSize(
            Barn.class,
            ASSET_PATH,
            new Dimension(967, 967)
    );
    // Taille de tuile courante utilisée pour recaler horizontalement le bâtiment.
    private static volatile int currentTileSize = DEFAULT_TILE_SIZE;

    // La taille affichée suit désormais les dimensions réelles du PNG
    // pour éviter d'écraser une image plus large ou plus plate.
    private static final int BASE_WIDTH = Math.max(1, (int) Math.round(SPRITE_SIZE.width * SPRITE_SCALE));
    private static final int BASE_HEIGHT = Math.max(1, (int) Math.round(SPRITE_SIZE.height * SPRITE_SCALE));
    
    // Position x dans le repère logique du champ.
    // On garde ici le placement historique de référence.
    private static final int BASE_X = (-BASE_WIDTH / 2) + HORIZONTAL_SHIFT_RIGHT;
    // On la garde un peu dégagée du bord haut, mais plus basse qu'avant.
    private static final int BASE_Y = -400;

    /**
     * Facteur d'échelle dérivé de la taille réelle des tuiles visibles.
     */
    private static double getTileScale() {
        return Math.max(1, currentTileSize) / (double) DEFAULT_TILE_SIZE;
    }

    /**
     * Largeur actuelle du sprite, synchronisée avec la taille des tuiles visibles.
     */
    public static int getWidth() {
        return Math.max(1, (int) Math.round(BASE_WIDTH * getTileScale()));
    }

    /**
     * Hauteur actuelle du sprite, synchronisée avec la taille des tuiles visibles.
     */
    public static int getHeight() {
        return Math.max(1, (int) Math.round(BASE_HEIGHT * getTileScale()));
    }

    /**
     * On calcule l'abscisse de dessin actuelle de la boutique.
     */
    public static int getDrawX() {
        return (int) Math.round(BASE_X * getTileScale());
    }

    /**
     * On calcule l'ordonnée de dessin actuelle de la boutique.
     */
    public static int getDrawY() {
        return (int) Math.round(BASE_Y * getTileScale());
    }

    /**
     * On reconstruit le rectangle de dessin complet de la boutique à l'échelle courante.
     */
    public static Rectangle getDrawBounds() {
        return new Rectangle(getDrawX(), getDrawY(), getWidth(), getHeight());
    }

    /**
     * On expose le décalage horizontal logique actuellement appliqué.
     */
    public static int getHorizontalTileShiftColumns() {
        return LEFT_SHIFT_COLUMNS;
    }

    /**
     * On met à jour la taille de tuile de référence si elle est valide.
     */
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
     * @return Rectangle représentant les limites infranchissables de la boutique principale (à droite)
     */
    public static Rectangle getCollisionBounds() {
        return BuildingGeometry.buildCollisionBounds(
                getDrawBounds(),
                HITBOX_WIDTH_RATIO,
                HITBOX_HEIGHT_RATIO,
                HITBOX_BOTTOM_INSET_RATIO
        );
    }

    /**
     * Vérifie si une entité centrée sur (centerX, centerY) recoupe la hitbox de
     * la boutique principale (à droite).
     * Les coordonnées reçues sont dans le repère logique partagé par le joueur et les lapins.
     */
    public static boolean collidesWithCenteredBox(double centerX, double centerY, int width, int height) {
        return BuildingGeometry.collidesWithCenteredBox(getCollisionBounds(), centerX, centerY, width, height);
    }

    /**
     * Vérifie qu'une entité centrée sur la position donnée ne touche pas
     * la boutique principale (à droite).
     */
    public static boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        return !collidesWithCenteredBox(centerX, centerY, width, height);
    }
}
