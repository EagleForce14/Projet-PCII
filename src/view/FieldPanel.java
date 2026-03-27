package view;

import model.culture.Culture;
import model.culture.GrilleCulture;
import model.culture.GrilleCulture.CellSide;
import model.culture.Stade;
import model.movement.Unit;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Objects;

/**
 * Panneau d'affichage du champ, compose d'une grille d'images.
 */
public class FieldPanel extends JPanel {
    // Taille préférée du panneau qui contient le champ.
    private static final int PREF_WIDTH = 1010;
    private static final int PREF_HEIGHT = 580;

    // Marge intérieure laissée autour de la grille pour éviter qu'elle colle au bord du panneau.
    private static final int INNER_PADDING = 24;

    // Petit décalage vertical pour positionner visuellement le champ un peu plus bas que le centre.
    // On garde toujours un petit biais vers le bas, mais moins prononce qu'avant,
    // pour liberer davantage d'espace a la hotbar placee en bas de l'ecran.
    private static final int VERTICAL_OFFSET = 18;

    // Couleurs utilisées pour surligner la case actuellement occupée par le joueur.
    private static final Color HIGHLIGHT_FILL = new Color(255, 255, 120, 90);
    private static final Color HIGHLIGHT_BORDER = new Color(255, 215, 0, 210);

    // Couleurs de l'effet visuel affiché après un arrosage réussi.
    private static final Color WATERED_FILL = new Color(70, 170, 255);
    private static final Color WATERED_BORDER = new Color(220, 245, 255);

    // Couleurs du survol dédié au placement des clôtures.
    private static final Color FENCE_PREVIEW_FILL = new Color(255, 232, 151, 130);
    private static final Color FENCE_PREVIEW_BORDER = new Color(255, 201, 74, 230);

    // Palette bois utilisée pour les clôtures rendues directement sur le champ.
    private static final Color FENCE_OUTLINE = new Color(27, 20, 15);
    private static final Color FENCE_CAP_DARK = new Color(70, 54, 43);
    private static final Color FENCE_CAP_LIGHT = new Color(94, 72, 58);
    private static final Color FENCE_WOOD_LIGHT = new Color(216, 177, 118);
    private static final Color FENCE_WOOD_MID = new Color(173, 126, 77);
    private static final Color FENCE_WOOD_DARK = new Color(112, 73, 42);
    private static final Color FENCE_SLAT_FILL = new Color(240, 132, 42);
    private static final Color FENCE_SLAT_LIGHT = new Color(255, 170, 72);
    private static final Color FENCE_SLAT_DARK = new Color(174, 78, 18);
    private static final Color FENCE_WOOD_SHADOW = new Color(73, 46, 25, 95);

    // Vitesse de pulsation de l'animation d'arrosage.
    // Plus la valeur est grande, plus le halo "respire" vite.
    private static final double WATER_PULSE_SPEED = 0.008;

    // On lit directement l'état réel des cultures.
    private final GrilleCulture grilleCulture;

    // Image de fond d'une case de terre.
    private final Image tileImage;

    // Images associées aux différents stades visuels d'une culture.
    private final Image jeunePousseImage;
    private final Image croissanceInterImage;
    private final Image maturiteImage;
    private final Image fletrieImage;

    // Coordonnées de la case actuellement surlignée.
    // Cette valeur vaut null quand aucune case n'est activable.
    private Point highlightedCell;

    // Le preview mémorise à la fois la case survolée et le bord réellement visé.
    private FencePreview fencePreview;

    /**
     * Petit objet immuable qui représente un placement de clôture "en attente".
     * On le garde volontairement minuscule pour pouvoir le recalculer souvent au survol.
     */
    public static final class FencePreview {
        private final Point cell;
        private final CellSide side;

        public FencePreview(Point cell, CellSide side) {
            this.cell = cell == null ? null : new Point(cell);
            this.side = side;
        }

        public Point getCell() {
            return cell == null ? null : new Point(cell);
        }

        public CellSide getSide() {
            return side;
        }
    }

    /**
     * Initialise le champ et charge l'image d'une parcelle.
     */
    public FieldPanel(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
        this.tileImage = ImageLoader.load("/assets/Terre.png");
        this.jeunePousseImage = ImageLoader.load("/assets/jeune_pousse.png");
        this.croissanceInterImage = ImageLoader.load("/assets/croissance_inter.png");
        this.maturiteImage = ImageLoader.load("/assets/maturite.png");
        this.fletrieImage = ImageLoader.load("/assets/fletrie.png");
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        // Le panneau reste transparent hors de la grille pour laisser voir le fond global.
        setOpaque(false);
    }

