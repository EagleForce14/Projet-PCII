package model.grotte;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Décrit la structure logique complète de la grotte.
 *
 * Le layout vise explicitement le rendu demandé :
 * un axe central pavé, quelques grandes salles jouables,
 * et beaucoup de roche autour pour retrouver une lecture "donjon".
 */
public final class GrotteMap {
    public enum TileType {
        ROCK,
        ROOM_FLOOR,
        PATH
    }

    private static final int WIDTH = 31;
    private static final int HEIGHT = 21;
    private static final Point SPAWN_CELL = new Point(15, 18);

    private static final Rectangle UPPER_LEFT_ROOM = new Rectangle(2, 2, 8, 7);
    private static final Rectangle SHRINE_ROOM = new Rectangle(11, 1, 9, 8);
    private static final Rectangle UPPER_RIGHT_ROOM = new Rectangle(21, 2, 8, 7);
    private static final Rectangle LOWER_LEFT_ROOM = new Rectangle(1, 12, 11, 7);
    private static final Rectangle LOWER_RIGHT_ROOM = new Rectangle(19, 12, 11, 7);

    private static final Rectangle MAIN_VERTICAL_PATH = new Rectangle(13, 7, 5, 13);
    private static final Rectangle UPPER_CROSS_PATH = new Rectangle(7, 8, 17, 3);
    private static final Rectangle LEFT_ROOM_ENTRY = new Rectangle(8, 15, 6, 3);
    private static final Rectangle RIGHT_ROOM_ENTRY = new Rectangle(17, 15, 6, 3);
    private static final Rectangle SHRINE_DAIS = new Rectangle(13, 3, 5, 4);
    private static final Point FARM_EXIT_CELL = new Point(15, 20);
    private static final double SHRINE_HAZARD_HALF_WIDTH_TILES = 2.75;
    private static final double SHRINE_HAZARD_FORWARD_REACH_TILES = 4.9;
    private static final double SHRINE_HAZARD_FRONT_OFFSET_TILES = 0.2;

    private final TileType[][] tiles;
    private final List<Point> blockedCells;
    private final List<Point> shrineDangerCells;
    private final List<Point> healingCells;

