package model.environment;

/**
 * Animation de récompense jouée quand un arbre vient d'être coupé.
 * Cette classe ne gère que les données de l'effet.
 */
public class WoodRewardEffect {
    private static final long WOOD_REWARD_EFFECT_MS = 720L;

    private final int gridX;
    private final int gridY;
    private final long startedAtMs;

    public WoodRewardEffect(int gridX, int gridY, long startedAtMs) {
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
        return now - startedAtMs >= WOOD_REWARD_EFFECT_MS;
    }

    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / WOOD_REWARD_EFFECT_MS);
    }
}
