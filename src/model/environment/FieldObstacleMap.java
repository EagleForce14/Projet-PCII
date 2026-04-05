package model.environment;

import model.culture.CellSide;
import model.movement.BuildingGeometry;
import view.FieldPanel;
import view.InventoryStatusOverlay;

import java.awt.Rectangle;

/**
 * Centralise les règles spatiales des obstacles fixes du champ.
 *
 * Aujourd'hui cela couvre :
 * - les arbres,
 * - la rivière,
 * - la menuiserie,
 * - et toutes les validations géométriques nécessaires autour d'eux.
 *
 * Le but est simple :
 * éviter que le joueur, les lapins, les arbres et la vue
 * ne réinventent chacun leur propre définition d'une case bloquante.
 */
public class FieldObstacleMap {
    private static final double TREE_EDGE_MARGIN_RATIO = 0.25;
    private static final double TREE_TO_TREE_MARGIN_RATIO = 0.12;
    private static final double TREE_TO_BARN_MARGIN_RATIO = 0.18;
    private static final int TREE_INTERACTION_PADDING = 10;
    private static final int[][] MATURE_TREE_BLOCKED_OFFSETS = {
            {-1, -2}, {0, -2}, {1, -2},
            {-1, -1}, {0, -1}, {1, -1},
            {0, 0}
    };

    private final TreeManager treeManager;
    private final FieldPanel fieldPanel;

    public FieldObstacleMap(TreeManager treeManager, FieldPanel fieldPanel) {
        this.treeManager = treeManager;
        this.fieldPanel = fieldPanel;
    }

