package view;

import model.culture.Culture;
import model.culture.CellSide;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.environment.FieldObstacleMap;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import model.movement.Barn;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
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
    // Taille préférée alignée sur la zone de jeu principale.
    // Le panneau peut s'étirer ensuite, mais ces valeurs servent de base cohérente
    // pour tous les calculs effectués avant l'affichage réel.
    private static final int PREF_WIDTH = 1180;
    private static final int PREF_HEIGHT = 850;

    // La taille d'une case
    private static final int PIXEL_ART_TILE_SIZE = 16;

    // Couleurs utilisées pour surligner la case actuellement occupée par le joueur.
    private static final Color HIGHLIGHT_FILL = new Color(255, 255, 120, 90);
    private static final Color HIGHLIGHT_BORDER = new Color(255, 215, 0, 210);

    // Couleurs de l'effet visuel affiché après un arrosage réussi.
    private static final Color WATERED_FILL = new Color(70, 170, 255);
    private static final Color WATERED_BORDER = new Color(220, 245, 255);

    // Couleurs utilisées pour visualiser la zone influencée par le compost.
    private static final Color COMPOST_RANGE_FILL = new Color(145, 214, 98, 70);
    private static final Color COMPOST_RANGE_BORDER = new Color(228, 255, 177, 170);

    // Couleurs du survol dédié au placement des clôtures.
    private static final Color FENCE_PREVIEW_FILL = new Color(255, 232, 151, 130);
    private static final Color FENCE_PREVIEW_BORDER = new Color(255, 201, 74, 230);
    private static final Color FENCE_PREVIEW_SOFT_LIGHT = new Color(255, 255, 214, 120);

    // Couleur de secours si une tuile de terrain ne peut pas être chargée.
    private static final Color DEFAULT_GRASS_FILL = new Color(92, 167, 74);

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
    private final TreeManager treeManager;
    private FieldObstacleMap fieldObstacleMap;

    // Plusieurs variantes évitent un motif trop répétitif.
    private final Image[] grassTileImages;
    private final Image[] tilledTileImages;
    private final Image[] pathTileImages;
    private final Image[] riverTileImages;
    private final Image marshTileImage;
    private final Image marshCenterTileImage;
    private final Image marshLeftEdgeTileImage;
    private final Image wetSoilTileImage;
    private final Image decorativeRiverEntryTileImage;
    private final Image decorativeRiverContinuationTileImage;

    // Images associées aux différents stades visuels d'une culture.
    private final Image jeunePousseImage;
    private final Image croissanceInterImage;
    private final Image maturiteImage;
    private final Image fletrieImage;
    private final Image compostImage;

    // Coordonnées de la case actuellement surlignée.
    // Cette valeur vaut null quand aucune case n'est activable.
    private Point highlightedCell;

    // Le preview mémorise à la fois la case survolée et le bord réellement visé.
    private FencePreview fencePreview;

    /*
     * La rivière se pose à la souris sur une case entière.
     * On garde donc un petit état de preview séparé :
     * - quelle case est visée,
     * - et si cette pose est réellement valide.
     */
    private Point riverPreviewCell;
    private boolean riverPreviewValid;

    // Cet indicateur dit seulement si la zone du compost doit être montrée ou non.
    // Les cases exactes sont relues directement depuis la grille au moment du dessin.
    private boolean compostInfluenceVisible;

    /**
     * Initialise la carte.
     */
    public FieldPanel(GrilleCulture grilleCulture, TreeManager treeManager) {
        this.grilleCulture = grilleCulture;
        this.treeManager = treeManager;
        this.grassTileImages = TerrainTileFactory.createGrassTiles(PIXEL_ART_TILE_SIZE);
        this.tilledTileImages = TerrainTileFactory.createSoilTiles(PIXEL_ART_TILE_SIZE);
        this.pathTileImages = TerrainTileFactory.createStoneWithGrass(PIXEL_ART_TILE_SIZE);
        this.riverTileImages = TerrainTileFactory.createRiverTiles(PIXEL_ART_TILE_SIZE);
        this.marshTileImage = ImageLoader.load("/assets/marecages.png");
        this.marshCenterTileImage = ImageLoader.load("/assets/marecagesCenter.png");
        this.marshLeftEdgeTileImage = ImageLoader.load("/assets/marecagesGauche.png");
        this.wetSoilTileImage = ImageLoader.load("/assets/TerreMouillee.png");
        this.decorativeRiverEntryTileImage = ImageLoader.load("/assets/entreeRiviere.png");
        this.decorativeRiverContinuationTileImage = ImageLoader.load("/assets/river2.png");
        this.jeunePousseImage = ImageLoader.load("/assets/jeune_pousse.png");
        this.croissanceInterImage = ImageLoader.load("/assets/croissance_inter.png");
        this.maturiteImage = ImageLoader.load("/assets/maturite.png");
        this.fletrieImage = ImageLoader.load("/assets/fletrie.png");
        this.compostImage = ImageLoader.load("/assets/Compost.png");
        this.compostInfluenceVisible = false;
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        // Le panneau reste transparent hors de la grille pour laisser voir le fond global.
        setOpaque(false);
    }

    public void setFieldObstacleMap(FieldObstacleMap fieldObstacleMap) {
        this.fieldObstacleMap = fieldObstacleMap;
    }

    // Les getters pour l'attribut grilleCulture, et pour la largeur et la hauteur de la grille
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
     * Place le joueur près du centre de la map.
     * C'est plus naturel qu'un départ très excentré et cela évite de "coller"
     * le joueur à un bord dès l'ouverture de la partie.
     */
    public Point getInitialPlayerOffset() {
        Rectangle fieldBounds = getPreferredFieldBounds();
        return getLogicalCellCenter(getColumnCount() / 2, getRowCount() / 2, fieldBounds);
    }

    /**
     * Met à jour la case surlignée. La surbrillance est portée par le FieldPanel
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
     * Le contrôleur prépare ici la preview de rivière.
     * On stocke à la fois la case et sa validité
     * pour pouvoir expliquer visuellement "ici oui" ou "ici non"
     * sans recalculer la règle au moment du paint.
     */
    public void setRiverPreview(Point riverPreviewCell, boolean valid) {
        Point nextCell = riverPreviewCell == null ? null : new Point(riverPreviewCell);
        if (Objects.equals(this.riverPreviewCell, nextCell) && riverPreviewValid == valid) {
            return;
        }

        this.riverPreviewCell = nextCell;
        this.riverPreviewValid = riverPreviewCell != null && valid;
        repaint();
    }

    public void clearRiverPreview() {
        setRiverPreview(null, false);
    }

    /**
     * L'overlay de compost reste volontairement binaire :
     * soit on montre les zones boostées par les composts posés,
     * soit on les cache toutes d'un coup.
     */
    public void toggleCompostInfluenceHighlight() {
        compostInfluenceVisible = !compostInfluenceVisible;
        repaint();
    }

    public void clearCompostInfluenceHighlight() {
        if (!compostInfluenceVisible) {
            return;
        }
        compostInfluenceVisible = false;
        repaint();
    }

    /**
     * Traduit la position réelle de la souris en preview de clôture.
     * Sur une case d'angle, on choisit simplement le bord libre le plus proche du curseur.
     */
    public FencePreview getFencePreviewAt(Point pointInFieldPanel) {
        if (pointInFieldPanel == null) {
            return null;
        }

        Point cell = getGridPositionAt(pointInFieldPanel.x, pointInFieldPanel.y);
        if (cell == null || !isFarmableCell(cell)) {
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

    /**
     * Expose le rectangle écran de la grange.
     * La vue d'environnement et le contrôleur s'en servent pour parler
     * exactement du même objet visuel sans recalculer chacun de leur côté.
     */
    public Rectangle getBarnScreenBounds() {
        Rectangle fieldBounds = getFieldBounds();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        int drawX = centerX + Barn.X;
        int drawY = centerY + Barn.Y;
        return new Rectangle(drawX, drawY, Barn.WIDTH, Barn.HEIGHT);
    }

    /**
     * Expose le repère logique complet du champ.
     * Les obstacles fixes et les entités mobiles y partagent la même origine.
     */
    public Rectangle getFieldLogicalBounds() {
        Rectangle fieldBounds = getFieldBounds();
        return new Rectangle(
                -(fieldBounds.width / 2),
                -(fieldBounds.height / 2),
                fieldBounds.width,
                fieldBounds.height
        );
    }

    private Rectangle getPreferredFieldBounds() {
        Dimension preferredSize = getPreferredSize();
        return computeFieldBounds(preferredSize.width, preferredSize.height);
    }

    private Rectangle computeFieldBounds(int panelWidth, int panelHeight) {
        int columns = getColumnCount();
        int rows = getRowCount();
        if (panelWidth <= 0 || panelHeight <= 0 || columns <= 0 || rows <= 0) {
            return new Rectangle();
        }

        /*
         * Ici on cherche un comportement précis :
         * la map doit recouvrir toute la zone visible.
         * On choisit donc la plus grande taille de case nécessaire pour couvrir
         * à la fois la largeur ET la hauteur, quitte à déborder de quelques pixels.
         * Le panneau est ensuite simplement "recadré" par la fenêtre.
         */
        int tileSize = (int) Math.ceil(Math.max(
                (double) panelWidth / columns,
                (double) panelHeight / rows
        ));
        tileSize = Math.max(1, tileSize);

        int gridW = tileSize * columns;
        int gridH = tileSize * rows;
        int startX = (panelWidth - gridW) / 2;
        int startY = (panelHeight - gridH) / 2;
        return new Rectangle(startX, startY, gridW, gridH);
    }

    /**
     * Centralise le test de coordonnées.
     * On évite ainsi de répéter la même condition dans tous les helpers de grille.
     */
    private boolean isInsideGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < getColumnCount() && gridY >= 0 && gridY < getRowCount();
    }

    /**
     * Une fois le rectangle global du champ connu, toutes les cases ont la même taille.
     * Ce helper donne cette taille unique pour éviter de refaire le calcul partout.
     */
    private int getTileSize(Rectangle fieldBounds) {
        return fieldBounds.width / getColumnCount();
    }

    /**
     * Construit les coordonnées écran d'une case.
     * Cette version sert au rendu : dessin du sol, des plantes, des surlignages, etc.
     */
    private Rectangle buildScreenCellBounds(int gridX, int gridY, Rectangle fieldBounds) {
        int tileSize = getTileSize(fieldBounds);
        int x = fieldBounds.x + (gridX * tileSize);
        int y = fieldBounds.y + (gridY * tileSize);
        return new Rectangle(x, y, tileSize, tileSize);
    }

    /**
     * Construit le rectangle logique d'une case dans le repère centré de la map.
     * Cette version est utilisée pour les collisions et les comparaisons avec la grange.
     */
    private Rectangle buildLogicalCellBounds(int gridX, int gridY, Rectangle fieldBounds) {
        int tileSize = getTileSize(fieldBounds);
        int logicalX = -(fieldBounds.width / 2) + (gridX * tileSize);
        int logicalY = -(fieldBounds.height / 2) + (gridY * tileSize);
        return new Rectangle(logicalX, logicalY, tileSize, tileSize);
    }

    /**
     * Donne le centre logique d'une case.
     * C'est utile pour positionner proprement le joueur ou d'autres entités sur une tuile.
     */
    private Point buildLogicalCellCenter(int gridX, int gridY, Rectangle fieldBounds) {
        Rectangle logicalCellBounds = buildLogicalCellBounds(gridX, gridY, fieldBounds);
        return new Point(
                logicalCellBounds.x + (logicalCellBounds.width / 2),
                logicalCellBounds.y + (logicalCellBounds.height / 2)
        );
    }

    /**
     * Retourne le case du champ affichée à l'écran correspondant à une case logique de la grille.
     */
    public Rectangle getCellBounds(int gridX, int gridY) {
        if (!isInsideGrid(gridX, gridY)) {
            return null;
        }

        return buildScreenCellBounds(gridX, gridY, getFieldBounds());
    }

    /**
     * Cette méthode sert surtout aux règles de gameplay.
     * Une case qui croise la grange ne doit jamais pouvoir être labourée ni plantée.
     */
    public boolean isBlockedByBarn(Point cell) {
        return cell != null && isBlockedByBarn(cell.x, cell.y);
    }

    public boolean isBlockedByBarn(int gridX, int gridY) {
        Rectangle logicalCellBounds = getLogicalCellBounds(gridX, gridY);
        if (logicalCellBounds == null) {
            return false;
        }

        Rectangle barnCourtyardBounds = getBarnCourtyardLogicalBounds();
        if (barnCourtyardBounds == null || !barnCourtyardBounds.intersects(logicalCellBounds)) {
            return false;
        }

        Rectangle barnSpriteBounds = getBarnLogicalDrawBounds();
        if (barnSpriteBounds != null && barnSpriteBounds.intersects(logicalCellBounds)) {
            return true;
        }

        /*
         * La zone pierre + herbe autour de la grange doit elle aussi désactiver les cases.
         * Comme l'utilisateur a demandé d'inclure les cases partiellement visibles,
         * toute intersection avec cette bordure suffit ici.
         */
        return true;
    }

    public boolean isFarmableCell(Point cell) {
        return cell != null
                && !isBlockedByBarn(cell)
                && !isBlockedByStaticObstacle(cell.x, cell.y);
    }

    /**
     * Une case peut être bloquée par une rivière,
     * par l'arbre posé dessus,
     * ou par la canopée d'un arbre voisin devenue très grande.
     */
    public boolean isBlockedByStaticObstacle(int gridX, int gridY) {
        if (fieldObstacleMap != null) {
            return fieldObstacleMap.blocksCell(gridX, gridY);
        }

        return grilleCulture.hasRiver(gridX, gridY) || treeManager.hasTreeAt(gridX, gridY);
    }

    /**
     * Les calculs de collisions et de déplacements utilisent un repère centré sur la map.
     * On reconstruit donc ici le rectangle logique d'une case dans ce même repère,
     * pour pouvoir comparer proprement la case à la grange.
     */
    public Rectangle getLogicalCellBounds(int gridX, int gridY) {
        if (!isInsideGrid(gridX, gridY)) {
            return null;
        }

        return buildLogicalCellBounds(gridX, gridY, getFieldBounds());
    }

    /**
     * Expose le rectangle logique associé au sprite complet de la grange.
     * Cela sert aux règles visuelles de placement des arbres, plus strictes que la seule hitbox.
     */
    public Rectangle getBarnLogicalDrawBounds() {
        return new Rectangle(Barn.X, Barn.Y, Barn.WIDTH, Barn.HEIGHT);
    }

    /**
     * Zone pierre + herbe autour de la grange :
     * elle colle à la grange, mais on retire volontairement la première rangée du haut
     * pour éviter d'afficher cette texture au-dessus du toit.
     */
    public Rectangle getBarnCourtyardLogicalBounds() {
        Rectangle barnDrawBounds = getBarnLogicalDrawBounds();
        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        int topInset = Math.min(tileSize, Math.max(0, barnDrawBounds.height));
        return new Rectangle(
                barnDrawBounds.x,
                barnDrawBounds.y + topInset,
                barnDrawBounds.width,
                Math.max(0, barnDrawBounds.height - topInset)
        );
    }

    private Point getLogicalCellCenter(int gridX, int gridY, Rectangle fieldBounds) {
        return buildLogicalCellCenter(gridX, gridY, fieldBounds);
    }

    /**
     * Expose le centre logique d'une case dans le repère actuel du champ.
     * Le démarrage du joueur peut ainsi se recaler sur une vraie case libre.
     */
    public Point getLogicalCellCenter(int gridX, int gridY) {
        if (!isInsideGrid(gridX, gridY)) {
            return null;
        }

        return buildLogicalCellCenter(gridX, gridY, getFieldBounds());
    }

    /**
     * Convertit des coordonnées écran du panneau vers une case logique du modele.
     */
    public Point getGridPositionAt(int pixelX, int pixelY) {
        Rectangle fieldBounds = getFieldBounds();
        if (!fieldBounds.contains(pixelX, pixelY)) {
            return null;
        }

        // Cette conversion permettra ensuite d'associer un clic utilisateur a une case du modèle.
        int tileSize = getTileSize(fieldBounds);
        int gridX = (pixelX - fieldBounds.x) / tileSize;
        int gridY = (pixelY - fieldBounds.y) / tileSize;

        if (!isInsideGrid(gridX, gridY)) {
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
            if (grilleCulture.canPlaceFence(cell.x, cell.y, side)) {
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
     * La preview et la vraie clôture doivent partager les mêmes proportions.
     * On déduit donc une seule fois les dimensions à partir de la taille de case.
     */
    private FenceMetrics createFenceMetrics(Rectangle cellBounds) {
        return new FenceMetrics(cellBounds.width);
    }

    /**
     * Renvoie le rectangle du "bandeau" principal d'une clôture sur le côté demandé.
     * Ce helper évite de recopier la même géométrie dans la preview et dans le rendu final.
     */
    private Rectangle getFenceBandBounds(Rectangle cellBounds, CellSide side, FenceMetrics metrics) {
        switch (side) {
            case TOP:
                return new Rectangle(
                        cellBounds.x,
                        cellBounds.y - metrics.getOutsideDepth(),
                        cellBounds.width,
                        metrics.getBandThickness()
                );
            case RIGHT:
                return new Rectangle(
                        cellBounds.x + cellBounds.width - metrics.getInsideDepth(),
                        cellBounds.y,
                        metrics.getBandThickness(),
                        cellBounds.height
                );
            case BOTTOM:
                return new Rectangle(
                        cellBounds.x,
                        cellBounds.y + cellBounds.height - metrics.getInsideDepth(),
                        cellBounds.width,
                        metrics.getBandThickness()
                );
            case LEFT:
                return new Rectangle(
                        cellBounds.x - metrics.getOutsideDepth(),
                        cellBounds.y,
                        metrics.getBandThickness(),
                        cellBounds.height
                );
            default:
                return null;
        }
    }

    /**
     * Dessine un trait sur le bord intérieur ou extérieur du bandeau de preview.
     * Cela évite deux gros switch quasi identiques juste pour changer un axe.
     */
    private void drawFencePreviewLine(Graphics2D g2, Rectangle previewBounds, CellSide side, boolean innerEdge, int inset, Color color) {
        if (previewBounds == null || side == null) {
            return;
        }

        g2.setColor(color);
        switch (side) {
            case TOP: {
                int y = innerEdge
                        ? previewBounds.y + previewBounds.height - 1 - inset
                        : previewBounds.y + inset;
                g2.drawLine(previewBounds.x, y, previewBounds.x + previewBounds.width - 1, y);
                break;
            }
            case RIGHT: {
                int x = innerEdge
                        ? previewBounds.x + inset
                        : previewBounds.x + previewBounds.width - 1 - inset;
                g2.drawLine(x, previewBounds.y, x, previewBounds.y + previewBounds.height - 1);
                break;
            }
            case BOTTOM: {
                int y = innerEdge
                        ? previewBounds.y + inset
                        : previewBounds.y + previewBounds.height - 1 - inset;
                g2.drawLine(previewBounds.x, y, previewBounds.x + previewBounds.width - 1, y);
                break;
            }
            case LEFT: {
                int x = innerEdge
                        ? previewBounds.x + previewBounds.width - 1 - inset
                        : previewBounds.x + inset;
                g2.drawLine(x, previewBounds.y, x, previewBounds.y + previewBounds.height - 1);
                break;
            }
            default:
                break;
        }
    }

    /**
     * La tuile affichée dépend uniquement des coordonnées et de l'état labouré.
     * Ce petit hash stable évite de stocker une variante par case dans le modèle.
     */
    private Image getGroundTile(int gridX, int gridY) {
        Image[] variants;
        if (isBlockedByBarn(gridX, gridY)) {
            variants = pathTileImages;
        } else if (grilleCulture.hasDecorativeRiver(gridX, gridY)) {
            return getDecorativeRiverTile(gridY);
        } else if (grilleCulture.hasRiver(gridX, gridY)) {
            variants = riverTileImages;
        } else if (grilleCulture.hasPath(gridX, gridY)) {
            variants = pathTileImages;
        } else if (grilleCulture.isLabouree(gridX, gridY)) {
            if (PredefinedFieldLayout.isLeftOfDecorativeRiver(this, gridX) && wetSoilTileImage != null) {
                return wetSoilTileImage;
            }
            variants = tilledTileImages;
        } else if (PredefinedFieldLayout.isLeftOfDecorativeRiver(this, gridX)) {
            return getLeftMarshTile(gridX);
        } else {
            variants = grassTileImages;
        }
        if (variants == null || variants.length == 0) {
            return null;
        }

        int variantIndex = Math.floorMod((gridX * 31) + (gridY * 17), variants.length);
        return variants[variantIndex];
    }

    /**
     * Le marécage gauche du champ utilise trois variantes selon la colonne :
     * bord contre la rivière, centre, et bord de fenêtre.
     */
    private Image getLeftMarshTile(int gridX) {
        if (PredefinedFieldLayout.isLeftWindowEdgeColumn(this, gridX) && marshLeftEdgeTileImage != null) {
            return marshLeftEdgeTileImage;
        }

        if (PredefinedFieldLayout.isAdjacentLeftToDecorativeRiver(this, gridX) && marshTileImage != null) {
            return marshTileImage;
        }

        if (marshCenterTileImage != null) {
            return marshCenterTileImage;
        }

        if (marshTileImage != null) {
            return marshTileImage;
        }

        return getFirstAvailableTile(grassTileImages);
    }

    /**
     * La première case de la colonne utilise l'entrée de rivière,
     * puis toutes les suivantes prolongent la rivière avec `river2.png`.
     */
    private Image getDecorativeRiverTile(int gridY) {
        if (gridY == 0 && decorativeRiverEntryTileImage != null) {
            return decorativeRiverEntryTileImage;
        }

        if (decorativeRiverContinuationTileImage != null) {
            return decorativeRiverContinuationTileImage;
        }

        return getFirstAvailableTile(riverTileImages);
    }

    private Image getFirstAvailableTile(Image[] variants) {
        if (variants == null || variants.length == 0) {
            return null;
        }

        return variants[0];
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

        // Les plantes sont elles aussi en pixel art :
        // on évite donc le flou de l'interpolation bilinéaire.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(cultureImage, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * Le survol de pose doit épouser le bord visé, pas la case entière.
     * On reprend presque la même emprise que la vraie clôture,
     * pour éviter l'effet "ruban flottant" qui collait mal à la terre.
     */
    private void drawFencePreview(Graphics2D g2, Rectangle cellBounds, CellSide side) {
        FenceMetrics metrics = createFenceMetrics(cellBounds);
        Rectangle previewBounds = getFenceBandBounds(cellBounds, side, metrics);
        if (previewBounds == null) {
            return;
        }

        g2.setColor(FENCE_PREVIEW_FILL);
        g2.fillRect(previewBounds.x, previewBounds.y, previewBounds.width, previewBounds.height);

        drawFencePreviewLine(g2, previewBounds, side, true, 0, FENCE_PREVIEW_BORDER);

        // Une ligne secondaire plus douce aide à lire la direction du futur relief
        // sans surcharger la terre.
        drawFencePreviewLine(g2, previewBounds, side, false, 1, FENCE_PREVIEW_SOFT_LIGHT);
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
        FenceMetrics metrics = createFenceMetrics(cellBounds);
        CellSide side = bottomSide ? CellSide.BOTTOM : CellSide.TOP;
        boolean connectedLeft = grilleCulture.hasFence(cellX - 1, cellY, side);
        boolean connectedRight = grilleCulture.hasFence(cellX + 1, cellY, side);

        int bandHeight = metrics.getBandThickness();
        int bandY = bottomSide
                ? cellBounds.y + cellBounds.height - metrics.getInsideDepth()
                : cellBounds.y - metrics.getOutsideDepth();

        int slatThickness = metrics.getSlatThickness();
        int slatGap = metrics.getSlatGap();
        int postWidth = metrics.getPostThickness();
        int postHeight = bandHeight + metrics.getPostExtension();
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
        g2.fillRect(cellBounds.x + 2, shadowY, Math.max(2, cellBounds.width - 4), metrics.getShadowThickness());
    }

    private void drawVerticalFence(Graphics2D g2, int cellX, int cellY, Rectangle cellBounds, boolean rightSide) {
        FenceMetrics metrics = createFenceMetrics(cellBounds);
        CellSide side = rightSide ? CellSide.RIGHT : CellSide.LEFT;
        boolean connectedTop = grilleCulture.hasFence(cellX, cellY - 1, side);
        boolean connectedBottom = grilleCulture.hasFence(cellX, cellY + 1, side);

        int bandWidth = metrics.getBandThickness();
        int bandX = rightSide
                ? cellBounds.x + cellBounds.width - metrics.getInsideDepth()
                : cellBounds.x - metrics.getOutsideDepth();

        int slatThickness = metrics.getSlatThickness();
        int slatGap = metrics.getSlatGap();
        int postHeight = metrics.getPostThickness();
        int postWidth = bandWidth + metrics.getPostExtension();
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
        g2.fillRect(shadowX, cellBounds.y + 2, metrics.getShadowThickness(), Math.max(2, cellBounds.height - 4));
    }

    /**
     * La structure de base d'un poteau ne dépend pas de son orientation :
     * on a toujours un contour sombre puis un remplissage bois.
     *
     * On factorise donc cette partie commune ici,
     * et chaque variante ajoute ensuite son relief propre.
     */
    private void drawPostBase(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        g2.setColor(FENCE_WOOD_MID);
        g2.fillRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2));
    }

    /**
     * Relief d'un poteau "horizontal" :
     * on éclaire légèrement le côté gauche
     * et on assombrit le côté droit.
     */
    private void drawHorizontalPostRelief(Graphics2D g2, int x, int y, int width, int height) {
        int innerHeight = Math.max(1, height - 2);

        g2.setColor(FENCE_WOOD_LIGHT);
        g2.fillRect(x + 1, y + 1, Math.max(2, width / 3), innerHeight);

        g2.setColor(FENCE_WOOD_DARK);
        g2.fillRect(x + width - 2, y + 1, 1, innerHeight);
    }

    /**
     * Relief d'un poteau "vertical" :
     * on éclaire le haut et on ombre légèrement le bas.
     */
    private void drawVerticalPostRelief(Graphics2D g2, int x, int y, int width, int height) {
        int innerWidth = Math.max(1, width - 2);

        g2.setColor(FENCE_WOOD_LIGHT);
        g2.fillRect(x + 1, y + 1, innerWidth, Math.max(2, height / 3));

        g2.setColor(FENCE_WOOD_DARK);
        g2.fillRect(x + 1, y + height - 2, innerWidth, 1);
    }

    /**
     * Capuchon d'un poteau horizontal.
     * Le capuchon se place sur le côté "extérieur" du relief,
     * soit en haut, soit en bas.
     */
    private void drawHorizontalPostCap(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        int capY = outerOnTop ? y : y + height - Math.max(3, height / 3);
        int capHeight = Math.max(3, height / 3);
        g2.setColor(FENCE_CAP_DARK);
        g2.fillRect(x + 1, capY, Math.max(1, width - 2), capHeight);

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(x + 2, capY + 1, Math.max(1, width - 4), 1);
    }

    /**
     * Capuchon d'un poteau vertical.
     * Même idée que la version horizontale, mais cette fois le capuchon
     * est accroché à gauche ou à droite.
     */
    private void drawVerticalPostCap(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        int capX = outerOnRight ? x + width - Math.max(3, width / 3) : x;
        int capWidth = Math.max(3, width / 3);
        g2.setColor(FENCE_CAP_DARK);
        g2.fillRect(capX, y + 1, capWidth, Math.max(1, height - 2));

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(capX + (outerOnRight ? 0 : 1), y + 2, 1, Math.max(1, height - 4));
    }

    private void drawHorizontalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        drawPostBase(g2, x, y, width, height);
        drawHorizontalPostRelief(g2, x, y, width, height);
        drawHorizontalPostCap(g2, x, y, width, height, outerOnTop);
    }

    private void drawVerticalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        drawPostBase(g2, x, y, width, height);
        drawVerticalPostRelief(g2, x, y, width, height);
        drawVerticalPostCap(g2, x, y, width, height, outerOnRight);
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
     * Dessine tout ce qui appartient à une seule case:
     * le sol, la culture éventuelle, l'animation d'arrosage et le surlignage du joueur.
     *
     * Ce découpage garde paintComponent lisible:
     * la méthode principale orchestre,
     * ce helper s'occupe du détail d'une tuile.
     */
    private void drawCell(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        drawGroundTile(g2, gridX, gridY, cellBounds);

        Culture culture = grilleCulture.getCulture(gridX, gridY);
        Image cultureImage = getCultureImage(culture);
        if (cultureImage != null) {
            drawCultureImage(g2, cultureImage, cellBounds.x, cellBounds.y, cellBounds.width);
        }

        if (shouldAnimateWateredCell(culture)) {
            drawWateringAnimation(g2, cellBounds.x, cellBounds.y, cellBounds.width);
        }

        if (grilleCulture.hasCompostAt(gridX, gridY)) {
            drawCompostDecoration(g2, cellBounds);
        }

        if (isHighlightedFarmableCell(gridX, gridY)) {
            drawCellHighlight(g2, cellBounds);
        }
    }

    /**
     * Dessine uniquement la tuile de sol.
     * On sépare ce travail pour éviter de mélanger le choix du fond avec le reste du rendu.
     */
    private void drawGroundTile(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (isBlockedByBarn(gridX, gridY) || grilleCulture.hasPath(gridX, gridY)) {
            drawLayeredStoneWithGrassGroundTile(g2, cellBounds);
            return;
        }

        Image groundTile = getGroundTile(gridX, gridY);
        if (groundTile != null) {
            drawScaledTile(g2, groundTile, cellBounds);
            return;
        }

        g2.setColor(DEFAULT_GRASS_FILL);
        g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
    }

    /**
     * Pour les zones pierre + herbe, on garde une vraie base d'herbe
     * puis on superpose la dalle pierre + herbe par-dessus.
     */
    private void drawLayeredStoneWithGrassGroundTile(Graphics2D g2, Rectangle cellBounds) {
        Image grassTile = getFirstAvailableTile(grassTileImages);
        if (grassTile != null) {
            drawScaledTile(g2, grassTile, cellBounds);
        } else {
            g2.setColor(DEFAULT_GRASS_FILL);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        }

        Image stoneWithGrassTile = getFirstAvailableTile(pathTileImages);
        if (stoneWithGrassTile != null) {
            drawScaledTile(g2, stoneWithGrassTile, cellBounds);
        }
    }

    private void drawScaledTile(Graphics2D g2, Image tile, Rectangle cellBounds) {
        if (tile == null || cellBounds == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(tile, cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height, this);
    }

    /**
     * Dit si la case affichée est celle actuellement occupée par le joueur
     * et si cette case peut réellement être utilisée côté gameplay.
     */
    private boolean isHighlightedFarmableCell(int gridX, int gridY) {
        return highlightedCell != null
                && highlightedCell.x == gridX
                && highlightedCell.y == gridY
                && isFarmableCell(highlightedCell);
    }

    /**
     * Le surlignage reste volontairement simple:
     * un voile jaune et deux contours pour bien ressortir sur l'herbe comme sur la terre.
     */
    private void drawCellHighlight(Graphics2D g2, Rectangle cellBounds) {
        g2.setColor(HIGHLIGHT_FILL);
        g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        g2.setColor(HIGHLIGHT_BORDER);
        g2.drawRect(cellBounds.x, cellBounds.y, cellBounds.width - 1, cellBounds.height - 1);
        g2.drawRect(cellBounds.x + 1, cellBounds.y + 1, cellBounds.width - 3, cellBounds.height - 3);
    }

    /**
     * Dessine l'objet compost posé sur une case d'herbe.
     * On garde un petit padding pour qu'il reste lisible sans toucher les bords de la tuile.
     */
    private void drawCompostDecoration(Graphics2D g2, Rectangle cellBounds) {
        if (compostImage == null) {
            return;
        }

        int padding = Math.max(4, cellBounds.width / 10);
        int drawWidth = cellBounds.width - (padding * 2);
        int drawHeight = cellBounds.height - (padding * 2);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(
                compostImage,
                cellBounds.x + padding,
                cellBounds.y + padding,
                drawWidth,
                drawHeight,
                this
        );
    }

    /**
     * Quand le joueur clique sur un compost,
     * on affiche toutes les cases de terre actuellement boostées par les composts posés.
     */
    private void drawCompostInfluenceOverlay(Graphics2D g2) {
        if (!compostInfluenceVisible) {
            return;
        }

        for (Point affectedCell : grilleCulture.getCompostAffectedSoilCells()) {
            Rectangle cellBounds = getCellBounds(affectedCell.x, affectedCell.y);
            if (cellBounds == null) {
                continue;
            }

            g2.setColor(COMPOST_RANGE_FILL);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);

            g2.setColor(COMPOST_RANGE_BORDER);
            g2.drawRect(cellBounds.x, cellBounds.y, cellBounds.width - 1, cellBounds.height - 1);
            g2.drawRect(cellBounds.x + 1, cellBounds.y + 1, cellBounds.width - 3, cellBounds.height - 3);
        }
    }

    /**
     * Les clôtures sont dessinées après les cases pour rester au premier plan.
     * On parcourt donc la grille une deuxième fois, mais avec une responsabilité très claire:
     * uniquement les segments de clôture déjà posés.
     */
    private void drawPlacedFences(Graphics2D g2) {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Rectangle cellBounds = getCellBounds(column, row);
                if (cellBounds == null) {
                    continue;
                }

                for (CellSide side : CellSide.values()) {
                    if (grilleCulture.hasFence(column, row, side)) {
                        drawFence(g2, column, row, cellBounds, side);
                    }
                }
            }
        }
    }

    /**
     * Dessine la preview de clôture, si le contrôleur en a préparé une.
     */
    private void drawFencePreviewOverlay(Graphics2D g2) {
        if (fencePreview == null) {
            return;
        }

        Point previewCell = fencePreview.getCell();
        Rectangle previewBounds = previewCell == null ? null : getCellBounds(previewCell.x, previewCell.y);
        if (previewBounds != null) {
            drawFencePreview(g2, previewBounds, fencePreview.getSide());
        }
    }

    /**
     * La pose de rivière doit être très lisible,
     * justement parce qu'on ne passe pas par la case sous le joueur.
     * On affiche donc le sprite réel avec une transparence,
     * puis un code couleur très franc :
     * bleu si la pose est autorisée, rouge chaud sinon.
     */
    private void drawRiverPreviewOverlay(Graphics2D g2) {
        if (riverPreviewCell == null) {
            return;
        }

        Rectangle previewBounds = getCellBounds(riverPreviewCell.x, riverPreviewCell.y);
        if (previewBounds == null) {
            return;
        }

        Image previewTile = (riverTileImages == null || riverTileImages.length == 0) ? null : riverTileImages[0];
        Graphics2D previewGraphics = (Graphics2D) g2.create();
        previewGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (previewTile != null) {
            float alpha = riverPreviewValid ? 0.72f : 0.38f;
            previewGraphics.setComposite(AlphaComposite.SrcOver.derive(alpha));
            previewGraphics.drawImage(
                    previewTile,
                    previewBounds.x,
                    previewBounds.y,
                    previewBounds.width,
                    previewBounds.height,
                    this
            );
            previewGraphics.setComposite(AlphaComposite.SrcOver);
        }

        Color fillColor = riverPreviewValid
                ? new Color(87, 176, 255, 70)
                : new Color(198, 93, 74, 88);
        Color borderColor = riverPreviewValid
                ? new Color(194, 237, 255, 230)
                : new Color(255, 210, 178, 235);

        previewGraphics.setColor(fillColor);
        previewGraphics.fillRect(previewBounds.x, previewBounds.y, previewBounds.width, previewBounds.height);

        previewGraphics.setColor(borderColor);
        previewGraphics.drawRect(previewBounds.x, previewBounds.y, previewBounds.width - 1, previewBounds.height - 1);
        previewGraphics.drawRect(previewBounds.x + 1, previewBounds.y + 1, previewBounds.width - 3, previewBounds.height - 3);

        if (!riverPreviewValid) {
            // Le petit X rend l'interdiction instantanément lisible,
            // même si le joueur regarde le champ en mouvement.
            previewGraphics.drawLine(
                    previewBounds.x + 3,
                    previewBounds.y + 3,
                    previewBounds.x + previewBounds.width - 4,
                    previewBounds.y + previewBounds.height - 4
            );
            previewGraphics.drawLine(
                    previewBounds.x + previewBounds.width - 4,
                    previewBounds.y + 3,
                    previewBounds.x + 3,
                    previewBounds.y + previewBounds.height - 4
            );
        }

        previewGraphics.dispose();
    }

    /**
     * Dessine la grille (avec les images).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Rectangle fieldBounds = getFieldBounds();
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Rectangle cellBounds = buildScreenCellBounds(column, row, fieldBounds);
                drawCell(g2, column, row, cellBounds);
            }
        }

        drawCompostInfluenceOverlay(g2);
        drawPlacedFences(g2);
        drawFencePreviewOverlay(g2);
        drawRiverPreviewOverlay(g2);

        g2.dispose();
    }
}
