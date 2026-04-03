package model.environment;

import model.culture.CellSide;

import java.awt.Rectangle;

/**
 * Décrit le segment précis qui bloque une entité.
 * L'IA des lapins s'en sert pour savoir quelle clôture elle est réellement en train de taper.
 */
public class FenceCollision {
    private final int gridX;
    private final int gridY;
    private final CellSide side;
    private final Rectangle bounds;

    // Le constructeur de la classe
    public FenceCollision(int gridX, int gridY, CellSide side, Rectangle bounds) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.side = side;
        this.bounds = bounds == null ? null : new Rectangle(bounds);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public CellSide getSide() {
        return side;
    }

    public Rectangle getBounds() {
        return bounds == null ? null : new Rectangle(bounds);
    }
}
