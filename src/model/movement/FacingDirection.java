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

    private final int deltaX;
    private final int deltaY;

    FacingDirection(int deltaX, int deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }
}
