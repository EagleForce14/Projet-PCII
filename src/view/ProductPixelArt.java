package view;

import model.culture.Type;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import model.shop.Seed;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Petit renderer reutilisable pour les items pixel-art du projet.
 * On centralise les motifs ici pour que l'inventaire et la boutique
 * partagent exactement les memes illustrations.
 */
public final class ProductPixelArt {
    private static final int DEFAULT_ART_COLUMNS = 5;
    private static final int DEFAULT_ART_ROWS = 5;
    private static final int FENCE_ART_COLUMNS = 9;
    private static final int FENCE_ART_ROWS = 7;
    private static final int PATH_ART_COLUMNS = 7;
    private static final int PATH_ART_ROWS = 7;

    // Le constructeur de la classe
    private ProductPixelArt() {}

    public static int getProductArtWidth(Product product, int pixelSize) {
        if (product instanceof Facility) {
            return getFacilityArtWidth(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    public static int getProductArtHeight(Product product, int pixelSize) {
        if (product instanceof Facility) {
            return getFacilityArtHeight(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    public static int getFacilityArtWidth(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_COLUMNS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return PATH_ART_COLUMNS * pixelSize;
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    public static int getFacilityArtHeight(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_ROWS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return PATH_ART_ROWS * pixelSize;
        }
        return DEFAULT_ART_ROWS * pixelSize;
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
                drawCarrot(g2d, x, y, pixelSize);
                break;
            case TOMATE:
                drawTomato(g2d, x, y, pixelSize);
                break;
            case POIVRON:
                drawPepper(g2d, x, y, pixelSize);
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
                drawStonePath(g2d, x, y, pixelSize);
                break;
            case ENGRAIS:
                drawFertilizer(g2d, x, y, pixelSize);
                break;
            case JARDINIER:
                drawGardener(g2d, x, y, pixelSize);
                break;
            default:
                break;
        }
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

    private static void drawCarrot(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(70, 155, 67));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(226, 115, 28));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (4 * pixelSize), pixelSize, new Color(202, 95, 19));
    }

    private static void drawTomato(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(235, 89, 73));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(248, 112, 96));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
    }

    private static void drawPepper(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(226, 186, 57));
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

    private static void drawFertilizer(Graphics2D g2d, int x, int y, int pixelSize) {
        Color sack = new Color(201, 162, 98);
        Color sackShadow = new Color(139, 103, 58);
        Color leaf = new Color(89, 159, 79);

        fillPixel(g2d, x + pixelSize, y, pixelSize, sackShadow);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, sackShadow);
        fillPixel(g2d, x, y + pixelSize, pixelSize, sack);
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, sack);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, sack);
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, sack);
        fillPixel(g2d, x, y + (2 * pixelSize), pixelSize, sackShadow);
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, sack);
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, sack);
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, sackShadow);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, leaf);
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

    /**
     * Petit pictogramme de chemin en pierre.
     *
     * Le but est qu'on lise tout de suite "sol mineral"
     * et surtout pas "terre labouree".
     * On utilise donc des gris froids et des joints sombres.
     */
    private static void drawStonePath(Graphics2D g2d, int x, int y, int pixelSize) {
        Color joint = new Color(63, 63, 67);
        Color stoneDark = new Color(104, 104, 110);
        Color stone = new Color(146, 146, 151);
        Color stoneLight = new Color(192, 192, 197);

        fillGridRect(g2d, x, y, pixelSize, 0, 0, 7, 7, joint);

        fillGridRect(g2d, x, y, pixelSize, 0, 0, 3, 2, stone);
        fillGridRect(g2d, x, y, pixelSize, 3, 0, 4, 2, stoneDark);
        fillGridRect(g2d, x, y, pixelSize, 1, 2, 3, 2, stoneDark);
        fillGridRect(g2d, x, y, pixelSize, 4, 2, 2, 2, stone);
        fillGridRect(g2d, x, y, pixelSize, 0, 4, 4, 3, stone);
        fillGridRect(g2d, x, y, pixelSize, 4, 4, 3, 3, stoneDark);

        fillGridRect(g2d, x, y, pixelSize, 1, 0, 1, 1, stoneLight);
        fillGridRect(g2d, x, y, pixelSize, 4, 0, 1, 1, stoneLight);
        fillGridRect(g2d, x, y, pixelSize, 2, 2, 1, 1, stoneLight);
        fillGridRect(g2d, x, y, pixelSize, 5, 2, 1, 1, stoneLight);
        fillGridRect(g2d, x, y, pixelSize, 1, 4, 1, 1, stoneLight);
        fillGridRect(g2d, x, y, pixelSize, 5, 5, 1, 1, stoneLight);
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
