package view;

import model.FacilityType;
import model.Inventaire;
import model.Type;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Graphics2D;

/**
 * Classe permettant d'afficher l'inventaire.
 *
 * On affiche une barre fixe, compacte et toujours visible,
 * centrée tout en bas de l'écran.
 */
public class InventoryStatusOverlay {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // L'ordre de la barre reste stable pour que le joueur memorise vite les emplacements.
    // On ne duplique plus un "mini modele" dans la vue:
    // la hotbar sait juste dans quel ordre afficher les types existants du vrai Inventaire.
    private static final Type[] HOTBAR_SEED_ORDER = {
            Type.TULIPE,
            Type.ROSE,
            Type.MARGUERITE,
            Type.ORCHIDEE,
            Type.CAROTTE,
            Type.TOMATE,
            Type.POIVRON,
            Type.COURGETTE
    };
    private static final FacilityType HOTBAR_FACILITY = FacilityType.CLOTURE;

    private final Font quantityFont;

    // Le constructeur de la classe
    public InventoryStatusOverlay() {
        this.quantityFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
    }

    /**
     * Le dessin de l'inventaire
     * On tient compte de la position du champ pour eviter de recouvrir la grille.
     */
    public void paint(Graphics2D g2d, Inventaire inventaire, Rectangle fieldBounds, int viewWidth, int viewHeight) {
        if (inventaire == null || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        int slotSize = 48;
        int slotGap = 6;
        int outerPadding = 10;
        int slotCount = getHotbarSlotCount();
        int barWidth = (slotCount * slotSize) + ((slotCount - 1) * slotGap) + (outerPadding * 2);
        int barHeight = slotSize + (outerPadding * 2);
        int barX = (viewWidth - barWidth) / 2;

        // L'affichage est tout en bas, mais avec une légère marge pour ne pas coller au bord inférieur de la fenêtre.
        int desiredY = viewHeight - barHeight - 10;
        int minimumClearY = fieldBounds == null ? desiredY : fieldBounds.y + fieldBounds.height + 12;
        int barY = Math.max(desiredY, minimumClearY);
        barY = Math.min(barY, viewHeight - barHeight - 10);

        drawHotbarShell(g2d, barX, barY, barWidth, barHeight);

        // Les slots sont dessines sur une seule ligne, comme une vraie hotbar.
        // C'est beaucoup plus discret qu'un panneau entier et bien plus rapide a lire.
        for (int index = 0; index < slotCount; index++) {
            int slotX = barX + outerPadding + (index * (slotSize + slotGap));
            int slotY = barY + outerPadding;
            drawSlot(g2d, inventaire, index, slotX, slotY, slotSize);
        }
    }

    /**
     * Dessine la "coque" (i.e. le background) de l'inventaire.
     * On va utiliser un style "retro-bois"
     */
    private void drawHotbarShell(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 90));
        g2d.fillRoundRect(x + 4, y + 4, width, height, 18, 18);

        g2d.setColor(new Color(57, 41, 24, 236));
        g2d.fillRoundRect(x, y, width, height, 18, 18);

        g2d.setColor(new Color(120, 88, 54, 220));
        g2d.fillRoundRect(x + 1, y + 1, width - 2, 16, 16, 16);

