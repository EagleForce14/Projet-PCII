package model.environment;

import view.FieldPanel;
import view.InventoryStatusOverlay;

import java.awt.Rectangle;

/**
 * Centralise les règles spatiales des obstacles fixes du champ.
 *
 * Aujourd'hui cela couvre :
 * - les arbres,
 * - la rivière,
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

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        Rectangle candidateBounds = TreeGeometry.buildAnchoredScaledBounds(
                cellBounds,
                TreeGeometry.MATURE_TREE_TILE_SCALE,
                TreeGeometry.MATURE_TREE_ANCHOR_X_RATIO,
                TreeGeometry.MATURE_TREE_ANCHOR_Y_RATIO
        );
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

        Rectangle candidateScreenBounds = TreeGeometry.buildAnchoredScaledBounds(
                fieldPanel.getCellBounds(gridX, gridY),
                TreeGeometry.MATURE_TREE_TILE_SCALE,
                TreeGeometry.MATURE_TREE_ANCHOR_X_RATIO,
                TreeGeometry.MATURE_TREE_ANCHOR_Y_RATIO
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

        int treeMargin = Math.max(6, (int) Math.round(tileSize * TREE_TO_TREE_MARGIN_RATIO));
        Rectangle protectedCandidateBounds = expand(candidateBounds, treeMargin);
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle existingBounds = getFutureMatureTreeBounds(tree.getGridX(), tree.getGridY());
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
        int left = (int) Math.round(centerX - (width / 2.0));
        int top = (int) Math.round(centerY - (height / 2.0));
        Rectangle entityBounds = new Rectangle(left, top, width, height);

        if (intersectsBarnCourtyard(entityBounds)) {
            return false;
        }

        if (intersectsPlacedRiver(entityBounds)) {
            return false;
        }

        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle treeHitbox = getTreeHitbox(tree.getGridX(), tree.getGridY());
            if (treeHitbox != null && treeHitbox.intersects(entityBounds)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sert uniquement avant la maturation d'un tronc :
     * on vérifie si la future hitbox de l'arbre complet recouvrirait déjà une entité.
     */
    public boolean matureTreeWouldOverlapCenteredBox(int gridX, int gridY, double centerX, double centerY, int width, int height) {
        Rectangle matureTreeHitbox = getFutureMatureTreeHitbox(gridX, gridY);
        if (matureTreeHitbox == null) {
            return false;
        }

        int left = (int) Math.round(centerX - (width / 2.0));
        int top = (int) Math.round(centerY - (height / 2.0));
        Rectangle entityBounds = new Rectangle(left, top, width, height);
        return matureTreeHitbox.intersects(entityBounds);
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

    private Rectangle getFutureMatureTreeBounds(int gridX, int gridY) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        return TreeGeometry.buildAnchoredScaledBounds(
                cellBounds,
                TreeGeometry.MATURE_TREE_TILE_SCALE,
                TreeGeometry.MATURE_TREE_ANCHOR_X_RATIO,
                TreeGeometry.MATURE_TREE_ANCHOR_Y_RATIO
        );
    }

    private Rectangle getTreeHitbox(int gridX, int gridY) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        TreeInstance tree = treeManager.getTreeAt(gridX, gridY);
        return TreeGeometry.buildTreeHitbox(cellBounds, tree != null && tree.isMature());
    }

    private Rectangle getFutureMatureTreeHitbox(int gridX, int gridY) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        return TreeGeometry.buildTreeHitbox(cellBounds, true);
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
}
