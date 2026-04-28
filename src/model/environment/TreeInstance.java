package model.environment;

/**
 * Représente un arbre posé sur une case précise du champ.

 * - non mature -> on affiche le tronc,
 * - mature -> on affiche l'arbre complet.
 */
public class TreeInstance {
    // Colonne de la case racine de l'arbre.
    private final int gridX;
    // Ligne de la case racine de l'arbre.
    private final int gridY;
    // Indique si cet arbre utilise la famille de sprites saule pleureur.
    private final boolean weepingWillow;
    // Indique si l'arbre classique mature doit utiliser la variante alternative.
    private final boolean alternateMatureSprite;
    // État de maturité actuel de l'arbre.
    private boolean mature;
    // Nombre d'impacts de coupe déjà reçus.
    private int cutImpactCount;

    /**
     * On construit un arbre neuf sans progression de coupe.
     */
    public TreeInstance(int gridX, int gridY, boolean mature, boolean weepingWillow, boolean alternateMatureSprite) {
        this(gridX, gridY, mature, weepingWillow, alternateMatureSprite, 0);
    }

    /**
     * On reconstruit ici un arbre complet avec tout son état courant.
     */
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

    /**
     * On expose la colonne d'ancrage de l'arbre.
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * On expose la ligne d'ancrage de l'arbre.
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * On indique si l'arbre est déjà passé à son grand sprite mature.
     */
    public boolean isMature() {
        return mature;
    }

    /**
     * On indique si cet arbre utilise la famille de sprites saule pleureur.
     */
    public boolean usesWeepingWillowSprite() {
        return weepingWillow;
    }

    /**
     * On indique si l'arbre mature classique doit prendre sa variante visuelle alternative.
     */
    public boolean usesAlternateMatureSprite() {
        return alternateMatureSprite;
    }

    /**
     * On fait passer l'arbre à l'état mature.
     */
    public void makeMature() {
        mature = true;
    }

    /**
     * On expose le nombre d'impacts de coupe déjà accumulés.
     */
    public int getCutImpactCount() {
        return cutImpactCount;
    }

    /**
     * On dit simplement si l'arbre a déjà commencé à être coupé.
     */
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

    /**
     * On convertit la progression de coupe en ratio prêt à afficher.
     */
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
