package model.movement;

/**
 * Direction cardinale utilisée par le joueur pour le rendu
 * et pour orienter les tirs du pistolet dans la grotte.
 */
public enum FacingDirection {
    UP(0, -1),
    RIGHT(1, 0),
    DOWN(0, 1),
    LEFT(-1, 0);

    // Décalage horizontal associé à cette direction.
    private final int deltaX;
    // Décalage vertical associé à cette direction.
    private final int deltaY;

    /**
     * On associe à chaque direction son vecteur logique de déplacement.
     */
    FacingDirection(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    /**
     * On renvoie la composante horizontale de la direction.
     */
    public int getDeltaX() {
        return deltaX;
    }

    /**
     * On renvoie la composante verticale de la direction.
     */
    public int getDeltaY() {
        return deltaY;
    }
}
