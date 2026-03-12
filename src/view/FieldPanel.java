package view;

import model.GrilleCulture;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import model.Unit;

/**
 * Panneau d'affichage du champ, compose d'une grille d'images.
 */
public class FieldPanel extends JPanel {
    private static final int PREF_WIDTH = 1010;
    private static final int PREF_HEIGHT = 580;
    private static final int INNER_PADDING = 24;
    private static final int VERTICAL_OFFSET = 34;
    private static final Color HIGHLIGHT_FILL = new Color(255, 255, 120, 90);
    private static final Color HIGHLIGHT_BORDER = new Color(255, 215, 0, 210);

    // La vue ne porte plus sa propre grille: elle affiche directement celle du modèle.
    private final GrilleCulture grilleCulture;
    private final Image tileImage;
    private Point highlightedCell;

    /**
     * Initialise le champ et charge l'image d'une parcelle.
     */
    public FieldPanel(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
        this.tileImage = ImageLoader.load("/assets/Terre.png");
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
        if (this.highlightedCell == null ? highlightedCell == null : this.highlightedCell.equals(highlightedCell)) {
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
     * Dessine la grille (avec les images).
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        Rectangle fieldBounds = getFieldBounds();
        int tileSize = fieldBounds.width / getColumnCount();
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
