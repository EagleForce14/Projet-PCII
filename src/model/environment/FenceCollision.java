package model.environment;

import model.culture.CellSide;

/**
 * Décrit le segment précis qui bloque une entité.
 * L'IA des lapins s'en sert pour savoir quelle clôture elle est réellement en train de taper.
 */
public class FenceCollision {
    // Colonne de la case qui porte la clôture touchée.
    private final int gridX;
    // Ligne de la case qui porte la clôture touchée.
    private final int gridY;
    // Côté précis de la case où se trouve le segment de clôture touché.
    private final CellSide side;

    /**
     * On mémorise ici la clôture précise qui vient de bloquer une entité.
     */
    public FenceCollision(int gridX, int gridY, CellSide side) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.side = side;
    }

    /**
     * On expose la colonne de la clôture rencontrée.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * On expose la ligne de la clôture rencontrée.
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * On expose le côté exact de la case où se trouve le segment touché.
     */
    public CellSide getSide() {
        return side;
    }
}
