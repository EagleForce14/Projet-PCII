package model.environment;

import model.culture.GrilleCulture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Centralise uniquement l'état des arbres du champ.
 */
public class TreeManager {
    private static final int TREE_CUT_REQUIRED_IMPACTS = 4;
    private static final int TREE_WOOD_REWARD = 2;

    private final GrilleCulture grilleCulture;
    private final TreeInstance[][] trees;
    private final List<TreeFellingEffect> fellingEffects;
    private final List<WoodRewardEffect> woodRewardEffects;
    private final Random random;

    // le constructeur
    public TreeManager(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
        this.trees = new TreeInstance[grilleCulture.getLargeur()][grilleCulture.getHauteur()];
        this.fellingEffects = new ArrayList<>();
        this.woodRewardEffects = new ArrayList<>();
        this.random = new Random();
    }

    public int getColumnCount() {
        return trees.length;
    }

    public int getRowCount() {
        return trees.length == 0 ? 0 : trees[0].length;
    }

    public synchronized boolean hasTreeAt(int gridX, int gridY) {
        return isInsideGrid(gridX, gridY) && trees[gridX][gridY] != null;
    }

    public synchronized TreeInstance getTreeAt(int gridX, int gridY) {
        if (!isInsideGrid(gridX, gridY) || trees[gridX][gridY] == null) {
            return null;
        }

        return trees[gridX][gridY].copy();
    }

    /**
     * Un arbre ne peut apparaître que sur une case encore en herbe,
     * donc non labourée, sans culture, sans chemin, sans rivière, sans compost
     * et sans autre arbre.
     */
    public synchronized boolean canPlaceTreeAt(int gridX, int gridY) {
        return !isInsideGrid(gridX, gridY)
                || trees[gridX][gridY] != null
                || grilleCulture.isLabouree(gridX, gridY)
                || grilleCulture.hasPath(gridX, gridY)
                || grilleCulture.hasRiver(gridX, gridY)
                || grilleCulture.hasBridgeAnchorAt(gridX, gridY)
                || grilleCulture.hasCompostAt(gridX, gridY)
                || grilleCulture.getCulture(gridX, gridY) != null;
    }

    // Pour placer l'arbre
    public synchronized boolean placeTree(int gridX, int gridY, boolean mature, boolean weepingWillow) {
        if (canPlaceTreeAt(gridX, gridY)) {
            return false;
        }

        trees[gridX][gridY] = new TreeInstance(
                gridX,
                gridY,
                mature,
                weepingWillow,
                shouldUseAlternateMatureSprite(weepingWillow)
        );
        return true;
    }

    // Pour faire grandir l'arbre
    public synchronized void growTree(int gridX, int gridY) {
        if (!hasTreeAt(gridX, gridY)) {
            return;
        }

        trees[gridX][gridY].makeMature();
    }

    /**
     * La vue parcourt un instantané pour éviter toute lecture concurrente
     * d'objets encore en mutation pendant le rendu.
     */
    public synchronized List<TreeInstance> getTreesSnapshot() {
        List<TreeInstance> snapshot = new ArrayList<>();
        for (TreeInstance[] tree : trees) {
            for (TreeInstance treeInstance : tree) {
                if (treeInstance != null) {
                    snapshot.add(treeInstance.copy());
                }
            }
        }

        return snapshot;
    }

    /**
     * Enregistre un clic de coupe sur un arbre précis.
     *
     * Tant que le nombre d'impacts requis n'est pas atteint,
     * on ne fait qu'augmenter la progression.
     * Au dernier impact, l'arbre disparaît, sa hitbox aussi,
     * et on lance les deux animations associées :
     * l'abattage puis la récompense bois.
     */
    public synchronized boolean cutTree(int gridX, int gridY) {
        if (!isInsideGrid(gridX, gridY) || trees[gridX][gridY] == null) {
            return false;
        }

        TreeInstance tree = trees[gridX][gridY];
        boolean treeFelled = tree.registerCutImpact(TREE_CUT_REQUIRED_IMPACTS);
        if (!treeFelled) {
            return false;
        }

        trees[gridX][gridY] = null;
        long now = System.currentTimeMillis();
        fellingEffects.add(new TreeFellingEffect(gridX, gridY, now));
        woodRewardEffects.add(new WoodRewardEffect(gridX, gridY, now));
        return true;
    }

    public synchronized List<TreeFellingEffect> getActiveFellingEffects() {
        long now = System.currentTimeMillis();
        cleanupExpiredEffects(now);
        return new ArrayList<>(fellingEffects);
    }

    public synchronized List<WoodRewardEffect> getActiveWoodRewardEffects() {
        long now = System.currentTimeMillis();
        cleanupExpiredEffects(now);
        return new ArrayList<>(woodRewardEffects);
    }

    public int getRequiredCutImpactCount() {
        return TREE_CUT_REQUIRED_IMPACTS;
    }

    public int getWoodRewardQuantity() {
        return TREE_WOOD_REWARD;
    }

    private boolean isInsideGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < trees.length && gridY >= 0 && gridY < trees[gridX].length;
    }

    private void cleanupExpiredEffects(long now) {
        fellingEffects.removeIf(effect -> effect == null || effect.isExpired(now));
        woodRewardEffects.removeIf(effect -> effect == null || effect.isExpired(now));
    }

    /**
     * Le tirage reste aléatoire tant qu'il ne ferait pas passer `arbre2`
     * en majorité sur le total des arbres classiques déjà posés.
     */
    private boolean shouldUseAlternateMatureSprite(boolean weepingWillow) {
        if (weepingWillow) {
            return false;
        }

        int defaultSpriteCount = 0;
        int alternateSpriteCount = 0;

        for (TreeInstance[] treeColumn : trees) {
            for (TreeInstance tree : treeColumn) {
                if (tree == null || tree.usesWeepingWillowSprite()) {
                    continue;
                }

                if (tree.usesAlternateMatureSprite()) {
                    alternateSpriteCount++;
                } else {
                    defaultSpriteCount++;
                }
            }
        }

        if ((alternateSpriteCount + 1) > defaultSpriteCount) {
            return false;
        }

        return random.nextBoolean();
    }
}
