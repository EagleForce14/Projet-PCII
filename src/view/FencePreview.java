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
    // Case de grille visée par la prévisualisation.
    private final Point cell;
    // Côté précis de la case sur lequel la clôture serait posée.
    private final CellSide side;

    /**
     * On mémorise la case et le côté exacts de la clôture en cours de prévisualisation.
     */
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

    /**
     * On renvoie le côté de case visé par la prévisualisation.
     */
    public CellSide getSide() {
        return side;
    }
}
