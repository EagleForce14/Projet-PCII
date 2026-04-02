package model.environment;

import model.culture.GrilleCulture;
import view.FieldPanel;

import java.awt.Rectangle;

/**
 * Centralise la préconfiguration visuelle et jouable du champ.
 */
public final class PredefinedFieldLayout {
    private static final int DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN = 2;
    private static final int DECORATIVE_RIVER_FALLBACK_COLUMN = 4;
    private static final int TILLED_STRIP_WIDTH = 4;
    private static final int FIRST_TILLED_ROW = 4;
    private static final int TILLED_STRIP_COUNT = 4;
    private static final int TILLED_ROW_STEP = 2;

    private PredefinedFieldLayout() {
        // Classe utilitaire.
    }

    public static void apply(FieldPanel fieldPanel) {
        if (fieldPanel == null) {
            return;
        }

        GrilleCulture grilleCulture = fieldPanel.getGrilleCulture();
        if (grilleCulture == null) {
            return;
        }

        installDecorativeRiverColumn(grilleCulture, fieldPanel);
        installPredefinedTilledStrips(grilleCulture, fieldPanel);
    }

    /**
     * Ajoute la colonne fixe de rivière demandée avant la génération des arbres.
     */
    private static void installDecorativeRiverColumn(GrilleCulture grilleCulture, FieldPanel fieldPanel) {
        int riverColumn = resolveDecorativeRiverColumn(fieldPanel);
        if (riverColumn < 0 || riverColumn >= grilleCulture.getLargeur()) {
            return;
        }

        for (int row = 0; row < grilleCulture.getHauteur(); row++) {
            grilleCulture.placeDecorativeRiver(riverColumn, row);
        }
    }

    /**
     * Prépare quatre bandes de 4 cases labourées à gauche de la rivière,
     * sur les lignes 5, 7, 9 et 11 (comptage humain, donc 1-indexé).
     */
    private static void installPredefinedTilledStrips(GrilleCulture grilleCulture, FieldPanel fieldPanel) {
        int riverColumn = resolveDecorativeRiverColumn(fieldPanel);
        if (riverColumn < TILLED_STRIP_WIDTH) {
            return;
        }

        int startColumn = Math.max(0, (riverColumn - TILLED_STRIP_WIDTH) / 2);
        int endColumnExclusive = Math.min(riverColumn, startColumn + TILLED_STRIP_WIDTH);

        for (int stripIndex = 0; stripIndex < TILLED_STRIP_COUNT; stripIndex++) {
            int row = FIRST_TILLED_ROW + (stripIndex * TILLED_ROW_STEP);
            if (row >= grilleCulture.getHauteur()) {
                break;
            }

            for (int column = startColumn; column < endColumnExclusive; column++) {
                grilleCulture.labourerCase(column, row);
            }
        }
    }

    /**
     * Réserve la section gauche de la rivière pour garder un couloir dégagé
     * autour des bandes labourées et des lignes intermédiaires.
     * Cela ne concerne que cette zone du champ, sans toucher au reste.
     */
    public static boolean blocksTreeSpawnInLeftRiverSection(FieldPanel fieldPanel, int gridX, int gridY) {
        if (fieldPanel == null) {
            return false;
        }

        if (!isLeftOfDecorativeRiver(fieldPanel, gridX)) {
            return false;
        }

        int firstReservedRow = FIRST_TILLED_ROW;
        int lastReservedRow = FIRST_TILLED_ROW + ((TILLED_STRIP_COUNT - 1) * TILLED_ROW_STEP);
        return gridY >= firstReservedRow && gridY <= lastReservedRow;
    }

    /**
     * Indique si une colonne se situe strictement à gauche de la rivière décorative.
     */
    public static boolean isLeftOfDecorativeRiver(FieldPanel fieldPanel, int gridX) {
        if (fieldPanel == null) {
            return false;
        }

        int riverColumn = resolveDecorativeRiverColumn(fieldPanel);
        return gridX >= 0 && gridX < riverColumn;
    }

    /**
     * Colonne immédiatement collée à gauche de la rivière décorative.
     */
    public static boolean isAdjacentLeftToDecorativeRiver(FieldPanel fieldPanel, int gridX) {
        if (fieldPanel == null) {
            return false;
        }

        int riverColumn = resolveDecorativeRiverColumn(fieldPanel);
        return riverColumn > 0 && gridX == (riverColumn - 1);
    }

    /**
     * Dernière colonne visible du côté gauche de la map.
     */
    public static boolean isLeftWindowEdgeColumn(FieldPanel fieldPanel, int gridX) {
        return fieldPanel != null && gridX == 0;
    }

    /**
     * Repère la première colonne touchée par l'image de la boutique/grange,
     * puis se décale de deux cases vers la gauche.
     */
    private static int resolveDecorativeRiverColumn(FieldPanel fieldPanel) {
        Rectangle barnBounds = fieldPanel.getBarnLogicalDrawBounds();
        Rectangle fieldBounds = fieldPanel.getFieldLogicalBounds();
        int leftmostBarnColumn = -1;

        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            Rectangle firstCellBounds = fieldPanel.getLogicalCellBounds(column, 0);
            if (firstCellBounds == null) {
                continue;
            }

            Rectangle columnBounds = new Rectangle(
                    firstCellBounds.x,
                    fieldBounds.y,
                    firstCellBounds.width,
                    fieldBounds.height
            );
            if (columnBounds.intersects(barnBounds)) {
                leftmostBarnColumn = column;
                break;
            }
        }

        if (leftmostBarnColumn < 0) {
            return DECORATIVE_RIVER_FALLBACK_COLUMN;
        }

        return Math.max(0, leftmostBarnColumn - DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN);
    }
}
