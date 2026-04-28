package tests;

import model.culture.GrilleCulture;
import model.enemy.EnemyUnit;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import view.FieldPanel;

/**
 * Vérifie que la zone droite de la ferme n'autorise plus
 * d'apparition de lapins par le haut de l'écran.
 */
public class TestEnemySpawnZones {
    private static final int VIEWPORT_WIDTH = 1180;
    private static final int VIEWPORT_HEIGHT = 850;
    private static final int SAMPLE_COUNT = 4000;

    public static void main(String[] args) {
        GrilleCulture grilleCulture = new GrilleCulture(null);
        TreeManager treeManager = new TreeManager(grilleCulture);
        FieldPanel fieldPanel = new FieldPanel(grilleCulture, treeManager);
        fieldPanel.setSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        PredefinedFieldLayout.apply(fieldPanel);

        int fieldWidth = fieldPanel.getFieldLogicalBounds().width;
        int fieldHeight = fieldPanel.getFieldLogicalBounds().height;
        int riverColumn = findDecorativeRiverColumn(grilleCulture);
        if (riverColumn < 0) {
            System.out.println("Test échoué : aucune colonne de rivière décorative n'a été trouvée.");
            return;
        }

        double riverRightEdgeX = (-fieldWidth / 2.0)
                + (((double) riverColumn + 1.0) * fieldWidth / GrilleCulture.LARGEUR_GRILLE);
        double topSpawnThresholdY = -(VIEWPORT_HEIGHT / 2.0);
        int rightZoneSpawnCount = 0;

        for (int sample = 0; sample < SAMPLE_COUNT; sample++) {
            EnemyUnit enemy = new EnemyUnit(
                    VIEWPORT_WIDTH,
                    VIEWPORT_HEIGHT,
                    fieldWidth,
                    fieldHeight,
                    grilleCulture,
                    grilleCulture.getGestionnaireObjectifs(),
                    null
            );

            double spawnX = enemy.getPreciseX();
            double spawnY = enemy.getPreciseY();
            if (spawnX < riverRightEdgeX) {
                continue;
            }

            rightZoneSpawnCount++;
            if (spawnY < topSpawnThresholdY) {
                System.out.println("Test échoué : un lapin a spawné en haut de la zone droite (" + spawnX + ", " + spawnY + ").");
                return;
            }
        }

        if (rightZoneSpawnCount == 0) {
            System.out.println("Test échoué : aucun spawn côté droit n'a été observé.");
            return;
        }

        System.out.println("Test réussi : aucun lapin n'apparaît en haut de la zone droite.");
    }

    private static int findDecorativeRiverColumn(GrilleCulture grilleCulture) {
        for (int column = 0; column < grilleCulture.getLargeur(); column++) {
            if (grilleCulture.hasRiver(column, 0)) {
                return column;
            }
        }

        return -1;
    }
}
