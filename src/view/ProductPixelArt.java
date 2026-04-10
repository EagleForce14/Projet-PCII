package view;

import model.culture.Type;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import model.shop.Seed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * Petit renderer réutilisable pour les items pixel-art du projet.
 * On centralise les motifs ici pour que l'inventaire et la boutique
 * partagent exactement les memes illustrations.
 */
public final class ProductPixelArt {
    private static final int DEFAULT_ART_COLUMNS = 5;
    private static final int DEFAULT_ART_ROWS = 5;
    private static final int CARROT_SEED_IMAGE_MAX_SIDE = 6;
    private static final int RADISH_SEED_IMAGE_MAX_SIDE = 6;
    private static final int CAULIFLOWER_SEED_IMAGE_MAX_SIDE = 6;
    private static final int FENCE_ART_COLUMNS = 9;
    private static final int FENCE_ART_ROWS = 7;
    private static final int PATH_ART_MAX_SIDE = 7;
    private static final int COMPOST_ART_MAX_SIDE = 6;
    private static final int BRIDGE_ART_MAX_SIDE = 8;
    private static final int WOOD_ART_MAX_SIDE = 8;
    private static final int COIN_ART_MAX_SIDE = 7;
    private static final Image PATH_IMAGE = ImageLoader.load("/assets/stone_with_grass.png");
    private static final Image COMPOST_IMAGE = ImageLoader.load("/assets/Compost.png");
    private static final Image BRIDGE_IMAGE = ImageLoader.load("/assets/bridge.png");
    private static final Image WOOD_IMAGE = ImageLoader.load("/assets/wood.png");
    private static final Image COIN_IMAGE = ImageLoader.load("/assets/coin.png");
    private static final Image CARROT_IMAGE = ImageLoader.load("/assets/carotte_mature.png");
    private static final Image RADISH_IMAGE = ImageLoader.load("/assets/radis_mature.png");
    private static final Image CAULIFLOWER_IMAGE = ImageLoader.load("/assets/choufleur_mature.png");

    // Le constructeur de la classe
    private ProductPixelArt() {}

