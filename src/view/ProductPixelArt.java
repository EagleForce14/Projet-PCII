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

    // Le constructeur de la classe
    private ProductPixelArt() {}

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
        Color wood = new Color(176, 118, 60);
        Color darkWood = new Color(118, 73, 35);

        fillPixel(g2d, x, y + pixelSize, pixelSize, wood);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, wood);
        fillPixel(g2d, x + (4 * pixelSize), y + pixelSize, pixelSize, wood);
        fillPixel(g2d, x, y + (2 * pixelSize), pixelSize, darkWood);
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, darkWood);
        fillPixel(g2d, x + (4 * pixelSize), y + (2 * pixelSize), pixelSize, darkWood);
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, wood);
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, wood);
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, darkWood);
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, darkWood);
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

    private static void drawStem(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(79, 163, 78));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(63, 137, 63));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(103, 188, 93));
    }

    private static void fillPixel(Graphics2D g2d, int x, int y, int pixelSize, Color color) {
        g2d.setColor(color);
        g2d.fillRect(x, y, pixelSize, pixelSize);
    }
}
