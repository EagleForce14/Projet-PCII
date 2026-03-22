package view;

import model.Culture;
import model.GrilleCulture;
import model.Stade;
import model.Unit;

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
    private static final int VERTICAL_OFFSET = 34;

    // Couleurs utilisées pour surligner la case actuellement occupée par le joueur.
    private static final Color HIGHLIGHT_FILL = new Color(255, 255, 120, 90);
    private static final Color HIGHLIGHT_BORDER = new Color(255, 215, 0, 210);

    // Couleurs de l'effet visuel affiché après un arrosage réussi.
    private static final Color WATERED_FILL = new Color(70, 170, 255);
    private static final Color WATERED_BORDER = new Color(220, 245, 255);

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

        g2.dispose();
    }
}
