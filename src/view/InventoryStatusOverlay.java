package view;

import model.FacilityType;
import model.Inventaire;
import model.Type;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Vue dédiée à l'inventaire affiché en bas de l'écran.
 */
public class InventoryStatusOverlay extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // L'ordre d'affichage reste fixe pour que le joueur se fabrique vite des repères.
    private static final Type[] INVENTORY_SEED_ORDER = {
            Type.TULIPE,
            Type.ROSE,
            Type.MARGUERITE,
            Type.ORCHIDEE,
            Type.CAROTTE,
            Type.TOMATE,
            Type.POIVRON,
            Type.COURGETTE
    };
    // Pour l'instant on n'expose qu'une seule installation dans la barre d'inventaire.
    private static final FacilityType INVENTORY_FACILITY = FacilityType.CLOTURE;

    // Le FieldPanel nous sert uniquement de point d'ancrage géométrique:
    private final FieldPanel fieldPanel;
    // Pour alimenter le contenu de la barre
    private final Inventaire inventaire;
    private final Font quantityFont;

    // Le constructeur de la classe
    public InventoryStatusOverlay(FieldPanel fieldPanel, Inventaire inventaire) {
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.quantityFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    /**
     * Recalcule les bornes visibles du champ dans le repère de cette couche.
     * Cela nous permet ensuite de garder un petit espace de sécurité entre la grille et la barre.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintInventory(g2d, getFieldBoundsInView(), getWidth(), getHeight());
        g2d.dispose();
    }

    /**
     * Dessine la barre complète d'inventaire.
     * On la maintient centrée tout en bas de l'écran.
     */
    private void paintInventory(Graphics2D g2d, Rectangle fieldBounds, int viewWidth, int viewHeight) {
        if (inventaire == null || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        int slotSize = 48;
        int slotGap = 6;
        int outerPadding = 10;
        int slotCount = getInventorySlotCount();
        int barWidth = (slotCount * slotSize) + ((slotCount - 1) * slotGap) + (outerPadding * 2);
        int barHeight = slotSize + (outerPadding * 2);
        int barX = (viewWidth - barWidth) / 2;

        // La barre reste basse avec un petit padding inférieur,
        // tout en évitant de remonter au-dessus de la grille si la fenêtre est serrée.
        int desiredY = viewHeight - barHeight - 10;
        int minimumClearY = fieldBounds == null ? desiredY : fieldBounds.y + fieldBounds.height + 12;
        int barY = Math.max(desiredY, minimumClearY);
        barY = Math.min(barY, viewHeight - barHeight - 10);

        drawInventoryShell(g2d, barX, barY, barWidth, barHeight);

        for (int index = 0; index < slotCount; index++) {
            int slotX = barX + outerPadding + (index * (slotSize + slotGap));
            int slotY = barY + outerPadding;
            drawSlot(g2d, index, slotX, slotY, slotSize);
        }
    }

    /**
     * Dessine le fond général de la barre.
     */
    private void drawInventoryShell(Graphics2D g2d, int x, int y, int width, int height) {
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
     * Dessine un emplacement unique de l'inventaire.
     */
    private void drawSlot(Graphics2D g2d, int slotIndex, int x, int y, int size) {
        int quantity = getQuantityForSlot(slotIndex);
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

        int iconPixelSize = hasStock ? 5 : 4;
        int iconArtSize = 5 * iconPixelSize;
        int iconX = x + ((size - iconArtSize) / 2);
        int iconY = y + 9;
        if (slotIndex < INVENTORY_SEED_ORDER.length) {
            drawPixelArt(g2d, INVENTORY_SEED_ORDER[slotIndex], iconX, iconY, iconPixelSize);
        } else {
            drawFence(g2d, iconX, iconY, iconPixelSize);
        }

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

    private int getInventorySlotCount() {
        return INVENTORY_SEED_ORDER.length + 1;
    }

    /**
     * Lit directement la quantité depuis le modèle.
     */
    private int getQuantityForSlot(int slotIndex) {
        if (slotIndex < INVENTORY_SEED_ORDER.length) {
            return inventaire.getQuantiteGraine(INVENTORY_SEED_ORDER[slotIndex]);
        }
        return inventaire.getQuantiteInstallation(INVENTORY_FACILITY);
    }

    /**
     * Dessine une petite icône pixel-art à partir du Type métier.
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

    private void drawTulip(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(226, 73, 126));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(255, 154, 190));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(255, 120, 165));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(255, 154, 190));
    }

    private void drawRose(Graphics2D g2d, int x, int y, int pixelSize) {
        drawStem(g2d, x, y, pixelSize);
        fillPixel(g2d, x + pixelSize, y, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(212, 61, 84));
        fillPixel(g2d, x, y + pixelSize, pixelSize, new Color(174, 41, 66));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(234, 91, 114));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(212, 61, 84));
    }

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

    private void drawCarrot(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(70, 155, 67));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 187, 85));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (3 * pixelSize), pixelSize, new Color(226, 115, 28));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(239, 132, 45));
        fillPixel(g2d, x + pixelSize, y + (4 * pixelSize), pixelSize, new Color(202, 95, 19));
    }

    private void drawTomato(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(235, 89, 73));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(248, 112, 96));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(220, 64, 49));
    }

    private void drawPepper(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + (2 * pixelSize), y, pixelSize, new Color(76, 144, 55));
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (3 * pixelSize), y + pixelSize, pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(226, 186, 57));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(245, 214, 88));
        fillPixel(g2d, x + (2 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(226, 186, 57));
    }

    private void drawZucchini(Graphics2D g2d, int x, int y, int pixelSize) {
        fillPixel(g2d, x + pixelSize, y + pixelSize, pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + pixelSize, pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + pixelSize, y + (2 * pixelSize), pixelSize, new Color(72, 146, 71));
        fillPixel(g2d, x + (2 * pixelSize), y + (2 * pixelSize), pixelSize, new Color(92, 173, 91));
        fillPixel(g2d, x + (3 * pixelSize), y + (3 * pixelSize), pixelSize, new Color(72, 146, 71));
    }

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
