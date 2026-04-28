package model.culture;

/**
 * Représente un des quatre bords d'une case de la grille.

 * Cette information sert à deux choses très concrètes :
 * - On s'en sert pour savoir sur quel bord une clôture est posée,
 * - la vue s'en sert pour dessiner la preview et la clôture du bon côté.
 */
public enum CellSide {
    TOP(0, -1, 1),
    RIGHT(1, 0, 1 << 1),
    BOTTOM(0, 1, 1 << 2),
    LEFT(-1, 0, 1 << 3);

    /*
     * Ces décalages indiquent où se trouve la case voisine
     * quand on regarde un bord précis.
     *
     * Exemple :
     * - TOP    -> la voisine est au-dessus,
     * - LEFT   -> la voisine est à gauche.
     */
    private final int deltaX;
    private final int deltaY;

    /*
     * Chaque bord occupe un bit différent dans le masque de clôtures.
     * Cela permet de stocker plusieurs côtés occupés dans un simple entier.
     */
    private final int mask;

    CellSide(int deltaX, int deltaY, int mask) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.mask = mask;
    }

    /** Donne le décalage horizontal vers la case voisine liée à ce bord. */
    public int getDeltaX() {
        return deltaX;
    }

    /** Donne le décalage vertical vers la case voisine liée à ce bord. */
    public int getDeltaY() {
        return deltaY;
    }

    /** Donne le bit utilisé pour stocker ce bord dans le masque de clôtures. */
    public int getMask() {
        return mask;
    }

    /**
     * Renvoie le bord opposé sur la case voisine.

     * Exemple :
     * le bord RIGHT d'une case correspond au bord LEFT de la case à droite.
     */
    public CellSide opposite() {
        switch (this) {
            case TOP:
                return BOTTOM;
            case RIGHT:
                return LEFT;
            case BOTTOM:
                return TOP;
            case LEFT:
                return RIGHT;
            default:
                return null;
        }
    }
}
