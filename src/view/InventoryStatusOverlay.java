package view;

import model.culture.Type;
import model.management.Inventaire;
import model.movement.MovementModel;
import model.shop.FacilityType;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Vue dédiée à l'inventaire affiché en bas de l'écran.
 */
public class InventoryStatusOverlay extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 6;
    private static final int OUTER_PADDING = 10;

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
    /*
     * Les installations sont maintenant listées explicitement dans l'ordre voulu.
     * Cela evite de coder des "cas speciaux" partout des qu'un nouvel objet apparait.
     */
    private static final FacilityType[] INVENTORY_FACILITY_ORDER = {
            FacilityType.CLOTURE,
            FacilityType.CHEMIN,
            FacilityType.COMPOST
    };

    // Le FieldPanel nous sert uniquement de point d'ancrage géométrique:
    private final FieldPanel fieldPanel;
    // Pour alimenter le contenu de la barre
    private final Inventaire inventaire;
    private final MovementModel movementModel;
    private final Font quantityFont;

    // Le constructeur de la classe
    public InventoryStatusOverlay(FieldPanel fieldPanel, Inventaire inventaire, MovementModel movementModel) {
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.movementModel = movementModel;
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

    /**
     * L'overlay couvre tout l'écran pour le rendu, mais il ne doit capturer
     * les clics que sur la barre visible d'inventaire.
     */
    @Override
    public boolean contains(int x, int y) {
        Rectangle barBounds = getInventoryBarBounds(getFieldBoundsInView(), getWidth(), getHeight());
        return barBounds.contains(x, y);
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

        Rectangle barBounds = getInventoryBarBounds(fieldBounds, viewWidth, viewHeight);

        drawInventoryShell(g2d, barBounds.x, barBounds.y, barBounds.width, barBounds.height);

        for (int index = 0; index < getInventorySlotCount(); index++) {
            Rectangle slotBounds = getSlotBounds(index, barBounds);
            if (slotBounds != null) {
                drawSlot(g2d, index, slotBounds.x, slotBounds.y, slotBounds.width);
            }
        }
    }

    /**
     * Géométrie partagée de la barre d'inventaire.
     * On l'expose en statique pour que d'autres composants puissent respecter
     * la même limite visuelle sans recopier les calculs.
     */
    public static Rectangle computeInventoryBarBounds(Rectangle fieldBounds, int viewWidth, int viewHeight) {
        int slotCount = getInventorySlotCount();
        int barWidth = (slotCount * SLOT_SIZE) + ((slotCount - 1) * SLOT_GAP) + (OUTER_PADDING * 2);
        int barHeight = SLOT_SIZE + (OUTER_PADDING * 2);
        int barX = (viewWidth - barWidth) / 2;

        // La barre reste basse avec un petit padding inférieur,
        // tout en évitant de remonter au-dessus de la grille si la fenêtre est serrée.
        int desiredY = viewHeight - barHeight - 10;
        int minimumClearY = fieldBounds == null ? desiredY : fieldBounds.y + fieldBounds.height + 12;
        int barY = Math.max(desiredY, minimumClearY);
        barY = Math.min(barY, viewHeight - barHeight - 10);
        return new Rectangle(barX, barY, barWidth, barHeight);
    }

    private Rectangle getInventoryBarBounds(Rectangle fieldBounds, int viewWidth, int viewHeight) {
        return computeInventoryBarBounds(fieldBounds, viewWidth, viewHeight);
    }

    private Rectangle getSlotBounds(int slotIndex, Rectangle barBounds) {
        if (slotIndex < 0 || slotIndex >= getInventorySlotCount() || barBounds == null) {
            return null;
        }

        int slotX = barBounds.x + OUTER_PADDING + (slotIndex * (SLOT_SIZE + SLOT_GAP));
        int slotY = barBounds.y + OUTER_PADDING;
        return new Rectangle(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
    }

    /**
     * Le contrôleur appelle ce helper pour transformer un clic dans l'overlay
     * en numéro de slot d'inventaire.
     */
    public int getSlotIndexAt(Point point) {
        if (point == null) {
            return -1;
        }

        Rectangle barBounds = getInventoryBarBounds(getFieldBoundsInView(), getWidth(), getHeight());
        if (!barBounds.contains(point)) {
            return -1;
        }

        for (int slotIndex = 0; slotIndex < getInventorySlotCount(); slotIndex++) {
            Rectangle slotBounds = getSlotBounds(slotIndex, barBounds);
            if (slotBounds != null && slotBounds.contains(point)) {
                return slotIndex;
            }
        }

        return -1;
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
        boolean selected = isSlotSelected(slotIndex);

        Color slotFill = selected
                ? new Color(108, 74, 37, 248)
                : hasStock ? new Color(76, 56, 34, 245) : new Color(50, 39, 28, 220);
        Color slotInner = selected
                ? new Color(166, 122, 60, 190)
                : hasStock ? new Color(109, 80, 49, 180) : new Color(69, 55, 42, 120);
        Color slotBorder = selected
                ? new Color(255, 230, 132)
                : hasStock ? new Color(241, 220, 154) : new Color(126, 106, 81);
        Color quantityFill = hasStock ? new Color(255, 249, 228) : new Color(185, 175, 161);

        g2d.setColor(slotFill);
        g2d.fillRoundRect(x, y, size, size, 10, 10);

        g2d.setColor(slotInner);
        g2d.fillRoundRect(x + 3, y + 3, size - 6, 10, 8, 8);

        g2d.setColor(slotBorder);
        g2d.drawRoundRect(x, y, size - 1, size - 1, 10, 10);
        if (selected) {
            g2d.drawRoundRect(x + 1, y + 1, size - 3, size - 3, 9, 9);
        }

        int iconPixelSize = hasStock ? 5 : 4;
        FacilityType facilityType = getFacilityTypeForSlot(slotIndex);
        int iconArtWidth = slotIndex < INVENTORY_SEED_ORDER.length
                ? 5 * iconPixelSize
                : ProductPixelArt.getFacilityArtWidth(facilityType, iconPixelSize);
        int iconArtHeight = slotIndex < INVENTORY_SEED_ORDER.length
                ? 5 * iconPixelSize
                : ProductPixelArt.getFacilityArtHeight(facilityType, iconPixelSize);
        int iconX = x + ((size - iconArtWidth) / 2);
        int iconY = y + ((size - iconArtHeight) / 2) + 1;
        if (slotIndex < INVENTORY_SEED_ORDER.length) {
            ProductPixelArt.drawSeed(g2d, INVENTORY_SEED_ORDER[slotIndex], iconX, iconY, iconPixelSize);
        } else {
            ProductPixelArt.drawFacility(g2d, facilityType, iconX, iconY, iconPixelSize);
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

    private static int getInventorySlotCount() {
        return INVENTORY_SEED_ORDER.length + INVENTORY_FACILITY_ORDER.length;
    }

    private boolean isSeedSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < INVENTORY_SEED_ORDER.length;
    }

    public Type getSeedTypeForSlot(int slotIndex) {
        return isSeedSlot(slotIndex) ? INVENTORY_SEED_ORDER[slotIndex] : null;
    }

    public FacilityType getFacilityTypeForSlot(int slotIndex) {
        int facilityIndex = slotIndex - INVENTORY_SEED_ORDER.length;
        if (facilityIndex < 0 || facilityIndex >= INVENTORY_FACILITY_ORDER.length) {
            return null;
        }
        return INVENTORY_FACILITY_ORDER[facilityIndex];
    }

    private boolean isSlotSelected(int slotIndex) {
        if (movementModel == null) {
            return false;
        }
        if (isSeedSlot(slotIndex)) {
            return getSeedTypeForSlot(slotIndex) == movementModel.getSelectedSeedType();
        }
        return getFacilityTypeForSlot(slotIndex) == movementModel.getSelectedFacilityType();
    }

    /**
     * Lit directement la quantité depuis le modèle.
     */
    private int getQuantityForSlot(int slotIndex) {
        if (slotIndex < INVENTORY_SEED_ORDER.length) {
            return inventaire.getQuantiteGraine(INVENTORY_SEED_ORDER[slotIndex]);
        }
        FacilityType facilityType = getFacilityTypeForSlot(slotIndex);
        return facilityType == null ? 0 : inventaire.getQuantiteInstallation(facilityType);
    }
}