    // Les getters pour la l'attribut grilleCulture, et pour la largeur et la hauteur de la grille
    public GrilleCulture getGrilleCulture() {
        return grilleCulture;
    }

    public int getColumnCount() {
        return grilleCulture.getLargeur();
    }

    public int getRowCount() {
        return grilleCulture.getHauteur();
    }

    /**
     * Calcule une position initiale située juste en dehors du champ, près de son
     * coin haut-gauche, dans le repère utilisé par les unités.
     * Le repère des unités est centré sur le champ: la valeur renvoyée représente
     * donc un décalage par rapport au centre visuel de la grille.
     */
    public Point getInitialPlayerOffset() {
        Rectangle fieldBounds = getPreferredFieldBounds();
        int margin = Math.max(12, Unit.SIZE / 2);
        int spawnX = -(fieldBounds.width / 2) - margin;
        int spawnY = -(fieldBounds.height / 2) - margin;
        return new Point(spawnX, spawnY);
    }

    /**
     * Met a jour la case surlignée. La surbrillance est portée par le FieldPanel
     * pour rester parfaitement alignée avec les images du champ.
     */
    public void setHighlightedCell(Point highlightedCell) {
        if (Objects.equals(this.highlightedCell, highlightedCell)) {
            return;
        }

        this.highlightedCell = highlightedCell == null ? null : new Point(highlightedCell);
        repaint();
    }

    /**
     * Le contrôleur lit ce preview juste avant de valider un clic.
     * On renvoie une copie pour rester cohérents avec les autres getters du panneau.
     */
    public FencePreview getFencePreview() {
        if (fencePreview == null) {
            return null;
        }

        return new FencePreview(fencePreview.getCell(), fencePreview.getSide());
    }

    /**
     * Met à jour le survol de clôture.
     * Rien n'est affiché si la souris n'est sur aucune case de bord libre.
     */
    public void setFencePreview(FencePreview fencePreview) {
        if (sameFencePreview(this.fencePreview, fencePreview)) {
            return;
        }

        this.fencePreview = fencePreview == null
                ? null
                : new FencePreview(fencePreview.getCell(), fencePreview.getSide());
        repaint();
    }

    public void clearFencePreview() {
        setFencePreview(null);
    }

    /**
     * Traduit la position réelle de la souris en preview de clôture.
     * Sur une case d'angle, on choisit simplement le bord extérieur libre le plus proche du curseur.
     */
    public FencePreview getFencePreviewAt(Point pointInFieldPanel) {
        if (pointInFieldPanel == null) {
            return null;
        }

        Point cell = getGridPositionAt(pointInFieldPanel.x, pointInFieldPanel.y);
        if (cell == null) {
            return null;
        }

        CellSide side = findClosestPlaceableFenceSide(cell, pointInFieldPanel.x, pointInFieldPanel.y);
        if (side == null) {
            return null;
        }

        return new FencePreview(cell, side);
    }

    /**
     * Retourne la case réellement occupée par un rectangle d'unité.
     * Une case n'est retenue que si la représentation visuelle complète du joueur
     * est entièrement contenue dans cette case. Si le joueur chevauche deux cases,
     * ou touche une frontière, aucune case n'est considérée comme occupée.
     */
    public Point getFullyOccupiedCell(Rectangle unitBounds) {
        if (unitBounds == null) {
            return null;
        }

        Point topLeftCell = getGridPositionAt(unitBounds.x, unitBounds.y);
        Point bottomRightCell = getGridPositionAt(unitBounds.x + unitBounds.width - 1, unitBounds.y + unitBounds.height - 1);
        if (topLeftCell == null || !topLeftCell.equals(bottomRightCell)) {
            return null;
        }

        Rectangle cellBounds = getCellBounds(topLeftCell.x, topLeftCell.y);
        if (cellBounds == null || !cellBounds.contains(unitBounds)) {
            return null;
        }

        return topLeftCell;
    }

    /**
     * Retourne uniquement le rectangle occupé par la grille d'images du champ
     * a l'intérieur du panneau.
     */
    public Rectangle getFieldBounds() {
        return computeFieldBounds(getWidth(), getHeight());
    }

