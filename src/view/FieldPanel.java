package view;

import model.culture.Culture;
import model.culture.CellSide;
import model.culture.FenceDestructionEffect;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.environment.FieldObstacleMap;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import model.movement.Barn;
import model.movement.Stall;
import model.movement.Unit;
import model.movement.Workshop;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Panneau d'affichage du champ, compose d'une grille d'images.
 */
public class FieldPanel extends JPanel implements PlayableMapPanel {
    // Échelles de rendu des stades visuels des carottes sur la carte.
    private static final double CARROT_YOUNG_MAP_SCALE = 0.62;
    private static final double CARROT_INTERMEDIATE_MAP_SCALE = 0.68;
    private static final double CARROT_MATURE_MAP_SCALE = 0.78;
    private static final double CARROT_WILTED_MAP_SCALE = 0.72;
    // Échelles de rendu des stades visuels des radis sur la carte.
    private static final double RADISH_YOUNG_MAP_SCALE = 0.92;
    private static final double RADISH_INTERMEDIATE_MAP_SCALE = 0.80;
    private static final double RADISH_MATURE_MAP_SCALE = 0.82;
    private static final double RADISH_WILTED_MAP_SCALE = 0.80;
    // Échelles de rendu des stades visuels des choux-fleurs sur la carte.
    private static final double CAULIFLOWER_YOUNG_MAP_SCALE = 0.88;
    private static final double CAULIFLOWER_INTERMEDIATE_MAP_SCALE = 0.90;
    private static final double CAULIFLOWER_MATURE_MAP_SCALE = 0.82;
    private static final double CAULIFLOWER_WILTED_MAP_SCALE = 0.82;
    // Échelle de rendu dédiée aux marguerites pour calmer leur hauteur visuelle dans la case.
    private static final double DAISY_MAP_SCALE = 0.88;
    // Échelle de rendu du stade jeune des nénuphars sur la carte.
    private static final double WATER_LILY_YOUNG_MAP_SCALE = 0.78;
    // Échelle de rendu du stade jeune des iris des marais sur la carte.
    private static final double MARSH_IRIS_YOUNG_MAP_SCALE = 0.78;
    // Décalage vertical appliqué aux marguerites pour les garder visuellement centrées.
    private static final double DAISY_VERTICAL_OFFSET_RATIO = -0.10;

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
    private static final Color LABOUR_WARNING_BADGE_FILL = new Color(204, 58, 48, 230);
    private static final Color LABOUR_WARNING_BADGE_BORDER = new Color(255, 221, 214, 245);

    // Couleurs de l'effet visuel affiché après un arrosage réussi.
    private static final Color WATERED_FILL = new Color(70, 170, 255);
    private static final Color WATERED_BORDER = new Color(220, 245, 255);

    // Couleurs utilisées pour visualiser la zone influencée par le compost.
    private static final Color COMPOST_RANGE_FILL = new Color(145, 214, 98, 70);
    private static final Color COMPOST_RANGE_BORDER = new Color(228, 255, 177, 170);
    private static final Color BRIDGE_PLACEMENT_FILL = new Color(109, 201, 255, 74);
    private static final Color BRIDGE_PLACEMENT_BORDER = new Color(214, 247, 255, 210);

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
    private static final Color FENCE_HEALTH_FRAME = new Color(31, 21, 18, 235);
    private static final Color FENCE_HEALTH_BACKGROUND = new Color(78, 34, 30, 220);
    private static final Color FENCE_HEALTH_STABLE = new Color(147, 219, 95, 235);
    private static final Color FENCE_HEALTH_WARNING = new Color(255, 199, 80, 240);
    private static final Color FENCE_HEALTH_DANGER = new Color(255, 118, 65, 242);
    private static final Color FENCE_HEALTH_CRITICAL = new Color(224, 53, 39, 246);
    private static final Color FENCE_HEALTH_HIGHLIGHT = new Color(255, 241, 210, 210);
    private static final Color FENCE_EXPLOSION_OUTER = new Color(255, 173, 84, 180);
    private static final Color FENCE_EXPLOSION_INNER = new Color(255, 234, 169, 220);
    private static final Color FENCE_EXPLOSION_DEBRIS = new Color(121, 74, 43, 210);
    private static final int BARN_TOP_STONE_COLUMN_WIDTH = 2;
    private static final double BARN_BUSH_FILL_RATIO = 1.18;
    private static final double BARN_BUSH_UPWARD_OFFSET_RATIO = 0.05;
    private static final double BARN_BUSH_HORIZONTAL_OFFSET_RATIO = 0.08;
    private static final double VERTICAL_BUSH_WIDTH_RATIO = 0.82;
    private static final double VERTICAL_BUSH_HEIGHT_RATIO = 1.18;
    private static final double VERTICAL_BUSH_RIGHT_INSET_RATIO = 0.06;
    private static final double VERTICAL_BUSH_UPWARD_OFFSET_RATIO = 0.03;
    private static final int VERTICAL_BUSH_LEFT_PIXEL_SHIFT = 3;
    private static final int VERTICAL_BUSH_RIGHT_PIXEL_SHIFT = 3;
    private static final double FARM_CAVE_ENTRY_HORIZONTAL_OFFSET_TILES = 1.1;
    private static final Color CAVE_ENTRANCE_SHADOW = new Color(8, 7, 10, 118);

    // Vitesse de pulsation de l'animation d'arrosage.
    // Plus la valeur est grande, plus le halo "respire" vite.
    private static final double WATER_PULSE_SPEED = 0.008;

    // On lit directement l'état réel des cultures.
    private final GrilleCulture grilleCulture;
    // Gestionnaire relu pour dessiner et bloquer les arbres du décor.
    private final TreeManager treeManager;
    // Carte d'obstacles enrichie utilisée quand elle est disponible.
    private FieldObstacleMap fieldObstacleMap;

    // Plusieurs variantes évitent un motif trop répétitif.
    private final Image[] grassTileImages;
    private final Image[] tilledTileImages;
    private final Image[] pathTileImages;
    private final Image[] riverTileImages;
    private final Image leftRiverPathTileImage;
    private final Image marshTileImage;
    private final Image marshCenterTileImage;
    private final Image marshLeftEdgeTileImage;
    private final Image wetSoilTileImage;
    private final Image bushTileImage;
    private final Image verticalBushTileImage;
    private final Image verticalBushRightTileImage;
    private final Image decorativeRiverEntryTileImage;
    private final Image decorativeRiverContinuationTileImage;
    private final Image caveEntranceImage;

    // Images associées aux différents stades visuels d'une culture.
    private final Image carotteJeunePousseImage;
    private final Image carotteIntermediaireImage;
    private final Image carotteMatureImage;
    private final Image carotteFletrieImage;
    private final Image roseJeunePousseImage;
    private final Image roseIntermediaireImage;
    private final Image roseMatureImage;
    private final Image roseFletrieImage;
    private final Image tulipeJeunePousseImage;
    private final Image tulipeIntermediaireImage;
    private final Image tulipeMatureImage;
    private final Image tulipeFletrieImage;
    private final Image margueriteJeunePousseImage;
    private final Image margueriteIntermediaireImage;
    private final Image margueriteMatureImage;
    private final Image margueriteFletrieImage;
    private final Image radisJeuneImage;
    private final Image radisIntermediaireImage;
    private final Image radisMatureImage;
    private final Image radisFletriImage;
    private final Image choufleurJeuneImage;
    private final Image choufleurIntermediaireImage;
    private final Image choufleurMatureImage;
    private final Image choufleurFletriImage;
    private final Image nenupharJeuneImage;
    private final Image nenupharIntermediaireImage;
    private final Image nenupharMatureImage;
    private final Image nenupharFletriImage;
    private final Image irisMaraisJeuneImage;
    private final Image irisMaraisIntermediaireImage;
    private final Image irisMaraisMatureImage;
    private final Image irisMaraisFletrieImage;
    private final Image compostImage;
    private final Image bridgeImage;

    // Coordonnées de la case actuellement surlignée.
    // Cette valeur vaut null quand aucune case n'est activable.
    private Point highlightedCell;

    // Le preview mémorise à la fois la case survolée et le bord réellement visé.
    private FencePreview fencePreview;

