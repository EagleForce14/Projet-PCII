package view.grotte;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import view.ImageLoader;

/**
 * Génère toutes les images temporaires de la grotte directement en code.
 *
 * L'objectif est double :
 * - garder un rendu pixel art détaillé malgré l'absence d'assets finaux,
 * - produire uniquement des visuels statiques, donc faciles à mettre en cache.
 */
public final class GrotteTileFactory {
    // Taille de base utilisée pour construire les visuels procéduraux.
    private static final int BASE_TILE_SIZE = 48;
    // Cache partagé des variantes de roche.
    private static Image[] rockTilesCache;
    // Cache partagé des variantes de sol de salle.
    private static Image[] roomFloorTilesCache;
    // Cache partagé des tuiles de chemin.
    private static Image[] pathTilesCache;
    // Cache partagé du visuel de mur horizontal.
    private static Image horizontalWallTileCache;
    // Cache partagé du visuel de mur vertical.
    private static Image verticalWallTileCache;
    // Cache partagé du visuel de statue du sanctuaire.
    private static Image shrineStatueImageCache;

    // Couleurs de base utilisées pour générer la roche.
    private static final Color ROCK_BACKGROUND = new Color(28, 23, 35);
    private static final Color ROCK_OUTLINE = new Color(18, 14, 22);
    private static final Color ROCK_DARK = new Color(58, 48, 66);
    private static final Color ROCK_MID = new Color(85, 72, 93);
    private static final Color ROCK_LIGHT = new Color(116, 98, 122);
    private static final Color ROCK_HIGHLIGHT = new Color(143, 123, 148);

    // Couleurs de base utilisées pour générer le sol des salles.
    private static final Color ROOM_FLOOR_BACKGROUND = new Color(41, 34, 41);
    private static final Color ROOM_FLOOR_GROUT = new Color(60, 48, 51);
    private static final Color ROOM_FLOOR_DARK = new Color(88, 69, 67);
    private static final Color ROOM_FLOOR_MID = new Color(123, 94, 82);
    private static final Color ROOM_FLOOR_LIGHT = new Color(156, 121, 96);

    // Couleurs de base utilisées pour générer les chemins.
    private static final Color PATH_BACKGROUND = new Color(77, 50, 33);
    private static final Color PATH_GROUT = new Color(92, 56, 31);
    private static final Color PATH_DARK = new Color(150, 99, 50);
    private static final Color PATH_MID = new Color(186, 124, 63);
    private static final Color PATH_LIGHT = new Color(222, 159, 86);
    private static final Color PATH_HIGHLIGHT = new Color(244, 190, 118);

    // Couleurs utilitaires des torches et flammes décoratives.
    private static final Color TORCH_METAL = new Color(87, 64, 45);
    private static final Color TORCH_HANDLE = new Color(63, 42, 29);
    private static final Color FLAME_CORE = new Color(255, 235, 146);
    private static final Color FLAME_MID = new Color(255, 173, 61);
    private static final Color FLAME_OUTER = new Color(234, 92, 33);
    private static final Color FLAME_GLOW = new Color(255, 183, 72, 78);

    // Couleurs utilisées pour l'eau et les cristaux.
    private static final Color WATER_DEEP = new Color(38, 92, 124);
    private static final Color WATER_MID = new Color(61, 138, 177);
    private static final Color WATER_LIGHT = new Color(144, 228, 244);
    private static final Color CRYSTAL_LIGHT = new Color(142, 242, 255);
    private static final Color CRYSTAL_MID = new Color(76, 197, 244);
    private static final Color CRYSTAL_DARK = new Color(41, 120, 177);

    // Couleurs utilisées pour la statue et les os du sanctuaire.
    private static final Color STATUE_DARK = new Color(83, 72, 66);
    private static final Color STATUE_MID = new Color(119, 102, 90);
    private static final Color STATUE_LIGHT = new Color(155, 137, 115);
    private static final Color BONE = new Color(213, 198, 170);
    private static final Color SKULL_SHADOW = new Color(136, 108, 86);

    // Couleurs utilisées pour la lave et les gemmes.
    private static final Color LAVA_DARK = new Color(130, 29, 18);
    private static final Color LAVA_MID = new Color(214, 71, 24);
    private static final Color LAVA_LIGHT = new Color(255, 149, 47);
    private static final Color GEM_RED = new Color(227, 57, 46);
    private static final Color GEM_HIGHLIGHT = new Color(255, 178, 145);

    // Couleurs utilisées pour les éléments d'atelier, de bois et de stockage.
    private static final Color POT_DARK = new Color(51, 56, 48);
    private static final Color POT_MID = new Color(82, 95, 80);
    private static final Color POT_LIGHT = new Color(116, 134, 114);
    private static final Color BREW = new Color(158, 232, 112);
    private static final Color WOOD_DARK = new Color(86, 54, 31);
    private static final Color WOOD_MID = new Color(122, 78, 44);
    private static final Color WOOD_LIGHT = new Color(164, 111, 67);
    private static final Color WEB = new Color(202, 202, 202, 160);
    private static final Color COIN = new Color(238, 193, 68);

