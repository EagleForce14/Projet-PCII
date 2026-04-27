package model.environment;

/**
 * Animation de récompense jouée quand un arbre vient d'être coupé.
 * Cette classe ne gère que les données de l'effet.
 */
public class WoodRewardEffect {
    // Durée totale de l'animation de récompense bois.
    private static final long WOOD_REWARD_EFFECT_MS = 720L;

    // Colonne de la case où la récompense doit apparaître.
    private final int gridX;
    // Ligne de la case où la récompense doit apparaître.
    private final int gridY;
    // Instant exact de départ de l'effet.
    private final long startedAtMs;

    /**
     * On crée ici un effet visuel de récompense bois sur une case donnée.
     */
    public WoodRewardEffect(int gridX, int gridY, long startedAtMs) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.startedAtMs = startedAtMs;
    }

    /**
     * On expose la colonne où la récompense doit être dessinée.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * On expose la ligne où la récompense doit être dessinée.
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * On dit si l'effet a déjà dépassé sa durée de vie.
     */
    public boolean isExpired(long now) {
        return now - startedAtMs >= WOOD_REWARD_EFFECT_MS;
    }

    /**
     * On convertit l'avancement temporel en ratio de progression entre 0 et 1.
     */
    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / WOOD_REWARD_EFFECT_MS);
    }
}