    private Rectangle getPreferredFieldBounds() {
        Dimension preferredSize = getPreferredSize();
        return computeFieldBounds(preferredSize.width, preferredSize.height);
    }

    private Rectangle computeFieldBounds(int panelWidth, int panelHeight) {
        int columns = getColumnCount();
        int rows = getRowCount();
        // On réduit la marge interne pour laisser la grille occuper davantage d'espace visible.
        int availableWidth = Math.max(0, panelWidth - 2 * INNER_PADDING);
        int availableHeight = Math.max(0, panelHeight - 2 * INNER_PADDING);
        int tileW = availableWidth / columns;
        int tileH = availableHeight / rows;
        // Une case = une cellule logique du modèle, donc on conserve des tuiles carrées.
        int tileSize = Math.min(tileW, tileH);
        int gridW = tileSize * columns;
        int gridH = tileSize * rows;
        int startX = (panelWidth - gridW) / 2;
        // On décale légèrement le champ vers le bas tout en restant entièrement visible.
        int startY = Math.min((panelHeight - gridH), ((panelHeight - gridH) / 2) + VERTICAL_OFFSET);
        return new Rectangle(startX, startY, gridW, gridH);
    }

    /**
     * Retourne le case du champ affichée à l'écran correspondant à une case logique de la grille.
     */
    public Rectangle getCellBounds(int gridX, int gridY) {
        if (gridX < 0 || gridX >= getColumnCount() || gridY < 0 || gridY >= getRowCount()) {
            return null;
        }

        // Ce rectangle sert de point d'ancrage visuel pour une case précise du modèle.
        Rectangle fieldBounds = getFieldBounds();
        int tileSize = fieldBounds.width / getColumnCount();
        int x = fieldBounds.x + (gridX * tileSize);
        int y = fieldBounds.y + (gridY * tileSize);
        return new Rectangle(x, y, tileSize, tileSize);
    }

    /**
     * Convertit des coordonnees écran du panneau vers une case logique du modele.
     */
    public Point getGridPositionAt(int pixelX, int pixelY) {
        Rectangle fieldBounds = getFieldBounds();
        if (!fieldBounds.contains(pixelX, pixelY)) {
            return null;
        }

        // Cette conversion permettra ensuite d'associer un clic utilisateur a une case du modèle.
        int tileSize = fieldBounds.width / getColumnCount();
        int gridX = (pixelX - fieldBounds.x) / tileSize;
        int gridY = (pixelY - fieldBounds.y) / tileSize;

        if (gridX < 0 || gridX >= getColumnCount() || gridY < 0 || gridY >= getRowCount()) {
            return null;
        }

        return new Point(gridX, gridY);
    }

    /**
     * Convertit le stade du modèle en image d'affichage.
     */
    private Image getCultureImage(Culture culture) {
        if (culture == null) {
            return null;
        }

        Stade stade = culture.getStadeCroissance();
        if (stade == Stade.JEUNE_POUSSE) {
            return jeunePousseImage;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return croissanceInterImage;
        }
        if (stade == Stade.MATURE) {
            return maturiteImage;
        }
        if (stade == Stade.FLETRIE) {
            return fletrieImage;
        }
        return null;
    }

    /**
     * L'animation d'arrosage ne doit exister que juste après un arrosage réussi.
     * On la cale donc sur l'état métier déjà présent dans le modèle:
     * la culture doit être encore au stade intermédiaire et marquée comme arrosée.
     * Dès que le stade change, cette méthode renvoie false et l'effet disparaît tout seul.
     */
    private boolean shouldAnimateWateredCell(Culture culture) {
        return culture != null
                && culture.getStadeCroissance() == Stade.INTERMEDIAIRE
                && culture.isArrosee();
    }

