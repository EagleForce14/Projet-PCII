package model.movement;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

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
    private static final Dimension SPRITE_SIZE = loadSpriteSize();

    // La taille affichée suit désormais les dimensions réelles du PNG
    // pour éviter d'écraser une image plus large ou plus plate.
    public static final int WIDTH = Math.max(1, (int) Math.round(SPRITE_SIZE.width * SPRITE_SCALE));
    public static final int HEIGHT = Math.max(1, (int) Math.round(SPRITE_SIZE.height * SPRITE_SCALE));
    
    // Position x dans le repère logique du champ.
    // On décale volontairement la boutique/grange vers la droite d'environ une case.
    public static final int X = (-WIDTH / 2) + HORIZONTAL_SHIFT_RIGHT;
    // On la garde un peu dégagée du bord haut, mais plus basse qu'avant.
    public static final int Y = -400;

    /**
     * Retourne la zone de collision (hitbox).
     * Celle-ci reste plus étroite et plus basse que l'image entière :
     * le joueur et les lapins ne doivent pas heurter le toit ou le vide autour du sprite.
     *
     * @return Rectangle représentant les limites infranchissables de la grange
     */
    public static Rectangle getCollisionBounds() {
        int hitboxWidth = Math.max(1, (int) Math.round(WIDTH * HITBOX_WIDTH_RATIO));
        int hitboxHeight = Math.max(1, (int) Math.round(HEIGHT * HITBOX_HEIGHT_RATIO));
        int hitboxX = X + ((WIDTH - hitboxWidth) / 2);
        int hitboxY = Y + HEIGHT - hitboxHeight - Math.max(1, (int) Math.round(HEIGHT * HITBOX_BOTTOM_INSET_RATIO));
        
        return new Rectangle(hitboxX, hitboxY, hitboxWidth, hitboxHeight);
    }

    /**
     * Vérifie si une entité centrée sur (centerX, centerY) recoupe la hitbox de la grange.
     * Les coordonnées reçues sont dans le repère logique partagé par le joueur et les lapins.
     */
    public static boolean collidesWithCenteredBox(double centerX, double centerY, int width, int height) {
        int left = (int) Math.round(centerX - (width / 2.0));
        int top = (int) Math.round(centerY - (height / 2.0));
        Rectangle entityBounds = new Rectangle(left, top, width, height);
        return getCollisionBounds().intersects(entityBounds);
    }

    /**
     * Vérifie qu'une entité centrée sur la position donnée ne touche pas la grange.
     */
    public static boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        return !collidesWithCenteredBox(centerX, centerY, width, height);
    }

    private static Dimension loadSpriteSize() {
        BufferedImage image = null;

        try {
            URL imageUrl = Barn.class.getResource(ASSET_PATH);
            if (imageUrl != null) {
                image = ImageIO.read(imageUrl);
            } else {
                File file = new File("src" + ASSET_PATH);
                if (file.exists()) {
                    image = ImageIO.read(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Impossible de lire les dimensions de la grange : " + ASSET_PATH);
            e.printStackTrace();
        }

        if (image != null) {
            return new Dimension(image.getWidth(), image.getHeight());
        }

        // Fallback historique : on retombe sur l'équivalent visuel d'environ 290x290.
        return new Dimension(967, 967);
    }
}