    /**
     * Empêche de créer un objet de cette classe.
     * Toutes ses méthodes servent seulement à fabriquer des images.
     */
    private GrotteTileFactory() {
    }

    /**
     * Renvoie les tuiles de roche de base utilisées dans la grotte.
     */
    public static synchronized Image[] createRockTiles() {
        if (rockTilesCache != null) {
            return rockTilesCache;
        }

        rockTilesCache = new Image[] {
                createRockTile(0),
                createRockTile(1),
                createRockTile(2)
        };
        return rockTilesCache;
    }

    /**
     * Renvoie les tuiles de sol utilisées dans les salles de la grotte.
     */
    public static synchronized Image[] createRoomFloorTiles() {
        if (roomFloorTilesCache != null) {
            return roomFloorTilesCache;
        }

        roomFloorTilesCache = new Image[] {
                createRoomFloorTile(0),
                createRoomFloorTile(1),
                createRoomFloorTile(2)
        };
        return roomFloorTilesCache;
    }

    /**
     * Renvoie la tuile de chemin en pierre.
     * Si l'image dédiée n'existe pas, la méthode utilise une dalle simple de secours.
     */
    public static synchronized Image[] createPathTiles() {
        if (pathTilesCache != null) {
            return pathTilesCache;
        }

        Image stonePathTile = ImageLoader.load("/assets/stone_grotte.png");
        if (stonePathTile != null) {
            pathTilesCache = new Image[] { stonePathTile };
        } else {
        // Si l'asset manque, on retombe sur une dalle neutre de salle
        // plutôt que sur l'ancienne génération procédurale du chemin.
            pathTilesCache = new Image[] { createRoomFloorTile(0) };
        }

        return pathTilesCache;
    }

    /**
     * Renvoie l'image utilisée pour les murs horizontaux.
     */
    public static synchronized Image createHorizontalWallTile() {
        if (horizontalWallTileCache != null) {
            return horizontalWallTileCache;
        }

        Image wallTile = ImageLoader.load("/assets/grotte_mur_droit.png");
        if (wallTile != null) {
            horizontalWallTileCache = wallTile;
            return horizontalWallTileCache;
        }

        horizontalWallTileCache = ImageLoader.load("/assets/grotte_mur_vertical.png");
        return horizontalWallTileCache;
    }

    /**
     * Renvoie l'image utilisée pour les murs verticaux.
     */
    public static synchronized Image createVerticalWallTile() {
        if (verticalWallTileCache != null) {
            return verticalWallTileCache;
        }

        Image wallTile = ImageLoader.load("/assets/grotte_mur_vertical.png");
        if (wallTile != null) {
            verticalWallTileCache = wallTile;
            return verticalWallTileCache;
        }

        verticalWallTileCache = ImageLoader.load("/assets/grotte_mur_droit.png");
        return verticalWallTileCache;
    }

    /**
     * Renvoie la statue placée dans la salle du sanctuaire.
     */
    public static synchronized Image createShrineStatueImage() {
        if (shrineStatueImageCache != null) {
            return shrineStatueImageCache;
        }

        shrineStatueImageCache = ImageLoader.load("/assets/statue_grotte.png");
        return shrineStatueImageCache;
    }

    /**
     * Déclenche le chargement anticipé des principaux visuels statiques de la grotte.
     */
    public static void warmupSharedTiles() {
        createRockTiles();
        createRoomFloorTiles();
        createPathTiles();
        createHorizontalWallTile();
        createVerticalWallTile();
        createShrineStatueImage();
    }

    /**
     * Dessine le décor supplémentaire de la salle d'eau :
     * cascade, bassin, cristaux et plantes.
     */
    public static BufferedImage createWaterRoomOverlay(int width, int height) {
        BufferedImage image = createTransparentCanvas(width, height);
        Graphics2D g2 = createPixelGraphics(image);

        int waterfallX = scale(width, 0.07);
        int waterfallWidth = Math.max(12, scale(width, 0.04));
        int waterfallTop = scale(height, 0.04);
        int waterfallBottom = scale(height, 0.62);
        fillRect(g2, waterfallX, waterfallTop, waterfallWidth, waterfallBottom - waterfallTop, WATER_LIGHT);
        fillRect(g2, waterfallX + 2, waterfallTop, Math.max(4, waterfallWidth / 3), waterfallBottom - waterfallTop, WATER_MID);

        drawTorch(g2, scale(width, 0.78), scale(height, 0.19), Math.max(14, scale(width, 0.05)));

        drawPool(g2,
                scale(width, 0.10),
                scale(height, 0.34),
                scale(width, 0.66),
                scale(height, 0.46));

        drawCrystalCluster(g2, scale(width, 0.13), scale(height, 0.52), Math.max(24, scale(width, 0.12)));
        drawSmallPlant(g2, scale(width, 0.20), scale(height, 0.78), scale(width, 0.08), new Color(167, 89, 55));
        drawSmallPlant(g2, scale(width, 0.77), scale(height, 0.70), scale(width, 0.06), new Color(167, 89, 55));
        g2.dispose();
        return image;
    }

