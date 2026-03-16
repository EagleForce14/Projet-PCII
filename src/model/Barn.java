package model;

import java.awt.Rectangle;

/** EN COURS
 * Représente la grange, un obstacle fixe situé en haut du champ.
 * Les entités (Unité, Ennemis) ne peuvent pas la traverser.
 */
public class Barn {
    // Dimensions logiques adaptées au rendu visuel
    public static final int WIDTH = 290;
    public static final int HEIGHT = 290;
    
    // Position x pour être centrée horizontalement (0 est le centre)
    public static final int X = -WIDTH / 2 + 90;
    // Position y de la grange
    public static final int Y = -490;

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
}
