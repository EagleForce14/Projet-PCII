package tests;

import model.culture.GrilleCulture;
import model.enemy.EnemyUnit;
import model.environment.FieldObstacleMap;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import model.management.Inventaire;
import model.movement.Unit;
import model.shop.FacilityType;
import view.FieldPanel;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Vérifie le bon fonctionnement du pont :
 * - la case de pose est bien détectée sur la berge droite,
 * - le pont est consommé à la pose,
 * - le joueur et le lapin peuvent traverser tant qu'ils restent dans l'image du pont,
 * - les bords de rivière et les autres lignes restent bloqués.
 */
public class TestBridgePlacement {
    public static void main(String[] args) {
        GrilleCulture grilleCulture = new GrilleCulture(null);
        TreeManager treeManager = new TreeManager(grilleCulture);
        FieldPanel fieldPanel = new FieldPanel(grilleCulture, treeManager);
        fieldPanel.setSize(1180, 850);

        PredefinedFieldLayout.apply(fieldPanel);

        FieldObstacleMap obstacleMap = new FieldObstacleMap(treeManager, fieldPanel);
        fieldPanel.setFieldObstacleMap(obstacleMap);

        Point bridgeCandidateCell = findBridgeCandidateCell(fieldPanel);
        if (bridgeCandidateCell == null) {
            System.out.println("Test échoué : aucune case candidate de pont n'a été trouvée.");
            return;
        }

        if (!fieldPanel.isBridgePlacementCandidateCell(bridgeCandidateCell)) {
            System.out.println("Test échoué : la case candidate n'est pas reconnue comme posable.");
            return;
        }

        Inventaire inventaire = new Inventaire();
        inventaire.ajoutInstallation(FacilityType.PONT, 1);
        grilleCulture.placeBridge(bridgeCandidateCell.x, bridgeCandidateCell.y, inventaire);

        if (!grilleCulture.hasBridgeAnchorAt(bridgeCandidateCell.x, bridgeCandidateCell.y)) {
            System.out.println("Test échoué : le pont n'a pas été enregistré sur sa case d'ancrage.");
            return;
        }

        if (inventaire.getQuantiteInstallation(FacilityType.PONT) != 0) {
            System.out.println("Test échoué : l'inventaire n'a pas consommé le pont posé.");
            return;
        }

        Rectangle bridgedRiverCellBounds = fieldPanel.getLogicalCellBounds(bridgeCandidateCell.x - 1, bridgeCandidateCell.y);
        if (bridgedRiverCellBounds == null) {
            System.out.println("Test échoué : impossible de récupérer la case de rivière du pont.");
            return;
        }

        int centeredBridgeX = bridgedRiverCellBounds.x + (bridgedRiverCellBounds.width / 2);
        int centeredBridgeY = bridgedRiverCellBounds.y + (bridgedRiverCellBounds.height / 2);
        if (!obstacleMap.canOccupyCenteredBox(centeredBridgeX, centeredBridgeY, Unit.SIZE, Unit.SIZE)) {
            System.out.println("Test échoué : le joueur devrait pouvoir traverser au centre du pont.");
            return;
        }

        if (!obstacleMap.canOccupyCenteredBox(
                centeredBridgeX,
                centeredBridgeY,
                EnemyUnit.getCollisionSize(),
                EnemyUnit.getCollisionSize(),
                true
        )) {
            System.out.println("Test échoué : le lapin devrait lui aussi pouvoir traverser au centre du pont.");
            return;
        }

        Point blockedRiverLine = findUnbridgedRiverLine(fieldPanel, bridgeCandidateCell);
        if (blockedRiverLine == null) {
            System.out.println("Test échoué : aucune autre ligne de rivière n'a été trouvée pour le contrôle négatif.");
            return;
        }

        Rectangle blockedRiverCellBounds = fieldPanel.getLogicalCellBounds(blockedRiverLine.x - 1, blockedRiverLine.y);
        int blockedRiverX = blockedRiverCellBounds.x + (blockedRiverCellBounds.width / 2);
        int blockedRiverY = blockedRiverCellBounds.y + (blockedRiverCellBounds.height / 2);
        if (obstacleMap.canOccupyCenteredBox(blockedRiverX, blockedRiverY, Unit.SIZE, Unit.SIZE)) {
            System.out.println("Test échoué : la rivière reste traversable hors du pont.");
            return;
        }

        if (obstacleMap.canOccupyCenteredBox(
                blockedRiverX,
                blockedRiverY,
                EnemyUnit.getCollisionSize(),
                EnemyUnit.getCollisionSize(),
                true
        )) {
            System.out.println("Test échoué : le lapin ne devrait pas traverser la rivière hors du pont.");
            return;
        }

        System.out.println("Test réussi : le pont ouvre bien le passage dans son image pour le joueur et les lapins.");
    }

    private static Point findBridgeCandidateCell(FieldPanel fieldPanel) {
        for (int row = 0; row < fieldPanel.getRowCount(); row++) {
            for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
                if (fieldPanel.isBridgePlacementCandidateCell(column, row)) {
                    return new Point(column, row);
                }
            }
        }

        return null;
    }

    private static Point findUnbridgedRiverLine(FieldPanel fieldPanel, Point placedBridgeCell) {
        for (int row = 0; row < fieldPanel.getRowCount(); row++) {
            if (row == placedBridgeCell.y) {
                continue;
            }

            for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
                if (fieldPanel.isBridgePlacementCandidateCell(column, row)) {
                    return new Point(column, row);
                }
            }
        }

        return null;
    }
}