    // Cet indicateur dit seulement si la zone du compost doit être montrée ou non.
    // Les cases exactes sont relues directement depuis la grille au moment du dessin.
    private boolean compostInfluenceVisible;
    // Indique si toutes les cases candidates au pont doivent être mises en avant.
    private boolean bridgePlacementHighlightVisible;
    // Bornes du champ utilisées pour savoir si le cache décor de la boutique reste valable.
    private Rectangle cachedBarnDecorFieldBounds;
    // Zone de cases bloquées par la partie haute droite du décor.
    private Rectangle cachedBarnBlockedGridBounds;
    // Colonne de rivière utilisée pour les décorations de la zone droite.
    private int cachedDecorativeRiverColumn = Integer.MIN_VALUE;
    // Zone logique globale traitée comme décoration haute à droite de la rivière.
    private Rectangle cachedRightRiverUpperDecorationLogicalBounds;
    // Image déjà rendue du terrain fixe pour éviter de tout repeindre à chaque frame.
    private BufferedImage staticTerrainCache;
    // Largeur utilisée pour savoir si le cache terrain doit être reconstruit.
    private int cachedTerrainWidth = -1;
    // Hauteur utilisée pour savoir si le cache terrain doit être reconstruit.
    private int cachedTerrainHeight = -1;
    // Bornes du champ correspondant au cache terrain courant.
    private Rectangle cachedTerrainFieldBounds;
    // Cache des rectangles utiles pour dessiner les cultures déjà visibles.
    private final Map<Image, Rectangle> visibleCultureBoundsCache = new IdentityHashMap<>();

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
        this.leftRiverPathTileImage = ImageLoader.load("/assets/stone_with_grass_leftRiver.png");
        this.marshTileImage = ImageLoader.load("/assets/marecages.png");
        this.marshCenterTileImage = ImageLoader.load("/assets/marecagesCenter.png");
        this.marshLeftEdgeTileImage = ImageLoader.load("/assets/marecagesGauche.png");
        this.wetSoilTileImage = ImageLoader.load("/assets/TerreMouillee.png");
        this.bushTileImage = ImageLoader.load("/assets/bush.png");
        this.verticalBushTileImage = ImageLoader.load("/assets/bush_vertical.png");
        this.verticalBushRightTileImage = ImageLoader.load("/assets/bush_vertical_right.png");
        this.decorativeRiverEntryTileImage = ImageLoader.load("/assets/entreeRiviere.png");
        this.decorativeRiverContinuationTileImage = ImageLoader.load("/assets/river2.png");
        this.caveEntranceImage = ImageLoader.load("/assets/cave_entrance.png");
        this.carotteJeunePousseImage = ImageLoader.load("/assets/carotte_jeune_pousse.png");
        this.carotteIntermediaireImage = ImageLoader.load("/assets/carotte_intermediaire.png");
        this.carotteMatureImage = ImageLoader.load("/assets/carotte_mature.png");
        this.carotteFletrieImage = ImageLoader.load("/assets/carotte_fletrie.png");
        this.roseJeunePousseImage = ImageLoader.load("/assets/marguerithe_jeune_pousse.png");
        this.roseIntermediaireImage = ImageLoader.load("/assets/rose_inter.png");
        this.roseMatureImage = ImageLoader.load("/assets/rose_mature.png");
        this.roseFletrieImage = ImageLoader.load("/assets/rose_fletrie.png");
        this.tulipeJeunePousseImage = ImageLoader.load("/assets/marguerithe_jeune_pousse.png");
        this.tulipeIntermediaireImage = ImageLoader.load("/assets/tulipe_inter.png");
        this.tulipeMatureImage = ImageLoader.load("/assets/tulipe_mature.png");
        this.tulipeFletrieImage = ImageLoader.load("/assets/tulipe_fletrie.png");
        this.margueriteJeunePousseImage = ImageLoader.load("/assets/marguerithe_jeune_pousse.png");
        this.margueriteIntermediaireImage = ImageLoader.load("/assets/marguerithe_inter.png");
        this.margueriteMatureImage = ImageLoader.load("/assets/marguerithe_mature.png");
        this.margueriteFletrieImage = ImageLoader.load("/assets/marguerithe_fletrie.png");
        this.radisJeuneImage = ImageLoader.load("/assets/radis_jeune.png");
        this.radisIntermediaireImage = ImageLoader.load("/assets/radis_inter.png");
        this.radisMatureImage = ImageLoader.load("/assets/radis_mature.png");
        this.radisFletriImage = ImageLoader.load("/assets/radis_fletri.png");
        this.choufleurJeuneImage = ImageLoader.load("/assets/choufleur_jeune.png");
        this.choufleurIntermediaireImage = ImageLoader.load("/assets/choufleur_inter.png");
        this.choufleurMatureImage = ImageLoader.load("/assets/choufleur_mature.png");
        this.choufleurFletriImage = ImageLoader.load("/assets/choufleur_fletri.png");
        this.nenupharJeuneImage = ImageLoader.load("/assets/nenuphar_jeune.png");
        this.nenupharIntermediaireImage = ImageLoader.load("/assets/nenuphar_inter.png");
        this.nenupharMatureImage = ImageLoader.load("/assets/nenuphar_mature.png");
        this.nenupharFletriImage = ImageLoader.load("/assets/nenuphar_fletri.png");
        this.irisMaraisJeuneImage = ImageLoader.load("/assets/iris_marais_jeune.png");
        this.irisMaraisIntermediaireImage = ImageLoader.load("/assets/iris_marais_inter.png");
        this.irisMaraisMatureImage = ImageLoader.load("/assets/iris_marais_mature.png");
        this.irisMaraisFletrieImage = ImageLoader.load("/assets/iris_marais_fletrie.png");
        this.compostImage = ImageLoader.load("/assets/Compost.png");
        this.bridgeImage = ImageLoader.load("/assets/bridge.png");
        this.compostInfluenceVisible = false;
        this.bridgePlacementHighlightVisible = false;
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        // Le panneau reste transparent hors de la grille pour laisser voir le fond global.
        setOpaque(false);
    }

    /**
     * On branche la carte d'obstacles détaillée utilisée par les règles de placement et de collision.
     */
    public void setFieldObstacleMap(FieldObstacleMap fieldObstacleMap) {
        this.fieldObstacleMap = fieldObstacleMap;
    }

    /**
     * Certains composants UI ont besoin d'interroger les règles d'obstacle
     * sans rebrancher toute la chaîne de dépendances du démarrage.
     * On expose donc ce helper en lecture seule.
     */
    public FieldObstacleMap getFieldObstacleMap() {
        return fieldObstacleMap;
    }

    /**
     * On expose la grille de culture réellement affichée par ce panneau.
     */
    public GrilleCulture getGrilleCulture() {
        return grilleCulture;
    }

    /**
     * On renvoie le nombre total de colonnes de la grille.
     */
    public int getColumnCount() {
        return grilleCulture.getLargeur();
    }

    /**
     * On renvoie le nombre total de lignes de la grille.
     */
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
     * On renvoie la vitesse à appliquer selon que la case est un chemin ou non.
     */
    @Override
    public int resolveMovementSpeed(Point cell) {
        return cell != null && grilleCulture.hasPath(cell.x, cell.y) ? Unit.PATH_SPEED : Unit.NORMAL_SPEED;
    }

    /**
     * On expose ce panneau lui-même comme composant de map jouable.
     */
    @Override
    public Component getMapComponent() {
        return this;
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

    /**
     * On efface toute prévisualisation de clôture en cours.
     */
    public void clearFencePreview() {
        setFencePreview(null);
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

    /**
     * On cache explicitement la zone d'influence des composts.
     */
    public void clearCompostInfluenceHighlight() {
        if (!compostInfluenceVisible) {
            return;
        }
        compostInfluenceVisible = false;
        repaint();
    }

    /**
     * Le pont utilise un highlight persistant sur toutes ses cases candidates.
     * Contrairement à la clôture, ce n'est pas un preview de souris :
     * on montre toute la colonne utile tant que l'objet est sélectionné.
     */
    public void setBridgePlacementHighlightVisible(boolean visible) {
        if (bridgePlacementHighlightVisible == visible) {
            return;
        }

        bridgePlacementHighlightVisible = visible;
        repaint();
    }

    /**
     * Une case candidate au pont doit être une berge droite libre,
     * donc collée à une case de rivière sur sa gauche et encore exploitable
     * du point de vue des obstacles fixes du décor.
     */
    public boolean isBridgePlacementCandidateCell(Point cell) {
        return cell != null && isBridgePlacementCandidateCell(cell.x, cell.y);
    }

    /**
     * On dit si une case précise remplit toutes les conditions de pose d'un pont.
     */
    public boolean isBridgePlacementCandidateCell(int gridX, int gridY) {
        return grilleCulture.canPlaceBridge(gridX, gridY)
                && !isBlockedByBarn(gridX, gridY)
                && !isBlockedByStall(gridX, gridY)
                && !isBlockedByWorkshop(gridX, gridY)
                && isBlockedByStaticObstacle(gridX, gridY);
    }

    /**
     * Le pont posé est dessiné à partir de sa case d'ancrage côté droit.
     * On expose ses bornes écran pour que l'environnement et les collisions
     * puissent réutiliser exactement la même géométrie visible.
     */
    public Rectangle getBridgeScreenBounds(int bridgeAnchorX, int bridgeAnchorY) {
        return buildBridgeBounds(bridgeAnchorX, bridgeAnchorY, false);
    }

    /**
     * Variante logique de la méthode précédente :
     * elle sert au gameplay, notamment pour autoriser la traversée
     * uniquement à l'intérieur du sprite du pont.
     */
    public Rectangle getBridgeLogicalBounds(int bridgeAnchorX, int bridgeAnchorY) {
        return buildBridgeBounds(bridgeAnchorX, bridgeAnchorY, true);
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
        Rectangle fieldBounds = computeFieldBounds(getWidth(), getHeight());
        syncBarnTileSize(fieldBounds);
        return fieldBounds;
    }

    /**
     * Expose le rectangle écran de la boutique principale (à droite).
     * La vue d'environnement et le contrôleur s'en servent pour parler
     * exactement du même objet visuel sans recalculer chacun de leur côté.
     */
    public Rectangle getBarnScreenBounds() {
        Rectangle fieldBounds = getFieldBounds();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        int drawX = centerX + Barn.getDrawX();
        int drawY = centerY + Barn.Y;
        return new Rectangle(drawX, drawY, Barn.WIDTH, Barn.HEIGHT);
    }

    /**
     * La menuiserie est calculée dans le même repère logique que la boutique principale (à droite),
     * puis simplement décalée vers l'écran comme le reste du décor fixe.
     */
    public Rectangle getWorkshopScreenBounds() {
        Rectangle fieldBounds = getFieldBounds();
        Rectangle workshopBounds = getWorkshopLogicalDrawBounds();
        if (workshopBounds == null) {
            return null;
        }

        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        return new Rectangle(
                centerX + workshopBounds.x,
                centerY + workshopBounds.y,
                workshopBounds.width,
                workshopBounds.height
        );
    }

    /**
     * L'échoppe partage le même repère logique que les autres bâtiments fixes.
     * On peut donc la projeter à l'écran exactement comme la boutique principale (à droite)
     * et la menuiserie.
     */
    public Rectangle getStallScreenBounds() {
        Rectangle fieldBounds = getFieldBounds();
        Rectangle stallBounds = getStallLogicalDrawBounds();
        if (stallBounds == null) {
            return null;
        }

        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        return new Rectangle(
                centerX + stallBounds.x,
                centerY + stallBounds.y,
                stallBounds.width,
                stallBounds.height
        );
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

    /**
     * On reconstruit les bornes idéales du champ à partir de la taille préférée du panneau.
     */
    private Rectangle getPreferredFieldBounds() {
        Dimension preferredSize = getPreferredSize();
        Rectangle fieldBounds = computeFieldBounds(preferredSize.width, preferredSize.height);
        syncBarnTileSize(fieldBounds);
        return fieldBounds;
    }

    /**
     * On calcule le rectangle occupé par la grille à partir de la taille réelle du panneau.
     */
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
        return gridX < 0 || gridX >= getColumnCount() || gridY < 0 || gridY >= getRowCount();
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
     * Cette version est utilisée pour les collisions et les comparaisons avec
     * la boutique principale (à droite).
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
        if (isInsideGrid(gridX, gridY)) {
            return null;
        }

        return buildScreenCellBounds(gridX, gridY, getFieldBounds());
    }

    /**
     * Cette méthode sert surtout aux règles de gameplay.
     * Une case qui croise la boutique principale (à droite) ne doit jamais
     * pouvoir être labourée ni plantée.
     */
    public boolean isBlockedByBarn(Point cell) {
        return cell != null && isBlockedByBarn(cell.x, cell.y);
    }

    /**
     * On vérifie si une case touche la zone occupée par la boutique principale ou sa cour.
     */
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
        if (barnSpriteBounds != null) {
            barnSpriteBounds.intersects(logicalCellBounds);
        }

        /*
         * La zone pierre + herbe autour de la boutique principale (à droite)
         * doit elle aussi désactiver les cases.
         * Comme l'utilisateur a demandé d'inclure les cases partiellement visibles,
         * toute intersection avec cette bordure suffit ici.
         */
        return true;
    }

    /**
     * Variante pratique quand on a déjà la case sous forme de point.
     */
    public boolean isBlockedByWorkshop(Point cell) {
        return cell != null && isBlockedByWorkshop(cell.x, cell.y);
    }

    /**
     * Variante pratique quand on a déjà la case sous forme de point.
     */
    public boolean isBlockedByStall(Point cell) {
        return cell != null && isBlockedByStall(cell.x, cell.y);
    }

    /**
     * La menuiserie bloque seulement sa hitbox basse :
     * on garde ainsi un comportement cohérent avec ce que le joueur peut réellement heurter.
     */
    public boolean isBlockedByWorkshop(int gridX, int gridY) {
        Rectangle logicalCellBounds = getLogicalCellBounds(gridX, gridY);
        if (logicalCellBounds == null) {
            return false;
        }

        Rectangle workshopCollisionBounds = getWorkshopLogicalCollisionBounds();
        return workshopCollisionBounds != null && workshopCollisionBounds.intersects(logicalCellBounds);
    }

    /**
     * L'échoppe est elle aussi un obstacle fixe :
     * seule sa partie basse solide doit empêcher le passage.
     */
    public boolean isBlockedByStall(int gridX, int gridY) {
        Rectangle logicalCellBounds = getLogicalCellBounds(gridX, gridY);
        if (logicalCellBounds == null) {
            return false;
        }

        Rectangle stallCollisionBounds = getStallLogicalCollisionBounds();
        return stallCollisionBounds != null && stallCollisionBounds.intersects(logicalCellBounds);
    }

    /**
     * On dit si une case peut vraiment servir au champ sans tomber sur un décor bloquant.
     */
    public boolean isFarmableCell(Point cell) {
        return cell != null
                && !isFarmCaveDecorationCell(cell.x, cell.y)
                && !isBlockedByBarn(cell)
                && !isBlockedByStall(cell)
                && !isBlockedByWorkshop(cell)
                && isBlockedByStaticObstacle(cell.x, cell.y);
    }

    /**
     * Une case peut être bloquée par une rivière,
     * par l'arbre posé dessus,
     * ou par la canopée d'un arbre voisin devenue très grande.
     */
    public boolean isBlockedByStaticObstacle(int gridX, int gridY) {
        if (fieldObstacleMap != null) {
            return !fieldObstacleMap.blocksCell(gridX, gridY);
        }

        return !grilleCulture.hasRiver(gridX, gridY)
                && !treeManager.hasTreeAt(gridX, gridY)
                && !isBlockedByStall(gridX, gridY)
                && !isBlockedByWorkshop(gridX, gridY)
                && !hasRightStoneExtensionAt(gridX, gridY)
                && !hasDecorativeBushAt(gridX, gridY);
    }

    /**
     * On regroupe ici tous les petits buissons décoratifs qui bloquent aussi le gameplay.
     */
    public boolean hasDecorativeBushAt(int gridX, int gridY) {
        // Toute la végétation purement décorative liée à la boutique
        // passe par ce point d'entrée unique pour le rendu et le gameplay.
        return isBarnTopBushCell(gridX, gridY)
                || isBarnEntranceLeftVerticalBushCell(gridX, gridY)
                || isBarnLeftVerticalBushCell(gridX, gridY)
                || isBarnEntranceRightVerticalBushCell(gridX, gridY)
                || isBarnRightVerticalBushCell(gridX, gridY);
    }

    /**
     * Expose uniquement la prolongation de pavés côté droit,
     * car c'est cette zone qu'on veut réserver contre la pousse des arbres.
     */
    public boolean hasRightStoneExtensionAt(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        return barnBlockedGridBounds != null
                && gridX >= barnBlockedGridBounds.x + barnBlockedGridBounds.width
                && gridX < getColumnCount()
                && gridY >= barnBlockedGridBounds.y
                && gridY < barnBlockedGridBounds.y + barnBlockedGridBounds.height;
    }

    /**
     * À droite de la rivière, toute la bande décorative du haut
     * (buissons, boutique, chemin de pierre et marge visuelle)
     * est traitée comme une seule zone pour les lapins.
     */
    public Rectangle getRightRiverUpperDecorationLogicalBounds() {
        refreshBarnDecorCache();
        return cachedRightRiverUpperDecorationLogicalBounds == null
                ? null
                : new Rectangle(cachedRightRiverUpperDecorationLogicalBounds);
    }

    /**
     * On dit si une case fait partie de la grande zone décorative haute à droite de la rivière.
     */
    public boolean isRightRiverUpperDecorationCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        int riverColumn = findDecorativeRiverColumn();
        int lastBlockedRow = barnBlockedGridBounds == null
                ? -1
                : barnBlockedGridBounds.y + barnBlockedGridBounds.height - 1;

        // Version "case par case" de la grande zone interdite aux lapins côté droit.
        // On s'en sert surtout pour ignorer les petits buissons déjà couverts par cette zone globale.
        return barnBlockedGridBounds != null
                && riverColumn >= 0
                && gridX > riverColumn
                && gridX < getColumnCount()
                && gridY >= 0
                && gridY <= lastBlockedRow;
    }

    /**
     * Réserve aussi une petite respiration sous la zone pierreuse de la boutique :
     * côté droit de la rivière, les deux premières lignes juste après cette zone
     * restent libres de toute pousse d'arbre.
     */
    public boolean blocksTreeSpawnInRightRiverPostBarnRows(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        int riverColumn = findDecorativeRiverColumn();
        if (barnBlockedGridBounds == null || riverColumn < 0) {
            return false;
        }

        int firstReservedRow = barnBlockedGridBounds.y + barnBlockedGridBounds.height;
        int lastReservedRow = Math.min(getRowCount() - 1, firstReservedRow + 1);
        if (firstReservedRow > lastReservedRow) {
            return false;
        }

        return gridX > riverColumn
                && gridX < getColumnCount()
                && gridY >= firstReservedRow
                && gridY <= lastReservedRow;
    }

    /**
     * Les calculs de collisions et de déplacements utilisent un repère centré sur la map.
     * On reconstruit donc ici le rectangle logique d'une case dans ce même repère,
     * pour pouvoir comparer proprement la case à la boutique principale (à droite).
     */
    public Rectangle getLogicalCellBounds(int gridX, int gridY) {
        if (isInsideGrid(gridX, gridY)) {
            return null;
        }

        return buildLogicalCellBounds(gridX, gridY, getFieldBounds());
    }

    /**
     * Construit les bornes complètes du sprite du pont à partir de sa case d'ancrage.

     * Le pont recouvre la berge gauche, la rivière et la berge droite.
     * Toute la géométrie reste donc dérivée des mêmes cases de grille,
     * ce qui garantit que le rendu et les collisions restent alignés.
     */
    private Rectangle buildBridgeBounds(int bridgeAnchorX, int bridgeAnchorY, boolean logical) {
        Rectangle anchorCellBounds = logical
                ? getLogicalCellBounds(bridgeAnchorX, bridgeAnchorY)
                : getCellBounds(bridgeAnchorX, bridgeAnchorY);
        Rectangle riverCellBounds = logical
                ? getLogicalCellBounds(bridgeAnchorX - 1, bridgeAnchorY)
                : getCellBounds(bridgeAnchorX - 1, bridgeAnchorY);
        Rectangle leftBankCellBounds = logical
                ? getLogicalCellBounds(bridgeAnchorX - 2, bridgeAnchorY)
                : getCellBounds(bridgeAnchorX - 2, bridgeAnchorY);
        if (anchorCellBounds == null || riverCellBounds == null || leftBankCellBounds == null) {
            return null;
        }

        int fullSpanX = leftBankCellBounds.x;
        int fullSpanWidth = (anchorCellBounds.x + anchorCellBounds.width) - fullSpanX;
        int horizontalInset = Math.max(4, anchorCellBounds.width / 8);
        int drawWidth = Math.max(anchorCellBounds.width, fullSpanWidth - (horizontalInset * 2));
        Dimension scaledSize = getBridgeScaledSize(drawWidth);
        int drawX = fullSpanX + horizontalInset;
        int drawY = riverCellBounds.y
                + ((riverCellBounds.height - scaledSize.height) / 2)
                - Math.max(2, riverCellBounds.height / 16);

        return new Rectangle(drawX, drawY, scaledSize.width, scaledSize.height);
    }

    /**
     * On conserve les proportions du sprite du pont quand on l'étire sur plusieurs cases.
     */
    private Dimension getBridgeScaledSize(int targetWidth) {
        int imageWidth = bridgeImage == null ? -1 : bridgeImage.getWidth(this);
        int imageHeight = bridgeImage == null ? -1 : bridgeImage.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Dimension(targetWidth, Math.max(1, (targetWidth * 416) / 1175));
        }

        return new Dimension(targetWidth, Math.max(1, (targetWidth * imageHeight) / imageWidth));
    }

    /**
     * Expose la vraie emprise logique d'un segment de clôture.
     * Les collisions des lapins et le rendu doivent parler exactement de la même forme.
     */
    public Rectangle getLogicalFenceBounds(int gridX, int gridY, CellSide side) {
        return getFenceBounds(gridX, gridY, side, true);
    }

    /**
     * Expose le rectangle logique associé au sprite complet de la boutique principale (à droite).
     * Cela sert aux règles visuelles de placement des arbres, plus strictes que la seule hitbox.
     */
    public Rectangle getBarnLogicalDrawBounds() {
        Rectangle fieldBounds = getFieldBounds();
        syncBarnTileSize(fieldBounds);
        return new Rectangle(Barn.getDrawX(), Barn.Y, Barn.WIDTH, Barn.HEIGHT);
    }

    /**
     * On expose les bornes logiques complètes du sprite de la menuiserie.
     */
    public Rectangle getWorkshopLogicalDrawBounds() {
        return Workshop.getDrawBounds(getFieldLogicalBounds());
    }

    /**
     * On expose la hitbox logique basse utilisée pour bloquer le passage devant la menuiserie.
     */
    public Rectangle getWorkshopLogicalCollisionBounds() {
        return Workshop.getCollisionBounds(getFieldLogicalBounds());
    }

    /**
     * On expose les bornes logiques complètes du sprite de l'échoppe.
     */
    public Rectangle getStallLogicalDrawBounds() {
        return Stall.getDrawBounds(getFieldLogicalBounds());
    }

    /**
     * On expose la hitbox logique basse utilisée pour bloquer le passage devant l'échoppe.
     */
    public Rectangle getStallLogicalCollisionBounds() {
        return Stall.getCollisionBounds(getFieldLogicalBounds());
    }

    /**
     * Zone pierre + herbe autour de la boutique principale (à droite) :
     * elle colle à la boutique principale (à droite),
     * mais on retire volontairement la première rangée du haut
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

    /**
     * On redirige vers le helper central qui calcule le centre logique d'une case.
     */
    private Point getLogicalCellCenter(int gridX, int gridY, Rectangle fieldBounds) {
        return buildLogicalCellCenter(gridX, gridY, fieldBounds);
    }

    /**
     * Expose le centre logique d'une case dans le repère actuel du champ.
     * Le démarrage du joueur peut ainsi se recaler sur une vraie case libre.
     */
    public Point getLogicalCellCenter(int gridX, int gridY) {
        if (isInsideGrid(gridX, gridY)) {
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

        if (isInsideGrid(gridX, gridY)) {
            return null;
        }

        return new Point(gridX, gridY);
    }

    /**
     * Les arbres ne doivent jamais apparaître sur la zone visuelle de l'entrée de grotte.
     * Cette info est lue par les règles de placement.
     */
    public boolean isFarmCaveDecorationCell(int gridX, int gridY) {
        Rectangle cellBounds = getLogicalCellBounds(gridX, gridY);
        Rectangle caveImageBounds = getFarmCaveImageLogicalBounds();
        return cellBounds != null
                && caveImageBounds != null
                && caveImageBounds.intersects(cellBounds);
    }

    /**
     * Zone logique utilisée pour déclencher l'entrée de la grotte.
     */
    public Rectangle getFarmCaveEntryTriggerLogicalBounds() {
        Rectangle triggerGridBounds = resolveFarmCaveEntryTriggerGridBounds();
        Rectangle fieldBounds = getFieldBounds();
        int tileSize = getTileSize(fieldBounds);
        int logicalStartX = -(fieldBounds.width / 2);
        int logicalStartY = -(fieldBounds.height / 2);
        int horizontalOffset = (int) Math.round(tileSize * FARM_CAVE_ENTRY_HORIZONTAL_OFFSET_TILES);

        return new Rectangle(
                logicalStartX + (triggerGridBounds.x * tileSize) + horizontalOffset,
                logicalStartY + (triggerGridBounds.y * tileSize),
                triggerGridBounds.width * tileSize,
                triggerGridBounds.height * tileSize
        );
    }

    /**
     * Zone de blocage élargie autour de l'entrée, utilisée uniquement
     * par les collisions non-joueur (lapins) et par les règles de spawn d'arbres.
     */
    public Rectangle getFarmCaveBlockingLogicalBounds() {
        Rectangle fieldBounds = getFieldBounds();
        Rectangle entranceScreenBounds = computeFarmCaveEntranceScreenBounds(fieldBounds);
        if (fieldBounds == null || entranceScreenBounds == null) {
            return null;
        }

        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        int blockingPadding = Math.max(5, Math.min(entranceScreenBounds.width, entranceScreenBounds.height) / 6);
        return new Rectangle(
                entranceScreenBounds.x - centerX - blockingPadding,
                entranceScreenBounds.y - centerY - blockingPadding,
                entranceScreenBounds.width + (blockingPadding * 2),
                entranceScreenBounds.height + (blockingPadding * 2)
        );
    }

    /**
     * Emprise exacte du sprite de l'entrée de grotte dans le repère logique du champ.
     * Elle sert à marquer les cases qui contiennent visuellement un morceau de l'image.
     */
    public Rectangle getFarmCaveImageLogicalBounds() {
        Rectangle fieldBounds = getFieldBounds();
        Rectangle entranceScreenBounds = computeFarmCaveEntranceScreenBounds(fieldBounds);
        if (fieldBounds == null || entranceScreenBounds == null) {
            return null;
        }

        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        return new Rectangle(
                entranceScreenBounds.x - centerX,
                entranceScreenBounds.y - centerY,
                entranceScreenBounds.width,
                entranceScreenBounds.height
        );
    }

    /**
     * Position de retour depuis la grotte :
     * on place le joueur juste à droite de l'entrée visuelle,
     * mais hors de la zone de trigger pour éviter une ré-entrée immédiate.
     */
    public Point getFarmCaveReturnOffset() {
        Rectangle mouthBounds = resolveFarmCaveMouthGridBounds();
        Rectangle triggerBounds = resolveFarmCaveEntryTriggerGridBounds();
        int preferredRow = mouthBounds.y;
        int startColumn = mouthBounds.x + mouthBounds.width + 1;

        int[] candidateRows = {
                preferredRow,
                Math.max(0, preferredRow - 1),
                Math.min(getRowCount() - 1, preferredRow + 1)
        };

        for (int row : candidateRows) {
            for (int xOffset = 0; xOffset < 4; xOffset++) {
                int column = startColumn + xOffset;
                if (isInsideGrid(column, row) || triggerBounds.contains(column, row)) {
                    continue;
                }

                Point candidateCell = new Point(column, row);
                if (!isFarmableCell(candidateCell) || isFarmCaveDecorationCell(column, row)) {
                    continue;
                }

                return getLogicalCellCenter(column, row);
            }
        }

        return getInitialPlayerOffset();
    }

    /**
     * Convertit le stade du modèle en image d'affichage.
     */
    private Image getCultureImage(Culture culture) {
        if (culture == null) {
            return null;
        }

        if (culture.getType() == Type.CAROTTE) {
            return getCarrotCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.ROSE) {
            return getRoseCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.TULIPE) {
            return getTulipCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.MARGUERITE) {
            return getDaisyCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.RADIS) {
            return getRadishCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.CHOUFLEUR) {
            return getCauliflowerCultureImage(culture.getStadeCroissance());
        }
        if (culture.getType() == Type.NENUPHAR || culture.getType() == Type.IRIS_DES_MARAIS) {
            return getLeftZoneFlowerCultureImage(culture.getType(), culture.getStadeCroissance());
        }

        return null;
    }

    private Image getRoseCultureImage(Stade stade) {
        return getStageImage(stade, roseJeunePousseImage, roseIntermediaireImage, roseMatureImage, roseFletrieImage);
    }

    private Image getTulipCultureImage(Stade stade) {
        return getStageImage(stade, tulipeJeunePousseImage, tulipeIntermediaireImage, tulipeMatureImage, tulipeFletrieImage);
    }

    private Image getDaisyCultureImage(Stade stade) {
        return getStageImage(stade, margueriteJeunePousseImage, margueriteIntermediaireImage, margueriteMatureImage, margueriteFletrieImage);
    }

    /**
     * Les fleurs vendues uniquement à l'échoppe suivent toutes la même logique visuelle :
     * on choisit simplement le bon lot d'images, puis on laisse le stade faire le reste.
     */
    private Image getLeftZoneFlowerCultureImage(Type type, Stade stade) {
        if (type == Type.NENUPHAR) {
            return getStageImage(stade, nenupharJeuneImage, nenupharIntermediaireImage, nenupharMatureImage, nenupharFletriImage);
        }
        if (type == Type.IRIS_DES_MARAIS) {
            return getStageImage(stade, irisMaraisJeuneImage, irisMaraisIntermediaireImage, irisMaraisMatureImage, irisMaraisFletrieImage);
        }
        return null;
    }

    private Image getStageImage(Stade stade, Image jeuneImage, Image intermediaireImage, Image matureImage, Image fletrieImage) {
        if (stade == Stade.JEUNE_POUSSE) {
            return jeuneImage;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return intermediaireImage;
        }
        if (stade == Stade.MATURE) {
            return matureImage;
        }
        if (stade == Stade.FLETRIE) {
            return fletrieImage;
        }
        return null;
    }

    private Image getCauliflowerCultureImage(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return choufleurJeuneImage;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return choufleurIntermediaireImage;
        }
        if (stade == Stade.MATURE) {
            return choufleurMatureImage;
        }
        if (stade == Stade.FLETRIE) {
            return choufleurFletriImage;
        }
        return null;
    }

    private Image getRadishCultureImage(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return radisJeuneImage;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return radisIntermediaireImage;
        }
        if (stade == Stade.MATURE) {
            return radisMatureImage;
        }
        if (stade == Stade.FLETRIE) {
            return radisFletriImage;
        }
        return null;
    }

    private Image getCarrotCultureImage(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return carotteJeunePousseImage;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return carotteIntermediaireImage;
        }
        if (stade == Stade.MATURE) {
            return carotteMatureImage;
        }
        if (stade == Stade.FLETRIE) {
            return carotteFletrieImage;
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

    /** Donne la même clôture, mais dans le repère écran. */
    private Rectangle getScreenFenceBounds(int gridX, int gridY, CellSide side) {
        return getFenceBounds(gridX, gridY, side, false);
    }

    /** Centralise le calcul de bounds pour éviter deux versions quasi identiques. */
    private Rectangle getFenceBounds(int gridX, int gridY, CellSide side, boolean logical) {
        Rectangle cellBounds = logical ? getLogicalCellBounds(gridX, gridY) : getCellBounds(gridX, gridY);
        if (cellBounds == null || side == null) {
            return null;
        }

        return getFenceBandBounds(cellBounds, side, createFenceMetrics(cellBounds));
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
        } else if (grilleCulture.hasRiver(gridX, gridY)) {
            return getDecorativeRiverTile(gridY);
        } else if (grilleCulture.hasPath(gridX, gridY)) {
            if (PredefinedFieldLayout.isLeftOfDecorativeRiver(this, gridX) && leftRiverPathTileImage != null) {
                return leftRiverPathTileImage;
            }
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

    /**
     * On prend simplement la première variante chargée quand plusieurs tuiles existent.
     */
    private Image getFirstAvailableTile(Image[] variants) {
        if (variants == null || variants.length == 0) {
            return null;
        }

        return variants[0];
    }

    /**
     * Dessine l'image dans la case sans la déformer.
     */
    private void drawCultureImage(Graphics2D g2, Culture culture, Image cultureImage, int x, int y, int tileSize) {
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
        if (culture != null && culture.getType() == Type.CAROTTE) {
            scale *= getCarrotMapScale(culture.getStadeCroissance());
        } else if (culture != null && culture.getType() == Type.ROSE) {
            scale *= DAISY_MAP_SCALE;
        } else if (culture != null && culture.getType() == Type.TULIPE) {
            scale *= DAISY_MAP_SCALE;
        } else if (culture != null && culture.getType() == Type.MARGUERITE) {
            scale *= DAISY_MAP_SCALE;
        } else if (culture != null && culture.getType() == Type.RADIS) {
            scale *= getRadishMapScale(culture.getStadeCroissance());
        } else if (culture != null && culture.getType() == Type.CHOUFLEUR) {
            scale *= getCauliflowerMapScale(culture.getStadeCroissance());
        } else if (culture != null && culture.getType() == Type.NENUPHAR) {
            scale *= getWaterLilyMapScale(culture.getStadeCroissance());
        } else if (culture != null && culture.getType() == Type.IRIS_DES_MARAIS) {
            scale *= getMarshIrisMapScale(culture.getStadeCroissance());
        }

        int drawWidth = (int) Math.round(imageWidth * scale);
        int drawHeight = (int) Math.round(imageHeight * scale);
        Rectangle visibleBounds = getVisibleCultureBounds(cultureImage, imageWidth, imageHeight);
        int visibleWidth = Math.max(1, visibleBounds.width);
        int visibleHeight = Math.max(1, visibleBounds.height);
        int scaledVisibleWidth = Math.max(1, (int) Math.round(visibleWidth * scale));
        int scaledVisibleHeight = Math.max(1, (int) Math.round(visibleHeight * scale));

        // On centre la zone réellement visible de l'image plutôt que son canevas brut:
        // cela corrige les sprites avec marges transparentes asymétriques.
        int drawX = x + ((tileSize - scaledVisibleWidth) / 2) - (int) Math.round(visibleBounds.x * scale);
        int drawY = y + ((tileSize - scaledVisibleHeight) / 2) - (int) Math.round(visibleBounds.y * scale);

        /*
         * L'iris des marais mature dépasse volontairement de sa case.
         * Si on le centre complètement, sa base retombe trop bas visuellement.
         * On aligne donc le bas réellement visible de l'image sur le centre de la case
         * pour les stades où la fleur commence vraiment à prendre de la hauteur.
         */
        if (shouldAnchorVisibleBottomAtCellCenter(culture)) {
            int visibleTopY = y + (tileSize / 2) - scaledVisibleHeight;
            drawY = visibleTopY - (int) Math.round(visibleBounds.y * scale);
        }

        if (culture != null && culture.getType() == Type.ROSE) {
            drawY += (int) Math.round(tileSize * DAISY_VERTICAL_OFFSET_RATIO);
        } else if (culture != null && culture.getType() == Type.TULIPE) {
            drawY += (int) Math.round(tileSize * DAISY_VERTICAL_OFFSET_RATIO);
        } else if (culture != null && culture.getType() == Type.MARGUERITE) {
            drawY += (int) Math.round(tileSize * DAISY_VERTICAL_OFFSET_RATIO);
        }

        // Les plantes sont elles aussi en pixel art :
        // on évite donc le flou de l'interpolation bilinéaire.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(cultureImage, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * On applique un ancrage spécial aux iris des marais qui montent haut visuellement.
     */
    private boolean shouldAnchorVisibleBottomAtCellCenter(Culture culture) {
        if (culture == null || culture.getType() != Type.IRIS_DES_MARAIS) {
            return false;
        }

        Stade stade = culture.getStadeCroissance();
        return stade == Stade.INTERMEDIAIRE || stade == Stade.MATURE || stade == Stade.FLETRIE;
    }

    /**
     * On repère la vraie zone opaque d'un sprite pour le centrer sans ses marges transparentes.
     */
    private Rectangle getVisibleCultureBounds(Image cultureImage, int imageWidth, int imageHeight) {
        Rectangle cachedBounds = visibleCultureBoundsCache.get(cultureImage);
        if (cachedBounds != null) {
            return cachedBounds;
        }

        BufferedImage bufferedImage = toBufferedImage(cultureImage, imageWidth, imageHeight);
        if (bufferedImage == null) {
            Rectangle fullBounds = new Rectangle(0, 0, imageWidth, imageHeight);
            visibleCultureBoundsCache.put(cultureImage, fullBounds);
            return fullBounds;
        }

        int minX = imageWidth;
        int minY = imageHeight;
        int maxX = -1;
        int maxY = -1;
        for (int pixelY = 0; pixelY < imageHeight; pixelY++) {
            for (int pixelX = 0; pixelX < imageWidth; pixelX++) {
                int alpha = (bufferedImage.getRGB(pixelX, pixelY) >>> 24) & 0xFF;
                if (alpha == 0) {
                    continue;
                }
                minX = Math.min(minX, pixelX);
                minY = Math.min(minY, pixelY);
                maxX = Math.max(maxX, pixelX);
                maxY = Math.max(maxY, pixelY);
            }
        }

        Rectangle visibleBounds = maxX >= minX && maxY >= minY
                ? new Rectangle(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1)
                : new Rectangle(0, 0, imageWidth, imageHeight);
        visibleCultureBoundsCache.put(cultureImage, visibleBounds);
        return visibleBounds;
    }

    /**
     * On choisit l'échelle d'affichage d'une carotte selon son stade.
     */
    private double getCarrotMapScale(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return CARROT_YOUNG_MAP_SCALE;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return CARROT_INTERMEDIATE_MAP_SCALE;
        }
        if (stade == Stade.MATURE) {
            return CARROT_MATURE_MAP_SCALE;
        }
        if (stade == Stade.FLETRIE) {
            return CARROT_WILTED_MAP_SCALE;
        }
        return CARROT_MATURE_MAP_SCALE;
    }

    /**
     * On choisit l'échelle d'affichage d'un radis selon son stade.
     */
    private double getRadishMapScale(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return RADISH_YOUNG_MAP_SCALE;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return RADISH_INTERMEDIATE_MAP_SCALE;
        }
        if (stade == Stade.MATURE) {
            return RADISH_MATURE_MAP_SCALE;
        }
        if (stade == Stade.FLETRIE) {
            return RADISH_WILTED_MAP_SCALE;
        }
        return RADISH_MATURE_MAP_SCALE;
    }

    /**
     * On choisit l'échelle d'affichage d'un chou-fleur selon son stade.
     */
    private double getCauliflowerMapScale(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return CAULIFLOWER_YOUNG_MAP_SCALE;
        }
        if (stade == Stade.INTERMEDIAIRE) {
            return CAULIFLOWER_INTERMEDIATE_MAP_SCALE;
        }
        if (stade == Stade.MATURE) {
            return CAULIFLOWER_MATURE_MAP_SCALE;
        }
        if (stade == Stade.FLETRIE) {
            return CAULIFLOWER_WILTED_MAP_SCALE;
        }
        return CAULIFLOWER_MATURE_MAP_SCALE;
    }

    /**
     * On applique la seule réduction spéciale utile aux jeunes nénuphars.
     */
    private double getWaterLilyMapScale(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return WATER_LILY_YOUNG_MAP_SCALE;
        }
        return 1.0;
    }

    /**
     * On applique la seule réduction spéciale utile aux jeunes iris des marais.
     */
    private double getMarshIrisMapScale(Stade stade) {
        if (stade == Stade.JEUNE_POUSSE) {
            return MARSH_IRIS_YOUNG_MAP_SCALE;
        }
        return 1.0;
    }

    /**
     * On convertit une image générique en `BufferedImage` pour pouvoir lire ses pixels.
     */
    private BufferedImage toBufferedImage(Image image, int imageWidth, int imageHeight) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        if (imageWidth <= 0 || imageHeight <= 0) {
            return null;
        }

        BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imageGraphics = bufferedImage.createGraphics();
        imageGraphics.drawImage(image, 0, 0, null);
        imageGraphics.dispose();
        return bufferedImage;
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

    /**
     * On redirige vers la bonne variante de clôture selon le côté demandé.
     */
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

    /**
     * On dessine une clôture verticale complète, raccords compris avec les segments voisins.
     */
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

     * On factorise donc cette partie commune ici,
     * et chaque variante ajoute ensuite son relief propre.
     */
    private void drawPostBase(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        g2.setColor(FENCE_WOOD_MID);
        g2.fillRect(x + 1, y + 1, getInsetSpan(width, 2), getInsetSpan(height, 2));
    }

    /**
     * Relief d'un poteau "horizontal" :
     * on éclaire légèrement le côté gauche
     * et on assombrit le côté droit.
     */
    private void drawHorizontalPostRelief(Graphics2D g2, int x, int y, int width, int height) {
        int innerHeight = getInsetSpan(height, 2);

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
        int innerWidth = getInsetSpan(width, 2);

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
        g2.fillRect(x + 1, capY, getInsetSpan(width, 2), capHeight);

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(x + 2, capY + 1, getInsetSpan(width, 4), 1);
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
        g2.fillRect(capX, y + 1, capWidth, getInsetSpan(height, 2));

        g2.setColor(FENCE_CAP_LIGHT);
        g2.fillRect(capX + (outerOnRight ? 0 : 1), y + 2, 1, getInsetSpan(height, 4));
    }

    /**
     * On compose un poteau horizontal complet à partir de sa base, de son relief et de son capuchon.
     */
    private void drawHorizontalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        drawPostBase(g2, x, y, width, height);
        drawHorizontalPostRelief(g2, x, y, width, height);
        drawHorizontalPostCap(g2, x, y, width, height, outerOnTop);
    }

    /**
     * On compose un poteau vertical complet à partir de sa base, de son relief et de son capuchon.
     */
    private void drawVerticalPost(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        drawPostBase(g2, x, y, width, height);
        drawVerticalPostRelief(g2, x, y, width, height);
        drawVerticalPostCap(g2, x, y, width, height, outerOnRight);
    }

    /**
     * On dessine une latte horizontale avec son petit relief pixel-art.
     */
    private void drawHorizontalSlat(Graphics2D g2, int x, int y, int width, int height, boolean outerOnTop) {
        drawSlatBase(g2, x, y, width, height);

        g2.setColor(outerOnTop ? FENCE_SLAT_LIGHT : FENCE_SLAT_DARK);
        g2.fillRect(x + 1, outerOnTop ? y + 1 : y + height - 2, getInsetSpan(width, 2), 1);

        if (height >= 4) {
            g2.setColor(FENCE_SLAT_DARK);
            g2.fillRect(x + Math.max(2, width / 3), y + 1, 1, getInsetSpan(height, 2));
        }
    }

    /**
     * On dessine une latte verticale avec son petit relief pixel-art.
     */
    private void drawVerticalSlat(Graphics2D g2, int x, int y, int width, int height, boolean outerOnRight) {
        drawSlatBase(g2, x, y, width, height);

        g2.setColor(outerOnRight ? FENCE_SLAT_DARK : FENCE_SLAT_LIGHT);
        g2.fillRect(outerOnRight ? x + width - 2 : x + 1, y + 1, 1, getInsetSpan(height, 2));

        if (width >= 4) {
            g2.setColor(FENCE_SLAT_DARK);
            g2.fillRect(x + 1, y + Math.max(2, height / 3), getInsetSpan(width, 2), 1);
        }
    }

    /**
     * Dessine tout ce qui appartient à une seule case:
     * le sol, la culture éventuelle, l'animation d'arrosage et le surlignage du joueur.
     * Ce découpage garde paintComponent lisible:
     * la méthode principale orchestre,
     * ce helper s'occupe du détail d'une tuile.
     */
    private void drawCell(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        drawDynamicCell(g2, gridX, gridY, cellBounds);
    }

    /**
     * Partie dynamique d'une case :
     * cultures, compost, animations et overlays liés au gameplay courant.
     */
    private void drawDynamicCell(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (cellBounds == null) {
            return;
        }

        drawCultureLayer(g2, gridX, gridY, cellBounds);
        drawCellGameplayOverlays(g2, gridX, gridY, cellBounds);
    }

    /**
     * On dessine tout ce qui correspond à la culture elle-même dans la case.
     */
    private void drawCultureLayer(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (shouldRedrawGroundDynamically(gridX, gridY)) {
            drawGroundTile(g2, gridX, gridY, cellBounds);
        }

        Culture culture = grilleCulture.getCulture(gridX, gridY);
        Image cultureImage = getCultureImage(culture);
        if (cultureImage != null) {
            drawCultureImage(g2, culture, cultureImage, cellBounds.x, cellBounds.y, cellBounds.width);
        }

        if (shouldAnimateWateredCell(culture)) {
            drawWateringAnimation(g2, cellBounds.x, cellBounds.y, cellBounds.width);
        }
    }

    /**
     * Le cache de terrain contient le décor immobile initial.
     * Dès qu'une case passe en terre labourée, sa tuile de sol doit repasser
     * dans le flux dynamique pour rester visuellement à jour.
     */
    private boolean shouldRedrawGroundDynamically(int gridX, int gridY) {
        return grilleCulture.isLabouree(gridX, gridY);
    }

    /**
     * On superpose ensuite les aides visuelles liées au gameplay de la case.
     */
    private void drawCellGameplayOverlays(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (grilleCulture.hasCompostAt(gridX, gridY)) {
            drawCompostDecoration(g2, cellBounds);
        }

        if (shouldHighlightBridgePlacementCell(gridX, gridY)) {
            drawBridgePlacementHighlight(g2, cellBounds);
        }

        if (isHighlightedFarmableCell(gridX, gridY)) {
            drawCellHighlight(g2, cellBounds);
        }

        if (shouldShowLabourFenceWarning(gridX, gridY)) {
            drawLabourFenceWarningBadge(g2, cellBounds);
        }
    }

    /**
     * On invalide complètement le cache du sol fixe pour forcer son recalcul.
     */
    private void invalidateStaticTerrainCache() {
        staticTerrainCache = null;
        cachedTerrainWidth = -1;
        cachedTerrainHeight = -1;
        cachedTerrainFieldBounds = null;
    }

    /**
     * Certains changements (comme la pose d'un chemin) modifient le sol
     * sans passer par la partie dynamique des cases.
     * On expose donc un point d'entrée simple pour forcer la reconstruction
     * du cache de terrain au prochain repaint.
     */
    public void refreshStaticTerrain() {
        invalidateStaticTerrainCache();
        repaint();
    }

    /**
     * On reconstruit le cache du terrain fixe uniquement si la taille visible a changé.
     */
    private void ensureStaticTerrainCache() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            invalidateStaticTerrainCache();
            return;
        }

        Rectangle fieldBounds = getFieldBounds();
        if (staticTerrainCache != null
                && cachedTerrainWidth == getWidth()
                && cachedTerrainHeight == getHeight()
                && Objects.equals(cachedTerrainFieldBounds, fieldBounds)) {
            return;
        }

        cachedTerrainWidth = getWidth();
        cachedTerrainHeight = getHeight();
        cachedTerrainFieldBounds = fieldBounds == null ? null : new Rectangle(fieldBounds);
        staticTerrainCache = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D cacheGraphics = staticTerrainCache.createGraphics();
        cacheGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        paintStaticTerrain(cacheGraphics, fieldBounds);
        cacheGraphics.dispose();
    }

    /**
     * Le cache contient uniquement le décor immobile du champ :
     * sol, chemins, rivière et habillage autour de la boutique principale (à droite).
     */
    private void paintStaticTerrain(Graphics2D g2, Rectangle fieldBounds) {
        if (fieldBounds == null) {
            return;
        }

        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Rectangle cellBounds = buildScreenCellBounds(column, row, fieldBounds);
                drawGroundTile(g2, column, row, cellBounds);
                drawBarnTopBushDecoration(g2, column, row, cellBounds);
                drawBarnLeftVerticalBushDecoration(g2, column, row, cellBounds);
                drawBarnRightVerticalBushDecoration(g2, column, row, cellBounds);
            }
        }

        drawFarmCaveEntrance(g2, fieldBounds);
    }

    /**
     * Entrée de grotte visuelle simple côté ferme.
     * On garde uniquement le sprite dédié, sans couloir ni murs ajoutés.
     */
    private void drawFarmCaveEntrance(Graphics2D g2, Rectangle fieldBounds) {
        Rectangle entranceScreenBounds = computeFarmCaveEntranceScreenBounds(fieldBounds);
        if (entranceScreenBounds == null) {
            return;
        }

        g2.setColor(CAVE_ENTRANCE_SHADOW);
        g2.fillOval(
                entranceScreenBounds.x + Math.max(4, entranceScreenBounds.width / 8),
                entranceScreenBounds.y + entranceScreenBounds.height - Math.max(9, entranceScreenBounds.height / 5),
                Math.max(20, entranceScreenBounds.width - Math.max(8, entranceScreenBounds.width / 4)),
                Math.max(7, entranceScreenBounds.height / 6)
        );

        g2.drawImage(
                caveEntranceImage,
                entranceScreenBounds.x,
                entranceScreenBounds.y,
                entranceScreenBounds.width,
                entranceScreenBounds.height,
                this
        );
    }

    /**
     * Position bas-gauche, strictement à gauche de la rivière décorative.
     */
    private Rectangle resolveFarmCaveMouthGridBounds() {
        int mouthWidth = 1;
        int mouthHeight = 1;
        int mouthX = 0;
        int mouthY = Math.max(0, getRowCount() - mouthHeight - 2);
        return new Rectangle(mouthX, mouthY, mouthWidth, mouthHeight);
    }

    /**
     * On fait coïncider la zone de déclenchement avec la bouche visuelle de la grotte.
     */
    private Rectangle resolveFarmCaveEntryTriggerGridBounds() {
        return resolveFarmCaveMouthGridBounds();
    }

    /**
     * On convertit l'entrée de grotte logique en vrai rectangle écran du sprite.
     */
    private Rectangle computeFarmCaveEntranceScreenBounds(Rectangle fieldBounds) {
        Rectangle mouthGridBounds = resolveFarmCaveMouthGridBounds();
        Rectangle mouthScreenBounds = buildScreenAreaBounds(mouthGridBounds, fieldBounds);
        if (mouthScreenBounds == null || caveEntranceImage == null) {
            return null;
        }

        int sourceWidth = caveEntranceImage.getWidth(this);
        int sourceHeight = caveEntranceImage.getHeight(this);
        if (sourceWidth <= 0 || sourceHeight <= 0) {
            return null;
        }

        int targetHeight = Math.max(36, (int) Math.round(mouthScreenBounds.height * 1.72));
        int targetWidth = Math.max(34, (targetHeight * sourceWidth) / sourceHeight);
        int horizontalOffset = (int) Math.round(mouthScreenBounds.width * FARM_CAVE_ENTRY_HORIZONTAL_OFFSET_TILES);
        int entranceX = mouthScreenBounds.x + ((mouthScreenBounds.width - targetWidth) / 2) + horizontalOffset;
        int entranceY = mouthScreenBounds.y + mouthScreenBounds.height - targetHeight;
        return new Rectangle(entranceX, entranceY, targetWidth, targetHeight);
    }

    /**
     * On convertit une petite zone de grille en rectangle écran continu.
     */
    private Rectangle buildScreenAreaBounds(Rectangle gridArea, Rectangle fieldBounds) {
        if (gridArea == null || fieldBounds == null) {
            return null;
        }

        return new Rectangle(
                fieldBounds.x + (gridArea.x * getTileSize(fieldBounds)),
                fieldBounds.y + (gridArea.y * getTileSize(fieldBounds)),
                gridArea.width * getTileSize(fieldBounds),
                gridArea.height * getTileSize(fieldBounds)
        );
    }

    /**
     * Dessine uniquement la tuile de sol.
     * On sépare volontairement ce travail pour éviter de mélanger le choix du fond avec le reste du rendu.
     */
    private void drawGroundTile(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        // Les zones pierreuses de la boutique sont prioritaires :
        // elles doivent écraser le sol "naturel" quel que soit le contenu logique de la case.
        if (isBlockedByBarn(gridX, gridY)
                || grilleCulture.hasPath(gridX, gridY)
                || isBarnStoneBandExtensionCell(gridX, gridY)) {
            drawLayeredStoneWithGrassGroundTile(g2, gridX, cellBounds);
            return;
        }
        if (isBarnTopStoneColumnCell(gridX, gridY)) {
            drawLayeredStoneWithGrassGroundTile(g2, gridX, cellBounds);
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
    private void drawLayeredStoneWithGrassGroundTile(Graphics2D g2, int gridX, Rectangle cellBounds) {
        Image baseGroundTile = resolvePathBaseGroundTile(gridX);
        if (baseGroundTile != null) {
            drawScaledTile(g2, baseGroundTile, cellBounds);
        } else {
            g2.setColor(DEFAULT_GRASS_FILL);
            g2.fillRect(cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height);
        }

        Image stoneWithGrassTile = resolvePathGroundTile(gridX);
        if (stoneWithGrassTile != null) {
            drawScaledTile(g2, stoneWithGrassTile, cellBounds);
        }
    }

    /**
     * Le chemin repose visuellement sur le sol naturel de sa zone.
     * À gauche de la rivière, on garde donc la base marécageuse au lieu de l'herbe standard.
     */
    private Image resolvePathBaseGroundTile(int gridX) {
        if (PredefinedFieldLayout.isLeftOfDecorativeRiver(this, gridX)) {
            return getLeftMarshTile(gridX);
        }
        return getFirstAvailableTile(grassTileImages);
    }

    /**
     * Choisit la bonne tuile de chemin selon la zone du champ.
     * La partie gauche de la rivière garde son propre visuel de pierre
     * pour mieux s'intégrer au marécage.
     */
    private Image resolvePathGroundTile(int gridX) {
        if (PredefinedFieldLayout.isLeftOfDecorativeRiver(this, gridX) && leftRiverPathTileImage != null) {
            return leftRiverPathTileImage;
        }
        return getFirstAvailableTile(pathTileImages);
    }

    /**
     * On dessine une tuile sur toute la case avec une interpolation nette.
     */
    private void drawScaledTile(Graphics2D g2, Image tile, Rectangle cellBounds) {
        if (tile == null || cellBounds == null) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(tile, cellBounds.x, cellBounds.y, cellBounds.width, cellBounds.height, this);
    }

    /**
     * Ajoute une ligne de buissons au-dessus de la boutique :
     * côté gauche jusqu'à la rivière, puis tout le côté droit jusqu'au bord.
     */
    private void drawBarnTopBushDecoration(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (bushTileImage == null || cellBounds == null) {
            return;
        }
        if (!isBarnTopBushCell(gridX, gridY)) {
            return;
        }

        drawDecorativeSprite(g2, bushTileImage, cellBounds);
    }

    /**
     * On dessine les buissons verticaux ancrés à gauche de la zone boutique.
     */
    private void drawBarnLeftVerticalBushDecoration(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (verticalBushTileImage == null || cellBounds == null || !isBarnAnyLeftVerticalBushCell(gridX, gridY)) {
            return;
        }

        drawLeftAnchoredDecorativeSprite(g2, verticalBushTileImage, cellBounds);
    }

    /**
     * On dessine les buissons verticaux ancrés à droite de la zone boutique.
     */
    private void drawBarnRightVerticalBushDecoration(Graphics2D g2, int gridX, int gridY, Rectangle cellBounds) {
        if (verticalBushRightTileImage == null || cellBounds == null || !isBarnAnyRightVerticalBushCell(gridX, gridY)) {
            return;
        }

        drawRightAnchoredDecorativeSprite(g2, verticalBushRightTileImage, cellBounds);
    }

    /**
     * On dit si une case appartient à la ligne de buissons posée au-dessus de la boutique.
     */
    private boolean isBarnTopBushCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        if (barnBlockedGridBounds == null || barnBlockedGridBounds.y <= 0) {
            return false;
        }

        int bushRow = barnBlockedGridBounds.y - 1;
        // L'entrée pavée coupe la ligne du haut en deux tronçons de buissons.
        int stoneStartColumn = barnBlockedGridBounds.x + Math.max(0, (barnBlockedGridBounds.width - BARN_TOP_STONE_COLUMN_WIDTH) / 2);
        int stoneEndColumnExclusive = Math.min(getColumnCount(), stoneStartColumn + BARN_TOP_STONE_COLUMN_WIDTH);
        int riverColumn = findDecorativeRiverColumn();
        int leftBushStartColumn = Math.max(0, riverColumn + 1);
        int rightBushEndColumnExclusive = getColumnCount();

        return gridY == bushRow
                && ((gridX >= leftBushStartColumn && gridX < stoneStartColumn)
                || (gridX >= stoneEndColumnExclusive && gridX < rightBushEndColumnExclusive));
    }

    /**
     * On dit si une case fait partie de la colonne pavée qui monte au-dessus de l'entrée.
     */
    private boolean isBarnTopStoneColumnCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        if (barnBlockedGridBounds == null || barnBlockedGridBounds.y <= 0) {
            return false;
        }

        // Cette "colonne" recrée l'axe d'entrée en pavés qui monte jusqu'en haut de l'écran.
        int stoneStartColumn = barnBlockedGridBounds.x
                + Math.max(0, (barnBlockedGridBounds.width - BARN_TOP_STONE_COLUMN_WIDTH) / 2);
        int stoneEndColumnExclusive = Math.min(getColumnCount(), stoneStartColumn + BARN_TOP_STONE_COLUMN_WIDTH);
        return gridY >= 0
                && gridY < barnBlockedGridBounds.y
                && gridX >= stoneStartColumn
                && gridX < stoneEndColumnExclusive;
    }

    /**
     * On dit si une case porte le buisson vertical gauche principal.
     */
    private boolean isBarnLeftVerticalBushCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        int leftColumn = barnBlockedGridBounds == null ? -1 : barnBlockedGridBounds.x - 1;
        return barnBlockedGridBounds != null
                && leftColumn >= 0
                && gridX == leftColumn
                && gridY >= barnBlockedGridBounds.y
                && gridY < barnBlockedGridBounds.y + barnBlockedGridBounds.height;
    }

    /**
     * On dit si une case porte le petit buisson vertical gauche placé en haut de l'entrée.
     */
    private boolean isBarnEntranceLeftVerticalBushCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        if (barnBlockedGridBounds == null || barnBlockedGridBounds.y <= 0) {
            return false;
        }

        // Variante spéciale : un bush vertical posé sur la case d'entrée en haut à gauche.
        int stoneStartColumn = barnBlockedGridBounds.x
                + Math.max(0, (barnBlockedGridBounds.width - BARN_TOP_STONE_COLUMN_WIDTH) / 2);
        return gridX == stoneStartColumn && gridY == 0;
    }

    /**
     * On regroupe ici toutes les variantes de buisson vertical gauche.
     */
    private boolean isBarnAnyLeftVerticalBushCell(int gridX, int gridY) {
        return isBarnLeftVerticalBushCell(gridX, gridY)
                || isBarnEntranceLeftVerticalBushCell(gridX, gridY);
    }

    /**
     * On dit si une case porte le buisson vertical droit principal.
     */
    private boolean isBarnRightVerticalBushCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        int rightColumn = getColumnCount() - 1;
        return barnBlockedGridBounds != null
                && gridX == rightColumn
                && gridY >= barnBlockedGridBounds.y
                && gridY < barnBlockedGridBounds.y + barnBlockedGridBounds.height;
    }

    /**
     * On dit si une case porte le petit buisson vertical droit placé en haut de l'entrée.
     */
    private boolean isBarnEntranceRightVerticalBushCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        if (barnBlockedGridBounds == null || barnBlockedGridBounds.y <= 0) {
            return false;
        }

        // Symétrique du cas précédent, mais pour la case d'entrée en haut à droite.
        int stoneStartColumn = barnBlockedGridBounds.x
                + Math.max(0, (barnBlockedGridBounds.width - BARN_TOP_STONE_COLUMN_WIDTH) / 2);
        int stoneEndColumnExclusive = Math.min(getColumnCount(), stoneStartColumn + BARN_TOP_STONE_COLUMN_WIDTH);
        return gridX == (stoneEndColumnExclusive - 1) && gridY == 0;
    }

    /**
     * On regroupe ici toutes les variantes de buisson vertical droit.
     */
    private boolean isBarnAnyRightVerticalBushCell(int gridX, int gridY) {
        return isBarnRightVerticalBushCell(gridX, gridY)
                || isBarnEntranceRightVerticalBushCell(gridX, gridY);
    }

    /**
     * Prolonge les mêmes rangées de pierre sous la boutique :
     * tout le reste vers la droite, et au plus une colonne côté gauche
     * sans jamais mordre sur la rivière décorative.
     */
    private boolean isBarnStoneBandExtensionCell(int gridX, int gridY) {
        Rectangle barnBlockedGridBounds = getBarnBlockedGridBounds();
        if (barnBlockedGridBounds == null) {
            return false;
        }

        int leftExtensionColumn = barnBlockedGridBounds.x - 1;
        boolean rightExtension = hasRightStoneExtensionAt(gridX, gridY);
        boolean safeLeftExtension = gridX == leftExtensionColumn
                && gridY >= barnBlockedGridBounds.y
                && gridY < barnBlockedGridBounds.y + barnBlockedGridBounds.height;
        // À gauche, on n'ajoute jamais la colonne si elle empiète sur la rivière décorative.
        safeLeftExtension = safeLeftExtension
                && leftExtensionColumn >= 0
                && !grilleCulture.hasRiver(leftExtensionColumn, gridY)
                && !PredefinedFieldLayout.isLeftOfDecorativeRiver(this, leftExtensionColumn);

        return rightExtension || safeLeftExtension;
    }

    /**
     * On garde la géométrie de la boutique synchronisée avec la taille réelle des cases.
     */
    private void syncBarnTileSize(Rectangle fieldBounds) {
        if (fieldBounds != null && fieldBounds.width > 0 && fieldBounds.height > 0) {
            Barn.updateTileSize(getTileSize(fieldBounds));
        }
    }

    /**
     * On relit la colonne de rivière décorative déjà calculée dans le cache.
     */
    private int findDecorativeRiverColumn() {
        refreshBarnDecorCache();
        return cachedDecorativeRiverColumn;
    }

    /**
     * On relit la zone de cases bloquées par la boutique déjà calculée dans le cache.
     */
    private Rectangle getBarnBlockedGridBounds() {
        refreshBarnDecorCache();
        return cachedBarnBlockedGridBounds;
    }

    /**
     * Les zones décoratives autour de la boutique ne dépendent que de la géométrie du champ.
     * On les recalcule donc uniquement si la taille visible de la grille change.
     */
    private void refreshBarnDecorCache() {
        Rectangle fieldBounds = getFieldBounds();
        if (fieldBounds == null) {
            cachedBarnDecorFieldBounds = null;
            cachedBarnBlockedGridBounds = null;
            cachedDecorativeRiverColumn = -1;
            cachedRightRiverUpperDecorationLogicalBounds = null;
            return;
        }

        if (cachedBarnDecorFieldBounds != null && cachedBarnDecorFieldBounds.equals(fieldBounds)) {
            return;
        }

        // La géométrie décorative dépend uniquement de la taille visible de la grille.
        // On la recalcule donc en bloc quand ce rectangle change.
        cachedBarnDecorFieldBounds = new Rectangle(fieldBounds);
        cachedBarnBlockedGridBounds = computeBarnBlockedGridBounds();
        cachedDecorativeRiverColumn = computeDecorativeRiverColumn();
        cachedRightRiverUpperDecorationLogicalBounds = computeRightRiverUpperDecorationLogicalBounds(
                cachedBarnDecorFieldBounds,
                cachedBarnBlockedGridBounds,
                cachedDecorativeRiverColumn
        );
    }

    /**
     * On retrouve la colonne de rivière décorative à partir de la première ligne de la grille.
     */
    private int computeDecorativeRiverColumn() {
        // La rivière décorative est une colonne continue :
        // il suffit donc de lire la première ligne pour retrouver son index.
        for (int column = 0; column < getColumnCount(); column++) {
            if (grilleCulture.hasRiver(column, 0)) {
                return column;
            }
        }

        return -1;
    }

    /**
     * On reconstruit l'emprise en cases de la boutique à partir du vrai test de blocage.
     */
    private Rectangle computeBarnBlockedGridBounds() {
        int minBlockedColumn = Integer.MAX_VALUE;
        int maxBlockedColumn = Integer.MIN_VALUE;
        int topBlockedRow = Integer.MAX_VALUE;
        int bottomBlockedRow = Integer.MIN_VALUE;

        // On reconstruit ici une "boîte englobante grille" de la boutique
        // à partir du vrai test de blocage, pour que décor et collisions restent alignés.
        for (int column = 0; column < getColumnCount(); column++) {
            for (int row = 0; row < getRowCount(); row++) {
                if (!isBlockedByBarn(column, row)) {
                    continue;
                }

                minBlockedColumn = Math.min(minBlockedColumn, column);
                maxBlockedColumn = Math.max(maxBlockedColumn, column);
                topBlockedRow = Math.min(topBlockedRow, row);
                bottomBlockedRow = Math.max(bottomBlockedRow, row);
            }
        }

        if (minBlockedColumn == Integer.MAX_VALUE) {
            return null;
        }

        return new Rectangle(
                minBlockedColumn,
                topBlockedRow,
                (maxBlockedColumn - minBlockedColumn) + 1,
                (bottomBlockedRow - topBlockedRow) + 1
        );
    }

    /**
     * On calcule la grande zone logique décorative située en haut à droite de la rivière.
     */
    private Rectangle computeRightRiverUpperDecorationLogicalBounds(
            Rectangle fieldBounds,
            Rectangle barnBlockedGridBounds,
            int decorativeRiverColumn
    ) {
        if (fieldBounds == null || barnBlockedGridBounds == null || decorativeRiverColumn < 0) {
            return null;
        }

        int startColumn = Math.max(0, decorativeRiverColumn + 1);
        int endColumn = getColumnCount() - 1;
        int endRow = Math.min(getRowCount() - 1, barnBlockedGridBounds.y + barnBlockedGridBounds.height - 1);
        if (startColumn > endColumn || endRow < 0) {
            return null;
        }

        // Cette emprise correspond à la zone haute décorative côté droit :
        // les lapins n'y montent pas, ils restent limités à la zone de champ utile.
        Rectangle topLeftBounds = buildLogicalCellBounds(startColumn, 0, fieldBounds);
        Rectangle bottomRightBounds = buildLogicalCellBounds(endColumn, endRow, fieldBounds);

        return new Rectangle(
                topLeftBounds.x,
                topLeftBounds.y,
                (bottomRightBounds.x + bottomRightBounds.width) - topLeftBounds.x,
                (bottomRightBounds.y + bottomRightBounds.height) - topLeftBounds.y
        );
    }

    /**
     * On dessine un décor simple centré en bas de la case sans casser son ratio.
     */
    private void drawDecorativeSprite(
            Graphics2D g2,
            Image sprite,
            Rectangle cellBounds
    ) {
        if (sprite == null || cellBounds == null) {
            return;
        }

        int imageWidth = sprite.getWidth(this);
        int imageHeight = sprite.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        int availableWidth = scaleSize(cellBounds.width, BARN_BUSH_FILL_RATIO);
        int availableHeight = scaleSize(cellBounds.height, BARN_BUSH_FILL_RATIO);
        // On conserve le ratio du sprite d'origine puis on l'aligne sur le bas de la case,
        // ce qui donne une lecture plus naturelle pour des buissons ou éléments "plantés".
        double scale = Math.min(
                (double) availableWidth / imageWidth,
                (double) availableHeight / imageHeight
        );

        int drawWidth = scaleSize(imageWidth, scale);
        int drawHeight = scaleSize(imageHeight, scale);
        int horizontalOffset = scaleOffset(cellBounds.width, BARN_BUSH_HORIZONTAL_OFFSET_RATIO);
        int drawX = cellBounds.x + ((cellBounds.width - drawWidth) / 2) + horizontalOffset;
        int upwardOffset = scalePositiveOffset(cellBounds.height);
        int drawY = cellBounds.y + cellBounds.height - drawHeight - upwardOffset;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * On dessine un décor vertical collé au bord gauche de la case.
     */
    private void drawLeftAnchoredDecorativeSprite(Graphics2D g2, Image sprite, Rectangle cellBounds) {
        if (sprite == null || cellBounds == null) {
            return;
        }

        int imageWidth = sprite.getWidth(this);
        int imageHeight = sprite.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        int availableWidth = scaleSize(cellBounds.width, VERTICAL_BUSH_WIDTH_RATIO);
        int availableHeight = scaleSize(cellBounds.height, VERTICAL_BUSH_HEIGHT_RATIO);
        // Variante ancrée à gauche pour "coller" le feuillage au bord de la case pavée.
        double scale = Math.min(
                (double) availableWidth / imageWidth,
                (double) availableHeight / imageHeight
        );

        int drawWidth = scaleSize(imageWidth, scale);
        int drawHeight = scaleSize(imageHeight, scale);
        int leftInset = Math.max(0, scaleOffset(cellBounds.width, VERTICAL_BUSH_RIGHT_INSET_RATIO));
        int drawX = cellBounds.x + leftInset - VERTICAL_BUSH_LEFT_PIXEL_SHIFT;
        int upwardOffset = Math.max(0, scaleOffset(cellBounds.height, VERTICAL_BUSH_UPWARD_OFFSET_RATIO));
        int drawY = cellBounds.y + cellBounds.height - drawHeight - upwardOffset;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * On dessine un décor vertical collé au bord droit de la case.
     */
    private void drawRightAnchoredDecorativeSprite(Graphics2D g2, Image sprite, Rectangle cellBounds) {
        if (sprite == null || cellBounds == null) {
            return;
        }

        int imageWidth = sprite.getWidth(this);
        int imageHeight = sprite.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        int availableWidth = scaleSize(cellBounds.width, VERTICAL_BUSH_WIDTH_RATIO);
        int availableHeight = scaleSize(cellBounds.height, VERTICAL_BUSH_HEIGHT_RATIO);
        // Même principe, mais ancré à droite pour les variantes symétriques.
        double scale = Math.min(
                (double) availableWidth / imageWidth,
                (double) availableHeight / imageHeight
        );

        int drawWidth = scaleSize(imageWidth, scale);
        int drawHeight = scaleSize(imageHeight, scale);
        int rightInset = Math.max(0, scaleOffset(cellBounds.width, VERTICAL_BUSH_RIGHT_INSET_RATIO));
        int drawX = cellBounds.x + cellBounds.width - drawWidth - rightInset + VERTICAL_BUSH_RIGHT_PIXEL_SHIFT;
        int upwardOffset = Math.max(0, scaleOffset(cellBounds.height, VERTICAL_BUSH_UPWARD_OFFSET_RATIO));
        int drawY = cellBounds.y + cellBounds.height - drawHeight - upwardOffset;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(sprite, drawX, drawY, drawWidth, drawHeight, this);
    }

    /**
     * On dessine la base commune d'une latte avant d'y ajouter son relief.
     */
    private void drawSlatBase(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(FENCE_OUTLINE);
        g2.fillRect(x, y, width, height);

        // Les lattes sont volontairement plus orangées que les poteaux
        // pour se détacher immédiatement de la terre du champ.
        g2.setColor(FENCE_SLAT_FILL);
        g2.fillRect(x + 1, y + 1, getInsetSpan(width, 2), getInsetSpan(height, 2));
    }

    /**
     * On retire un inset global tout en gardant au moins un pixel visible.
     */
    private int getInsetSpan(int size, int totalInset) {
        return Math.max(1, size - totalInset);
    }

    /**
     * On redimensionne une valeur entière avec un ratio en gardant au moins 1 pixel.
     */
    private int scaleSize(int size, double ratio) {
        return Math.max(1, (int) Math.round(size * ratio));
    }

    /**
     * On transforme un ratio en décalage entier simple.
     */
    private int scaleOffset(int size, double ratio) {
        return (int) Math.round(size * ratio);
    }

    /**
     * On force un décalage calculé à rester positif pour les petits sprites décoratifs.
     */
    private int scalePositiveOffset(int size) {
        return Math.max(0, scaleOffset(size, FieldPanel.BARN_BUSH_UPWARD_OFFSET_RATIO));
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
        drawFilledOverlayWithDoubleBorder(g2, cellBounds, HIGHLIGHT_FILL, HIGHLIGHT_BORDER);
    }

    /**
     * Les cases de pose du pont doivent rester visibles sans écraser complètement le sol.
     * On reprend donc le même langage visuel que les autres overlays du champ,
     * mais avec une palette bleutée liée à l'idée de franchissement de rivière.
     */
    private void drawBridgePlacementHighlight(Graphics2D g2, Rectangle cellBounds) {
        drawFilledOverlayWithDoubleBorder(g2, cellBounds, BRIDGE_PLACEMENT_FILL, BRIDGE_PLACEMENT_BORDER);
    }

    /** Dit si la case active doit montrer l'avertissement rouge de labourage. */
    private boolean shouldShowLabourFenceWarning(int gridX, int gridY) {
        return highlightedCell != null
                && highlightedCell.x == gridX
                && highlightedCell.y == gridY
                && isFarmableCell(highlightedCell)
                && !grilleCulture.isLabouree(gridX, gridY)
                && !grilleCulture.hasPath(gridX, gridY)
                && !grilleCulture.hasCompostAt(gridX, gridY)
                && !grilleCulture.hasRiver(gridX, gridY)
                && grilleCulture.isLabourBlockedByAdjacentFence(gridX, gridY);
    }

    /** Dessine un petit repère rouge directement dans la case du joueur. */
    private void drawLabourFenceWarningBadge(Graphics2D g2, Rectangle cellBounds) {
        int badgeSize = Math.max(10, cellBounds.width / 4);
        int badgeX = cellBounds.x + cellBounds.width - badgeSize - 5;
        int badgeY = cellBounds.y + 5;
        int arc = Math.max(6, badgeSize / 2);

        g2.setColor(LABOUR_WARNING_BADGE_FILL);
        g2.fillRoundRect(badgeX, badgeY, badgeSize, badgeSize, arc, arc);

        g2.setColor(LABOUR_WARNING_BADGE_BORDER);
        g2.drawRoundRect(badgeX, badgeY, badgeSize - 1, badgeSize - 1, arc, arc);
        g2.drawLine(badgeX + 3, badgeY + 3, badgeX + badgeSize - 4, badgeY + badgeSize - 4);
        g2.drawLine(badgeX + badgeSize - 4, badgeY + 3, badgeX + 3, badgeY + badgeSize - 4);
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

            drawFilledOverlayWithDoubleBorder(g2, cellBounds, COMPOST_RANGE_FILL, COMPOST_RANGE_BORDER);
        }
    }

    private boolean shouldHighlightBridgePlacementCell(int gridX, int gridY) {
        return bridgePlacementHighlightVisible && isBridgePlacementCandidateCell(gridX, gridY);
    }

    /** Parcourt les clôtures pour dessiner soit le bois, soit les barres de vie. */
    private void drawPlacedFenceLayer(Graphics2D g2, boolean healthBarLayer) {
        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Rectangle cellBounds = getCellBounds(column, row);
                if (cellBounds == null) {
                    continue;
                }

                for (CellSide side : CellSide.values()) {
                    if (grilleCulture.hasFence(column, row, side)) {
                        if (healthBarLayer) {
                            drawFenceHealthBar(g2, column, row, side);
                        } else {
                            drawFence(g2, column, row, cellBounds, side);
                        }
                    }
                }
            }
        }
    }

    /**
     * Les clôtures sont dessinées après les cases pour rester au premier plan.
     * On parcourt donc la grille une deuxième fois, mais
     * uniquement les segments de clôture déjà posés.
     */
    private void drawPlacedFences(Graphics2D g2) {
        drawPlacedFenceLayer(g2, false);
    }

    /**
     * La barre de vie ne doit apparaître que quand elle apporte vraiment une info.
     */
    private void drawFenceHealthBars(Graphics2D g2) {
        drawPlacedFenceLayer(g2, true);
    }

    /** Dessine une petite barre au-dessus du segment touché. */
    private void drawFenceHealthBar(Graphics2D g2, int cellX, int cellY, CellSide side) {
        if (!grilleCulture.shouldShowFenceHealthBar(cellX, cellY, side)) {
            return;
        }

        Rectangle fenceBounds = getScreenFenceBounds(cellX, cellY, side);
        if (fenceBounds == null) {
            return;
        }

        Rectangle fieldBounds = getFieldBounds();
        int barWidth = Math.max(26, Math.max(fenceBounds.width, fenceBounds.height) + 10);
        int barHeight = Math.max(6, Math.min(10, Math.max(fenceBounds.width, fenceBounds.height) / 5));
        int barX = fenceBounds.x + ((fenceBounds.width - barWidth) / 2);
        int barY = fenceBounds.y - barHeight - 7;
        barX = clampInt(barX, fieldBounds.x + 2, fieldBounds.x + fieldBounds.width - barWidth - 2);
        barY = Math.max(fieldBounds.y + 2, barY);

        int frameArc = Math.max(6, barHeight + 2);
        int innerPadding = 2;
        int innerWidth = Math.max(1, barWidth - (innerPadding * 2));
        int innerHeight = Math.max(1, barHeight - (innerPadding * 2));
        double integrityRatio = grilleCulture.getFenceIntegrityRatio(cellX, cellY, side);
        int fillWidth = (int) Math.round(innerWidth * integrityRatio);

        g2.setColor(FENCE_HEALTH_FRAME);
        g2.fillRoundRect(barX, barY, barWidth, barHeight, frameArc, frameArc);

        g2.setColor(FENCE_HEALTH_BACKGROUND);
        g2.fillRoundRect(
                barX + innerPadding,
                barY + innerPadding,
                innerWidth,
                innerHeight,
                Math.max(4, frameArc - 2),
                Math.max(4, frameArc - 2)
        );

        if (fillWidth > 0) {
            g2.setColor(getFenceHealthColor(integrityRatio));
            g2.fillRoundRect(
                    barX + innerPadding,
                    barY + innerPadding,
                    fillWidth,
                    innerHeight,
                    Math.max(4, frameArc - 2),
                    Math.max(4, frameArc - 2)
            );

            g2.setColor(FENCE_HEALTH_HIGHLIGHT);
            g2.fillRect(barX + innerPadding + 1, barY + innerPadding + 1, Math.max(1, fillWidth - 2), 1);
        }
    }

    /** Choisit une couleur simple selon l'état restant de la clôture. */
    private Color getFenceHealthColor(double integrityRatio) {
        if (integrityRatio <= 0.25) {
            return FENCE_HEALTH_CRITICAL;
        }
        if (integrityRatio <= 0.50) {
            return FENCE_HEALTH_DANGER;
        }
        if (integrityRatio <= 0.75) {
            return FENCE_HEALTH_WARNING;
        }
        return FENCE_HEALTH_STABLE;
    }

    /**
     * Quand un segment casse, on garde un petit "boum" :
     * flash, cœur lumineux, puis quelques éclats.
     */
    private void drawFenceDestructionEffects(Graphics2D g2) {
        long now = System.currentTimeMillis();
        List<FenceDestructionEffect> effects = grilleCulture.getActiveFenceDestructionEffects();
        for (FenceDestructionEffect effect : effects) {
            drawFenceDestructionEffect(g2, effect, now);
        }
    }

    /** Dessine le petit "boum" autour du segment détruit. */
    private void drawFenceDestructionEffect(Graphics2D g2, FenceDestructionEffect effect, long now) {
        Rectangle fenceBounds = getScreenFenceBounds(effect.getGridX(), effect.getGridY(), effect.getSide());
        if (fenceBounds == null) {
            return;
        }

        double progress = effect.getProgress(now);
        if (progress >= 1.0) {
            return;
        }

        Graphics2D explosionGraphics = (Graphics2D) g2.create();
        int centerX = fenceBounds.x + (fenceBounds.width / 2);
        int centerY = fenceBounds.y + (fenceBounds.height / 2);
        int baseRadius = Math.max(8, Math.max(fenceBounds.width, fenceBounds.height));
        int outerRadius = baseRadius + (int) Math.round(progress * 16);
        int innerRadius = Math.max(4, (int) Math.round(baseRadius * (0.9 - (progress * 0.45))));
        int outerAlpha = (int) Math.round(180 * (1.0 - progress));
        int innerAlpha = (int) Math.round(235 * (1.0 - progress));
        int debrisDistance = baseRadius + (int) Math.round(progress * 18);
        int debrisSize = Math.max(3, baseRadius / 3);

        explosionGraphics.setColor(withAlpha(FENCE_EXPLOSION_OUTER, outerAlpha));
        explosionGraphics.fillOval(centerX - outerRadius, centerY - outerRadius, outerRadius * 2, outerRadius * 2);

        explosionGraphics.setColor(withAlpha(FENCE_EXPLOSION_INNER, innerAlpha));
        explosionGraphics.fillOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);

        drawExplosionShard(explosionGraphics, centerX, centerY, -1, -1, debrisDistance, debrisSize, progress);
        drawExplosionShard(explosionGraphics, centerX, centerY, 1, -1, debrisDistance, debrisSize, progress);
        drawExplosionShard(explosionGraphics, centerX, centerY, -1, 1, debrisDistance, debrisSize, progress);
        drawExplosionShard(explosionGraphics, centerX, centerY, 1, 1, debrisDistance, debrisSize, progress);
        drawExplosionShard(explosionGraphics, centerX, centerY, 0, -1, debrisDistance + 4, debrisSize, progress);
        drawExplosionShard(explosionGraphics, centerX, centerY, 0, 1, debrisDistance + 4, debrisSize, progress);
        explosionGraphics.dispose();
    }

    /** Ajoute un éclat carré très simple autour de l'explosion. */
    private void drawExplosionShard(Graphics2D g2, int centerX, int centerY, int directionX, int directionY, int distance, int size, double progress) {
        int drawX = centerX + (directionX * distance) - (size / 2);
        int drawY = centerY + (directionY * distance) - (size / 2);
        int alpha = (int) Math.round(210 * (1.0 - progress));

        g2.setColor(withAlpha(FENCE_EXPLOSION_DEBRIS, alpha));
        g2.fillRect(drawX, drawY, size, size);
    }

    /** Reprend une couleur existante en changeant juste son alpha. */
    private Color withAlpha(Color color, int alpha) {
        int clampedAlpha = clampInt(alpha, 0, 255);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampedAlpha);
    }

    /** Force une valeur à rester entre un min et un max. */
    private int clampInt(int value, int min, int max) {
        if (min > max) {
            return min;
        }

        return Math.max(min, Math.min(max, value));
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

    private void drawFilledOverlayWithDoubleBorder(
            Graphics2D g2,
            Rectangle bounds,
            Color fillColor,
            Color borderColor
    ) {
        if (g2 == null || bounds == null) {
            return;
        }

        g2.setColor(fillColor);
        g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

        g2.setColor(borderColor);
        g2.drawRect(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
        g2.drawRect(bounds.x + 1, bounds.y + 1, bounds.width - 3, bounds.height - 3);
    }

    /**
     * Dessine la grille (avec les images).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Rectangle fieldBounds = getFieldBounds();
        ensureStaticTerrainCache();
        if (staticTerrainCache != null) {
            g2.drawImage(staticTerrainCache, 0, 0, this);
        } else {
            paintStaticTerrain(g2, fieldBounds);
        }

        for (int row = 0; row < getRowCount(); row++) {
            for (int column = 0; column < getColumnCount(); column++) {
                Rectangle cellBounds = buildScreenCellBounds(column, row, fieldBounds);
                drawCell(g2, column, row, cellBounds);
            }
        }

        drawCompostInfluenceOverlay(g2);
        drawPlacedFences(g2);
        drawFenceHealthBars(g2);
        drawFencePreviewOverlay(g2);
        drawFenceDestructionEffects(g2);

        g2.dispose();
    }
}
