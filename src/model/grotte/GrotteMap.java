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

    private static final Rectangle WATER_ROOM = new Rectangle(2, 2, 8, 7);
    private static final Rectangle SHRINE_ROOM = new Rectangle(11, 1, 9, 8);
    private static final Rectangle LAVA_ROOM = new Rectangle(21, 2, 8, 7);
    private static final Rectangle WORKSHOP_ROOM = new Rectangle(1, 12, 11, 7);
    private static final Rectangle STORAGE_ROOM = new Rectangle(19, 12, 11, 7);

    private static final Rectangle MAIN_VERTICAL_PATH = new Rectangle(13, 7, 5, 13);
    private static final Rectangle UPPER_CROSS_PATH = new Rectangle(7, 8, 17, 3);
    private static final Rectangle LEFT_ROOM_ENTRY = new Rectangle(8, 15, 6, 3);
    private static final Rectangle RIGHT_ROOM_ENTRY = new Rectangle(17, 15, 6, 3);
    private static final Rectangle SHRINE_DAIS = new Rectangle(13, 3, 5, 4);
    private static final Point FARM_EXIT_CELL = new Point(15, 20);

    private final TileType[][] tiles;
    private final List<Point> blockedCells;

    public GrotteMap() {
        this.tiles = new TileType[HEIGHT][WIDTH];
        this.blockedCells = new ArrayList<>();
        buildLayout();
        cacheBlockedCells();
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

    public Rectangle getWaterRoomBounds() {
        return new Rectangle(WATER_ROOM);
    }

    public Rectangle getShrineRoomBounds() {
        return new Rectangle(SHRINE_ROOM);
    }

    public Rectangle getShrineDaisBounds() {
        return new Rectangle(SHRINE_DAIS);
    }

    public Rectangle getLavaRoomBounds() {
        return new Rectangle(LAVA_ROOM);
    }

    public Rectangle getWorkshopRoomBounds() {
        return new Rectangle(WORKSHOP_ROOM);
    }

    public Rectangle getStorageRoomBounds() {
        return new Rectangle(STORAGE_ROOM);
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
        return FARM_EXIT_CELL.x == column && FARM_EXIT_CELL.y == row;
    }

    public List<Point> getBlockedCells() {
        return Collections.unmodifiableList(blockedCells);
    }

    private void buildLayout() {
        fillWith(TileType.ROCK);

        /*
         * On creuse peu de salles, mais elles sont plus grandes que dans la référence :
         * cela garde l'esprit "hub de grotte" tout en laissant vraiment de l'espace
         * pour circuler à l'intérieur.
         */
        carveRoundedRoom(WATER_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(SHRINE_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(LAVA_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(WORKSHOP_ROOM, TileType.ROOM_FLOOR);
        carveRoundedRoom(STORAGE_ROOM, TileType.ROOM_FLOOR);

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
}
