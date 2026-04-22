package view.grotte;

import model.grotte.GrotteMap;
import model.grotte.ShrineHazardState;
import model.movement.Unit;
import view.PlayableMapPanel;
import view.CustomFontLoader;
import view.HudProgressBarPainter;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Panneau d'affichage dédié à la grotte.
 *
 * Optimisation importante :
 * tout le décor statique est pré-rendu dans un buffer.
 * Pendant le jeu, on ne redessine donc plus que cette image en cache
 * et la surbrillance de la case active.
 */
public final class GrotteFieldPanel extends JPanel implements PlayableMapPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    private static final int PREF_WIDTH = 1180;
    private static final int PREF_HEIGHT = 850;

    private static final Color SCENE_BACKGROUND = new Color(12, 10, 18);
    private static final Color WALL_SHADOW = new Color(7, 6, 10, 118);
    private static final Color EDGE_LIGHT = new Color(255, 198, 128, 34);
    private static final Color VIGNETTE = new Color(0, 0, 0, 48);
    private static final Color SHRINE_WARNING_FILL_ON = new Color(206, 36, 36, 126);
    private static final Color SHRINE_WARNING_FILL_OFF = new Color(128, 24, 24, 48);
    private static final Color SHRINE_WARNING_BORDER_ON = new Color(255, 126, 126, 220);
    private static final Color SHRINE_WARNING_BORDER_OFF = new Color(168, 70, 70, 118);
    private static final Color SHRINE_BAR_FRAME = new Color(214, 106, 106, 248);
    private static final Color SHRINE_BAR_BACKGROUND = new Color(38, 10, 12, 228);
    private static final Color SHRINE_BAR_FILL = new Color(202, 42, 42, 248);
    private static final Color SHRINE_BAR_HIGHLIGHT = new Color(255, 184, 184, 210);
    private static final Color SHRINE_BAR_LABEL = new Color(255, 223, 223);
    private static final Color SHRINE_BAR_LABEL_SHADOW = new Color(60, 10, 10, 220);
    private static final Color HEAL_ZONE_FILL = new Color(44, 172, 78, 54);
    private static final Color HEAL_ZONE_BORDER = new Color(130, 236, 162, 160);
    private static final Color HEAL_PLUS_COLOR = new Color(194, 255, 210, 220);
    private static final Color HEAL_PLUS_SHADOW = new Color(14, 50, 26, 180);

    private final GrotteMap grotteMap;
    private final ShrineHazardState shrineHazardState;
    private final Image[] rockTiles;
    private final Image[] roomFloorTiles;
    private final Image[] pathTiles;
    private final Image horizontalWallTile;
    private final Image verticalWallTile;
    private final Image shrineStatueImage;
    private final Font shrineHazardTitleFont;
    private final Font shrineHazardTimerFont;

    private BufferedImage staticSceneCache;
    private BufferedImage scaledHorizontalWallTile;
    private BufferedImage scaledVerticalWallTile;
    private int cachedSceneWidth = -1;
    private int cachedSceneHeight = -1;
    private int cachedHorizontalWallTileSize = -1;
    private int cachedHorizontalWallTileHeight = -1;
    private int cachedVerticalWallTileSize = -1;
    private int cachedVerticalWallTileWidth = -1;

    public GrotteFieldPanel(GrotteMap grotteMap, ShrineHazardState shrineHazardState) {
        this.grotteMap = grotteMap;
        this.shrineHazardState = shrineHazardState;
        this.rockTiles = GrotteTileFactory.createRockTiles();
        this.roomFloorTiles = GrotteTileFactory.createRoomFloorTiles();
        this.pathTiles = GrotteTileFactory.createPathTiles();
        this.horizontalWallTile = GrotteTileFactory.createHorizontalWallTile();
        this.verticalWallTile = GrotteTileFactory.createVerticalWallTile();
        this.shrineStatueImage = GrotteTileFactory.createShrineStatueImage();
        this.shrineHazardTitleFont = CustomFontLoader.loadFont(FONT_PATH, 10.5f);
        this.shrineHazardTimerFont = CustomFontLoader.loadFont(FONT_PATH, 9.0f);
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        setOpaque(false);
    }

    public int getColumnCount() {
        return grotteMap.getWidth();
    }

    public int getRowCount() {
        return grotteMap.getHeight();
    }

    public GrotteMap getGrotteMap() {
        return grotteMap;
    }

    @Override
    public Rectangle getFieldBounds() {
        int columns = getColumnCount();
        int rows = getRowCount();
        if (getWidth() <= 0 || getHeight() <= 0 || columns <= 0 || rows <= 0) {
            return new Rectangle();
        }

        int tileSize = (int) Math.ceil(Math.max(
                (double) getWidth() / columns,
                (double) getHeight() / rows
        ));
        int gridWidth = tileSize * columns;
        int gridHeight = tileSize * rows;
        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;
        return new Rectangle(startX, startY, gridWidth, gridHeight);
    }

    @Override
    public Point getInitialPlayerOffset() {
        Point spawnCell = grotteMap.getSpawnCell();
        Rectangle preferredFieldBounds = getPreferredFieldBounds();
        int tileSize = getTileSize(preferredFieldBounds);
        return new Point(
                -(preferredFieldBounds.width / 2) + (spawnCell.x * tileSize) + (tileSize / 2),
                -(preferredFieldBounds.height / 2) + (spawnCell.y * tileSize) + (tileSize / 2)
        );
    }

    @Override
    public Point getFullyOccupiedCell(Rectangle unitBounds) {
        if (unitBounds == null) {
            return null;
        }

        Point topLeftCell = getGridPositionAt(unitBounds.x, unitBounds.y);
        Point bottomRightCell = getGridPositionAt(
                unitBounds.x + unitBounds.width - 1,
                unitBounds.y + unitBounds.height - 1
        );
        if (topLeftCell == null || !topLeftCell.equals(bottomRightCell)) {
            return null;
        }

        Rectangle cellBounds = getCellBounds(topLeftCell.x, topLeftCell.y);
        if (cellBounds == null || !cellBounds.contains(unitBounds)) {
            return null;
        }

        return topLeftCell;
    }

    @Override
    public boolean isFarmableCell(Point cell) {
        return cell != null && grotteMap.isWalkableCell(cell.x, cell.y);
    }

    @Override
    public Point getGridPositionAt(int pixelX, int pixelY) {
        Rectangle fieldBounds = getFieldBounds();
        if (!fieldBounds.contains(pixelX, pixelY)) {
            return null;
        }

        int tileSize = getTileSize(fieldBounds);
        int gridX = (pixelX - fieldBounds.x) / tileSize;
        int gridY = (pixelY - fieldBounds.y) / tileSize;
        return grotteMap.isInside(gridX, gridY) ? new Point(gridX, gridY) : null;
    }

    @Override
    public void setHighlightedCell(Point highlightedCell) {
        // En grotte, la surbrillance de case est volontairement désactivée.
        // On ignore donc les mises à jour de highlight.
    }

    @Override
    public int resolveMovementSpeed(Point cell) {
        return cell != null && grotteMap.isPathCell(cell.x, cell.y) ? Unit.PATH_SPEED : Unit.NORMAL_SPEED;
    }

    @Override
    public Component getMapComponent() {
        return this;
    }

    public Rectangle getCellBounds(int column, int row) {
        if (!grotteMap.isInside(column, row)) {
            return null;
        }

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        return new Rectangle(
                fieldBounds.x + (column * tileSize),
                fieldBounds.y + (row * tileSize),
                tileSize,
                tileSize
        );
    }

    public Rectangle getLogicalCellBounds(int column, int row) {
        if (!grotteMap.isInside(column, row)) {
            return null;
        }

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        return new Rectangle(
                -(fieldBounds.width / 2) + (column * tileSize),
                -(fieldBounds.height / 2) + (row * tileSize),
                tileSize,
                tileSize
        );
    }

    public Point getLogicalGridPositionAt(int logicalPixelX, int logicalPixelY) {
        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        int logicalStartX = -(fieldBounds.width / 2);
        int logicalStartY = -(fieldBounds.height / 2);

        int gridX = (logicalPixelX - logicalStartX) / tileSize;
        int gridY = (logicalPixelY - logicalStartY) / tileSize;
        return grotteMap.isInside(gridX, gridY) ? new Point(gridX, gridY) : null;
    }

    /**
     * Les façades de mur dépassent visuellement dans des cases pourtant marchables.
     * On expose donc leurs zones logiques pour que la collision suive le rendu.
     */
    public Rectangle getTopWallCollisionBounds(int column, int row) {
        Rectangle cellBounds = getLogicalCellBounds(column, row);
        if (cellBounds == null) {
            return null;
        }

        int wallHeight = Math.min(cellBounds.height, resolveHorizontalWallHeight(cellBounds.width));
        return new Rectangle(cellBounds.x, cellBounds.y, cellBounds.width, wallHeight);
    }

    public Rectangle getBottomWallCollisionBounds(int column, int row) {
        Rectangle cellBounds = getLogicalCellBounds(column, row);
        if (cellBounds == null) {
            return null;
        }

        int wallHeight = Math.min(cellBounds.height, resolveHorizontalWallHeight(cellBounds.width));
        return new Rectangle(
                cellBounds.x,
                cellBounds.y + cellBounds.height - wallHeight,
                cellBounds.width,
                wallHeight
        );
    }

    public Rectangle getLeftWallCollisionBounds(int column, int row) {
        Rectangle cellBounds = getLogicalCellBounds(column, row);
        if (cellBounds == null) {
            return null;
        }

        int wallWidth = Math.min(cellBounds.width, resolveVerticalWallWidth(cellBounds.height));
        return new Rectangle(cellBounds.x, cellBounds.y, wallWidth, cellBounds.height);
    }

    public Rectangle getRightWallCollisionBounds(int column, int row) {
        Rectangle cellBounds = getLogicalCellBounds(column, row);
        if (cellBounds == null) {
            return null;
        }

        int wallWidth = Math.min(cellBounds.width, resolveVerticalWallWidth(cellBounds.height));
        return new Rectangle(
                cellBounds.x + cellBounds.width - wallWidth,
                cellBounds.y,
                wallWidth,
                cellBounds.height
        );
    }

    /**
     * La statue du sanctuaire est décorative, mais elle doit aussi bloquer le passage.
     * La hitbox reste volontairement un peu plus petite que le sprite
     * pour garder une sensation naturelle autour du socle.
     */
    public Rectangle getShrineStatueCollisionBounds() {
        Rectangle daisBounds = buildLogicalAreaBounds(grotteMap.getShrineDaisBounds());
        if (daisBounds == null || daisBounds.width <= 0 || daisBounds.height <= 0) {
            return null;
        }

        int tileSize = getLogicalTileSize();
        Rectangle statueBounds = buildShrineStatueBounds(daisBounds, tileSize);
        if (statueBounds == null) {
            return null;
        }

        int collisionInsetX = Math.max(6, statueBounds.width / 6);
        int collisionInsetTop = Math.max(8, statueBounds.height / 8);
        int collisionInsetBottom = Math.max(8, statueBounds.height / 6);
        return new Rectangle(
                statueBounds.x + collisionInsetX,
                statueBounds.y + collisionInsetTop,
                Math.max(12, statueBounds.width - (collisionInsetX * 2)),
                Math.max(14, statueBounds.height - collisionInsetTop - collisionInsetBottom)
        );
    }

    private void ensureStaticSceneCache() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            staticSceneCache = null;
            cachedSceneWidth = -1;
            cachedSceneHeight = -1;
            return;
        }

        if (staticSceneCache != null
                && cachedSceneWidth == getWidth()
                && cachedSceneHeight == getHeight()) {
            return;
        }

        cachedSceneWidth = getWidth();
        cachedSceneHeight = getHeight();
        staticSceneCache = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = staticSceneCache.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        paintStaticScene(g2);
        g2.dispose();
    }

    private void paintStaticScene(Graphics2D g2) {
        g2.setColor(SCENE_BACKGROUND);
        g2.fillRect(0, 0, getWidth(), getHeight());

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);

        drawRockLayer(g2, fieldBounds, tileSize);
        drawWalkableGroundLayer(g2, fieldBounds, tileSize);
        drawRockEdgeRelief(g2, fieldBounds, tileSize);
        drawShrineStatue(g2, fieldBounds, tileSize);
        drawVignette(g2);
    }

    private void drawRockLayer(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                drawScaledTile(g2, getRockTile(column, row), buildCellBounds(fieldBounds, tileSize, column, row));
            }
        }
    }

    private void drawWalkableGroundLayer(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                if (!grotteMap.isWalkableCell(column, row)) {
                    continue;
                }

                drawScaledTile(g2, getGroundTile(column, row), buildCellBounds(fieldBounds, tileSize, column, row));
            }
        }
    }

    private void drawRockEdgeRelief(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        int shadowThickness = Math.max(5, tileSize / 7);
        int lightThickness = Math.max(2, tileSize / 14);
        BufferedImage horizontalWallTexture = getHorizontalWallTexture(tileSize);
        BufferedImage verticalWallTexture = getVerticalWallTexture(tileSize);

        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                if (!grotteMap.isWalkableCell(column, row)) {
                    continue;
                }

                Rectangle cellBounds = buildCellBounds(fieldBounds, tileSize, column, row);

                if (grotteMap.isWallCell(column, row - 1)) {
                    drawHorizontalWallFront(g2, cellBounds, horizontalWallTexture, shadowThickness, lightThickness);
                }
                if (grotteMap.isWallCell(column - 1, row) && !grotteMap.isActualFarmExitCell(column, row)) {
                    drawVerticalWallBorder(g2, cellBounds, verticalWallTexture, true, shadowThickness);
                }
                if (grotteMap.isWallCell(column + 1, row) && !grotteMap.isActualFarmExitCell(column, row)) {
                    drawVerticalWallBorder(g2, cellBounds, verticalWallTexture, false, shadowThickness);
                }
                if (grotteMap.isWallCell(column, row + 1) && !grotteMap.isActualFarmExitCell(column, row)) {
                    drawBottomWallBorder(g2, cellBounds, horizontalWallTexture, shadowThickness);
                }
            }
        }
    }

    /**
     * Les murs horizontaux partagent tous le même sprite.
     * On le met à l'échelle une seule fois par taille de tuile,
     * puis on le réutilise partout dans le cache statique.
     */
    private BufferedImage getHorizontalWallTexture(int tileSize) {
        if (horizontalWallTile == null) {
            return null;
        }

        int sourceWidth = horizontalWallTile.getWidth(null);
        int sourceHeight = horizontalWallTile.getHeight(null);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return null;
        }

        // On force une façade plus haute que le simple ratio de l'image
        // pour donner un vrai effet de paroi qui domine le sol.
        int wallHeight = resolveHorizontalWallHeight(tileSize);
        if (scaledHorizontalWallTile != null
                && cachedHorizontalWallTileSize == tileSize
                && cachedHorizontalWallTileHeight == wallHeight) {
            return scaledHorizontalWallTile;
        }

        cachedHorizontalWallTileSize = tileSize;
        cachedHorizontalWallTileHeight = wallHeight;
        scaledHorizontalWallTile = new BufferedImage(tileSize, wallHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = scaledHorizontalWallTile.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(horizontalWallTile, 0, 0, tileSize, wallHeight, this);
        g2.dispose();
        return scaledHorizontalWallTile;
    }

    /**
     * Les bordures latérales utilisent l'asset vertical dédié.
     * L'image est mise à l'échelle une seule fois par taille de tuile.
     */
    private BufferedImage getVerticalWallTexture(int tileSize) {
        if (verticalWallTile == null) {
            return null;
        }

        int sourceWidth = verticalWallTile.getWidth(null);
        int sourceHeight = verticalWallTile.getHeight(null);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return null;
        }

        int wallWidth = resolveVerticalWallWidth(tileSize);
        if (scaledVerticalWallTile != null
                && cachedVerticalWallTileSize == tileSize
                && cachedVerticalWallTileWidth == wallWidth) {
            return scaledVerticalWallTile;
        }

        cachedVerticalWallTileSize = tileSize;
        cachedVerticalWallTileWidth = wallWidth;
        scaledVerticalWallTile = new BufferedImage(wallWidth, tileSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = scaledVerticalWallTile.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(verticalWallTile, 0, 0, wallWidth, tileSize, this);
        g2.dispose();
        return scaledVerticalWallTile;
    }

    private void drawHorizontalWallFront(
            Graphics2D g2,
            Rectangle cellBounds,
            BufferedImage horizontalWallTexture,
            int shadowThickness,
            int lightThickness
    ) {
        if (horizontalWallTexture == null) {
            g2.setColor(WALL_SHADOW);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, shadowThickness);
            g2.setColor(EDGE_LIGHT);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, lightThickness);
            return;
        }

        int wallHeight = Math.min(cellBounds.height, horizontalWallTexture.getHeight());
        g2.drawImage(horizontalWallTexture, cellBounds.x, cellBounds.y, cellBounds.width, wallHeight, this);

        // Une bande d'ombre plus profonde au pied du mur aide l'oeil
        // à lire la différence de niveau entre la paroi et la zone marchable.
        int footShadowHeight = Math.max(3, wallHeight / 5);
        g2.setColor(new Color(0, 0, 0, 68));
        g2.fillRect(
                cellBounds.x,
                cellBounds.y + wallHeight - footShadowHeight,
                cellBounds.width,
                footShadowHeight
        );

        int castShadowHeight = Math.max(4, cellBounds.height / 4);
        g2.setColor(new Color(0, 0, 0, 54));
        g2.fillRect(
                cellBounds.x,
                cellBounds.y + wallHeight - Math.max(1, footShadowHeight / 2),
                cellBounds.width,
                Math.min(castShadowHeight, cellBounds.height - wallHeight + Math.max(1, footShadowHeight / 2))
        );

        g2.setColor(new Color(255, 214, 154, 34));
        g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, Math.max(2, wallHeight / 12));
    }

    private void drawVerticalWallBorder(
            Graphics2D g2,
            Rectangle cellBounds,
            BufferedImage verticalWallTexture,
            boolean leftSide,
            int fallbackThickness
    ) {
        if (verticalWallTexture == null) {
            int drawX = leftSide ? cellBounds.x : cellBounds.x + cellBounds.width - fallbackThickness;
            g2.setColor(new Color(0, 0, 0, 58));
            g2.fillRect(drawX, cellBounds.y, fallbackThickness, cellBounds.height);
            return;
        }

        int wallWidth = Math.min(cellBounds.width, verticalWallTexture.getWidth());
        int drawX = leftSide ? cellBounds.x : cellBounds.x + cellBounds.width - wallWidth;
        g2.drawImage(verticalWallTexture, drawX, cellBounds.y, wallWidth, cellBounds.height, this);

        int innerShadowWidth = Math.max(3, wallWidth / 4);
        g2.setColor(new Color(0, 0, 0, leftSide ? 54 : 72));
        g2.fillRect(
                leftSide ? drawX + wallWidth - innerShadowWidth : drawX,
                cellBounds.y,
                innerShadowWidth,
                cellBounds.height
        );
    }

    private void drawBottomWallBorder(
            Graphics2D g2,
            Rectangle cellBounds,
            BufferedImage horizontalWallTexture,
            int fallbackThickness
    ) {
        if (horizontalWallTexture == null) {
            g2.setColor(new Color(0, 0, 0, 44));
            g2.fillRect(cellBounds.x, cellBounds.y + cellBounds.height - fallbackThickness, cellBounds.width, fallbackThickness);
            return;
        }

        // La bordure basse doit garder exactement la même échelle visuelle
        // que les autres façades horizontales utilisant `grotte_mur_droit`.
        int wallHeight = Math.min(cellBounds.height, horizontalWallTexture.getHeight());
        int drawY = cellBounds.y + cellBounds.height - wallHeight;
        g2.drawImage(horizontalWallTexture, cellBounds.x, drawY, cellBounds.width, wallHeight, this);

        int topShadowHeight = Math.max(3, wallHeight / 6);
        g2.setColor(new Color(0, 0, 0, 44));
        g2.fillRect(cellBounds.x, drawY, cellBounds.width, topShadowHeight);

        int footShadowHeight = Math.max(3, wallHeight / 5);
        g2.setColor(new Color(0, 0, 0, 34));
        g2.fillRect(
                cellBounds.x,
                cellBounds.y + cellBounds.height - footShadowHeight,
                cellBounds.width,
                footShadowHeight
        );
    }

    /**
     * On réintroduit uniquement la statue du sanctuaire.
     * Tout le reste du décor généré a été volontairement retiré.
     */
    private void drawShrineStatue(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        if (shrineStatueImage == null) {
            return;
        }

        Rectangle daisBounds = buildAreaBounds(fieldBounds, tileSize, grotteMap.getShrineDaisBounds());
        if (daisBounds == null || daisBounds.width <= 0 || daisBounds.height <= 0) {
            return;
        }
        Rectangle statueBounds = buildShrineStatueBounds(daisBounds, tileSize);
        if (statueBounds == null) {
            return;
        }

        g2.setColor(new Color(0, 0, 0, 62));
        g2.fillOval(
                statueBounds.x + Math.max(4, statueBounds.width / 7),
                daisBounds.y + daisBounds.height - Math.max(10, tileSize / 3),
                Math.max(14, statueBounds.width - Math.max(8, statueBounds.width / 3)),
                Math.max(8, tileSize / 3)
        );

        g2.drawImage(
                shrineStatueImage,
                statueBounds.x,
                statueBounds.y,
                statueBounds.width,
                statueBounds.height,
                this
        );
    }

    private void drawVignette(Graphics2D g2) {
        int band = Math.max(36, Math.min(getWidth(), getHeight()) / 8);
        g2.setColor(VIGNETTE);
        g2.fillRect(0, 0, getWidth(), band);
        g2.fillRect(0, getHeight() - band, getWidth(), band);
        g2.fillRect(0, 0, band, getHeight());
        g2.fillRect(getWidth() - band, 0, band, getHeight());
    }

    private Image getRockTile(int column, int row) {
        return rockTiles[Math.floorMod((column * 13) + (row * 7), rockTiles.length)];
    }

    private Image getGroundTile(int column, int row) {
        if (grotteMap.isPathCell(column, row)) {
            return pathTiles[Math.floorMod((column * 31) + (row * 17), pathTiles.length)];
        }

        return roomFloorTiles[Math.floorMod((column * 19) + (row * 11), roomFloorTiles.length)];
    }

    private Rectangle buildCellBounds(Rectangle fieldBounds, int tileSize, int column, int row) {
        return new Rectangle(
                fieldBounds.x + (column * tileSize),
                fieldBounds.y + (row * tileSize),
                tileSize,
                tileSize
        );
    }

    private Rectangle buildAreaBounds(Rectangle fieldBounds, int tileSize, Rectangle gridArea) {
        if (gridArea == null) {
            return null;
        }

        return new Rectangle(
                fieldBounds.x + (gridArea.x * tileSize),
                fieldBounds.y + (gridArea.y * tileSize),
                gridArea.width * tileSize,
                gridArea.height * tileSize
        );
    }

    private Rectangle buildLogicalAreaBounds(Rectangle gridArea) {
        if (gridArea == null) {
            return null;
        }

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        return new Rectangle(
                -(fieldBounds.width / 2) + (gridArea.x * tileSize),
                -(fieldBounds.height / 2) + (gridArea.y * tileSize),
                gridArea.width * tileSize,
                gridArea.height * tileSize
        );
    }

    /**
     * La statue doit être calculée une seule fois avec la même formule
     * pour le rendu et pour la collision.
     */
    private Rectangle buildShrineStatueBounds(Rectangle daisBounds, int tileSize) {
        if (shrineStatueImage == null || daisBounds == null || tileSize <= 0) {
            return null;
        }

        int sourceWidth = shrineStatueImage.getWidth(null);
        int sourceHeight = shrineStatueImage.getHeight(null);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return null;
        }

        int drawHeight = Math.max(tileSize * 2, (int) Math.round(daisBounds.height * 0.82));
        int drawWidth = Math.max(
                (int) Math.round(tileSize * 1.5),
                (int) Math.round(drawHeight * (sourceWidth / (double) sourceHeight))
        );
        int drawX = daisBounds.x + ((daisBounds.width - drawWidth) / 2);
        int drawY = daisBounds.y + daisBounds.height - drawHeight + Math.max(2, tileSize / 10);
        return new Rectangle(drawX, drawY, drawWidth, drawHeight);
    }

    private int getLogicalTileSize() {
        return getTileSize(getFieldBounds());
    }

    private int resolveHorizontalWallHeight(int tileSize) {
        if (horizontalWallTile == null) {
            return Math.max(10, (int) Math.round(tileSize * 0.72));
        }

        int sourceWidth = horizontalWallTile.getWidth(null);
        int sourceHeight = horizontalWallTile.getHeight(null);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return Math.max(10, (int) Math.round(tileSize * 0.72));
        }

        int naturalWallHeight = (int) Math.round(tileSize * (sourceHeight / (double) sourceWidth));
        int targetWallHeight = (int) Math.round(tileSize * 0.72);
        return Math.max(10, Math.min(tileSize, Math.max(naturalWallHeight, targetWallHeight)));
    }

    private int resolveVerticalWallWidth(int tileSize) {
        if (verticalWallTile == null) {
            return Math.max(8, (int) Math.round(tileSize * 0.42));
        }

        int sourceWidth = verticalWallTile.getWidth(null);
        int sourceHeight = verticalWallTile.getHeight(null);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return Math.max(8, (int) Math.round(tileSize * 0.42));
        }

        int naturalWallWidth = (int) Math.round(tileSize * (sourceWidth / (double) sourceHeight));
        int targetWallWidth = (int) Math.round(tileSize * 0.42);
        return Math.max(8, Math.min(tileSize, Math.max(naturalWallWidth, targetWallWidth)));
    }

    private int getTileSize(Rectangle fieldBounds) {
        return Math.max(1, fieldBounds.width / getColumnCount());
    }

    private Rectangle getPreferredFieldBounds() {
        Dimension preferredSize = getPreferredSize();
        if (preferredSize == null) {
            return new Rectangle();
        }

        int tileSize = (int) Math.ceil(Math.max(
                (double) preferredSize.width / getColumnCount(),
                (double) preferredSize.height / getRowCount()
        ));
        int gridWidth = tileSize * getColumnCount();
        int gridHeight = tileSize * getRowCount();
        int startX = (preferredSize.width - gridWidth) / 2;
        int startY = (preferredSize.height - gridHeight) / 2;
        return new Rectangle(startX, startY, gridWidth, gridHeight);
    }

    private void drawScaledTile(Graphics2D g2, Image tile, Rectangle cellBounds) {
        if (tile == null || cellBounds == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(tile, cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height, this);
    }

    /**
     * Les cases touchées par la prochaine onde létale deviennent progressivement lisibles
     * pendant la fenêtre d'alerte, avec un clignotement franc sur les 10 dernières secondes.
     */
    private void drawShrineWarningCells(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        if (shrineHazardState == null || !shrineHazardState.isWarningPhase()) {
            return;
        }

        boolean blinkVisible = shrineHazardState.isWarningBlinkVisible();
        Color fillColor = blinkVisible ? SHRINE_WARNING_FILL_ON : SHRINE_WARNING_FILL_OFF;
        Color borderColor = blinkVisible ? SHRINE_WARNING_BORDER_ON : SHRINE_WARNING_BORDER_OFF;

        for (Point dangerCell : grotteMap.getShrineDangerCells()) {
            Rectangle cellBounds = buildCellBounds(fieldBounds, tileSize, dangerCell.x, dangerCell.y);
            g2.setColor(fillColor);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);

            g2.setColor(borderColor);
            g2.drawRect(cellBounds.x, cellBounds.y, cellBounds.width - 1, cellBounds.height - 1);
        }
    }

    /**
     * Barre rouge placée directement au niveau de la statue pour annoncer la prochaine onde.
     * On réutilise le painter HUD partagé afin de garder un rendu homogène.
     */
    private void drawShrineCountdownBar(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        if (shrineHazardState == null) {
            return;
        }

        Rectangle shrineRoomBounds = buildAreaBounds(fieldBounds, tileSize, grotteMap.getShrineRoomBounds());
        Rectangle daisBounds = buildAreaBounds(fieldBounds, tileSize, grotteMap.getShrineDaisBounds());
        if (shrineRoomBounds == null || daisBounds == null) {
            return;
        }

        Rectangle statueBounds = buildShrineStatueBounds(daisBounds, tileSize);
        if (statueBounds == null) {
            return;
        }

        FontMetrics titleMetrics = g2.getFontMetrics(shrineHazardTitleFont);
        FontMetrics timerMetrics = g2.getFontMetrics(shrineHazardTimerFont);
        int labelSpacing = 4;
        int labelBlockHeight = titleMetrics.getHeight() + timerMetrics.getHeight() + labelSpacing;
        int barWidth = Math.max(124, Math.min(shrineRoomBounds.width - 16, Math.max(statueBounds.width + tileSize, tileSize * 3)));
        int barHeight = Math.max(16, tileSize / 3);
        int barX = shrineRoomBounds.x + ((shrineRoomBounds.width - barWidth) / 2);
        int barY = Math.max(shrineRoomBounds.y + 12 + labelBlockHeight, statueBounds.y - barHeight - 12);
        int textTopY = barY - labelBlockHeight - 5;
        int titleBaselineY = textTopY + titleMetrics.getAscent();
        int timerBaselineY = textTopY + titleMetrics.getHeight() + labelSpacing + timerMetrics.getAscent();

        g2.setColor(new Color(0, 0, 0, 85));
        g2.fillRoundRect(barX + 2, barY + 2, barWidth, barHeight, barHeight + 4, barHeight + 4);

        HudProgressBarPainter.paint(
                g2,
                barX,
                barY,
                barWidth,
                barHeight,
                shrineHazardState.getRemainingRatio(),
                SHRINE_BAR_FRAME,
                SHRINE_BAR_BACKGROUND,
                SHRINE_BAR_FILL,
                SHRINE_BAR_HIGHLIGHT
        );

        String title = "Onde létale";
        String remainingText = shrineHazardState.getRemainingSeconds() + "s";
        drawShadowedCenteredText(g2, title, barX, titleBaselineY, barWidth, shrineHazardTitleFont);
        drawShadowedCenteredText(g2, remainingText, barX, timerBaselineY, barWidth, shrineHazardTimerFont);
    }

    private void drawShadowedCenteredText(Graphics2D g2, String text, int x, int baselineY, int width, Font font) {
        if (text == null || text.isEmpty() || font == null) {
            return;
        }

        g2.setFont(font);
        FontMetrics metrics = g2.getFontMetrics(font);
        int textX = x + ((width - metrics.stringWidth(text)) / 2);
        g2.setColor(SHRINE_BAR_LABEL_SHADOW);
        g2.drawString(text, textX + 1, baselineY + 1);
        g2.setColor(SHRINE_BAR_LABEL);
        g2.drawString(text, textX, baselineY);
    }

    /**
     * Les zones de soin restent lisibles sans agresser l'écran :
     * 4 cases légèrement teintées + des petits "+" verts fixes.
     */
    private void drawHealingZones(Graphics2D g2, Rectangle fieldBounds, int tileSize) {
        if (grotteMap == null) {
            return;
        }

        int plusThickness = Math.max(2, tileSize / 8);
        int plusLength = Math.max(6, tileSize / 3);

        for (Point healingCell : grotteMap.getHealingCells()) {
            Rectangle cellBounds = buildCellBounds(fieldBounds, tileSize, healingCell.x, healingCell.y);
            g2.setColor(HEAL_ZONE_FILL);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
            g2.setColor(HEAL_ZONE_BORDER);
            g2.drawRect(cellBounds.x, cellBounds.y, cellBounds.width - 1, cellBounds.height - 1);

            int centerX = cellBounds.x + (cellBounds.width / 2);
            int centerY = (int) Math.round(cellBounds.y + (cellBounds.height / 2.0) - (tileSize * 0.06));

            g2.setColor(HEAL_PLUS_SHADOW);
            g2.fillRect(centerX - (plusThickness / 2) + 1, centerY - (plusLength / 2) + 1, plusThickness, plusLength);
            g2.fillRect(centerX - (plusLength / 2) + 1, centerY - (plusThickness / 2) + 1, plusLength, plusThickness);
            g2.setColor(HEAL_PLUS_COLOR);
            g2.fillRect(centerX - (plusThickness / 2), centerY - (plusLength / 2), plusThickness, plusLength);
            g2.fillRect(centerX - (plusLength / 2), centerY - (plusThickness / 2), plusLength, plusThickness);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ensureStaticSceneCache();

        Graphics2D g2 = (Graphics2D) g.create();
        if (staticSceneCache != null) {
            g2.drawImage(staticSceneCache, 0, 0, this);
        }

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);

        drawHealingZones(g2, fieldBounds, tileSize);
        drawShrineWarningCells(g2, fieldBounds, tileSize);
        drawShrineCountdownBar(g2, fieldBounds, tileSize);

        g2.dispose();
    }
}