    /**
     * Dessine un halo bleu pulsé sur la case.
     * On fait simplement varier l'opacité avec le temps courant.
     */
    private void drawWateringAnimation(Graphics2D g2, int x, int y, int tileSize) {
        // La sinusoïde produit un va-et-vient doux entre 0 et 1.
        double pulse = (Math.sin(System.currentTimeMillis() * WATER_PULSE_SPEED) + 1.0) / 2.0;

        // On garde des valeurs modestes pour que la plante reste visible sous l'effet.
        int fillAlpha = 35 + (int) Math.round(pulse * 55);
        int borderAlpha = 110 + (int) Math.round(pulse * 100);

        // On dessine le halo légèrement à l'intérieur de la case pour conserver un petit cadre.
        int inset = Math.max(4, tileSize / 10);

        // Taille réelle de la zone animée une fois la marge intérieure retirée.
        int size = tileSize - (2 * inset);

        // Rayon des coins arrondis du halo.
        int arc = Math.max(10, tileSize / 4);

        g2.setColor(new Color(WATERED_FILL.getRed(), WATERED_FILL.getGreen(), WATERED_FILL.getBlue(), fillAlpha));
        g2.fillRoundRect(x + inset, y + inset, size, size, arc, arc);

        g2.setColor(new Color(WATERED_BORDER.getRed(), WATERED_BORDER.getGreen(), WATERED_BORDER.getBlue(), borderAlpha));
        g2.drawRoundRect(x + inset, y + inset, size - 1, size - 1, arc, arc);
        g2.drawRoundRect(x + inset + 2, y + inset + 2, size - 5, size - 5, arc, arc);
    }

    private boolean sameFencePreview(FencePreview first, FencePreview second) {
        if (first == second) {
            return true;
        }
        if (first == null || second == null) {
            return false;
        }

        return Objects.equals(first.getCell(), second.getCell()) && first.getSide() == second.getSide();
    }

    /**
     * On ne retient que les bords vraiment plaçables.
     * Cela permet, par exemple, de garder un coin utilisable même si un de ses deux côtés a déjà reçu une clôture.
     */
    private CellSide findClosestPlaceableFenceSide(Point cell, int pixelX, int pixelY) {
        Rectangle cellBounds = getCellBounds(cell.x, cell.y);
        if (cellBounds == null) {
            return null;
        }

        CellSide bestSide = null;
        int bestDistance = Integer.MAX_VALUE;
        for (CellSide side : CellSide.values()) {
            if (!grilleCulture.canPlaceFence(cell.x, cell.y, side)) {
                continue;
            }

            int distance = distanceToSide(cellBounds, side, pixelX, pixelY);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestSide = side;
            }
        }

