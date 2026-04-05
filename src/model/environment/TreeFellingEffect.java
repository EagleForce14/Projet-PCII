package model.environment;

/**
 * Petit effet visuel lancé quand un arbre est coupé.
 *
 * On ne garde volontairement que la position de la souche
 * et l'instant de départ de l'animation :
 * la vue recalcule ensuite toutes les tailles et transparences
 * à partir de cette base commune.
 */
public class TreeFellingEffect {
    private static final long TREE_FELLING_EFFECT_MS = 520L;

    private final int gridX;
    private final int gridY;
    private final long startedAtMs;

    public TreeFellingEffect(int gridX, int gridY, long startedAtMs) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.startedAtMs = startedAtMs;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public boolean isExpired(long now) {
        return now - startedAtMs >= TREE_FELLING_EFFECT_MS;
    }

    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / TREE_FELLING_EFFECT_MS);
    }
}
