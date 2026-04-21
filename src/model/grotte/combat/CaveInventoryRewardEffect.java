package model.grotte.combat;

import model.grotte.drop.CaveDropDefinition;

/**
 * Animation courte jouée quand un drop de grotte est ramassé.
 *
 * Le modèle garde seulement des données métier simples :
 * - le type d'objet récupéré,
 * - la case d'origine du loot,
 * - l'instant de départ.
 *
 * La vue convertit ensuite ces infos en trajectoire visuelle
 * jusqu'au slot d'inventaire correspondant.
 */
public final class CaveInventoryRewardEffect {
    private static final long REWARD_EFFECT_MS = 720L;

    private final CaveDropDefinition definition;
    private final int sourceGridX;
    private final int sourceGridY;
    private final long startedAtMs;

    public CaveInventoryRewardEffect(
            CaveDropDefinition definition,
            int sourceGridX,
            int sourceGridY,
            long startedAtMs
    ) {
        if (definition == null) {
            throw new IllegalArgumentException("La définition de récompense ne peut pas être null.");
        }
        this.definition = definition;
        this.sourceGridX = sourceGridX;
        this.sourceGridY = sourceGridY;
        this.startedAtMs = startedAtMs;
    }

    public CaveDropDefinition getDefinition() {
        return definition;
    }

    public int getSourceGridX() {
        return sourceGridX;
    }

    public int getSourceGridY() {
        return sourceGridY;
    }

    public boolean isExpired(long now) {
        return now - startedAtMs >= REWARD_EFFECT_MS;
    }

    public double getProgress(long now) {
        double elapsed = Math.max(0L, now - startedAtMs);
        return Math.min(1.0, elapsed / REWARD_EFFECT_MS);
    }
}