        return bestSide;
    }

    private int distanceToSide(Rectangle cellBounds, CellSide side, int pixelX, int pixelY) {
        switch (side) {
            case TOP:
                return Math.abs(pixelY - cellBounds.y);
            case RIGHT:
                return Math.abs(pixelX - (cellBounds.x + cellBounds.width));
            case BOTTOM:
                return Math.abs(pixelY - (cellBounds.y + cellBounds.height));
            case LEFT:
                return Math.abs(pixelX - cellBounds.x);
            default:
                return Integer.MAX_VALUE;
        }
    }

    /**
     * Dessine l'image dans la case sans la déformer.
     */
    private void drawCultureImage(Graphics2D g2, Image cultureImage, int x, int y, int tileSize) {
        int imageWidth = cultureImage.getWidth(this);
        int imageHeight = cultureImage.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        // On garde un petit bord vide pour que la plante ne colle pas au contour de la case.
        int padding = Math.max(4, tileSize / 10);
        int availableWidth = tileSize - (2 * padding);
        int availableHeight = tileSize - (2 * padding);

        // On applique un facteur d'échelle pour préserver les proportions d'origine.
        double scale = Math.min(
                (double) availableWidth / imageWidth,
                (double) availableHeight / imageHeight
        );

        int drawWidth = (int) Math.round(imageWidth * scale);
        int drawHeight = (int) Math.round(imageHeight * scale);

        // On s'assure que l'image reste centrée dans la case même si elle n'occupe pas tout l'espace disponible.
        int drawX = x + ((tileSize - drawWidth) / 2);
        int drawY = y + ((tileSize - drawHeight) / 2);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(cultureImage, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * Le survol de pose doit épouser le bord visé, pas la case entière.
     * On dessine donc une bande fine exactement à l'endroit où la clôture pourrait arriver.
     */
    private void drawFencePreview(Graphics2D g2, Rectangle cellBounds, CellSide side) {
        int stripThickness = Math.max(6, cellBounds.width / 8);
        int inset = Math.max(4, cellBounds.width / 10);

        g2.setColor(FENCE_PREVIEW_FILL);
        switch (side) {
            case TOP:
                g2.fillRoundRect(cellBounds.x + inset, cellBounds.y, cellBounds.width - (2 * inset), stripThickness, 8, 8);
                break;
            case RIGHT:
                g2.fillRoundRect(cellBounds.x + cellBounds.width - stripThickness, cellBounds.y + inset,
                        stripThickness, cellBounds.height - (2 * inset), 8, 8);
                break;
            case BOTTOM:
                g2.fillRoundRect(cellBounds.x + inset, cellBounds.y + cellBounds.height - stripThickness,
                        cellBounds.width - (2 * inset), stripThickness, 8, 8);
                break;
            case LEFT:
                g2.fillRoundRect(cellBounds.x, cellBounds.y + inset, stripThickness, cellBounds.height - (2 * inset), 8, 8);
                break;
            default:
                return;
        }

        g2.setColor(FENCE_PREVIEW_BORDER);
        switch (side) {
            case TOP:
                g2.drawLine(cellBounds.x + inset, cellBounds.y + stripThickness - 1,
                        cellBounds.x + cellBounds.width - inset, cellBounds.y + stripThickness - 1);
                break;
            case RIGHT:
                g2.drawLine(cellBounds.x + cellBounds.width - stripThickness, cellBounds.y + inset,
                        cellBounds.x + cellBounds.width - stripThickness, cellBounds.y + cellBounds.height - inset);
                break;
            case BOTTOM:
                g2.drawLine(cellBounds.x + inset, cellBounds.y + cellBounds.height - stripThickness,
                        cellBounds.x + cellBounds.width - inset, cellBounds.y + cellBounds.height - stripThickness);
                break;
            case LEFT:
                g2.drawLine(cellBounds.x + stripThickness - 1, cellBounds.y + inset,
                        cellBounds.x + stripThickness - 1, cellBounds.y + cellBounds.height - inset);
                break;
            default:
                break;
        }
    }

    private void drawFence(Graphics2D g2, int cellX, int cellY, Rectangle cellBounds, CellSide side) {
        switch (side) {
            case TOP:
                drawHorizontalFence(g2, cellX, cellY, cellBounds, false);
                break;
            case RIGHT:
                drawVerticalFence(g2, cellX, cellY, cellBounds, true);
                break;
            case BOTTOM:
                drawHorizontalFence(g2, cellX, cellY, cellBounds, true);
                break;
            case LEFT:
                drawVerticalFence(g2, cellX, cellY, cellBounds, false);
                break;
            default:
                break;
        }
    }

    /**
     * Cette version colle vraiment au bord de la case.
     * L'ombre côté intérieur aide à comprendre que la clôture est debout,
     * pas couchée sur la terre.
     */
    private void drawHorizontalFence(Graphics2D g2, int cellX, int cellY, Rectangle cellBounds, boolean bottomSide) {
        int tileSize = cellBounds.width;
        CellSide side = bottomSide ? CellSide.BOTTOM : CellSide.TOP;
        boolean connectedLeft = grilleCulture.hasFence(cellX - 1, cellY, side);
        boolean connectedRight = grilleCulture.hasFence(cellX + 1, cellY, side);

        int outsideDepth = Math.max(2, tileSize / 18);
        int insideDepth = Math.max(6, tileSize / 9);
        int bandHeight = outsideDepth + insideDepth;
        int bandY = bottomSide
                ? cellBounds.y + cellBounds.height - insideDepth
                : cellBounds.y - outsideDepth;

        int slatThickness = Math.max(2, bandHeight / 3);
        int slatGap = Math.max(1, tileSize / 20);
        int postWidth = Math.max(7, tileSize / 8);
        int postHeight = bandHeight + Math.max(4, tileSize / 12);
        int leftPostX = cellBounds.x - (postWidth / 2);
        int rightPostX = cellBounds.x + cellBounds.width - (postWidth / 2);
        int postY = bandY - ((postHeight - bandHeight) / 2);

        int railStartX = connectedLeft ? cellBounds.x : leftPostX + postWidth - 1;
        int railEndX = connectedRight ? cellBounds.x + cellBounds.width : rightPostX + 1;
        int railWidth = Math.max(2, railEndX - railStartX);
        int firstSlatY = bandY + 1;
        int secondSlatY = bandY + bandHeight - slatThickness - 1;

        drawHorizontalSlat(g2, railStartX, firstSlatY, railWidth, slatThickness, !bottomSide);
        if (secondSlatY - firstSlatY >= slatThickness + slatGap) {
            drawHorizontalSlat(g2, railStartX, secondSlatY, railWidth, slatThickness, !bottomSide);
        }

        if (!connectedLeft) {
            drawHorizontalPost(g2, leftPostX, postY, postWidth, postHeight, !bottomSide);
        }
        if (!connectedRight) {
            drawHorizontalPost(g2, rightPostX, postY, postWidth, postHeight, !bottomSide);
        }

        g2.setColor(FENCE_WOOD_SHADOW);
        int shadowY = bottomSide ? bandY - 1 : bandY + bandHeight;
        g2.fillRect(cellBounds.x + 2, shadowY, Math.max(2, cellBounds.width - 4), Math.max(2, tileSize / 18));
    }

    private void drawVerticalFence(Graphics2D g2, int cellX, int cellY, Rectangle cellBounds, boolean rightSide) {
        int tileSize = cellBounds.width;
        CellSide side = rightSide ? CellSide.RIGHT : CellSide.LEFT;
        boolean connectedTop = grilleCulture.hasFence(cellX, cellY - 1, side);
        boolean connectedBottom = grilleCulture.hasFence(cellX, cellY + 1, side);

        int outsideDepth = Math.max(2, tileSize / 18);
        int insideDepth = Math.max(6, tileSize / 9);
        int bandWidth = outsideDepth + insideDepth;
        int bandX = rightSide
                ? cellBounds.x + cellBounds.width - insideDepth
                : cellBounds.x - outsideDepth;

        int slatThickness = Math.max(2, bandWidth / 3);
        int slatGap = Math.max(1, tileSize / 20);
        int postHeight = Math.max(7, tileSize / 8);
        int postWidth = bandWidth + Math.max(4, tileSize / 12);
        int topPostY = cellBounds.y - (postHeight / 2);
        int bottomPostY = cellBounds.y + cellBounds.height - (postHeight / 2);
        int postX = bandX - ((postWidth - bandWidth) / 2);

        int railStartY = connectedTop ? cellBounds.y : topPostY + postHeight - 1;
        int railEndY = connectedBottom ? cellBounds.y + cellBounds.height : bottomPostY + 1;
        int railHeight = Math.max(2, railEndY - railStartY);
        int firstSlatX = bandX + 1;
        int secondSlatX = bandX + bandWidth - slatThickness - 1;

        drawVerticalSlat(g2, firstSlatX, railStartY, slatThickness, railHeight, rightSide);
        if (secondSlatX - firstSlatX >= slatThickness + slatGap) {
            drawVerticalSlat(g2, secondSlatX, railStartY, slatThickness, railHeight, rightSide);
        }

        if (!connectedTop) {
            drawVerticalPost(g2, postX, topPostY, postWidth, postHeight, rightSide);
        }
        if (!connectedBottom) {
            drawVerticalPost(g2, postX, bottomPostY, postWidth, postHeight, rightSide);
        }

        g2.setColor(FENCE_WOOD_SHADOW);
        int shadowX = rightSide ? bandX - 1 : bandX + bandWidth;
        g2.fillRect(shadowX, cellBounds.y + 2, Math.max(2, tileSize / 18), Math.max(2, cellBounds.height - 4));
    }

    private void drawHorizontalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        g2.setColor(FENCE_WOOD_MID);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2));

        g2.setColor(FENCE_WOOD_LIGHT);
        g2.fillRect(x + 1, y + 1, Math.max(2, width / 3), Math.max(1, height - 2));

        g2.setColor(FENCE_WOOD_DARK);
        g2.fillRect(x + width - 2, y + 1, 1, Math.max(1, height - 2));

        int capY = outerOnTop ? y : y + height - Math.max(3, height / 3);
        int capHeight = Math.max(3, height / 3);
        g2.setColor(FENCE_CAP_DARK);
        g2.fillRect(x + 1, capY, Math.max(1, width - 2), capHeight);

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(x + 2, capY + 1, Math.max(1, width - 4), 1);
    }

    private void drawVerticalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        g2.setColor(FENCE_WOOD_MID);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2));

        g2.setColor(FENCE_WOOD_LIGHT);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(2, height / 3));

        g2.setColor(FENCE_WOOD_DARK);
        g2.fillRect(x + 1, y + height - 2, Math.max(1, width - 2), 1);

        int capX = outerOnRight ? x + width - Math.max(3, width / 3) : x;
        int capWidth = Math.max(3, width / 3);
        g2.setColor(FENCE_CAP_DARK);
        g2.fillRect(capX, y + 1, capWidth, Math.max(1, height - 2));

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(capX + (outerOnRight ? 0 : 1), y + 2, 1, Math.max(1, height - 4));
    }

    private void drawHorizontalSlat(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        // Les lattes sont volontairement plus orangées que les poteaux
        // pour se détacher immédiatement de la terre du champ.
        g2.setColor(FENCE_SLAT_FILL);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2));

        g2.setColor(outerOnTop ? FENCE_SLAT_LIGHT : FENCE_SLAT_DARK);
        g2.fillRect(x + 1, outerOnTop ? y + 1 : y + height - 2, Math.max(1, width - 2), 1);

        if (height >= 4) {
            g2.setColor(FENCE_SLAT_DARK);
            g2.fillRect(x + Math.max(2, width / 3), y + 1, 1, Math.max(1, height - 2));
        }
    }

    private void drawVerticalSlat(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        g2.setColor(FENCE_SLAT_FILL);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2));

        g2.setColor(outerOnRight ? FENCE_SLAT_DARK : FENCE_SLAT_LIGHT);
        g2.fillRect(outerOnRight ? x + width - 2 : x + 1, y + 1, 1, Math.max(1, height - 2));

        if (width >= 4) {
            g2.setColor(FENCE_SLAT_DARK);
            g2.fillRect(x + 1, y + Math.max(2, height / 3), Math.max(1, width - 2), 1);
        }
    }

    /**
     * Dessine la grille (avec les images).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Rectangle total occupé par la grille dans le panneau.
        Rectangle fieldBounds = getFieldBounds();

        // Taille d'une case carrée à l'écran.
        int tileSize = fieldBounds.width / getColumnCount();

        // Point d'origine de la grille dans le panneau.
        int startX = fieldBounds.x;
        int startY = fieldBounds.y;

        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                int x = startX + c * tileSize;
                int y = startY + r * tileSize;
                if (tileImage != null) {
                    g2.drawImage(tileImage, x, y, tileSize, tileSize, this);
                } else {
                    g2.setColor(new Color(200, 190, 170));
                    g2.fillRect(x, y, tileSize, tileSize);
                    g2.setColor(new Color(160, 150, 130));
                    g2.drawRect(x, y, tileSize, tileSize);
                }

                Culture culture = grilleCulture.getCulture(c, r);
                Image cultureImage = getCultureImage(culture);
                if (cultureImage != null) {
                    drawCultureImage(g2, cultureImage, x, y, tileSize);
                }

                // Si la culture vient d'être arrosée, on ajoute un halo pulsé sur la case.
                // L'effet persiste tant que l'état du modèle reste "intermédiaire + arrosée".
                if (shouldAnimateWateredCell(culture)) {
                    drawWateringAnimation(g2, x, y, tileSize);
                }

                // La case active est surlignée uniquement quand le rectangle du joueur
                // est entièrement contenu dans cette case logique.
                if (highlightedCell != null && highlightedCell.x == c && highlightedCell.y == r) {
                    g2.setColor(HIGHLIGHT_FILL);
                    g2.fillRect(x, y, tileSize, tileSize);
                    g2.setColor(HIGHLIGHT_BORDER);
                    g2.drawRect(x, y, tileSize - 1, tileSize - 1);
                    g2.drawRect(x + 1, y + 1, tileSize - 3, tileSize - 3);
                }
            }
        }

        // On dessine les clôtures après le contenu des cases pour qu'elles restent bien lisibles.
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnCount(); c++) {
                Rectangle cellBounds = getCellBounds(c, r);
                if (cellBounds == null) {
                    continue;
                }

                for (CellSide side : CellSide.values()) {
                    if (grilleCulture.hasFence(c, r, side)) {
                        drawFence(g2, c, r, cellBounds, side);
                    }
                }
            }
        }

        if (fencePreview != null) {
            Point previewCell = fencePreview.getCell();
            Rectangle previewBounds = previewCell == null ? null : getCellBounds(previewCell.x, previewCell.y);
            if (previewBounds != null) {
                drawFencePreview(g2, previewBounds, fencePreview.getSide());
            }
        }

        g2.dispose();
    }
}
