package model.environment;

/**
 * Représente un arbre posé sur une case précise du champ.
 *
 * - non mature -> on affiche le tronc,
 * - mature -> on affiche l'arbre complet.
 */
public class TreeInstance {
    private final int gridX;
    private final int gridY;
    private final boolean weepingWillow;
    private final boolean alternateMatureSprite;
    private boolean mature;
    private int cutImpactCount;

    // Le constructeur
    public TreeInstance(int gridX, int gridY, boolean mature, boolean weepingWillow, boolean alternateMatureSprite) {
        this(gridX, gridY, mature, weepingWillow, alternateMatureSprite, 0);
    }

    public TreeInstance(
            int gridX,
            int gridY,
            boolean mature,
            boolean weepingWillow,
            boolean alternateMatureSprite,
            int cutImpactCount
    ) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.mature = mature;
        this.weepingWillow = weepingWillow;
        this.alternateMatureSprite = alternateMatureSprite;
        this.cutImpactCount = Math.max(0, cutImpactCount);
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public boolean isMature() {
        return mature;
    }

    public boolean usesWeepingWillowSprite() {
        return weepingWillow;
    }

    public boolean usesAlternateMatureSprite() {
        return alternateMatureSprite;
    }

    public void makeMature() {
        mature = true;
    }

    public int getCutImpactCount() {
        return cutImpactCount;
    }

    public boolean hasCutProgress() {
        return cutImpactCount > 0;
    }

    /**
     * Chaque clic de coupe enlève une "étape" à l'arbre.
     * La méthode renvoie true uniquement quand l'arbre vient de céder.
     */
    public boolean registerCutImpact(int requiredImpactCount) {
        if (requiredImpactCount <= 0) {
            return true;
        }

        if (cutImpactCount < requiredImpactCount) {
            cutImpactCount++;
        }

        return cutImpactCount >= requiredImpactCount;
    }

    public double getCutProgressRatio(int requiredImpactCount) {
        if (requiredImpactCount <= 0) {
            return 1.0;
        }

        return Math.min(1.0, cutImpactCount / (double) requiredImpactCount);
    }

    /**
     * La vue travaille sur des copies pour ne jamais exposer
     * l'état interne modifiable du gestionnaire.
     */
    public TreeInstance copy() {
        return new TreeInstance(gridX, gridY, mature, weepingWillow, alternateMatureSprite, cutImpactCount);
    }
}
