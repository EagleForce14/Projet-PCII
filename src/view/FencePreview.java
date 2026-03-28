package view;

import model.culture.CellSide;

import java.awt.Point;

/**
 * Représente une prévisualisation de pose de clôture.
 *
 * Cette classe reste volontairement minuscule :
 * elle stocke seulement
 * - la case visée
 * - le côté de cette case
 *
 */
public final class FencePreview {
    private final Point cell;
    private final CellSide side;

    public FencePreview(Point cell, CellSide side) {
        this.cell = cell == null ? null : new Point(cell);
        this.side = side;
    }

    /**
     * On renvoie une copie du point pour garder l'objet immutable de l'extérieur.
     */
    public Point getCell() {
        return cell == null ? null : new Point(cell);
    }

    public CellSide getSide() {
        return side;
    }
}
