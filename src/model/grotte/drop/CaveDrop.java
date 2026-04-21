package model.grotte.drop;

/**
 * Instance concrète d'un objet posé au sol dans la grotte.
 */
public final class CaveDrop {
    private final CaveDropDefinition definition;
    private final int gridX;
    private final int gridY;
    private final long spawnedAtMs;

    public CaveDrop(CaveDropDefinition definition, int gridX, int gridY, long spawnedAtMs) {
        if (definition == null) {
            throw new IllegalArgumentException("La définition du drop ne peut pas être null.");
        }
        this.definition = definition;
        this.gridX = gridX;
        this.gridY = gridY;
        this.spawnedAtMs = spawnedAtMs;
    }

    public CaveDropDefinition getDefinition() {
        return definition;
    }

    /**
     * Le drop est rattaché à une case logique.
     * Cela permet un ramassage simple :
     * si le joueur entre sur cette case, l'objet est pris immédiatement.
     */
    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    /**
     * On garde l'instant d'apparition pour éviter qu'un drop disparaisse
     * exactement dans la même frame où il vient d'être créé.
     */
    public long getSpawnedAtMs() {
        return spawnedAtMs;
    }
}