    public static int getProductArtWidth(Product product, int pixelSize) {
        if (product instanceof Seed) {
            return getSeedArtWidth(((Seed) product).getType(), pixelSize);
        }
        if (product instanceof Facility) {
            return getFacilityArtWidth(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    public static int getProductArtHeight(Product product, int pixelSize) {
        if (product instanceof Seed) {
            return getSeedArtHeight(((Seed) product).getType(), pixelSize);
        }
        if (product instanceof Facility) {
            return getFacilityArtHeight(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    public static int getSeedArtWidth(Type type, int pixelSize) {
        if (type == Type.CAROTTE) {
            return getScaledImageSize(CARROT_IMAGE, CARROT_SEED_IMAGE_MAX_SIDE * pixelSize).width;
        }
        if (type == Type.RADIS) {
            return getScaledImageSize(RADISH_IMAGE, RADISH_SEED_IMAGE_MAX_SIDE * pixelSize).width;
        }
        if (type == Type.CHOUFLEUR) {
            return getScaledImageSize(CAULIFLOWER_IMAGE, CAULIFLOWER_SEED_IMAGE_MAX_SIDE * pixelSize).width;
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    public static int getSeedArtHeight(Type type, int pixelSize) {
        if (type == Type.CAROTTE) {
            return getScaledImageSize(CARROT_IMAGE, CARROT_SEED_IMAGE_MAX_SIDE * pixelSize).height;
        }
        if (type == Type.RADIS) {
            return getScaledImageSize(RADISH_IMAGE, RADISH_SEED_IMAGE_MAX_SIDE * pixelSize).height;
        }
        if (type == Type.CHOUFLEUR) {
            return getScaledImageSize(CAULIFLOWER_IMAGE, CAULIFLOWER_SEED_IMAGE_MAX_SIDE * pixelSize).height;
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    public static int getFacilityArtWidth(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_COLUMNS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return getScaledImageSize(PATH_IMAGE, PATH_ART_MAX_SIDE * pixelSize).width;
        }
        if (type == FacilityType.COMPOST) {
            return getScaledImageSize(COMPOST_IMAGE, COMPOST_ART_MAX_SIDE * pixelSize).width;
        }
        if (type == FacilityType.PONT) {
            return getScaledImageSize(BRIDGE_IMAGE, BRIDGE_ART_MAX_SIDE * pixelSize).width;
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    public static int getFacilityArtHeight(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_ROWS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return getScaledImageSize(PATH_IMAGE, PATH_ART_MAX_SIDE * pixelSize).height;
        }
        if (type == FacilityType.COMPOST) {
            return getScaledImageSize(COMPOST_IMAGE, COMPOST_ART_MAX_SIDE * pixelSize).height;
        }
        if (type == FacilityType.PONT) {
            return getScaledImageSize(BRIDGE_IMAGE, BRIDGE_ART_MAX_SIDE * pixelSize).height;
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    public static int getWoodArtWidth(int pixelSize) {
        return getScaledImageSize(WOOD_IMAGE, WOOD_ART_MAX_SIDE * pixelSize).width;
    }

    public static int getWoodArtHeight(int pixelSize) {
        return getScaledImageSize(WOOD_IMAGE, WOOD_ART_MAX_SIDE * pixelSize).height;
    }

    public static int getCoinArtWidth(int pixelSize) {
        return getScaledImageSize(COIN_IMAGE, COIN_ART_MAX_SIDE * pixelSize).width;
    }

    public static int getCoinArtHeight(int pixelSize) {
        return getScaledImageSize(COIN_IMAGE, COIN_ART_MAX_SIDE * pixelSize).height;
    }

    public static void drawProduct(Graphics2D g2d, Product product, int x, int y, int pixelSize) {
        if (product instanceof Seed) {
            drawSeed(g2d, ((Seed) product).getType(), x, y, pixelSize);
        } else if (product instanceof Facility) {
            drawFacility(g2d, ((Facility) product).getType(), x, y, pixelSize);
        }
    }

    public static void drawSeed(Graphics2D g2d, Type type, int x, int y, int pixelSize) {
        switch (type) {
            case TULIPE:
                drawTulip(g2d, x, y, pixelSize);
                break;
            case ROSE:
                drawRose(g2d, x, y, pixelSize);
                break;
            case MARGUERITE:
                drawDaisy(g2d, x, y, pixelSize);
                break;
            case ORCHIDEE:
                drawOrchid(g2d, x, y, pixelSize);
                break;
            case CAROTTE:
                drawScaledImage(g2d, CARROT_IMAGE, x, y, CARROT_SEED_IMAGE_MAX_SIDE * pixelSize);
                break;
            case RADIS:
                drawScaledImage(g2d, RADISH_IMAGE, x, y, RADISH_SEED_IMAGE_MAX_SIDE * pixelSize);
                break;
            case CHOUFLEUR:
                drawScaledImage(g2d, CAULIFLOWER_IMAGE, x, y, CAULIFLOWER_SEED_IMAGE_MAX_SIDE * pixelSize);
                break;
            case COURGETTE:
                drawZucchini(g2d, x, y, pixelSize);
                break;
            default:
                break;
        }
    }

    public static void drawFacility(Graphics2D g2d, FacilityType type, int x, int y, int pixelSize) {
        switch (type) {
            case CLOTURE:
                drawFence(g2d, x, y, pixelSize);
                break;
            case CHEMIN:
                drawScaledImage(g2d, PATH_IMAGE, x, y, PATH_ART_MAX_SIDE * pixelSize);
                break;
            case COMPOST:
                drawScaledImage(g2d, COMPOST_IMAGE, x, y, COMPOST_ART_MAX_SIDE * pixelSize);
                break;
            case PONT:
                drawScaledImage(g2d, BRIDGE_IMAGE, x, y, BRIDGE_ART_MAX_SIDE * pixelSize);
                break;
            case JARDINIER:
                drawGardener(g2d, x, y, pixelSize);
                break;
            default:
                break;
        }
    }

    public static void drawWoodResource(Graphics2D g2d, int x, int y, int pixelSize) {
        drawScaledImage(g2d, WOOD_IMAGE, x, y, WOOD_ART_MAX_SIDE * pixelSize);
    }

    public static void drawCoinResource(Graphics2D g2d, int x, int y, int pixelSize) {
        drawScaledImage(g2d, COIN_IMAGE, x, y, COIN_ART_MAX_SIDE * pixelSize);
    }

    private static void drawTulip(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(226, 73, 126));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(255, 154, 190));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(255, 120, 165));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(255, 154, 190));
    }

    private static void drawRose(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + pixelSize, y, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(212, 61, 84));
        fillPixel(g2d, x, y + pixelSize, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(234, 91, 114));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(212, 61, 84));
    }

    private static void drawDaisy(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        Color petal = new Color(245, 239, 227);
        fillPixel(g2d, x + pixelSize, y, pixelSize, petal);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, petal);
        fillPixel(g2d, x, y + pixelSize, pixelSize, petal);
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, petal);
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, petal);
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, petal);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(236, 201, 72));
    }

    private static void drawOrchid(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        Color purple = new Color(170, 110, 214);
        Color lightPurple = new Color(213, 170, 244);
        fillPixel(g2d, x + pixelSize, y, pixelSize, purple);
        fillPixel(g2d, x + (3 * pixelSize), y, pixelSize, purple);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, lightPurple);
        fillPixel(g2d, x, y + (2 * pixelSize), pixelSize, purple);
        fillPixel(g2d, x + (4 * pixelSize), y + (2 * pixelSize), pixelSize, purple);
    }

