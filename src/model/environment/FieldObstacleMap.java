package model.environment;

import model.culture.CellSide;
import model.movement.BuildingGeometry;
import model.movement.MovementCollisionMap;
import view.FieldPanel;
import view.InventoryStatusOverlay;

import java.awt.Rectangle;

/**
 * Centralise les règles spatiales des obstacles fixes du champ.

 * Aujourd'hui cela couvre :
 * - les arbres,
 * - la rivière,
 * - l'échoppe,
 * - la menuiserie,
 * - et toutes les validations géométriques nécessaires autour d'eux.

 * Le but est simple :
 * éviter que le joueur, les lapins, les arbres et la vue
 * ne réinventent chacun leur propre définition d'une case bloquante.
 */
public class FieldObstacleMap implements MovementCollisionMap {
    // Marge de sécurité gardée entre un grand arbre et le bord du champ.
    private static final double TREE_EDGE_MARGIN_RATIO = 0.25;
    // Marge minimale gardée entre deux grands arbres.
    private static final double TREE_TO_TREE_MARGIN_RATIO = 0.12;
    // Marge minimale gardée entre un arbre mature et un bâtiment majeur.
    private static final double TREE_TO_BARN_MARGIN_RATIO = 0.18;
    // Petite dilatation utilisée pour rendre la coupe d'arbre plus confortable.
    private static final int TREE_INTERACTION_PADDING = 10;
    // Empreinte logique d'un arbre mature quand on raisonne case par case.
    private static final int[][] MATURE_TREE_BLOCKED_OFFSETS = {
            {-1, -2}, {0, -2}, {1, -2},
            {-1, -1}, {0, -1}, {1, -1},
            {0, 0}
    };

    // Gestionnaire qui détient l'état vivant de tous les arbres du champ.
    private final TreeManager treeManager;
    // Vue du champ qui fournit toutes les bornes logiques utiles aux collisions.
    private final FieldPanel fieldPanel;

    /**
     * On relie ici les règles d'obstacles à l'état des arbres et à la géométrie réelle du champ.
     */
    public FieldObstacleMap(TreeManager treeManager, FieldPanel fieldPanel) {
        this.treeManager = treeManager;
        this.fieldPanel = fieldPanel;
    }

