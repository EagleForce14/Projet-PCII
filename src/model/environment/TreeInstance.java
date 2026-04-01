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
    private final boolean alternateMatureSprite;
    private boolean mature;

    // Le constructeur
    public TreeInstance(int gridX, int gridY, boolean mature, boolean alternateMatureSprite) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.mature = mature;
        this.alternateMatureSprite = alternateMatureSprite;
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

    public boolean usesAlternateMatureSprite() {
        return alternateMatureSprite;
    }

    public void makeMature() {
        mature = true;
    }

    /**
     * La vue travaille sur des copies pour ne jamais exposer
     * l'état interne modifiable du gestionnaire.
     */
    public TreeInstance copy() {
        return new TreeInstance(gridX, gridY, mature, alternateMatureSprite);
    }
}