    private static void drawZucchini(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(72, 146, 71));
    }

    private static void drawFence(Graphics2D g2d, int x, int y, int pixelSize) {
        Color cap = new Color(72, 56, 46);
        Color woodLight = new Color(255, 173, 49);
        Color wood = new Color(255, 155, 36);
        Color woodDark = new Color(219, 128, 11);
        Color slat = new Color(239, 117, 34);
        Color slatDark = new Color(191, 86, 21);

        // Les deux poteaux sont volontairement dessinés avec exactement le même motif.
        fillGridRect(g2d, x, y, pixelSize, 1, 0, 2, 1, cap);
        fillGridRect(g2d, x, y, pixelSize, 6, 0, 2, 1, cap);

        fillGridRect(g2d, x, y, pixelSize, 1, 1, 2, 5, wood);
        fillGridRect(g2d, x, y, pixelSize, 6, 1, 2, 5, wood);
        fillGridRect(g2d, x, y, pixelSize, 1, 1, 1, 5, woodLight);
        fillGridRect(g2d, x, y, pixelSize, 6, 1, 1, 5, woodLight);
        fillGridRect(g2d, x, y, pixelSize, 2, 3, 1, 2, woodDark);
        fillGridRect(g2d, x, y, pixelSize, 7, 3, 1, 2, woodDark);

        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slatDark);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slatDark);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 1, 1, new Color(255, 145, 61));
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 1, 1, new Color(255, 145, 61));
    }

    private static void drawGardener(Graphics2D g2d, int x, int y, int pixelSize) {
        Color hat = new Color(170, 109, 61);
        Color skin = new Color(238, 192, 154);
        Color shirt = new Color(67, 121, 170);
        Color pants = new Color(54, 76, 108);

        fillPixel(g2d, x + pixelSize, y, pixelSize, hat);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, hat);
        fillPixel(g2d, x, y + pixelSize, pixelSize, hat);
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, skin);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, skin);
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, hat);
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, shirt);
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, shirt);
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, pants);
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, pants);
    }

    private static void drawStem(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(79, 163, 78));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(63, 137, 63));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(103, 188, 93));
    }

    private static void fillPixel(Graphics2D g2d, int x, int y, int pixelSize, Color color) {
        g2d.setColor(color);
        g2d.fillRect(x, y, pixelSize, pixelSize);
    }

    /**
     * Helper unique pour dessiner une image d'objet dans l'UI.
     *
     * On lui passe simplement :
     * - l'image source,
     * - la position,
     * - la taille cible maximale en pixels.
     *
     * La méthode se charge ensuite :
     * - de respecter le ratio de l'image,
     * - d'utiliser un rendu "nearest neighbor" pour garder un aspect net.
     */
    private static void drawScaledImage(Graphics2D g2d, Image image, int x, int y, int maxSide) {
        if (image == null) {
            return;
        }

        Dimension scaledSize = getScaledImageSize(image, maxSide);
        Graphics2D imageGraphics = (Graphics2D) g2d.create();
        imageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        imageGraphics.drawImage(image, x, y, scaledSize.width, scaledSize.height, null);
        imageGraphics.dispose();
    }

    /**
     * Calcule une seule fois la taille affichée d'une image.
     *
     * On évite ainsi de dupliquer la même lecture
     * de largeur/hauteur source dans plusieurs méthodes.
     */
    private static Dimension getScaledImageSize(Image image, int maxSide) {
        if (image == null) {
            return new Dimension(maxSide, maxSide);
        }

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Dimension(maxSide, maxSide);
        }

        if (imageWidth >= imageHeight) {
            return new Dimension(maxSide, Math.max(1, (maxSide * imageHeight) / imageWidth));
        }
        return new Dimension(Math.max(1, (maxSide * imageWidth) / imageHeight), maxSide);
    }

    private static void fillGridRect(Graphics2D g2d, int originX, int originY, int pixelSize,
                                     int gridX, int gridY, int gridWidth, int gridHeight, Color color) {
        g2d.setColor(color);
        g2d.fillRect(
                originX + (gridX * pixelSize),
                originY + (gridY * pixelSize),
                gridWidth * pixelSize,
                gridHeight * pixelSize
        );
    }
}
