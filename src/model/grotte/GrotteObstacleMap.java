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
 *
 * Toute la roche non creusée bloque le joueur.
 * Comme la carte est statique, on s'appuie sur la liste pré-calculée
 * des cases rocheuses pour éviter des tests inutiles.
 */
public final class GrotteObstacleMap implements MovementCollisionMap {
    private final GrotteMap grotteMap;
    private final GrotteFieldPanel grotteFieldPanel;
    private final List<Point> topWallFrontCells;
    private final List<Point> bottomWallFrontCells;
    private final List<Point> leftWallFrontCells;
    private final List<Point> rightWallFrontCells;

    public GrotteObstacleMap(GrotteMap grotteMap, GrotteFieldPanel grotteFieldPanel) {
        this.grotteMap = grotteMap;
        this.grotteFieldPanel = grotteFieldPanel;
        this.topWallFrontCells = new ArrayList<>();
        this.bottomWallFrontCells = new ArrayList<>();
        this.leftWallFrontCells = new ArrayList<>();
        this.rightWallFrontCells = new ArrayList<>();
        cacheWallFrontCells();
    }

    @Override
    public boolean canOccupyCenteredBox(double centerX, double centerY, int width, int height) {
        Rectangle entityBounds = BuildingGeometry.buildCenteredBounds(centerX, centerY, width, height);
        for (Point blockedCell : grotteMap.getBlockedCells()) {
            Rectangle rockBounds = grotteFieldPanel.getLogicalCellBounds(blockedCell.x, blockedCell.y);
            if (rockBounds != null && rockBounds.intersects(entityBounds)) {
                return false;
            }
        }

        if (intersectsAnyFront(entityBounds, topWallFrontCells, WallFront.TOP)
                || intersectsAnyFront(entityBounds, bottomWallFrontCells, WallFront.BOTTOM)
                || intersectsAnyFront(entityBounds, leftWallFrontCells, WallFront.LEFT)
                || intersectsAnyFront(entityBounds, rightWallFrontCells, WallFront.RIGHT)) {
            return false;
        }

        Rectangle shrineStatueBounds = grotteFieldPanel.getShrineStatueCollisionBounds();
        if (shrineStatueBounds != null && shrineStatueBounds.intersects(entityBounds)) {
            return false;
        }

        Rectangle chestBounds = grotteFieldPanel.getChestCollisionBounds();
        if (chestBounds != null && chestBounds.intersects(entityBounds)) {
            return false;
        }

        return true;
    }

    /**
     * On mémorise les seules cases qui reçoivent une vraie façade murale,
     * ce qui évite de recalculer toute la carte à chaque frame de déplacement.
     */
    private void cacheWallFrontCells() {
        for (int row = 0; row < grotteMap.getHeight(); row++) {
            for (int column = 0; column < grotteMap.getWidth(); column++) {
                if (!grotteMap.isWalkableCell(column, row)) {
                    continue;
                }

                if (grotteMap.isWallCell(column, row - 1)) {
                    topWallFrontCells.add(new Point(column, row));
                }
                if (grotteMap.isWallCell(column, row + 1) && !grotteMap.isFarmExitCell(column, row)) {
                    bottomWallFrontCells.add(new Point(column, row));
                }
                if (grotteMap.isWallCell(column - 1, row) && !grotteMap.isFarmExitCell(column, row)) {
                    leftWallFrontCells.add(new Point(column, row));
                }
                if (grotteMap.isWallCell(column + 1, row) && !grotteMap.isFarmExitCell(column, row)) {
                    rightWallFrontCells.add(new Point(column, row));
                }
            }
        }
    }

    private boolean intersectsAnyFront(Rectangle entityBounds, List<Point> cells, WallFront wallFront) {
        for (Point cell : cells) {
            Rectangle wallBounds = switch (wallFront) {
                case TOP -> grotteFieldPanel.getTopWallCollisionBounds(cell.x, cell.y);
                case BOTTOM -> grotteFieldPanel.getBottomWallCollisionBounds(cell.x, cell.y);
                case LEFT -> grotteFieldPanel.getLeftWallCollisionBounds(cell.x, cell.y);
                case RIGHT -> grotteFieldPanel.getRightWallCollisionBounds(cell.x, cell.y);
            };
            if (wallBounds != null && wallBounds.intersects(entityBounds)) {
                return true;
            }
        }

        return false;
    }

    private enum WallFront {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}