    /**
     * Dessine le décor supplémentaire de la salle du sanctuaire.
     */
    public static BufferedImage createShrineRoomOverlay(int width, int height) {
        BufferedImage image = createTransparentCanvas(width, height);
        Graphics2D g2 = createPixelGraphics(image);

        drawTorch(g2, scale(width, 0.23), scale(height, 0.20), Math.max(16, scale(width, 0.05)));
        drawTorch(g2, scale(width, 0.76), scale(height, 0.20), Math.max(16, scale(width, 0.05)));
        drawStatue(g2,
                scale(width, 0.33),
                scale(height, 0.17),
                scale(width, 0.34),
                scale(height, 0.58));
        drawBones(g2, scale(width, 0.14), scale(height, 0.58), Math.max(24, scale(width, 0.12)));
        drawBones(g2, scale(width, 0.71), scale(height, 0.56), Math.max(24, scale(width, 0.12)));
        g2.dispose();
        return image;
    }

    /**
     * Dessine le décor supplémentaire de la salle de lave.
     */
    public static BufferedImage createLavaRoomOverlay(int width, int height) {
        BufferedImage image = createTransparentCanvas(width, height);
        Graphics2D g2 = createPixelGraphics(image);

        drawTorch(g2, scale(width, 0.20), scale(height, 0.24), Math.max(14, scale(width, 0.05)));
        drawLavaPool(g2,
                scale(width, 0.30),
                scale(height, 0.10),
                scale(width, 0.58),
                scale(height, 0.42));
        drawPedestal(g2,
                scale(width, 0.48),
                scale(height, 0.22),
                scale(width, 0.18),
                scale(height, 0.18));
        drawStoneBridge(g2,
                scale(width, 0.23),
                scale(height, 0.61),
                scale(width, 0.32),
                scale(height, 0.14));
        g2.dispose();
        return image;
    }

    /**
     * Dessine le décor supplémentaire de la salle d'atelier.
     */
    public static BufferedImage createWorkshopRoomOverlay(int width, int height) {
        BufferedImage image = createTransparentCanvas(width, height);
        Graphics2D g2 = createPixelGraphics(image);

        drawTorch(g2, scale(width, 0.78), scale(height, 0.24), Math.max(14, scale(width, 0.05)));
        drawShelf(g2, scale(width, 0.36), scale(height, 0.15), scale(width, 0.22), scale(height, 0.18));
        drawWorkbench(g2, scale(width, 0.10), scale(height, 0.26), scale(width, 0.22), scale(height, 0.12));
        drawCauldron(g2, scale(width, 0.31), scale(height, 0.42), scale(width, 0.28), scale(height, 0.28));
        drawSmallPlant(g2, scale(width, 0.68), scale(height, 0.74), scale(width, 0.08), new Color(165, 87, 57));
        g2.dispose();
        return image;
    }

    /**
     * Dessine le décor supplémentaire de la salle de stockage.
     */
    public static BufferedImage createStorageRoomOverlay(int width, int height) {
        BufferedImage image = createTransparentCanvas(width, height);
        Graphics2D g2 = createPixelGraphics(image);

        drawTorch(g2, scale(width, 0.19), scale(height, 0.24), Math.max(14, scale(width, 0.05)));
        drawTorch(g2, scale(width, 0.80), scale(height, 0.24), Math.max(14, scale(width, 0.05)));
        drawDoor(g2, scale(width, 0.43), scale(height, 0.18), scale(width, 0.14), scale(height, 0.28));
        drawCrate(g2, scale(width, 0.28), scale(height, 0.53), scale(width, 0.16), scale(height, 0.12));
        drawCrate(g2, scale(width, 0.62), scale(height, 0.54), scale(width, 0.18), scale(height, 0.14));
        drawCoinPile(g2, scale(width, 0.71), scale(height, 0.62), Math.max(18, scale(width, 0.11)));
        drawBones(g2, scale(width, 0.50), scale(height, 0.67), Math.max(22, scale(width, 0.10)));
        drawCobweb(g2, scale(width, 0.09), scale(height, 0.24), Math.max(24, scale(width, 0.10)));
        drawCobweb(g2, scale(width, 0.84), scale(height, 0.22), Math.max(24, scale(width, 0.10)));
        g2.dispose();
        return image;
    }

