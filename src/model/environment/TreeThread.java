package model.environment;

import model.enemy.EnemyModel;
import model.enemy.EnemyUnit;
import model.movement.Unit;
import model.runtime.GamePauseController;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Thread dédié au cycle de vie des arbres du décor.
 *
 * Au lancement :
 * quelques arbres sont déjà matures.
 *
 * Ensuite :
 * le thread fait apparaître un tronc sur une case d'herbe libre,
 * attend le délai de croissance, puis transforme ce tronc en arbre complet.
 */
public class TreeThread extends Thread {
    private static final int INITIAL_TREE_COUNT = 4;
    private static final long TREE_SPAWN_DELAY_MS = 12000;
    private static final long TREE_GROWTH_DELAY_MS = 8000;
    private static final long GROWTH_RETRY_DELAY_MS = 250;

    private final TreeManager treeManager;
    private final TreeObstacleMap treeObstacleMap;
    private final Unit playerUnit;
    private final EnemyModel enemyModel;
    private final Random random;
    private final GamePauseController pauseController;
    private volatile boolean actif = true;

    public TreeThread(TreeManager treeManager, TreeObstacleMap treeObstacleMap, Unit playerUnit, EnemyModel enemyModel) {
        this.treeManager = treeManager;
        this.treeObstacleMap = treeObstacleMap;
        this.playerUnit = playerUnit;
        this.enemyModel = enemyModel;
        this.random = new Random();
        this.pauseController = GamePauseController.getInstance();
    }

    /**
     * Les arbres visibles dès le début sont directement matures.
     * On appelle cette méthode une seule fois au démarrage d'une partie,
     * quand la géométrie réelle du champ est déjà calculable.
     */
    public void installerArbresInitiaux() {
        for (int index = 0; index < INITIAL_TREE_COUNT; index++) {
            Point cell = pickRandomAvailableGrassCell();
            if (cell == null) {
                return;
            }

            treeManager.placeTree(cell.x, cell.y, true);
        }
    }

    public void arreter() {
        actif = false;
        interrupt();
    }

    @Override
    public void run() {
        while (actif) {
            try {
                // On espace volontairement les apparitions pour que le joueur
                // voie la pousse d'un nouvel arbre au lieu d'un remplissage brutal.
                pauseController.sleep(TREE_SPAWN_DELAY_MS);

                Point cell = pickRandomAvailableGrassCell();
                if (cell == null) {
                    continue;
                }

                if (!treeManager.placeTree(cell.x, cell.y, false)) {
                    continue;
                }

                pauseController.sleep(TREE_GROWTH_DELAY_MS);
                waitForFreeGrowthArea(cell.x, cell.y);
                treeManager.growTree(cell.x, cell.y);
            } catch (InterruptedException e) {
                if (!actif) {
                    return;
                }
            }
        }
    }

    /**
     * On délègue toute la validation géométrique au helper dédié :
     * espacement entre arbres, marge avec la grange,
     * et exclusion des zones trop proches du bord visible.
     */
    private Point pickRandomAvailableGrassCell() {
        List<Point> availableCells = new ArrayList<>();
        for (int column = 0; column < treeManager.getColumnCount(); column++) {
            for (int row = 0; row < treeManager.getRowCount(); row++) {
                if (treeObstacleMap.canPlaceTreeAt(column, row)) {
                    availableCells.add(new Point(column, row));
                }
            }
        }

        if (availableCells.isEmpty()) {
            return null;
        }

        return availableCells.get(random.nextInt(availableCells.size()));
    }

    /**
     * La hitbox de l'arbre mature ne doit jamais apparaître sur une entité déjà présente.
     * On garde donc le tronc tant que le joueur ou un lapin occupe encore cette future zone.
     */
    private void waitForFreeGrowthArea(int gridX, int gridY) throws InterruptedException {
        while (actif && isFutureMatureAreaOccupied(gridX, gridY)) {
            pauseController.sleep(GROWTH_RETRY_DELAY_MS);
        }
    }

    private boolean isFutureMatureAreaOccupied(int gridX, int gridY) {
        if (playerUnit != null
                && treeObstacleMap.matureTreeWouldOverlapCenteredBox(
                        gridX,
                        gridY,
                        playerUnit.getX(),
                        playerUnit.getY(),
                        Unit.SIZE,
                        Unit.SIZE
                )) {
            return true;
        }

        if (enemyModel == null) {
            return false;
        }

        for (EnemyUnit enemy : enemyModel.getEnemyUnits()) {
            if (treeObstacleMap.matureTreeWouldOverlapCenteredBox(
                    gridX,
                    gridY,
                    enemy.getX(),
                    enemy.getY(),
                    EnemyUnit.getCollisionSize(),
                    EnemyUnit.getCollisionSize()
            )) {
                return true;
            }
        }

        return false;
    }
}
