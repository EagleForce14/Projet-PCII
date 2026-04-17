package view;

import model.culture.FieldZone;
import model.culture.Type;
import model.management.Inventaire;
import model.movement.MovementModel;
import model.movement.Unit;
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
 *
 * L'inventaire est volontairement découpé en trois petits blocs visuels :
 * - les graines du champ principal,
 * - les graines du côté gauche de la rivière,
 * - puis les ressources et objets de pose.
 *
 * Le modèle métier ne change pas :
 * ce panneau ne fait que présenter différemment les mêmes slots logiques.
 */
public class InventoryStatusOverlay extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 6;
    private static final int GROUP_GAP = 12;
    private static final int OUTER_PADDING = 10;
    private static final Color LOCKED_SEED_OVERLAY = new Color(22, 18, 14, 148);
    private static final Color LOCKED_SEED_BORDER = new Color(150, 132, 112, 185);

    private static final Type[] MAIN_ZONE_SEED_ORDER = {
            Type.TULIPE,
            Type.ROSE,
            Type.MARGUERITE,
            Type.ORCHIDEE,
            Type.CAROTTE,
            Type.RADIS,
            Type.CHOUFLEUR
    };
    private static final Type[] LEFT_ZONE_SEED_ORDER = {
            Type.NENUPHAR
    };
    private static final FacilityType[] INVENTORY_FACILITY_ORDER = {
            FacilityType.CLOTURE,
            FacilityType.CHEMIN,
            FacilityType.COMPOST,
            FacilityType.PONT
    };

    private final FieldPanel fieldPanel;
    private final Inventaire inventaire;
    private final MovementModel movementModel;
    private final Font quantityFont;

    public InventoryStatusOverlay(FieldPanel fieldPanel, Inventaire inventaire, MovementModel movementModel) {
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.movementModel = movementModel;
        this.quantityFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
        setOpaque(false);
        setDoubleBuffered(true);
    }

    /**
     * Recalcule les bornes visibles du champ dans le repère de cette couche.
     * Cela nous permet ensuite de garder un petit espace de sécurité entre la grille et les barres.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    /**
     * L'overlay couvre tout l'écran pour le rendu, mais il ne doit capturer
     * les clics que sur les trois barres visibles.
     */
    @Override
    public boolean contains(int x, int y) {
        Rectangle[] groupBounds = getInventoryGroupBounds(getFieldBoundsInView(), getWidth(), getHeight());
        for (Rectangle groupBoundsItem : groupBounds) {
            if (groupBoundsItem.contains(x, y)) {
                return true;
            }
        }
        return false;
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
     * Dessine les trois blocs d'inventaire côte à côte.
     * On garde tout centré en bas de l'écran comme avant,
     * mais chaque famille d'objets a maintenant sa propre coque visuelle.
     */
    private void paintInventory(Graphics2D g2d, Rectangle fieldBounds, int viewWidth, int viewHeight) {
        if (inventaire == null || viewWidth <= 0 || viewHeight <= 0) {
            return;
        }

        Rectangle[] groupBounds = getInventoryGroupBounds(fieldBounds, viewWidth, viewHeight);
        for (Rectangle groupBoundsItem : groupBounds) {
            drawInventoryShell(g2d, groupBoundsItem.x, groupBoundsItem.y, groupBoundsItem.width, groupBoundsItem.height);
        }

        for (int slotIndex = 0; slotIndex < getInventorySlotCount(); slotIndex++) {
            Rectangle slotBounds = getSlotBounds(slotIndex, groupBounds);
            if (slotBounds != null) {
                drawSlot(g2d, slotIndex, slotBounds.x, slotBounds.y, slotBounds.width);
            }
        }
    }

    /**
     * Géométrie globale de l'inventaire.
     * Les autres composants n'ont pas besoin de connaître les trois blocs :
     * pour eux, une enveloppe commune suffit.
     */
    public static Rectangle computeInventoryBarBounds(Rectangle fieldBounds, int viewWidth, int viewHeight) {
        Rectangle[] groupBounds = computeInventoryGroupBounds(fieldBounds, viewWidth, viewHeight);
        if (groupBounds.length == 0) {
            return new Rectangle();
        }

        Rectangle union = new Rectangle(groupBounds[0]);
        for (int index = 1; index < groupBounds.length; index++) {
            union = union.union(groupBounds[index]);
        }
        return union;
    }

    /**
     * Expose la géométrie d'un slot précis pour les animations
     * qui doivent converger visuellement vers l'inventaire.
     */
    public static Rectangle computeSlotBounds(int slotIndex, Rectangle fieldBounds, int viewWidth, int viewHeight) {
        if (slotIndex < 0 || slotIndex >= getInventorySlotCount()) {
            return null;
        }

        return computeSlotBounds(slotIndex, computeInventoryGroupBounds(fieldBounds, viewWidth, viewHeight));
    }

    public static int getWoodSlotIndex() {
        return getTotalSeedSlotCount();
    }

    private Rectangle[] getInventoryGroupBounds(Rectangle fieldBounds, int viewWidth, int viewHeight) {
        return computeInventoryGroupBounds(fieldBounds, viewWidth, viewHeight);
    }

    /**
     * Calcule les trois barres sur une seule ligne.
     * On affiche d'abord la zone gauche, puis la zone principale, puis les objets.
     */
    private static Rectangle[] computeInventoryGroupBounds(Rectangle fieldBounds, int viewWidth, int viewHeight) {
        int[] groupSlotCounts = getInventoryGroupSlotCounts();
        int[] groupWidths = new int[groupSlotCounts.length];
        int totalWidth = 0;

        for (int index = 0; index < groupSlotCounts.length; index++) {
            groupWidths[index] = computeGroupWidth(groupSlotCounts[index]);
            totalWidth += groupWidths[index];
        }
        totalWidth += GROUP_GAP * Math.max(0, groupWidths.length - 1);

        int barHeight = SLOT_SIZE + (OUTER_PADDING * 2);
        int startX = (viewWidth - totalWidth) / 2;

        int desiredY = viewHeight - barHeight - 10;
        int minimumClearY = fieldBounds == null ? desiredY : fieldBounds.y + fieldBounds.height + 12;
        int barY = Math.max(desiredY, minimumClearY);
        barY = Math.min(barY, viewHeight - barHeight - 10);

        Rectangle[] groupBounds = new Rectangle[groupWidths.length];
        int currentX = startX;
        for (int index = 0; index < groupWidths.length; index++) {
            groupBounds[index] = new Rectangle(currentX, barY, groupWidths[index], barHeight);
            currentX += groupWidths[index] + GROUP_GAP;
        }

        return groupBounds;
    }

    private Rectangle getSlotBounds(int slotIndex, Rectangle[] groupBounds) {
        return computeSlotBounds(slotIndex, groupBounds);
    }

    private static Rectangle computeSlotBounds(int slotIndex, Rectangle[] groupBounds) {
        if (slotIndex < 0 || slotIndex >= getInventorySlotCount() || groupBounds == null || groupBounds.length == 0) {
            return null;
        }

        int groupIndex = getGroupIndexForSlot(slotIndex);
        int slotIndexInGroup = getSlotIndexInGroup(slotIndex);
        if (groupIndex < 0 || groupIndex >= groupBounds.length || slotIndexInGroup < 0) {
            return null;
        }

        Rectangle groupBoundsForSlot = groupBounds[groupIndex];
        int slotX = groupBoundsForSlot.x + OUTER_PADDING + (slotIndexInGroup * (SLOT_SIZE + SLOT_GAP));
        int slotY = groupBoundsForSlot.y + OUTER_PADDING;
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

        Rectangle[] groupBounds = getInventoryGroupBounds(getFieldBoundsInView(), getWidth(), getHeight());
        for (int slotIndex = 0; slotIndex < getInventorySlotCount(); slotIndex++) {
            Rectangle slotBounds = getSlotBounds(slotIndex, groupBounds);
            if (slotBounds != null && slotBounds.contains(point)) {
                return slotIndex;
            }
        }

        return -1;
    }

    /**
     * Dessine le fond d'un bloc d'inventaire.
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
        Type seedType = getSeedTypeForSlot(slotIndex);
        boolean seedLockedForCurrentZone = seedType != null && hasStock && !isSeedUsableInCurrentZone(seedType);

        Color slotFill = selected
                ? new Color(108, 74, 37, 248)
                : hasStock ? new Color(76, 56, 34, 245) : new Color(50, 39, 28, 220);
        Color slotInner = selected
                ? new Color(166, 122, 60, 190)
                : hasStock ? new Color(109, 80, 49, 180) : new Color(69, 55, 42, 120);
        Color slotBorder = selected
                ? new Color(255, 230, 132)
                : hasStock ? new Color(241, 220, 154) : new Color(126, 106, 81);
        Color quantityFill = seedLockedForCurrentZone
                ? new Color(218, 207, 190)
                : hasStock ? new Color(255, 249, 228) : new Color(185, 175, 161);

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
        int iconArtWidth = seedType != null
                ? ProductPixelArt.getSeedArtWidth(seedType, iconPixelSize)
                : isWoodSlot(slotIndex)
                ? ProductPixelArt.getWoodArtWidth(iconPixelSize)
                : ProductPixelArt.getFacilityArtWidth(facilityType, iconPixelSize);
        int iconArtHeight = seedType != null
                ? ProductPixelArt.getSeedArtHeight(seedType, iconPixelSize)
                : isWoodSlot(slotIndex)
                ? ProductPixelArt.getWoodArtHeight(iconPixelSize)
                : ProductPixelArt.getFacilityArtHeight(facilityType, iconPixelSize);
        int iconX = x + ((size - iconArtWidth) / 2);
        int iconY = y + ((size - iconArtHeight) / 2) + 1;
        if (seedType != null) {
            ProductPixelArt.drawSeed(g2d, seedType, iconX, iconY, iconPixelSize);
        } else if (isWoodSlot(slotIndex)) {
            ProductPixelArt.drawWoodResource(g2d, iconX, iconY, iconPixelSize);
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

        if (seedLockedForCurrentZone) {
            drawLockedSeedVeil(g2d, x, y, size);
        }
    }

    private static int getInventorySlotCount() {
        return getTotalSeedSlotCount() + getResourceGroupSlotCount();
    }

    private static int getTotalSeedSlotCount() {
        return MAIN_ZONE_SEED_ORDER.length + LEFT_ZONE_SEED_ORDER.length;
    }

    private static int getResourceGroupSlotCount() {
        return 1 + INVENTORY_FACILITY_ORDER.length;
    }

    private static int[] getInventoryGroupSlotCounts() {
        return new int[] {
                LEFT_ZONE_SEED_ORDER.length,
                MAIN_ZONE_SEED_ORDER.length,
                getResourceGroupSlotCount()
        };
    }

    private static int computeGroupWidth(int slotCount) {
        return (slotCount * SLOT_SIZE) + ((slotCount - 1) * SLOT_GAP) + (OUTER_PADDING * 2);
    }

    private static int getGroupIndexForSlot(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= getInventorySlotCount()) {
            return -1;
        }
        if (slotIndex < MAIN_ZONE_SEED_ORDER.length) {
            return 1;
        }
        if (slotIndex < getTotalSeedSlotCount()) {
            return 0;
        }
        return 2;
    }

    private static int getSlotIndexInGroup(int slotIndex) {
        int groupIndex = getGroupIndexForSlot(slotIndex);
        if (groupIndex == 0) {
            return slotIndex - MAIN_ZONE_SEED_ORDER.length;
        }
        if (groupIndex == 1) {
            return slotIndex;
        }
        if (groupIndex == 2) {
            return slotIndex - getTotalSeedSlotCount();
        }
        return -1;
    }

    private boolean isSeedSlot(int slotIndex) {
        return slotIndex >= 0 && slotIndex < getTotalSeedSlotCount();
    }

    private boolean isWoodSlot(int slotIndex) {
        return slotIndex == getWoodSlotIndex();
    }

    public Type getSeedTypeForSlot(int slotIndex) {
        if (!isSeedSlot(slotIndex)) {
            return null;
        }
        if (slotIndex < MAIN_ZONE_SEED_ORDER.length) {
            return MAIN_ZONE_SEED_ORDER[slotIndex];
        }
        return LEFT_ZONE_SEED_ORDER[slotIndex - MAIN_ZONE_SEED_ORDER.length];
    }

    public FacilityType getFacilityTypeForSlot(int slotIndex) {
        int facilityIndex = slotIndex - getTotalSeedSlotCount() - 1;
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
            Type seedType = getSeedTypeForSlot(slotIndex);
            return seedType == movementModel.getSelectedSeedType() && isSeedUsableInCurrentZone(seedType);
        }
        if (isWoodSlot(slotIndex)) {
            return false;
        }
        return getFacilityTypeForSlot(slotIndex) == movementModel.getSelectedFacilityType();
    }

    /**
     * Lit directement la quantité depuis le modèle.
     */
    private int getQuantityForSlot(int slotIndex) {
        if (isSeedSlot(slotIndex)) {
            Type seedType = getSeedTypeForSlot(slotIndex);
            return seedType == null ? 0 : inventaire.getQuantiteGraine(seedType);
        }
        if (isWoodSlot(slotIndex)) {
            return inventaire.getQuantiteBois();
        }
        FacilityType facilityType = getFacilityTypeForSlot(slotIndex);
        return facilityType == null ? 0 : inventaire.getQuantiteInstallation(facilityType);
    }

    /**
     * Une graine peut être visible dans l'inventaire sans être utilisable
     * sur le côté du champ où se trouve actuellement le joueur.
     */
    private boolean isSeedUsableInCurrentZone(Type seedType) {
        FieldZone currentFieldZone = getCurrentFieldZone();
        return currentFieldZone == null || seedType.canBePlantedIn(currentFieldZone);
    }

    /**
     * Le contrôleur s'appuie dessus pour refuser le clic
     * sur une graine verrouillée par la zone actuelle.
     */
    public boolean isSeedSelectableInCurrentZone(Type seedType) {
        return seedType != null && isSeedUsableInCurrentZone(seedType);
    }

    private FieldZone getCurrentFieldZone() {
        if (movementModel == null || fieldPanel == null || fieldPanel.getGrilleCulture() == null) {
            return null;
        }

        Unit playerUnit = movementModel.getPlayerUnit();
        if (playerUnit == null) {
            return null;
        }

        Rectangle fieldBounds = getFieldBoundsInView();
        int playerCenterX = fieldBounds.x + (fieldBounds.width / 2) + playerUnit.getX();
        int playerCenterY = fieldBounds.y + (fieldBounds.height / 2) + playerUnit.getY();
        Point playerCenterInField = SwingUtilities.convertPoint(this, playerCenterX, playerCenterY, fieldPanel);
        Point playerCell = fieldPanel.getGridPositionAt(playerCenterInField.x, playerCenterInField.y);
        if (playerCell == null) {
            return null;
        }

        /*
         * Ici on suit le centre du joueur et non la case de gameplay active.
         * Ainsi, le bloc du mauvais côté reste bien verrouillé
         * dès que le personnage a changé de zone, même hors d'une case plantable valide.
         */
        return fieldPanel.getGrilleCulture().getFieldZoneAt(playerCell.x, playerCell.y);
    }

    /**
     * Le voile signale qu'une graine est bien possédée, mais inutilisable
     * sur le côté actuel du champ.
     */
    private void drawLockedSeedVeil(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(LOCKED_SEED_OVERLAY);
        g2d.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);

        g2d.setColor(LOCKED_SEED_BORDER);
        g2d.drawLine(x + 8, y + 8, x + size - 9, y + size - 9);
        g2d.drawLine(x + 8, y + size - 9, x + size - 9, y + 8);
    }
}
