package view.grotte;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import view.ImageLoader;

/**
 * Génère toutes les images temporaires de la grotte directement en code.

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
     * Fabrique une tuile de roche en assemblant plusieurs petits blocs.
     */
    private static BufferedImage createRockTile(int variant) {
        BufferedImage image = createTransparentCanvas();
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
        drawSpeckles(image, variant);
        shadeLowerHalf(image, new Color(11, 9, 16, 78));
        return image;
    }

    /**
     * Fabrique une tuile de sol de salle avec plusieurs briques et fissures légères.
     */
    private static BufferedImage createRoomFloorTile(int variant) {
        BufferedImage image = createTransparentCanvas();
        fill(image, ROOM_FLOOR_BACKGROUND);

        paintBrick(image, 2, 3, 13, 11, ROOM_FLOOR_DARK);
        paintBrick(image, 16, 2, 14, 10, ROOM_FLOOR_MID);
        paintBrick(image, 31, 3, 14, 11, ROOM_FLOOR_DARK);
        paintBrick(image, 1, 15, 11, 12, ROOM_FLOOR_MID);
        paintBrick(image, 13, 16, 15, 11, ROOM_FLOOR_DARK);
        paintBrick(image, 29, 16, 16, 12, ROOM_FLOOR_MID);
        paintBrick(image, 4, 29, 13, 13, ROOM_FLOOR_DARK);
        paintBrick(image, 18, 30, 12, 12, ROOM_FLOOR_MID);
        paintBrick(image, 31, 30, 13, 13, ROOM_FLOOR_DARK);
        drawJointShadows(image);
        drawFloorCrack(image, 10 + variant);
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
    private static void paintBrick(BufferedImage image, int x, int y, int width, int height, Color fill) {
        fillRect(image, x, y, width, height, PATH_GROUT);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2), fill);
        fillRect(image, x + 1, y + 1, Math.max(1, width - 3), 1, GrotteTileFactory.ROOM_FLOOR_LIGHT);
        fillRect(image, x + 1, y + 1, 1, Math.max(1, height - 3), GrotteTileFactory.ROOM_FLOOR_LIGHT);
        fillRect(image, x + 2, y + height - 2, Math.max(1, width - 4), 1, darker(fill, 24));
        fillRect(image, x + width - 2, y + 2, 1, Math.max(1, height - 4), darker(fill, 18));
    }

    /**
     * Ajoute de petits points de détail pour éviter que la roche paraisse trop uniforme.
     */
    private static void drawSpeckles(BufferedImage image, int variant) {
        for (int i = 0; i < 12; i++) {
            int x = Math.floorMod((i * 7) + (variant * 5), image.getWidth());
            int y = Math.floorMod((i * 11) + (variant * 3), image.getHeight());
            putPixel(image, x, y, i % 2 == 0 ? GrotteTileFactory.ROCK_HIGHLIGHT : GrotteTileFactory.ROCK_OUTLINE);
        }
    }

    /**
     * Trace les lignes de joint entre les grandes briques du sol.
     */
    private static void drawJointShadows(BufferedImage image) {
        fillRect(image, 0, 14, image.getWidth(), 1, GrotteTileFactory.ROOM_FLOOR_GROUT);
        fillRect(image, 0, 29, image.getWidth(), 1, GrotteTileFactory.ROOM_FLOOR_GROUT);
        fillRect(image, 15, 0, 1, image.getHeight(), GrotteTileFactory.ROOM_FLOOR_GROUT);
        fillRect(image, 30, 0, 1, image.getHeight(), GrotteTileFactory.ROOM_FLOOR_GROUT);
    }

    /**
     * Dessine une fissure simple dans une dalle du sol.
     */
    private static void drawFloorCrack(BufferedImage image, int startX) {
        for (int i = 0; i < 6; i++) {
            int x = Math.min(image.getWidth() - 2, startX + i);
            int y = Math.min(image.getHeight() - 2, 22 + ((i + 1) / 2));
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
                image.setRGB(x, y, blend(new Color(image.getRGB(x, y), true), shadow).getRGB());
            }
        }
    }

    /**
     * Crée une image transparente de la taille demandée.
     */
    private static BufferedImage createTransparentCanvas() {
        return new BufferedImage(Math.max(1, GrotteTileFactory.BASE_TILE_SIZE), Math.max(1, GrotteTileFactory.BASE_TILE_SIZE), BufferedImage.TYPE_INT_ARGB);
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
    private static Color blend(Color first, Color second) {
        double firstWeight = 1.0 - 0.22;
        return new Color(
                clamp((int) Math.round((first.getRed() * firstWeight) + (second.getRed() * 0.22))),
                clamp((int) Math.round((first.getGreen() * firstWeight) + (second.getGreen() * 0.22))),
                clamp((int) Math.round((first.getBlue() * firstWeight) + (second.getBlue() * 0.22))),
                clamp((int) Math.round((first.getAlpha() * firstWeight) + (second.getAlpha() * 0.22)))
        );
    }

    /**
     * Garde une composante de couleur dans l'intervalle valide entre 0 et 255.
     */
    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