    /**
     * Fabrique une tuile de roche en assemblant plusieurs petits blocs.
     */
    private static BufferedImage createRockTile(int variant) {
        BufferedImage image = createTransparentCanvas(BASE_TILE_SIZE, BASE_TILE_SIZE);
        fill(image, ROCK_BACKGROUND);

        paintBoulder(image, 2, 2, 13, 11, variant);
        paintBoulder(image, 16, 1, 12, 10, variant + 1);
        paintBoulder(image, 31, 3, 13, 11, variant + 2);
        paintBoulder(image, 5, 15, 12, 10, variant + 3);
        paintBoulder(image, 19, 15, 11, 10, variant + 4);
        paintBoulder(image, 34, 16, 10, 9, variant + 5);
        paintBoulder(image, 1, 29, 11, 11, variant + 6);
        paintBoulder(image, 14, 30, 14, 12, variant + 7);
        paintBoulder(image, 30, 29, 13, 12, variant + 8);
        drawSpeckles(image, variant, ROCK_HIGHLIGHT, ROCK_OUTLINE);
        shadeLowerHalf(image, new Color(11, 9, 16, 78));
        return image;
    }

    /**
     * Fabrique une tuile de sol de salle avec plusieurs briques et fissures légères.
     */
    private static BufferedImage createRoomFloorTile(int variant) {
        BufferedImage image = createTransparentCanvas(BASE_TILE_SIZE, BASE_TILE_SIZE);
        fill(image, ROOM_FLOOR_BACKGROUND);

        paintBrick(image, 2, 3, 13, 11, ROOM_FLOOR_DARK, ROOM_FLOOR_LIGHT);
        paintBrick(image, 16, 2, 14, 10, ROOM_FLOOR_MID, ROOM_FLOOR_LIGHT);
        paintBrick(image, 31, 3, 14, 11, ROOM_FLOOR_DARK, ROOM_FLOOR_LIGHT);
        paintBrick(image, 1, 15, 11, 12, ROOM_FLOOR_MID, ROOM_FLOOR_LIGHT);
        paintBrick(image, 13, 16, 15, 11, ROOM_FLOOR_DARK, ROOM_FLOOR_LIGHT);
        paintBrick(image, 29, 16, 16, 12, ROOM_FLOOR_MID, ROOM_FLOOR_LIGHT);
        paintBrick(image, 4, 29, 13, 13, ROOM_FLOOR_DARK, ROOM_FLOOR_LIGHT);
        paintBrick(image, 18, 30, 12, 12, ROOM_FLOOR_MID, ROOM_FLOOR_LIGHT);
        paintBrick(image, 31, 30, 13, 13, ROOM_FLOOR_DARK, ROOM_FLOOR_LIGHT);
        drawJointShadows(image, ROOM_FLOOR_GROUT);
        drawFloorCrack(image, 10 + variant, 22, 6);
        return image;
    }