    /**
     * Le placement des arbres reste le cas le plus exigeant :
     * on tient compte de leur future taille mature,
     * du bord du champ, de la grange et des autres arbres.
     */
    public boolean canPlaceTreeAt(int gridX, int gridY) {
        if (treeManager.canPlaceTreeAt(gridX, gridY)) {
            return false;
        }
        if (fieldPanel.isBlockedByBarn(gridX, gridY)) {
            return false;
        }
        if (fieldPanel.hasDecorativeBushAt(gridX, gridY)) {
            return false;
        }
        if (fieldPanel.hasRightStoneExtensionAt(gridX, gridY)) {
            return false;
        }
        if (fieldPanel.blocksTreeSpawnInRightRiverPostBarnRows(gridX, gridY)) {
            return false;
        }
        if (PredefinedFieldLayout.blocksTreeSpawnInLeftRiverSection(fieldPanel, gridX, gridY)) {
            return false;
        }

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        boolean weepingWillow = shouldUseWeepingWillowAt(gridX);
        Rectangle candidateBounds = TreeGeometry.buildMatureTreeBounds(cellBounds, weepingWillow);
        if (cellBounds == null) {
            return false;
        }

        int tileSize = Math.max(cellBounds.width, cellBounds.height);
        Rectangle safeFieldBounds = shrink(
                fieldPanel.getFieldLogicalBounds(),
                Math.max(6, (int) Math.round(tileSize * TREE_EDGE_MARGIN_RATIO)),
                Math.max(6, (int) Math.round(tileSize * TREE_EDGE_MARGIN_RATIO))
        );
        if (safeFieldBounds == null || !safeFieldBounds.contains(candidateBounds)) {
            return false;
        }

        Rectangle candidateScreenBounds = TreeGeometry.buildMatureTreeBounds(
                fieldPanel.getCellBounds(gridX, gridY),
                weepingWillow
        );
        Rectangle inventoryBarBounds = InventoryStatusOverlay.computeInventoryBarBounds(
                fieldPanel.getFieldBounds(),
                fieldPanel.getWidth(),
                fieldPanel.getHeight()
        );
        if (candidateScreenBounds == null || candidateScreenBounds.y + candidateScreenBounds.height > inventoryBarBounds.y) {
            return false;
        }

        Rectangle barnClearanceBounds = expand(
                fieldPanel.getBarnLogicalDrawBounds(),
                Math.max(8, (int) Math.round(tileSize * TREE_TO_BARN_MARGIN_RATIO))
        );
        if (barnClearanceBounds != null && barnClearanceBounds.intersects(candidateBounds)) {
            return false;
        }

        Rectangle workshopClearanceBounds = expand(
                fieldPanel.getWorkshopLogicalDrawBounds(),
                Math.max(8, (int) Math.round(tileSize * TREE_TO_BARN_MARGIN_RATIO))
        );
        if (workshopClearanceBounds != null && workshopClearanceBounds.intersects(candidateBounds)) {
            return false;
        }

        int treeMargin = Math.max(6, (int) Math.round(tileSize * TREE_TO_TREE_MARGIN_RATIO));
        Rectangle protectedCandidateBounds = expand(candidateBounds, treeMargin);
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle existingBounds = getFutureMatureTreeBounds(tree);
            if (existingBounds != null && protectedCandidateBounds.intersects(expand(existingBounds, treeMargin))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Test unique utilisé par toutes les entités mobiles.
     * Si un jour on ajoute un autre obstacle fixe de terrain,
     * c'est ici qu'il devra être branché.
     */
    public boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        return canOccupyCenteredBox(centerX, centerY, width, height, false);
    }

    /**
     * Certains obstacles sont spécifiques aux lapins :
     * les clôtures doivent les arrêter, mais pas bloquer le joueur.
     */
    public boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height, boolean blockFences) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);

        if (blockFences && intersectsRightRiverUpperDecoration(entityBounds)) {
            return false;
        }

        if (intersectsBarnCourtyard(entityBounds)) {
            return false;
        }

        if (intersectsWorkshop(entityBounds)) {
            return false;
        }

        if (intersectsDecorativeBushCells(entityBounds, blockFences)) {
            return false;
        }

        if (intersectsPlacedRiver(entityBounds)) {
            return false;
        }

        if (blockFences && findIntersectingFenceCollision(entityBounds) != null) {
            return false;
        }

        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle treeHitbox = getTreeHitbox(tree);
            if (treeHitbox != null && treeHitbox.intersects(entityBounds)) {
                return false;
            }
        }

        return true;
    }

    public FenceCollision findBlockingFenceCollision(double centerX, double centerY, int width, int height) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        return findIntersectingFenceCollision(entityBounds);
    }

    public Rectangle getFenceLogicalBounds(int gridX, int gridY, CellSide side) {
        return fieldPanel.getLogicalFenceBounds(gridX, gridY, side);
    }

    public boolean shouldUseWeepingWillowAt(int gridX) {
        return PredefinedFieldLayout.isLeftOfDecorativeRiver(fieldPanel, gridX);
    }

    /**
     * Renvoie l'arbre qu'une entité est en train de "pousser".
     *
     * En pratique, le joueur ne pénètre jamais réellement la hitbox de l'arbre,
     * puisqu'elle bloque le déplacement.
     * On dilate donc légèrement cette hitbox pour que le bouton de coupe
     * apparaisse dès que le personnage se colle visuellement au tronc.
     */
    public TreeInstance findInteractableTree(double centerX, double centerY, int width, int height) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        TreeInstance bestTree = null;
        long bestDistanceSquared = Long.MAX_VALUE;

        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle treeHitbox = getTreeHitbox(tree);
            if (treeHitbox == null || !expand(treeHitbox, TREE_INTERACTION_PADDING).intersects(entityBounds)) {
                continue;
            }

            long distanceSquared = squaredDistance(entityBounds, treeHitbox);
            if (distanceSquared < bestDistanceSquared) {
                bestDistanceSquared = distanceSquared;
                bestTree = tree;
            }
        }

        return bestTree;
    }

    /**
     * Sert uniquement avant la maturation d'un tronc :
     * on vérifie si la future hitbox de l'arbre complet recouvrirait déjà une entité.
     */
    public boolean matureTreeWouldOverlapCenteredBox(int gridX, int gridY, double centerX, double centerY, int width, int height) {
        return treeWouldOverlapCenteredBox(gridX, gridY, true, centerX, centerY, width, height);
    }

    /**
     * Empêche la naissance d'un tronc directement sur le joueur ou un lapin pour éviter un blocage.
     */
    public boolean trunkWouldOverlapCenteredBox(int gridX, int gridY, double centerX, double centerY, int width, int height) {
        return treeWouldOverlapCenteredBox(gridX, gridY, false, centerX, centerY, width, height);
    }

    /**
     * Répond à une question de gameplay "case par case".
     * Une case est bloquée si la rivière l'occupe
     * ou si un arbre la recouvre visuellement / physiquement.
     */
    public boolean blocksCell(int gridX, int gridY) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        if (cellBounds == null) {
            return false;
        }

        if (fieldPanel.isBlockedByBarn(gridX, gridY)) {
            return true;
        }

        if (fieldPanel.isBlockedByWorkshop(gridX, gridY)) {
            return true;
        }

        if (fieldPanel.hasDecorativeBushAt(gridX, gridY)) {
            return true;
        }

        if (fieldPanel.hasRightStoneExtensionAt(gridX, gridY)) {
            return true;
        }

        if (fieldPanel.getGrilleCulture().hasRiver(gridX, gridY)) {
            return true;
        }

        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            if (tree.isMature()) {
                if (isInsideMatureFootprint(tree, gridX, gridY)) {
                    return true;
                }
                continue;
            }

            if (tree.getGridX() == gridX && tree.getGridY() == gridY) {
                return true;
            }
        }

        return false;
    }

    private boolean intersectsBarnCourtyard(Rectangle entityBounds) {
        Rectangle barnCourtyardBounds = fieldPanel.getBarnCourtyardLogicalBounds();
        return barnCourtyardBounds != null && barnCourtyardBounds.intersects(entityBounds);
    }

    private boolean intersectsWorkshop(Rectangle entityBounds) {
        Rectangle workshopBounds = fieldPanel.getWorkshopLogicalCollisionBounds();
        return workshopBounds != null && workshopBounds.intersects(entityBounds);
    }

    private boolean intersectsRightRiverUpperDecoration(Rectangle entityBounds) {
        Rectangle blockedBounds = fieldPanel.getRightRiverUpperDecorationLogicalBounds();
        return blockedBounds != null && blockedBounds.intersects(entityBounds);
    }

    private boolean intersectsPlacedRiver(Rectangle entityBounds) {
        /*
         * La grille reste petite.
         * On choisit donc ici une boucle toute simple et très lisible :
         * quelques centaines de tests d'intersection coûtent peu,
         * alors que la clarté du code nous aide partout ailleurs.
         */
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                if (!fieldPanel.getGrilleCulture().hasRiver(column, row)) {
                    continue;
                }

                Rectangle riverCellBounds = fieldPanel.getLogicalCellBounds(column, row);
                if (riverCellBounds != null && riverCellBounds.intersects(entityBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean intersectsDecorativeBushCells(Rectangle entityBounds, boolean ignoreRightRiverUpperDecoration) {
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                if (!fieldPanel.hasDecorativeBushAt(column, row)) {
                    continue;
                }
                if (ignoreRightRiverUpperDecoration && fieldPanel.isRightRiverUpperDecorationCell(column, row)) {
                    continue;
                }

                Rectangle bushCellBounds = fieldPanel.getLogicalCellBounds(column, row);
                if (bushCellBounds != null && bushCellBounds.intersects(entityBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    private FenceCollision findIntersectingFenceCollision(Rectangle entityBounds) {
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                for (CellSide side : CellSide.values()) {
                    if (!fieldPanel.getGrilleCulture().hasFence(column, row, side)) {
                        continue;
                    }

                    Rectangle fenceBounds = fieldPanel.getLogicalFenceBounds(column, row, side);
                    if (fenceBounds != null && fenceBounds.intersects(entityBounds)) {
                        return new FenceCollision(column, row, side, fenceBounds);
                    }
                }
            }
        }

        return null;
    }

    private Rectangle getFutureMatureTreeBounds(TreeInstance tree) {
        if (tree == null) {
            return null;
        }

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(tree.getGridX(), tree.getGridY());
        return TreeGeometry.buildMatureTreeBounds(cellBounds, tree.usesWeepingWillowSprite());
    }

    private Rectangle getTreeHitbox(TreeInstance tree) {
        if (tree == null) {
            return null;
        }

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(tree.getGridX(), tree.getGridY());
        return TreeGeometry.buildTreeHitbox(
                cellBounds,
                tree.isMature(),
                tree.usesWeepingWillowSprite()
        );
    }

    private boolean treeWouldOverlapCenteredBox(
            int gridX,
            int gridY,
            boolean mature,
            double centerX,
            double centerY,
            int width,
            int height
    ) {
        Rectangle treeHitbox = getProjectedTreeHitbox(gridX, gridY, mature);
        if (treeHitbox == null) {
            return false;
        }

        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        return treeHitbox.intersects(entityBounds);
    }

    private Rectangle getProjectedTreeHitbox(int gridX, int gridY, boolean mature) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        return TreeGeometry.buildTreeHitbox(cellBounds, mature, shouldUseWeepingWillowAt(gridX));
    }

    private boolean isInsideMatureFootprint(TreeInstance tree, int gridX, int gridY) {
        for (int[] offset : MATURE_TREE_BLOCKED_OFFSETS) {
            if (tree.getGridX() + offset[0] == gridX && tree.getGridY() + offset[1] == gridY) {
                return true;
            }
        }

        return false;
    }

    private Rectangle expand(Rectangle bounds, int margin) {
        if (bounds == null) {
            return null;
        }

        return new Rectangle(
                bounds.x - margin,
                bounds.y - margin,
                bounds.width + (2 * margin),
                bounds.height + (2 * margin)
        );
    }

    private Rectangle shrink(Rectangle bounds, int horizontalMargin, int verticalMargin) {
        if (bounds == null) {
            return null;
        }

        int width = bounds.width - (2 * horizontalMargin);
        int height = bounds.height - (2 * verticalMargin);
        if (width <= 0 || height <= 0) {
            return null;
        }

        return new Rectangle(
                bounds.x + horizontalMargin,
                bounds.y + verticalMargin,
                width,
                height
        );
    }

    private long squaredDistance(Rectangle a, Rectangle b) {
        long deltaX = (long) a.x + (a.width / 2L) - (b.x + (b.width / 2L));
        long deltaY = (long) a.y + (a.height / 2L) - (b.y + (b.height / 2L));
        return (deltaX * deltaX) + (deltaY * deltaY);
    }
}
