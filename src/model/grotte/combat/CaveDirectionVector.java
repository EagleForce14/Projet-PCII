package model.grotte.combat;

/**
 * Petit porteur de direction normalisée.
 * Il évite d'introduire une classe interne dans le modèle de combat.
 */
public final class CaveDirectionVector {
    private final double x;
    private final double y;

    public CaveDirectionVector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
