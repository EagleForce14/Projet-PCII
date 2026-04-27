package model.culture;

/**
 * Classe pour dessiner l'animation de destruction d'une clôture.
 * On ne stocke que le strict nécessaire :
 * la position du segment, son côté, et l'instant du "boum".
 */
public class FenceDestructionEffect {
    // Durée de l'animation en ms
    private static final long FENCE_DESTRUCTION_EFFECT_MS = 420L;

    // Attributs de l'effet
    private final int gridX;
    private final int gridY;
    private final CellSide side;
    private final long startedAtMs;

    /**
     * Constructeur de la classe FenceDestructionEffect qui prend en argument les coordonnées de la grille, 
     * le côté de la clôture et l'instant de début de l'effet
     */
    public FenceDestructionEffect(int gridX, int gridY, CellSide side, long startedAtMs) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.side = side;
        this.startedAtMs = startedAtMs;
    }

    // Getters pour les attributs de l'effet
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public CellSide getSide() {
        return side;
    }

    /**
     * Méthode pour vérifier si l'effet est expiré en fonction de l'instant actuel
     */
    public boolean isExpired(long now) {
        return now - startedAtMs >= FENCE_DESTRUCTION_EFFECT_MS;
    }

    /**
     * Méthode pour obtenir la progression de l'animation en fonction de l'instant actuel
     * La progression est un double entre 0.0 et 1.0 représentant le pourcentage de l'animation complété
     */
    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / FENCE_DESTRUCTION_EFFECT_MS);
    }
}
