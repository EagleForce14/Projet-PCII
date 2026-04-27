package model.environment;

import model.culture.GrilleCulture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Centralise uniquement l'état des arbres du champ.
 */
public class TreeManager {
    // Nombre d'impacts nécessaires avant qu'un arbre cède définitivement.
    private static final int TREE_CUT_REQUIRED_IMPACTS = 4;
    // Quantité de bois gagnée quand un arbre tombe.
    private static final int TREE_WOOD_REWARD = 2;

    // Grille de culture utilisée pour savoir si une case reste réellement libre.
    private final GrilleCulture grilleCulture;
    // Stockage principal des arbres par coordonnées de case.
    private final TreeInstance[][] trees;
    // Effets visuels d'abattage encore actifs.
    private final List<TreeFellingEffect> fellingEffects;
    // Effets visuels de récompense bois encore actifs.
    private final List<WoodRewardEffect> woodRewardEffects;
    // Aléatoire utilisé pour varier certains choix de sprite.
    private final Random random;

    /**
     * On prépare ici tout l'état nécessaire pour gérer les arbres du champ.
     */
    public TreeManager(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
        this.trees = new TreeInstance[grilleCulture.getLargeur()][grilleCulture.getHauteur()];
        this.fellingEffects = new ArrayList<>();
        this.woodRewardEffects = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * On expose le nombre de colonnes gérées par le tableau d'arbres.
     */
    public int getColumnCount() {
        return trees.length;
    }

    /**
     * On expose le nombre de lignes gérées par le tableau d'arbres.
     */
    public int getRowCount() {
        return trees.length == 0 ? 0 : trees[0].length;
    }

    /**
     * On dit simplement si un arbre existe actuellement sur cette case.
     */
    public synchronized boolean hasTreeAt(int gridX, int gridY) {
        return isInsideGrid(gridX, gridY) && trees[gridX][gridY] != null;
    }

    /**
     * On renvoie une copie de l'arbre demandé pour protéger l'état interne.
     */
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

    /**
     * On place un arbre sur la case si elle reste encore autorisée.
     */
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

    /**
     * On fait passer le tronc existant à l'état d'arbre mature.
     */
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

    /**
     * On renvoie les effets d'abattage encore visibles après nettoyage des expirés.
     */
    public synchronized List<TreeFellingEffect> getActiveFellingEffects() {
        long now = System.currentTimeMillis();
        cleanupExpiredEffects(now);
        return new ArrayList<>(fellingEffects);
    }

    /**
     * On renvoie les effets de récompense bois encore visibles après nettoyage des expirés.
     */
    public synchronized List<WoodRewardEffect> getActiveWoodRewardEffects() {
        long now = System.currentTimeMillis();
        cleanupExpiredEffects(now);
        return new ArrayList<>(woodRewardEffects);
    }

    /**
     * On expose la règle globale du nombre de coups nécessaires.
     */
    public int getRequiredCutImpactCount() {
        return TREE_CUT_REQUIRED_IMPACTS;
    }

    /**
     * On expose la quantité de bois donnée par arbre abattu.
     */
    public int getWoodRewardQuantity() {
        return TREE_WOOD_REWARD;
    }

    /**
     * On vérifie ici si des coordonnées tombent bien dans la grille des arbres.
     */
    private boolean isInsideGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < trees.length && gridY >= 0 && gridY < trees[gridX].length;
    }

    /**
     * On retire les effets visuels qui ont dépassé leur durée de vie.
     */
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
