package model.environment;

import model.culture.GrilleCulture;
import model.movement.Barn;
import view.FieldPanel;

import java.awt.Rectangle;

/**
 * Centralise la préconfiguration visuelle et jouable du champ.
 */
public final class PredefinedFieldLayout {
    // Nombre de colonnes gardées entre la rivière décorative et la boutique principale.
    private static final int DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN = 2;
    // Colonne de repli si la boutique ne permet pas de recalculer la rivière.
    private static final int DECORATIVE_RIVER_FALLBACK_COLUMN = 4;
    // Largeur de chaque bande pré-labourée.
    private static final int TILLED_STRIP_WIDTH = 4;
    // Première ligne labourée prédéfinie.
    private static final int FIRST_TILLED_ROW = 5;
    // Nombre total de bandes labourées à poser.
    private static final int TILLED_STRIP_COUNT = 4;
    // Écart vertical entre deux bandes labourées.
    private static final int TILLED_ROW_STEP = 2;
    // Rayon de dégagement gardé autour des terres déjà labourées à gauche de la rivière.
    private static final int LEFT_TILLED_CLEARANCE_RADIUS = 2;

    /**
     * On bloque l'instanciation car cette classe ne sert qu'à fournir des règles fixes de layout.
     */
    private PredefinedFieldLayout() {
        // Classe utilitaire.
    }

    /**
     * On applique ici toute la préconfiguration fixe du terrain avant la partie.
     */
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
     * sur les lignes 6, 8, 10 et 12 (comptage humain, donc 1-indexé).
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
                grilleCulture.labourerCaseSansObjectif(column, row);
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

        return isInsideReservedLeftRiverRows(gridY)
                || isNearTilledCell(fieldPanel.getGrilleCulture(), gridX, gridY);
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
     * On dit si la ligne se trouve dans la bande verticale réservée autour des terres à gauche.
     */
    private static boolean isInsideReservedLeftRiverRows(int gridY) {
        int lastReservedRow = FIRST_TILLED_ROW + ((TILLED_STRIP_COUNT - 1) * TILLED_ROW_STEP);
        return gridY >= FIRST_TILLED_ROW && gridY <= lastReservedRow;
    }

    /**
     * Même en dehors des lignes complètement réservées,
     * on garde une respiration d'une case autour de toute terre déjà labourée.
     */
    private static boolean isNearTilledCell(GrilleCulture grilleCulture, int gridX, int gridY) {
        if (grilleCulture == null) {
            return false;
        }

        for (int offsetX = -PredefinedFieldLayout.LEFT_TILLED_CLEARANCE_RADIUS; offsetX <= PredefinedFieldLayout.LEFT_TILLED_CLEARANCE_RADIUS; offsetX++) {
            for (int offsetY = -PredefinedFieldLayout.LEFT_TILLED_CLEARANCE_RADIUS; offsetY <= PredefinedFieldLayout.LEFT_TILLED_CLEARANCE_RADIUS; offsetY++) {
                if (grilleCulture.isLabouree(gridX + offsetX, gridY + offsetY)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Repère la première colonne touchée par l'image de la boutique principale (à droite),
     * puis conserve la même largeur de terrain du côté gauche de la rivière
     * même si la boutique a été décalée vers la gauche.
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

        return Math.max(
                0,
                leftmostBarnColumn
                        - DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN
                        + Barn.getHorizontalTileShiftColumns()
        );
    }
}
