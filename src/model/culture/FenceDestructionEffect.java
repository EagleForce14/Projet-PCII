package model.culture;

/**
 * Classe pour dessiner l'animation de destruction d'une clôture.
 * On ne stocke que le strict nécessaire :
 * la position du segment, son côté, et l'instant du "boum".
 */
public class FenceDestructionEffect {
    private static final long FENCE_DESTRUCTION_EFFECT_MS = 420L;

    private final int gridX;
    private final int gridY;
    private final CellSide side;
    private final long startedAtMs;

    public FenceDestructionEffect(int gridX, int gridY, CellSide side, long startedAtMs) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.side = side;
        this.startedAtMs = startedAtMs;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public CellSide getSide() {
        return side;
    }

    public boolean isExpired(long now) {
        return now - startedAtMs >= FENCE_DESTRUCTION_EFFECT_MS;
    }

    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / FENCE_DESTRUCTION_EFFECT_MS);
    }
}
