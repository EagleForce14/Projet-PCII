package model.enemy;

import java.awt.Rectangle;

/**
 * Représente un poste de garde de la grotte, associé à une salle
 * et à une zone de cases utilisée pour faire apparaître les monstres.
 */
final class CaveGuardPost {
    final int roomIndex;
    final Rectangle gridBounds;

    /**
     * On fige une copie des bornes pour éviter qu'un appelant les modifie ensuite.
     */
    CaveGuardPost(int roomIndex, Rectangle gridBounds) {
        this.roomIndex = roomIndex;
        this.gridBounds = gridBounds == null ? null : new Rectangle(gridBounds);
    }
}