    public GrotteMap() {
        this.tiles = new TileType[HEIGHT][WIDTH];
        this.blockedCells = new ArrayList<>();
        this.shrineDangerCells = new ArrayList<>();
        this.healingCells = new ArrayList<>();
        buildLayout();
        cacheBlockedCells();
        cacheShrineDangerCells();
        cacheHealingCells();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public Point getSpawnCell() {
        return new Point(SPAWN_CELL);
    }

    public Point getFarmExitCell() {
        return new Point(FARM_EXIT_CELL);
    }

    public Rectangle getUpperLeftRoomBounds() {
        return new Rectangle(UPPER_LEFT_ROOM);
    }

    public Rectangle getShrineRoomBounds() {
        return new Rectangle(SHRINE_ROOM);
    }

    public Rectangle getShrineDaisBounds() {
        return new Rectangle(SHRINE_DAIS);
    }

    public Rectangle getUpperRightRoomBounds() {
        return new Rectangle(UPPER_RIGHT_ROOM);
    }

    public Rectangle getLowerLeftRoomBounds() {
        return new Rectangle(LOWER_LEFT_ROOM);
    }

    public Rectangle getLowerRightRoomBounds() {
        return new Rectangle(LOWER_RIGHT_ROOM);
    }

    public boolean isInside(int column, int row) {
        return column >= 0 && column < WIDTH && row >= 0 && row < HEIGHT;
    }

    public TileType getTileType(int column, int row) {
        if (!isInside(column, row)) {
            return TileType.ROCK;
        }

        return tiles[row][column];
    }

    public boolean isWallCell(int column, int row) {
        return getTileType(column, row) == TileType.ROCK;
    }

    public boolean isWalkableCell(int column, int row) {
        return getTileType(column, row) != TileType.ROCK;
    }

    public boolean isPathCell(int column, int row) {
        return getTileType(column, row) == TileType.PATH;
    }

    public boolean isRoomFloorCell(int column, int row) {
        return getTileType(column, row) == TileType.ROOM_FLOOR;
    }

    public boolean isFarmExitCell(int column, int row) {
        return FARM_EXIT_CELL.x != column || FARM_EXIT_CELL.y != row;
    }

    /**
     * Cette méthode explicite complète `isFarmExitCell`,
     * dont la sémantique historique est conservée pour ne pas casser
     * le code local déjà présent dans le projet.
     */
    public boolean isActualFarmExitCell(int column, int row) {
        return FARM_EXIT_CELL.x == column && FARM_EXIT_CELL.y == row;
    }

    public List<Point> getBlockedCells() {
        return Collections.unmodifiableList(blockedCells);
    }

    /**
     * Les cases touchées par l'onde de la statue sont pré-calculées une seule fois.
     * Toute la logique de rendu et de danger réutilise ensuite exactement cette liste.
     */
    public List<Point> getShrineDangerCells() {
        return Collections.unmodifiableList(shrineDangerCells);
    }

    public boolean isShrineDangerCell(int column, int row) {
        for (Point dangerCell : shrineDangerCells) {
            if (dangerCell.x == column && dangerCell.y == row) {
                return true;
            }
        }
        return false;
    }

    public List<Point> getHealingCells() {
        return Collections.unmodifiableList(healingCells);
    }

    public boolean isHealingCell(int column, int row) {
        for (Point healingCell : healingCells) {
            if (healingCell.x == column && healingCell.y == row) {
                return true;
            }
        }
        return false;
    }

    private void buildLayout() {
        fillWith(TileType.ROCK);

        /*
         * On creuse peu de salles, mais elles sont plus grandes que dans la référence :
         * cela garde l'esprit "hub de grotte" tout en laissant vraiment de l'espace
         * pour circuler à l'intérieur.
         */
        carveRoundedRoom(UPPER_LEFT_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(SHRINE_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(UPPER_RIGHT_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(LOWER_LEFT_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(LOWER_RIGHT_ROOM, TileType.ROOM_FLOOR);

        // L'axe principal reste plus lumineux et plus lisible grâce aux dalles.
        carveRectangle(UPPER_CROSS_PATH, TileType.PATH);
        carveRectangle(MAIN_VERTICAL_PATH, TileType.PATH);
        carveRectangle(LEFT_ROOM_ENTRY, TileType.PATH);
        carveRectangle(RIGHT_ROOM_ENTRY, TileType.PATH);
        carveRectangle(SHRINE_DAIS, TileType.PATH);
        setTile(FARM_EXIT_CELL.x, FARM_EXIT_CELL.y, TileType.PATH);

        // On garde seulement les coins arrondis des salles.
        // Les petits blocs isolés à l'intérieur rendaient certains passages plus laids
        // et forçaient des détours inutiles.
    }

    private void fillWith(TileType tileType) {
        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                tiles[row][column] = tileType;
            }
        }
    }

    private void carveRectangle(Rectangle area, TileType tileType) {
        for (int row = area.y; row < area.y + area.height; row++) {
            for (int column = area.x; column < area.x + area.width; column++) {
                setTile(column, row, tileType);
            }
        }
    }

    private void carveRoundedRoom(Rectangle area, TileType tileType) {
        carveRectangle(area, tileType);

        // On coupe juste les coins pour garder une silhouette de grotte naturelle
        // sans rendre le plan difficile à lire dans le code.
        setTile(area.x, area.y, TileType.ROCK);
        setTile(area.x + area.width - 1, area.y, TileType.ROCK);
        setTile(area.x, area.y + area.height - 1, TileType.ROCK);
        setTile(area.x + area.width - 1, area.y + area.height - 1, TileType.ROCK);
    }

    private void setTile(int column, int row, TileType tileType) {
        if (isInside(column, row)) {
            tiles[row][column] = tileType;
        }
    }

    private void cacheBlockedCells() {
        blockedCells.clear();
        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                if (isWallCell(column, row)) {
                    blockedCells.add(new Point(column, row));
                }
            }
        }
    }

    /**
     * La zone létale doit surtout vivre devant la statue :
     * derrière, la hitbox bloque déjà naturellement le joueur.
     *
     * On construit donc une demi-ellipse projetée vers l'avant du sanctuaire,
     * plus concentrée et beaucoup plus lisible en gameplay.
     */
    private void cacheShrineDangerCells() {
        shrineDangerCells.clear();

        double statueCenterX = SHRINE_DAIS.getCenterX();
        double statueFrontY = SHRINE_DAIS.y + SHRINE_DAIS.height - SHRINE_HAZARD_FRONT_OFFSET_TILES;

        for (int row = 0; row < HEIGHT; row++) {
            for (int column = 0; column < WIDTH; column++) {
                if (!isWalkableCell(column, row)) {
                    continue;
                }

                double cellCenterX = column + 0.5;
                double cellCenterY = row + 0.5;
                double forwardDistance = cellCenterY - statueFrontY;
                if (forwardDistance < 0.0 || forwardDistance > SHRINE_HAZARD_FORWARD_REACH_TILES) {
                    continue;
                }

                double lateralDistance = cellCenterX - statueCenterX;
                double normalizedLateral = lateralDistance / SHRINE_HAZARD_HALF_WIDTH_TILES;
                double normalizedForward = forwardDistance / SHRINE_HAZARD_FORWARD_REACH_TILES;
                double forwardEllipseValue = (normalizedLateral * normalizedLateral) + (normalizedForward * normalizedForward);
                if (forwardEllipseValue <= 1.0) {
                    shrineDangerCells.add(new Point(column, row));
                }
            }
        }
    }

    /**
     * Chaque salle reçoit une petite zone de soin de 4 cases.
     * On vise d'abord le centre géométrique, puis on complète avec des cases
     * voisines dans la même salle si l'une des 4 cases centrales est bloquée.
     */
    private void cacheHealingCells() {
        healingCells.clear();
        addRoomHealingCells(UPPER_LEFT_ROOM);
        addRoomHealingCells(UPPER_RIGHT_ROOM);
        addRoomHealingCells(LOWER_LEFT_ROOM);
        addRoomHealingCells(LOWER_RIGHT_ROOM);
    }

    private void addRoomHealingCells(Rectangle roomBounds) {
        if (roomBounds == null) {
            return;
        }

        int centerStartX = roomBounds.x + ((roomBounds.width - 2) / 2);
        int centerStartY = roomBounds.y + ((roomBounds.height - 2) / 2);

        addHealingCellIfValid(centerStartX, centerStartY, roomBounds);
        addHealingCellIfValid(centerStartX + 1, centerStartY, roomBounds);
        addHealingCellIfValid(centerStartX, centerStartY + 1, roomBounds);
        addHealingCellIfValid(centerStartX + 1, centerStartY + 1, roomBounds);

        if (countHealingCellsInRoom(roomBounds) >= 4) {
            return;
        }

        double centerX = roomBounds.getCenterX();
        double centerY = roomBounds.getCenterY();
        for (int radius = 1; radius <= Math.max(roomBounds.width, roomBounds.height); radius++) {
            for (int row = roomBounds.y; row < roomBounds.y + roomBounds.height; row++) {
                for (int column = roomBounds.x; column < roomBounds.x + roomBounds.width; column++) {
                    if (Math.abs((column + 0.5) - centerX) > radius || Math.abs((row + 0.5) - centerY) > radius) {
                        continue;
                    }
                    addHealingCellIfValid(column, row, roomBounds);
                    if (countHealingCellsInRoom(roomBounds) >= 4) {
                        return;
                    }
                }
            }
        }
    }

    private int countHealingCellsInRoom(Rectangle roomBounds) {
        int count = 0;
        for (Point healingCell : healingCells) {
            if (roomBounds.contains(healingCell)) {
                count++;
            }
        }
        return count;
    }

    private void addHealingCellIfValid(int column, int row, Rectangle roomBounds) {
        if (!roomBounds.contains(column, row) || !isWalkableCell(column, row)) {
            return;
        }

        for (Point healingCell : healingCells) {
            if (healingCell.x == column && healingCell.y == row) {
                return;
            }
        }

        healingCells.add(new Point(column, row));
    }
}
