package view;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Interface partagée par toutes les maps jouables.
 *
 * Note : `isFarmableCell` se lit "cette case peut-elle servir de case active dans la zone courante ?"
 */
public interface PlayableMapPanel {
    Rectangle getFieldBounds();

    Point getFullyOccupiedCell(Rectangle unitBounds);

    boolean isFarmableCell(Point cell);

    Point getGridPositionAt(int pixelX, int pixelY);

    void setHighlightedCell(Point highlightedCell);

    Point getInitialPlayerOffset();

    int resolveMovementSpeed(Point cell);

    Component getMapComponent();
}
