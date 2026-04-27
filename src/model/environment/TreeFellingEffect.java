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
    // Durée totale de l'animation d'abattage.
    private static final long TREE_FELLING_EFFECT_MS = 520L;

    // Colonne de la souche où l'effet doit être joué.
    private final int gridX;
    // Ligne de la souche où l'effet doit être joué.
    private final int gridY;
    // Instant exact de départ de l'animation.
    private final long startedAtMs;

    /**
     * On crée ici un effet d'abattage ancré sur une case précise.
     */
    public TreeFellingEffect(int gridX, int gridY, long startedAtMs) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.startedAtMs = startedAtMs;
    }

    /**
     * On expose la colonne où l'effet doit être dessiné.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * On expose la ligne où l'effet doit être dessiné.
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * On dit si l'animation a déjà dépassé sa durée de vie.
     */
    public boolean isExpired(long now) {
        return now - startedAtMs >= TREE_FELLING_EFFECT_MS;
    }

    /**
     * On convertit l'avancement temporel en ratio de progression entre 0 et 1.
     */
    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / TREE_FELLING_EFFECT_MS);
    }
}