    /**
     * Le placement des arbres reste le cas le plus exigeant :
     * on tient compte de leur future taille mature,
     * du bord du champ, de la boutique principale (à droite) et des autres arbres.
     */
    public boolean canPlaceTreeAt(int gridX, int gridY) {
        // On refuse d'abord tout ce que le gestionnaire des arbres sait déjà interdire :
        // hors grille, case occupée, terre travaillée, rivière, pont ou autre objet de culture.
        if (treeManager.canPlaceTreeAt(gridX, gridY)) {
            return false;
        }
        // On interdit une pousse qui tomberait sur l'emprise de la boutique principale.
        if (fieldPanel.isBlockedByBarn(gridX, gridY)) {
            return false;
        }
        // On ne fait pas apparaître d'arbre sur un buisson purement décoratif.
        if (fieldPanel.hasDecorativeBushAt(gridX, gridY)) {
            return false;
        }
        // On protège aussi l'extension pierreuse placée près de la rivière droite.
        if (fieldPanel.hasRightStoneExtensionAt(gridX, gridY)) {
            return false;
        }
        // On garde libre la bande de circulation spécifique autour de la rivière droite et de la boutique.
        if (fieldPanel.blocksTreeSpawnInRightRiverPostBarnRows(gridX, gridY)) {
            return false;
        }
        // On réserve aussi tout le couloir agricole préconfiguré à gauche de la rivière.
        if (PredefinedFieldLayout.blocksTreeSpawnInLeftRiverSection(fieldPanel, gridX, gridY)) {
            return false;
        }
        // On évite enfin les cellules décoratives qui habillent l'entrée de grotte côté ferme.
        if (fieldPanel.isFarmCaveDecorationCell(gridX, gridY)) {
            return false;
        }

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        boolean weepingWillow = shouldUseWeepingWillowAt(gridX);
        Rectangle candidateBounds = TreeGeometry.buildMatureTreeBounds(cellBounds, weepingWillow);
        // Sans géométrie de case, on ne peut rien valider proprement.
        if (cellBounds == null) {
            return false;
        }

        // On ne valide pas seulement le tronc actuel :
        // on vérifie déjà que l'arbre une fois mature restera bien dans un champ "sûr",
        // avec une petite respiration visuelle autour des bords.
        int tileSize = Math.max(cellBounds.width, cellBounds.height);
        Rectangle safeFieldBounds = shrink(
                fieldPanel.getFieldLogicalBounds(),
                Math.max(6, (int) Math.round(tileSize * TREE_EDGE_MARGIN_RATIO)),
                Math.max(6, (int) Math.round(tileSize * TREE_EDGE_MARGIN_RATIO))
        );
        if (safeFieldBounds == null || !safeFieldBounds.contains(candidateBounds)) {
            return false;
        }

        // Côté rendu écran, on évite qu'une grande cime descende dans la barre d'inventaire.
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

        // On garde une zone de dégagement autour de la boutique principale
        // pour éviter qu'un arbre mature ne mange visuellement son sprite.
        Rectangle barnClearanceBounds = expand(
                fieldPanel.getBarnLogicalDrawBounds(),
                Math.max(8, (int) Math.round(tileSize * TREE_TO_BARN_MARGIN_RATIO))
        );
        if (barnClearanceBounds != null && barnClearanceBounds.intersects(candidateBounds)) {
            return false;
        }

        // Même logique pour la menuiserie : l'arbre ne doit ni toucher ni recouvrir le bâtiment.
        Rectangle workshopClearanceBounds = expand(
                fieldPanel.getWorkshopLogicalDrawBounds(),
                Math.max(8, (int) Math.round(tileSize * TREE_TO_BARN_MARGIN_RATIO))
        );
        if (workshopClearanceBounds != null && workshopClearanceBounds.intersects(candidateBounds)) {
            return false;
        }

        // Et même règle pour l'échoppe, afin de garder un décor lisible et praticable.
        Rectangle stallClearanceBounds = expand(
                fieldPanel.getStallLogicalDrawBounds(),
                Math.max(8, (int) Math.round(tileSize * TREE_TO_BARN_MARGIN_RATIO))
        );
        if (stallClearanceBounds != null && stallClearanceBounds.intersects(candidateBounds)) {
            return false;
        }

        // Enfin, on compare l'arbre projeté avec tous les arbres déjà présents,
        // non pas sur le tronc seul, mais sur leur future emprise mature protégée par une marge.
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

        // Ce bloc supplémentaire n'existe que pour les lapins :
        // il leur interdit de passer juste au-dessus de la première case de rivière.
        if (blockFences && intersectsTopRiverRabbitBlock(entityBounds)) {
            return false;
        }

        // Même idée pour la décoration haute à droite :
        // les lapins ne doivent pas l'utiliser comme faille de contournement.
        if (blockFences && intersectsRightRiverUpperDecoration(entityBounds)) {
            return false;
        }

        // L'entrée de grotte côté ferme doit rester une vraie masse bloquante pour les lapins.
        if (blockFences && intersectsFarmCaveBlockingZone(entityBounds)) {
            return false;
        }

        // Le joueur comme les autres entités ne traversent jamais la cour de la boutique.
        if (intersectsBarnCourtyard(entityBounds)) {
            return false;
        }

        // La menuiserie reste elle aussi un obstacle fixe plein.
        if (intersectsWorkshop(entityBounds)) {
            return false;
        }

        // Même règle pour l'échoppe.
        if (intersectsStall(entityBounds)) {
            return false;
        }

        // Les buissons décoratifs restent bloquants,
        // sauf certaines cellules déjà gérées à part pour la navigation des lapins.
        if (intersectsDecorativeBushCells(entityBounds, blockFences)) {
            return false;
        }

        /*
         * Le pont doit être traversable par toutes les entités mobiles
         * qui s'appuient sur cette méthode commune, pas seulement par le joueur.
         *
         * On garde donc une seule règle :
         * si le rectangle de collision reste entièrement dans l'image logique du pont,
         * la rivière ne bloque plus le passage.
         *
         * Les clôtures, elles, restent propres aux lapins via `blockFences`.
         */
        if (intersectsPlacedRiver(entityBounds)) {
            return false;
        }

        // Les clôtures ne bloquent que les lapins :
        // le joueur, lui, gère leur interaction autrement.
        if (blockFences && findIntersectingFenceCollision(entityBounds) != null) {
            return false;
        }

        // On finit par les arbres, car leur hitbox réelle dépend de leur état de maturité.
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            Rectangle treeHitbox = getTreeHitbox(tree);
            if (treeHitbox != null && treeHitbox.intersects(entityBounds)) {
                return false;
            }
        }