    /**
     * Dessine un bloc de roche avec quelques ombres et lumières simples.
     */
    private static void paintBoulder(BufferedImage image, int x, int y, int width, int height, int variant) {
        Color fill = variant % 2 == 0 ? ROCK_DARK : ROCK_MID;
        Color light = variant % 3 == 0 ? ROCK_HIGHLIGHT : ROCK_LIGHT;

        fillRect(image, x, y, width, height, ROCK_OUTLINE);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2), fill);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 4), 1, light);
        fillRect(image, x + 1, y + 1, 1, Math.max(1, height - 4), light);
        fillRect(image, x + 2, y + height - 2, Math.max(1, width - 4), 1, darker(fill, 20));
        fillRect(image, x + width - 2, y + 2, 1, Math.max(1, height - 4), darker(fill, 16));
        putPixel(image, x + Math.max(2, width / 3), y + Math.max(2, height / 3), light);
        putPixel(image, x + Math.max(2, (width * 2) / 3), y + Math.max(2, (height * 2) / 3), darker(fill, 10));
    }

    /**
     * Dessine une brique rectangulaire avec un contour et des reflets.
     */
    private static void paintBrick(BufferedImage image, int x, int y, int width, int height, Color fill, Color light) {
        fillRect(image, x, y, width, height, PATH_GROUT);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2), fill);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 3), 1, light);
        fillRect(image, x + 1, y + 1, 1, Math.max(1, height - 3), light);
        fillRect(image, x + 2, y + height - 2, Math.max(1, width - 4), 1, darker(fill, 24));
        fillRect(image, x + width - 2, y + 2, 1, Math.max(1, height - 4), darker(fill, 18));
    }

    /**
     * Ajoute de petits points de détail pour éviter que la roche paraisse trop uniforme.
     */
    private static void drawSpeckles(BufferedImage image, int variant, Color bright, Color dark) {
        for (int i = 0; i < 12; i++) {
            int x = Math.floorMod((i * 7) + (variant * 5), image.getWidth());
            int y = Math.floorMod((i * 11) + (variant * 3), image.getHeight());
            putPixel(image, x, y, i % 2 == 0 ? bright : dark);
        }
    }

    /**
     * Trace les lignes de joint entre les grandes briques du sol.
     */
    private static void drawJointShadows(BufferedImage image, Color groutColor) {
        fillRect(image, 0, 14, image.getWidth(), 1, groutColor);
        fillRect(image, 0, 29, image.getWidth(), 1, groutColor);
        fillRect(image, 15, 0, 1, image.getHeight(), groutColor);
        fillRect(image, 30, 0, 1, image.getHeight(), groutColor);
    }

    /**
     * Dessine une fissure simple dans une dalle du sol.
     */
    private static void drawFloorCrack(BufferedImage image, int startX, int startY, int length) {
        for (int i = 0; i < length; i++) {
            int x = Math.min(image.getWidth() - 2, startX + i);
            int y = Math.min(image.getHeight() - 2, startY + ((i + 1) / 2));
            putPixel(image, x, y, ROCK_OUTLINE);
            if (i % 2 == 0) {
                putPixel(image, x, Math.min(image.getHeight() - 1, y + 1), darker(PATH_BACKGROUND, 14));
            }
        }
    }

    /**
     * Assombrit la moitié basse d'une image pour lui donner un peu de profondeur.
     */
    private static void shadeLowerHalf(BufferedImage image, Color shadow) {
        for (int y = image.getHeight() / 2; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, blend(new Color(image.getRGB(x, y), true), shadow, 0.22).getRGB());
            }
        }
    }

    /**
     * Dessine un bassin d'eau avec quelques reflets et pierres de passage.
     */
    private static void drawPool(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(WATER_LIGHT.getRed(), WATER_LIGHT.getGreen(), WATER_LIGHT.getBlue(), 38));
        g2.fillOval(x - width / 8, y - height / 8, width + width / 4, height + height / 4);
        g2.setColor(WATER_DEEP);
        g2.fillRoundRect(x, y, width, height, Math.max(18, width / 6), Math.max(18, height / 5));
        g2.setColor(WATER_MID);
        g2.fillRoundRect(x + width / 12, y + height / 10, width - width / 6, height - height / 5, Math.max(14, width / 7), Math.max(14, height / 6));
        g2.setColor(WATER_LIGHT);
        g2.fillRect(x + width / 6, y + height / 4, Math.max(6, width / 5), Math.max(3, height / 20));
        g2.fillRect(x + width / 2, y + height / 2, Math.max(6, width / 4), Math.max(3, height / 24));
        drawSteppingStone(g2, x + width / 3, y + (height * 2 / 3), Math.max(16, width / 9), Math.max(12, height / 10));
        drawSteppingStone(g2, x + width / 2, y + (height / 2), Math.max(14, width / 10), Math.max(10, height / 11));
        drawSteppingStone(g2, x + (width * 2 / 3), y + (height * 3 / 4), Math.max(15, width / 10), Math.max(11, height / 11));
    }

    /**
     * Dessine une petite pierre sur laquelle on pourrait imaginer marcher au-dessus de l'eau.
     */
    private static void drawSteppingStone(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(96, 91, 97));
        g2.fillRoundRect(x, y, width, height, Math.max(6, width / 3), Math.max(6, height / 2));
        g2.setColor(new Color(140, 134, 138));
        g2.fillRect(x + 2, y + 2, Math.max(2, width - 5), 2);
    }

    /**
     * Dessine plusieurs cristaux rapprochés pour former un petit groupe.
     */
    private static void drawCrystalCluster(Graphics2D g2, int x, int y, int size) {
        drawCrystal(g2, x, y + size / 4, size / 3, size / 2);
        drawCrystal(g2, x + size / 4, y, size / 2, size);
        drawCrystal(g2, x + size / 2, y + size / 5, size / 3, size / 2);
    }

    /**
     * Dessine un cristal unique.
     */
    private static void drawCrystal(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(CRYSTAL_DARK);
        g2.fillRect(x, y + height / 3, width, Math.max(4, height - height / 3));
        g2.setColor(CRYSTAL_MID);
        g2.fillRect(x + 1, y + height / 3, Math.max(2, width - 2), Math.max(4, height - height / 3 - 1));
        g2.setColor(CRYSTAL_LIGHT);
        g2.fillRect(x + width / 3, y, Math.max(2, width / 3), Math.max(4, height / 3));
    }

    /**
     * Dessine la grande statue centrale de la salle du sanctuaire.
     */
    private static void drawStatue(Graphics2D g2, int x, int y, int width, int height) {
        int baseHeight = Math.max(14, height / 5);
        g2.setColor(PATH_DARK);
        g2.fillRect(x + width / 8, y + height - baseHeight, width - width / 4, baseHeight);
        g2.setColor(PATH_LIGHT);
        g2.fillRect(x + width / 6, y + height - baseHeight + 3, width - width / 3, Math.max(4, baseHeight / 3));

        int bodyX = x + width / 3;
        int bodyY = y + height / 5;
        int bodyWidth = width / 3;
        int bodyHeight = height / 2;
        g2.setColor(STATUE_DARK);
        g2.fillRect(bodyX, bodyY, bodyWidth, bodyHeight);
        g2.setColor(STATUE_MID);
        g2.fillRect(bodyX + 2, bodyY + 2, Math.max(4, bodyWidth - 4), Math.max(8, bodyHeight - 4));
        g2.setColor(STATUE_LIGHT);
        g2.fillOval(bodyX + bodyWidth / 5, y + height / 12, bodyWidth * 3 / 5, height / 5);
        g2.setColor(STATUE_DARK);
        g2.fillRect(x + width / 6, bodyY + height / 10, width / 8, height / 2);
        g2.fillRect(x + width * 11 / 16, bodyY + height / 10, width / 8, height / 2);
    }

    /**
     * Dessine quelques os décoratifs au sol.
     */
    private static void drawBones(Graphics2D g2, int x, int y, int size) {
        g2.setColor(BONE);
        g2.fillRect(x, y, size / 2, Math.max(4, size / 8));
        g2.fillRect(x + size / 4, y + size / 4, size / 2, Math.max(4, size / 8));
        g2.fillOval(x - 2, y - 2, Math.max(5, size / 5), Math.max(5, size / 5));
        g2.fillOval(x + size / 3, y - 2, Math.max(5, size / 5), Math.max(5, size / 5));
        g2.fillOval(x + size / 5, y + size / 4 - 2, Math.max(5, size / 5), Math.max(5, size / 5));
        g2.fillOval(x + size / 2, y + size / 4 - 2, Math.max(5, size / 5), Math.max(5, size / 5));
        g2.setColor(SKULL_SHADOW);
        g2.fillRect(x + size / 4, y + size / 2, Math.max(6, size / 4), Math.max(4, size / 9));
    }

    /**
     * Dessine une zone de lave avec des reflets plus clairs au milieu.
     */
    private static void drawLavaPool(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(LAVA_LIGHT.getRed(), LAVA_LIGHT.getGreen(), LAVA_LIGHT.getBlue(), 52));
        g2.fillOval(x - width / 8, y - height / 8, width + width / 4, height + height / 3);
        g2.setColor(LAVA_DARK);
        g2.fillRoundRect(x, y, width, height, Math.max(20, width / 4), Math.max(20, height / 4));
        g2.setColor(LAVA_MID);
        g2.fillRect(x + width / 8, y + height / 5, width - width / 4, Math.max(6, height / 4));
        g2.fillRect(x + width / 5, y + height / 2, width - width / 3, Math.max(6, height / 5));
        g2.setColor(LAVA_LIGHT);
        g2.fillRect(x + width / 4, y + height / 4, width / 5, Math.max(4, height / 10));
        g2.fillRect(x + width / 2, y + height / 2, width / 4, Math.max(4, height / 9));
    }

    /**
     * Dessine un piédestal avec une gemme au sommet.
     */
    private static void drawPedestal(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(PATH_DARK);
        g2.fillRect(x, y + height / 3, width, height - height / 3);
        g2.setColor(PATH_LIGHT);
        g2.fillRect(x + 2, y + height / 3 + 2, Math.max(4, width - 4), Math.max(4, height / 3));
        int gemSize = Math.max(10, Math.min(width, height) / 3);
        int gemX = x + (width - gemSize) / 2;
        int gemY = y + height / 6;
        g2.setColor(new Color(GEM_RED.getRed(), GEM_RED.getGreen(), GEM_RED.getBlue(), 70));
        g2.fillOval(gemX - gemSize / 2, gemY - gemSize / 2, gemSize * 2, gemSize * 2);
        g2.setColor(GEM_RED);
        g2.fillRect(gemX, gemY, gemSize, gemSize);
        g2.setColor(GEM_HIGHLIGHT);
        g2.fillRect(gemX + 2, gemY + 2, Math.max(2, gemSize / 3), Math.max(2, gemSize / 3));
    }

    /**
     * Dessine un petit pont de pierre dans la salle de lave.
     */
    private static void drawStoneBridge(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(PATH_DARK);
        g2.fillRect(x, y, width, height);
        g2.setColor(PATH_LIGHT);
        for (int i = 0; i < 4; i++) {
            int stepX = x + (i * width / 4);
            g2.fillRect(stepX + 2, y + 2, Math.max(6, width / 5), Math.max(4, height - 4));
        }
    }

    /**
     * Dessine une étagère avec quelques objets colorés.
     */
    private static void drawShelf(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, width, height);
        g2.setColor(WOOD_MID);
        g2.fillRect(x + 2, y + 2, Math.max(4, width - 4), Math.max(4, height - 4));
        g2.setColor(WOOD_LIGHT);
        g2.fillRect(x + 3, y + height / 3, Math.max(4, width - 6), 2);
        g2.fillRect(x + 3, y + (height * 2 / 3), Math.max(4, width - 6), 2);
        g2.setColor(new Color(126, 180, 224));
        g2.fillRect(x + width / 6, y + 5, Math.max(4, width / 8), Math.max(6, height / 5));
        g2.setColor(new Color(204, 146, 78));
        g2.fillRect(x + width / 2, y + 6, Math.max(4, width / 7), Math.max(6, height / 5));
        g2.setColor(new Color(174, 104, 176));
        g2.fillRect(x + width * 2 / 3, y + height / 2, Math.max(4, width / 8), Math.max(6, height / 5));
    }

    /**
     * Dessine un établi en bois.
     */
    private static void drawWorkbench(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, width, height / 3);
        g2.fillRect(x + 2, y + height / 3, Math.max(4, width / 8), Math.max(8, height * 2 / 3));
        g2.fillRect(x + width - Math.max(6, width / 8) - 2, y + height / 3, Math.max(4, width / 8), Math.max(8, height * 2 / 3));
        g2.setColor(WOOD_LIGHT);
        g2.fillRect(x + 2, y + 2, Math.max(4, width - 4), Math.max(4, height / 6));
    }

    /**
     * Dessine un chaudron avec son contenu et une petite flamme.
     */
    private static void drawCauldron(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(POT_DARK);
        g2.fillOval(x, y + height / 5, width, height * 3 / 5);
        g2.setColor(POT_MID);
        g2.fillOval(x + 3, y + height / 5 + 3, Math.max(8, width - 6), Math.max(10, height * 3 / 5 - 6));
        g2.setColor(BREW);
        g2.fillOval(x + width / 6, y + height / 4, width * 2 / 3, height / 5);
        g2.setColor(FLAME_OUTER);
        g2.fillRect(x + width / 3, y + height * 4 / 5, width / 5, Math.max(4, height / 8));
        g2.setColor(FLAME_CORE);
        g2.fillRect(x + width / 3 + 2, y + height * 4 / 5 + 1, Math.max(3, width / 5 - 4), Math.max(2, height / 10));
    }

    /**
     * Dessine une porte fermée dans la salle de stockage.
     */
    private static void drawDoor(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(58, 41, 28));
        g2.fillRoundRect(x, y, width, height, Math.max(8, width / 2), Math.max(8, width / 2));
        g2.setColor(new Color(96, 66, 43));
        g2.fillRoundRect(x + 2, y + 2, Math.max(6, width - 4), Math.max(8, height - 4), Math.max(8, width / 2), Math.max(8, width / 2));
        g2.setColor(new Color(180, 152, 84));
        g2.fillRect(x + width * 3 / 4, y + height / 2, 3, 3);
    }

    /**
     * Dessine une caisse de stockage en bois.
     */
    private static void drawCrate(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(WOOD_DARK);
        g2.fillRect(x, y, width, height);
        g2.setColor(WOOD_MID);
        g2.fillRect(x + 2, y + 2, Math.max(4, width - 4), Math.max(4, height - 4));
        g2.setColor(WOOD_LIGHT);
        g2.fillRect(x + 2, y + height / 3, Math.max(4, width - 4), 2);
        g2.fillRect(x + width / 3, y + 2, 2, Math.max(4, height - 4));
        g2.fillRect(x + width * 2 / 3, y + 2, 2, Math.max(4, height - 4));
    }

    /**
     * Dessine un petit tas de pièces.
     */
    private static void drawCoinPile(Graphics2D g2, int x, int y, int size) {
        g2.setColor(new Color(COIN.getRed(), COIN.getGreen(), COIN.getBlue(), 65));
        g2.fillOval(x - size / 3, y - size / 4, size + size / 2, size);
        g2.setColor(COIN);
        for (int i = 0; i < 6; i++) {
            g2.fillOval(x + (i * size / 8), y + ((i % 2) * size / 10), Math.max(5, size / 4), Math.max(3, size / 6));
        }
    }

    /**
     * Dessine une toile d'araignée simple dans un coin.
     */
    private static void drawCobweb(Graphics2D g2, int x, int y, int size) {
        g2.setColor(WEB);
        g2.drawLine(x, y, x + size, y + size);
        g2.drawLine(x + size, y, x, y + size);
        g2.drawLine(x + size / 2, y, x + size / 2, y + size);
        g2.drawLine(x, y + size / 2, x + size, y + size / 2);
        g2.drawRect(x + size / 4, y + size / 4, size / 2, size / 2);
    }

    /**
     * Dessine une petite plante décorative.
     */
    private static void drawSmallPlant(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, Math.max(3, size / 4), Math.max(4, size / 3));
        g2.fillRect(x - Math.max(2, size / 6), y + size / 4, Math.max(3, size / 4), Math.max(4, size / 3));
        g2.fillRect(x + Math.max(2, size / 4), y + size / 6, Math.max(3, size / 4), Math.max(4, size / 3));
    }

    /**
     * Dessine une torche avec sa poignée, sa flamme et son halo lumineux.
     */
    private static void drawTorch(Graphics2D g2, int x, int y, int size) {
        g2.setColor(FLAME_GLOW);
        g2.fillOval(x - size, y - size, size * 3, size * 3);

        g2.setColor(TORCH_HANDLE);
        g2.fillRect(x + size / 3, y + size / 2, Math.max(3, size / 4), size);
        g2.setColor(TORCH_METAL);
        g2.fillRect(x + size / 4, y + size / 2 - 2, Math.max(6, size / 2), 4);

        g2.setColor(FLAME_OUTER);
        g2.fillRect(x + size / 4, y, size / 2, size / 2);
        g2.setColor(FLAME_MID);
        g2.fillRect(x + size / 3, y + 2, Math.max(4, size / 3), Math.max(4, size / 3));
        g2.setColor(FLAME_CORE);
        g2.fillRect(x + size / 2 - 2, y + 4, 4, Math.max(4, size / 4));
    }

    /**
     * Crée une image transparente de la taille demandée.
     */
    private static BufferedImage createTransparentCanvas(int width, int height) {
        return new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Prépare un contexte de dessin adapté au style pixel-art.
     */
    private static Graphics2D createPixelGraphics(BufferedImage image) {
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        return g2;
    }

    /**
     * Remplit toute une image avec une seule couleur.
     */
    private static void fill(BufferedImage image, Color color) {
        Graphics2D g2 = createPixelGraphics(image);
        g2.setColor(color);
        g2.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2.dispose();
    }

    /**
     * Remplit un rectangle directement dans une image pixel par pixel.
     */
    private static void fillRect(BufferedImage image, int x, int y, int width, int height, Color color) {
        int startX = Math.max(0, x);
        int startY = Math.max(0, y);
        int endX = Math.min(image.getWidth(), x + width);
        int endY = Math.min(image.getHeight(), y + height);
        for (int drawY = startY; drawY < endY; drawY++) {
            for (int drawX = startX; drawX < endX; drawX++) {
                image.setRGB(drawX, drawY, color.getRGB());
            }
        }
    }

    /**
     * Remplit un rectangle via l'objet de dessin `Graphics2D`.
     */
    private static void fillRect(Graphics2D g2, int x, int y, int width, int height, Color color) {
        g2.setColor(color);
        g2.fillRect(x, y, Math.max(1, width), Math.max(1, height));
    }

    /**
     * Colore un pixel unique si sa position est bien à l'intérieur de l'image.
     */
    private static void putPixel(BufferedImage image, int x, int y, Color color) {
        if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
            return;
        }

        image.setRGB(x, y, color.getRGB());
    }

    /**
     * Renvoie une version plus sombre d'une couleur donnée.
     */
    private static Color darker(Color color, int delta) {
        return new Color(
                Math.max(0, color.getRed() - delta),
                Math.max(0, color.getGreen() - delta),
                Math.max(0, color.getBlue() - delta),
                color.getAlpha()
        );
    }

    /**
     * Mélange deux couleurs selon le poids demandé pour la seconde couleur.
     */
    private static Color blend(Color first, Color second, double secondWeight) {
        double firstWeight = 1.0 - secondWeight;
        return new Color(
                clamp((int) Math.round((first.getRed() * firstWeight) + (second.getRed() * secondWeight))),
                clamp((int) Math.round((first.getGreen() * firstWeight) + (second.getGreen() * secondWeight))),
                clamp((int) Math.round((first.getBlue() * firstWeight) + (second.getBlue() * secondWeight))),
                clamp((int) Math.round((first.getAlpha() * firstWeight) + (second.getAlpha() * secondWeight)))
        );
    }

    /**
     * Convertit un ratio en taille réelle à partir d'une taille de référence.
     */
    private static int scale(int size, double ratio) {
        return Math.max(1, (int) Math.round(size * ratio));
    }

    /**
     * Garde une composante de couleur dans l'intervalle valide entre 0 et 255.
     */
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
