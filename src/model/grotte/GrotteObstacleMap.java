package model.grotte;

import model.movement.BuildingGeometry;
import model.movement.MovementCollisionMap;
import view.grotte.GrotteFieldPanel;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Collision dédiée à la grotte.

 * Toute la roche non creusée bloque le joueur.
 * Comme la carte est statique, on s'appuie sur la liste pré-calculée
 * des cases rocheuses pour éviter des tests inutiles.
 */
public final class GrotteObstacleMap implements MovementCollisionMap {
    // Carte logique de grotte qui fournit les cases rocheuses et praticables.
    private final GrotteMap grotteMap;
    // Vue de grotte qui fournit les bornes logiques des murs et façades.
    private final GrotteFieldPanel grotteFieldPanel;
    // Cases praticables qui reçoivent une façade murale par le haut.
    private final List<Point> topWallFrontCells;
    // Cases praticables qui reçoivent une façade murale par le bas.
    private final List<Point> bottomWallFrontCells;
    // Cases praticables qui reçoivent une façade murale par la gauche.
    private final List<Point> leftWallFrontCells;
    // Cases praticables qui reçoivent une façade murale par la droite.
    private final List<Point> rightWallFrontCells;

    /**
     * On prépare ici la collision de grotte et ses caches de façades murales.
     */
    public GrotteObstacleMap(GrotteMap grotteMap, GrotteFieldPanel grotteFieldPanel) {
        this.grotteMap = grotteMap;
        this.grotteFieldPanel = grotteFieldPanel;
        this.topWallFrontCells = new ArrayList<>();
        this.bottomWallFrontCells = new ArrayList<>();
        this.leftWallFrontCells = new ArrayList<>();
        this.rightWallFrontCells = new ArrayList<>();
        cacheWallFrontCells();
    }

    /**
     * On dit si une hitbox centrée peut occuper librement sa position dans la grotte.
     */
    @Override
    public boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        // Premier filtre : toute case de roche pleine bloque immédiatement le déplacement.
        for (Point blockedCell : grotteMap.getBlockedCells()) {
            Rectangle rockBounds = grotteFieldPanel.getLogicalCellBounds(blockedCell.x, blockedCell.y);
            if (rockBounds != null && rockBounds.intersects(entityBounds)) {
                return false;
            }
        }

        // Deuxième filtre : les façades murales visibles devant certaines cases marchables
        // doivent aussi bloquer, sinon on "couperait" visuellement dans les murs.
        if (intersectsAnyFront(entityBounds, topWallFrontCells, WallFront.TOP)
                || intersectsAnyFront(entityBounds, bottomWallFrontCells, WallFront.BOTTOM)
                || intersectsAnyFront(entityBounds, leftWallFrontCells, WallFront.LEFT)
                || intersectsAnyFront(entityBounds, rightWallFrontCells, WallFront.RIGHT)) {
            return false;
        }

        // Dernier filtre : la statue du sanctuaire garde sa propre masse bloquante.
        Rectangle shrineStatueBounds = grotteFieldPanel.getShrineStatueCollisionBounds();
        return shrineStatueBounds == null || !shrineStatueBounds.intersects(entityBounds);
    }

    /**
     * On mémorise les seules cases qui reçoivent une vraie façade murale,
     * ce qui évite de recalculer toute la carte à chaque frame de déplacement.
     */
    private void cacheWallFrontCells() {
        for (int row = 0; row < grotteMap.getHeight(); row++) {
            for (int column = 0; column < grotteMap.getWidth(); column++) {
                // On ne cherche des façades que sur des cases réellement marchables.
                if (!grotteMap.isWalkableCell(column, row)) {
                    continue;
                }

                // Si une roche touche la case par le haut, on aura une façade haute visible et bloquante.
                if (grotteMap.isWallCell(column, row - 1)) {
                    topWallFrontCells.add(new Point(column, row));
                }
                // Même logique pour le bas, sauf sur la vraie sortie ferme que l'on doit laisser libre.
                if (grotteMap.isWallCell(column, row + 1) && grotteMap.isActualFarmExitCell(column, row)) {
                    bottomWallFrontCells.add(new Point(column, row));
                }
                // Même règle pour la façade gauche, avec la sortie ferme toujours préservée.
                if (grotteMap.isWallCell(column - 1, row) && grotteMap.isActualFarmExitCell(column, row)) {
                    leftWallFrontCells.add(new Point(column, row));
                }
                // Et enfin pour la façade droite.
                if (grotteMap.isWallCell(column + 1, row) && grotteMap.isActualFarmExitCell(column, row)) {
                    rightWallFrontCells.add(new Point(column, row));
                }
            }
        }
    }

    /**
     * On teste une famille de façades murales contre la hitbox de l'entité.
     */
    private boolean intersectsAnyFront(Rectangle entityBounds, List<Point> cells, WallFront wallFront) {
        for (Point cell : cells) {
            Rectangle wallBounds = switch (wallFront) {
                case TOP -> grotteFieldPanel.getTopWallCollisionBounds(cell.x, cell.y);
                case BOTTOM -> grotteFieldPanel.getBottomWallCollisionBounds(cell.x, cell.y);
                case LEFT -> grotteFieldPanel.getLeftWallCollisionBounds(cell.x, cell.y);
                case RIGHT -> grotteFieldPanel.getRightWallCollisionBounds(cell.x, cell.y);
            };
            // Dès qu'une façade visible coupe la hitbox, on bloque le déplacement.
            if (wallBounds != null && wallBounds.intersects(entityBounds)) {
                return true;
            }
        }

        return false;
    }

    /**
     * On distingue les quatre familles de façades pour lire plus simplement les collisions.
     */
    private enum WallFront {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}