        g2d.setColor(new Color(230, 214, 157, 255));
        g2d.drawRoundRect(x, y, width - 1, height - 1, 18, 18);
    }

    /**
     * Dessine un emplacement pour un élément.
     *
     * Un emplacement qui ne contient pas 0 élément est mis en lumière.
     * Un emplacement qui contient 0 élément est toujours affiché mais de manière plus sombre.
     */
    private void drawSlot(Graphics2D g2d, Inventaire inventaire, int slotIndex, int x, int y, int size) {
        int quantity = getQuantityForSlot(inventaire, slotIndex);
        boolean hasStock = quantity > 0;

        Color slotFill = hasStock ? new Color(76, 56, 34, 245) : new Color(50, 39, 28, 220);
        Color slotInner = hasStock ? new Color(109, 80, 49, 180) : new Color(69, 55, 42, 120);
        Color slotBorder = hasStock ? new Color(241, 220, 154) : new Color(126, 106, 81);
        Color quantityFill = hasStock ? new Color(255, 249, 228) : new Color(185, 175, 161);

        g2d.setColor(slotFill);
        g2d.fillRoundRect(x, y, size, size, 10, 10);

        g2d.setColor(slotInner);
        g2d.fillRoundRect(x + 3, y + 3, size - 6, 10, 8, 8);

        g2d.setColor(slotBorder);
        g2d.drawRoundRect(x, y, size - 1, size - 1, 10, 10);

        // On laisse l'icône occuper l'essentiel de l'emplacement.
        int iconPixelSize = hasStock ? 5 : 4;
        int iconArtSize = 5 * iconPixelSize;
        int iconX = x + ((size - iconArtSize) / 2);
        int iconY = y + 9;
        if (slotIndex < HOTBAR_SEED_ORDER.length) {
            drawPixelArt(g2d, HOTBAR_SEED_ORDER[slotIndex], iconX, iconY, iconPixelSize);
        } else {
            drawFence(g2d, iconX, iconY, iconPixelSize);
        }

        // Sur un emplacement vide, on assombrit légèrement.
        if (!hasStock) {
            g2d.setColor(new Color(20, 15, 10, 95));
            g2d.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);
        }

        g2d.setFont(quantityFont);
        g2d.setColor(new Color(32, 22, 13, 190));
        g2d.fillRoundRect(x + size - 18, y + size - 16, 14, 11, 6, 6);

        g2d.setColor(quantityFill);
        String quantityText = String.valueOf(quantity);
        int textX = x + size - 15;
        int textY = y + size - 7;
        if (quantity >= 10) {
            textX = x + size - 17;
        }
        g2d.drawString(quantityText, textX, textY);
    }

    /**
     * La hotbar expose 8 slots de graines plus 1 slot pour la cloture.
     * Ce calcul reste tres simple mais il dit explicitement
     * qu'on derive l'affichage depuis le modele existant, pas depuis une structure parallele.
     */
    private int getHotbarSlotCount() {
        return HOTBAR_SEED_ORDER.length + 1;
    }

    /**
     * Lit la quantite directement sur le vrai Inventaire.
     * La vue ne transporte donc plus son propre "InventoryEntry" prive.
     */
    private int getQuantityForSlot(Inventaire inventaire, int slotIndex) {
        if (slotIndex < HOTBAR_SEED_ORDER.length) {
            return inventaire.getQuantiteGraine(HOTBAR_SEED_ORDER[slotIndex]);
        }
        return inventaire.getQuantiteInstallation(HOTBAR_FACILITY);
    }

    /**
     * Permet de dessiner une mini icone simple en pixel-art a partir du Type.
     * Le choix du dessin repose donc directement sur le type metier deja present dans le modele.
     */
    private void drawPixelArt(Graphics2D g2d, Type type, int x, int y, int pixelSize) {
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

    // Pour dessiner une tulipe
    private void drawTulip(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(226, 73, 126));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(255, 154, 190));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(255, 120, 165));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(255, 154, 190));
    }

    // Pour dessiner une rose
    private void drawRose(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + pixelSize, y, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(212, 61, 84));
        fillPixel(g2d, x, y + pixelSize, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(234, 91, 114));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(212, 61, 84));
    }

    // Pour dessiner une marguerite
    private void drawDaisy(Graphics2D g2d, int x, int y, int pixelSize) {
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

    // Pour dessiner une orchidée
    private void drawOrchid(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        Color purple = new Color(170, 110, 214);
        Color lightPurple = new Color(213, 170, 244);
        fillPixel(g2d, x + pixelSize, y, pixelSize, purple);
        fillPixel(g2d, x + (3 * pixelSize), y, pixelSize, purple);
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, lightPurple);
        fillPixel(g2d, x, y + (2 * pixelSize), pixelSize, purple);
        fillPixel(g2d, x + (4 * pixelSize), y + (2 * pixelSize), pixelSize, purple);
    }

    // Pour dessiner une carotte
    private void drawCarrot(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(70, 155, 67));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(226, 115, 28));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (4 * pixelSize), pixelSize, new Color(202, 95, 19));
    }

    // Pour dessiner une tomate
    private void drawTomato(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(235, 89, 73));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(248, 112, 96));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
    }

    // Pour dessiner un poivron jaune
    private void drawPepper(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(226, 186, 57));
    }

    // Pour dessiner une courgette
    private void drawZucchini(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(72, 146, 71));
    }

    // Pour dessiner une clôture
    private void drawFence(Graphics2D g2d, int x, int y, int pixelSize) {
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

    /**
     * Petite tige commune aux fleurs.
     * C'est un detail minime, mais ca suffit a rendre les silhouettes plus lisibles.
     */
    private void drawStem(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(79, 163, 78));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(63, 137, 63));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(103, 188, 93));
    }

    private void fillPixel(Graphics2D g2d, int x, int y, int pixelSize, Color color) {
        g2d.setColor(color);
        g2d.fillRect(x, y, pixelSize, pixelSize);
    }
}
