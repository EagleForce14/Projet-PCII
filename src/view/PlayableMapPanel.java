package view;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Interface partagée par toutes les maps jouables.

 * Note : `isFarmableCell` se lit "cette case peut-elle servir de case active dans la zone courante ?"
 */
public interface PlayableMapPanel {
    /**
     * On renvoie le rectangle réellement occupé par la map dans son composant.
     */
    Rectangle getFieldBounds();

    /**
     * On dit si une hitbox occupe entièrement une seule case de la map.
     */
    Point getFullyOccupiedCell(Rectangle unitBounds);

    /**
     * On dit si une case peut servir de case active dans cette map.
     */
    boolean isFarmableCell(Point cell);

    /**
     * On convertit des coordonnées écran locales en coordonnées de case.
     */
    Point getGridPositionAt(int pixelX, int pixelY);

    /**
     * On met à jour la case que la map doit éventuellement surligner.
     */
    void setHighlightedCell(Point highlightedCell);

    /**
     * On renvoie le décalage logique de départ du joueur sur cette map.
     */
    Point getInitialPlayerOffset();

    /**
     * On renvoie la vitesse de déplacement à appliquer pour la case donnée.
     */
    int resolveMovementSpeed(Point cell);

    /**
     * On expose le vrai composant Swing qui porte la map.
     */
    Component getMapComponent();
}
