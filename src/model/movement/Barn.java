package model.movement;

import java.awt.Rectangle;

/**
 * Représente la grange, un obstacle fixe situé en haut du champ.
 * Les entités (Unité, Ennemis) ne peuvent pas la traverser.
 */
public class Barn {
    // Dimensions logiques adaptées au rendu visuel
    public static final int WIDTH = 290;
    public static final int HEIGHT = 290;
    
    // Position x pour être centrée horizontalement (0 est le centre)
    public static final int X = -WIDTH / 2 + 90;
    // Position y de la grange.
    // On la descend un peu pour qu'elle soit moins collée au haut de l'écran.
    public static final int Y = -420;

    /**
     * Retourne la zone de collision (hitbox).
     * Celle-ci est réduite par rapport à la taille globale pour 
     * correspondre au mieux aux contours réels (murs de la grange) sur le PNG.
     * @return Rectangle représentant les limites infranchissables de la grange
     */
    public static Rectangle getCollisionBounds() {
        // Marges spécifiques pour coller précisément aux murs de la grange
        int marginLeft = 30;
        int marginRight = 30;
        int marginTop = 50;
        int marginBottom = 70;

        int hitboxWidth = WIDTH - marginLeft - marginRight;
        int hitboxHeight = HEIGHT - marginTop - marginBottom;
        int hitboxX = X + marginLeft; 
        int hitboxY = Y + marginTop;
        
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
}