        return true;
    }

    /**
     * On renvoie la clôture précise rencontrée par une entité au lieu d'un simple booléen.
     */
    public FenceCollision findBlockingFenceCollision(double centerX, double centerY, int width, int height) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        return findIntersectingFenceCollision(entityBounds);
    }

    /**
     * On expose la géométrie logique d'un segment de clôture demandé.
     */
    public Rectangle getFenceLogicalBounds(int gridX, int gridY, CellSide side) {
        return fieldPanel.getLogicalFenceBounds(gridX, gridY, side);
    }

    /**
     * On choisit le saule pleureur uniquement du côté gauche de la rivière décorative.
     */
    public boolean shouldUseWeepingWillowAt(int gridX) {
        return PredefinedFieldLayout.isLeftOfDecorativeRiver(fieldPanel, gridX);
    }

    /**
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
        // Si la case n'existe pas géométriquement, on ne la traite pas comme bloquée ici.
        if (cellBounds == null) {
            return false;
        }

        // La case est bloquée si elle tombe dans l'emprise de la boutique principale.
        if (fieldPanel.isBlockedByBarn(gridX, gridY)) {
            return true;
        }

        // Même principe pour la menuiserie.
        if (fieldPanel.isBlockedByWorkshop(gridX, gridY)) {
            return true;
        }

        // Et pour l'échoppe.
        if (fieldPanel.isBlockedByStall(gridX, gridY)) {
            return true;
        }

        // Un buisson décoratif rend la case indisponible.
        if (fieldPanel.hasDecorativeBushAt(gridX, gridY)) {
            return true;
        }

        // L'extension de pierre à droite compte aussi comme case condamnée.
        if (fieldPanel.hasRightStoneExtensionAt(gridX, gridY)) {
            return true;
        }

        // Toute case de rivière est bloquante au niveau du terrain lui-même.
        if (fieldPanel.getGrilleCulture().hasRiver(gridX, gridY)) {
            return true;
        }

        // Pour les arbres matures, on raisonne sur leur vraie empreinte étendue.
        for (TreeInstance tree : treeManager.getTreesSnapshot()) {
            if (tree.isMature()) {
                if (isInsideMatureFootprint(tree, gridX, gridY)) {
                    return true;
                }
                continue;
            }

            // Pour un simple tronc, seule sa case d'origine reste bloquée.
            if (tree.getGridX() == gridX && tree.getGridY() == gridY) {
                return true;
            }
        }

        return false;
    }

    /**
     * On teste l'intersection avec la cour logique de la boutique principale.
     */
    private boolean intersectsBarnCourtyard(Rectangle entityBounds) {
        Rectangle barnCourtyardBounds = fieldPanel.getBarnCourtyardLogicalBounds();
        return barnCourtyardBounds != null && barnCourtyardBounds.intersects(entityBounds);
    }

    /**
     * On teste l'intersection avec la zone bloquante de l'entrée de grotte côté ferme.
     */
    private boolean intersectsFarmCaveBlockingZone(Rectangle entityBounds) {
        Rectangle caveBlockingBounds = fieldPanel.getFarmCaveBlockingLogicalBounds();
        return caveBlockingBounds != null && caveBlockingBounds.intersects(entityBounds);
    }

    /**
     * On teste l'intersection avec la hitbox logique de la menuiserie.
     */
    private boolean intersectsWorkshop(Rectangle entityBounds) {
        Rectangle workshopBounds = fieldPanel.getWorkshopLogicalCollisionBounds();
        return workshopBounds != null && workshopBounds.intersects(entityBounds);
    }

    /**
     * On teste l'intersection avec la hitbox logique de l'échoppe.
     */
    private boolean intersectsStall(Rectangle entityBounds) {
        Rectangle stallBounds = fieldPanel.getStallLogicalCollisionBounds();
        return stallBounds != null && stallBounds.intersects(entityBounds);
    }

    /**
     * On teste l'intersection avec la décoration haute de rivière à droite.
     */
    private boolean intersectsRightRiverUpperDecoration(Rectangle entityBounds) {
        Rectangle blockedBounds = fieldPanel.getRightRiverUpperDecorationLogicalBounds();
        return blockedBounds != null && blockedBounds.intersects(entityBounds);
    }

    /**
     * Empêche les lapins de contourner la rivière décorative en passant juste
     * au-dessus de sa toute première case, hors du champ visible du joueur.
     */
    private boolean intersectsTopRiverRabbitBlock(Rectangle entityBounds) {
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            if (!fieldPanel.getGrilleCulture().hasRiver(column, 0)) {
                continue;
            }

            Rectangle topRiverCellBounds = fieldPanel.getLogicalCellBounds(column, 0);
            if (topRiverCellBounds == null) {
                return false;
            }

            Rectangle topRiverBlockBounds = new Rectangle(
                    topRiverCellBounds.x,
                    topRiverCellBounds.y - topRiverCellBounds.height,
                    topRiverCellBounds.width,
                    topRiverCellBounds.height
            );
            return topRiverBlockBounds.intersects(entityBounds);
        }

        return false;
    }

    /**
     * La rivière est bloquante par défaut.

     * Seule exception : pour le joueur, un pont posé ouvre un couloir étroit
     * au milieu de la rivière. On vérifie alors que tout le corps de l'entité
     * reste bien contenu dans ce passage central, afin d'interdire la marche
     * sur les bords de l'image du pont.
     */
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
                if (riverCellBounds == null || !riverCellBounds.intersects(entityBounds)) {
                    continue;
                }

                if (isBridgePassageCrossable(column, row, entityBounds)) {
                    continue;
                }

                if (riverCellBounds.intersects(entityBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Le pont est ancré sur la case de berge droite.
     * Si cette ancre existe pour la ligne courante, on considère désormais
     * toute l'image logique du pont comme zone traversable.

     * Cela adoucit la règle précédente :
     * tant que le corps du joueur reste dans le sprite du pont,
     * la rivière ne doit plus le bloquer.
     */
    private boolean isBridgePassageCrossable(int riverGridX, int riverGridY, Rectangle entityBounds) {
        int bridgeAnchorX = riverGridX + 1;
        if (!fieldPanel.getGrilleCulture().hasBridgeAnchorAt(bridgeAnchorX, riverGridY)) {
            return false;
        }

        Rectangle bridgeBounds = fieldPanel.getBridgeLogicalBounds(bridgeAnchorX, riverGridY);
        return bridgeBounds != null && bridgeBounds.contains(entityBounds);
    }

    /**
     * On parcourt toutes les cases décoratives de buisson pour voir si l'une coupe la hitbox.
     */
    private boolean intersectsDecorativeBushCells(Rectangle entityBounds, boolean ignoreRightRiverUpperDecoration) {
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                // On saute immédiatement les cases qui ne portent aucun buisson décoratif.
                if (!fieldPanel.hasDecorativeBushAt(column, row)) {
                    continue;
                }
                // Pour les lapins, certaines décorations hautes à droite sont gérées par une règle dédiée.
                if (ignoreRightRiverUpperDecoration && fieldPanel.isRightRiverUpperDecorationCell(column, row)) {
                    continue;
                }

                Rectangle bushCellBounds = fieldPanel.getLogicalCellBounds(column, row);
                // Dès qu'un buisson décoratif touche la hitbox, la case est considérée bloquante.
                if (bushCellBounds != null && bushCellBounds.intersects(entityBounds)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * On cherche le premier segment de clôture qui coupe réellement la hitbox fournie.
     */
    private FenceCollision findIntersectingFenceCollision(Rectangle entityBounds) {
        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                for (CellSide side : CellSide.values()) {
                    // On ne construit une géométrie que pour les segments de clôture réellement posés.
                    if (!fieldPanel.getGrilleCulture().hasFence(column, row, side)) {
                        continue;
                    }

                    Rectangle fenceBounds = fieldPanel.getLogicalFenceBounds(column, row, side);
                    // On renvoie immédiatement la première clôture qui coupe la hitbox.
                    if (fenceBounds != null && fenceBounds.intersects(entityBounds)) {
                        return new FenceCollision(column, row, side, fenceBounds);
                    }
                }
            }
        }

        return null;
    }

    /**
     * On reconstruit la future emprise mature d'un arbre déjà présent.
     */
    private Rectangle getFutureMatureTreeBounds(TreeInstance tree) {
        if (tree == null) {
            return null;
        }

        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(tree.getGridX(), tree.getGridY());
        return TreeGeometry.buildMatureTreeBounds(cellBounds, tree.usesWeepingWillowSprite());
    }

    /**
     * On renvoie la hitbox réelle actuelle de l'arbre, tronc ou arbre mature selon son état.
     */
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

    /**
     * On projette la hitbox d'un arbre à un état donné pour voir si elle couperait déjà une entité.
     */
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

    /**
     * On construit la hitbox théorique d'un arbre à cette case, sans avoir besoin qu'il existe déjà.
     */
    private Rectangle getProjectedTreeHitbox(int gridX, int gridY, boolean mature) {
        Rectangle cellBounds = fieldPanel.getLogicalCellBounds(gridX, gridY);
        return TreeGeometry.buildTreeHitbox(cellBounds, mature, shouldUseWeepingWillowAt(gridX));
    }

    /**
     * On teste si la case donnée appartient à l'empreinte case-par-case d'un arbre mature.
     */
    private boolean isInsideMatureFootprint(TreeInstance tree, int gridX, int gridY) {
        for (int[] offset : MATURE_TREE_BLOCKED_OFFSETS) {
            if (tree.getGridX() + offset[0] == gridX && tree.getGridY() + offset[1] == gridY) {
                return true;
            }
        }

        return false;
    }

    /**
     * On dilate un rectangle pour créer une zone de sécurité autour de lui.
     */
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

    /**
     * On réduit un rectangle pour obtenir une zone intérieure plus sûre.
     */
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

    /**
     * On calcule une distance simple entre les centres de deux rectangles pour choisir le plus proche.
     */
    private long squaredDistance(Rectangle a, Rectangle b) {
        long deltaX = (long) a.x + (a.width / 2L) - (b.x + (b.width / 2L));
        long deltaY = (long) a.y + (a.height / 2L) - (b.y + (b.height / 2L));
        return (deltaX * deltaX) + (deltaY * deltaY);
    }
}
