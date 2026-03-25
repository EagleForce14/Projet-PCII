package view;

import model.EnemyModel;
import model.EnemyUnit;
import model.Inventaire;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

/**
 * Couche d'affichage dédiée aux lapins.
 * Cette vue ne pilote jamais l'IA elle-même: elle lit l'état du modèle,
 * dessine chaque lapin à la bonne position et affiche un overlay pour le lapin sélectionné.
 *
 * La gestion des interactions souris est volontairement déportée dans le contrôleur.
 * Ici, on expose seulement de petits helpers que le contrôleur peut appeler.
 */
public class EnemyView extends JPanel {
    // Taille visuelle du corps du lapin dessiné à l'écran.
    private static final int RABBIT_BODY_RADIUS = 12;
    // Rayon de tolérance pour rendre le clic plus confortable qu'un simple "pile sur le pixel".
    private static final int RABBIT_CLICK_RADIUS = 18;

    // Modèle contenant les lapins actifs.
    private final EnemyModel model;
    // Le champ sert de repère visuel pour convertir les coordonnées logiques en pixels.
    private final FieldPanel fieldPanel;
    // Renderer dédié à la carte d'information du lapin sélectionné.
    private final EnemyStatusOverlay statusOverlay;
    // La vue dédiée à l'inventaire de la grange.
    private final InventoryStatusOverlay inventoryOverlay;
    // Le modele de stock est lu directement a chaque frame pour rester synchronise avec la boutique.
    private final Inventaire inventaire;
    // Lapin actuellement sélectionné par un clic utilisateur.
    private EnemyUnit selectedEnemy;

    /**
     * Prépare uniquement l'état graphique de la vue.
     * Le branchement des listeners souris est fait dans le contrôleur.
     */
    public EnemyView(EnemyModel model, FieldPanel fieldPanel, Inventaire inventaire) {
        this.model = model;
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.statusOverlay = new EnemyStatusOverlay();
        this.inventoryOverlay = new InventoryStatusOverlay();
        this.setOpaque(false);
        this.setDoubleBuffered(true); // Évite les clignotements
    }

    /**
     * Convertit les bornes utiles du champ dans le repère de cette vue.
     * On a besoin de cette conversion car les lapins se déplacent dans un repère centré sur le champ.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    /**
     * Cherche quel lapin est visé par la souris.
     * Le point reçu est la position de la souris en pixels dans EnemyView.
     *
     * On ne demande pas au joueur de cliquer exactement au centre du lapin:
     * si le pointeur tombe dans un petit rayon autour d'un lapin, on considère
     * que ce lapin est bien ciblé.
     *
     * Si plusieurs lapins sont proches, on renvoie simplement le plus proche du curseur.
     * Si aucun lapin n'est assez proche, on renvoie null.
     */
    private EnemyUnit findEnemyAt(Point point) {
        Rectangle fieldBounds = getFieldBoundsInView();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        // Pour savoir si la souris "vise" un lapin, on travaille avec une distance au carré.
        // Cela évite une racine carrée inutile et garde un calcul simple.
        double clickRadiusSquared = (double) RABBIT_CLICK_RADIUS * RABBIT_CLICK_RADIUS;

        EnemyUnit nearestEnemy = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (EnemyUnit enemy : model.getEnemyUnits()) {
            // Les positions des lapins sont stockées dans un repère centré sur le champ.
            // On les convertit donc ici en vraie position écran.
            int rabbitCenterX = centerX + enemy.getX();
            int rabbitCenterY = centerY + enemy.getY();

            // Vecteur entre la souris et le centre du lapin.
            double dx = point.x - rabbitCenterX;
            double dy = point.y - rabbitCenterY;
            double distanceSquared = (dx * dx) + (dy * dy);

            // Si le lapin est trop loin du curseur, ou moins bon qu'un lapin déjà trouvé,
            // on l'ignore et on continue.
            if (distanceSquared > clickRadiusSquared || distanceSquared >= nearestDistanceSquared) {
                continue;
            }

            // À ce stade, c'est le meilleur candidat rencontré jusque-là.
            nearestEnemy = enemy;
            nearestDistanceSquared = distanceSquared;
        }

        return nearestEnemy;
    }

    /**
     * Appelé par le contrôleur lors d'un clic.
     * Le clic ne change pas le comportement du lapin: il choisit seulement quel lapin afficher dans l'overlay.
     */
    public void selectEnemyAt(Point point) {
        // Un clic sur le vide désélectionne naturellement le lapin courant
        // puisque findEnemyAt renverra null.
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Point d'entree unique pour les clics "dans le monde".
     *
     * Le controleur reste tres simple:
     * il transmet juste la position du clic, puis cette vue decide quel lapin
     * est vise dans son propre repere.
     */
    public void handleWorldClick(Point point) {
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Nettoie la sélection si le lapin a quitté l'écran ou a été supprimé du modèle.
     * Cela évite qu'un overlay reste affiché pour un lapin qui n'existe plus.
     */
    private void syncSelectedEnemyState() {
        if (selectedEnemy == null) {
            return;
        }

        if (!model.getEnemyUnits().contains(selectedEnemy) || selectedEnemy.hasFled()) {
            selectedEnemy = null;
        }
    }

    /**
     * Dessine un lapin stylisé très léger.
     * On garde peu de détails pour que la lecture du plateau reste simple.
     */
    private void drawRabbit(Graphics2D g2d, int rabbitCenterX, int rabbitCenterY, boolean isSelected) {
        // Halo discret pour montrer quel lapin pilote actuellement l'overlay.
        if (isSelected) {
            g2d.setColor(new Color(255, 218, 107, 70));
            g2d.fillOval(rabbitCenterX - 20, rabbitCenterY - 20, 40, 40);
        }

        // Les deux rectangles arrondis forment les oreilles.
        g2d.setColor(new Color(245, 232, 236));
        g2d.fillRoundRect(rabbitCenterX - 11, rabbitCenterY - 22, 7, 15, 5, 5);
        g2d.fillRoundRect(rabbitCenterX + 4, rabbitCenterY - 22, 7, 15, 5, 5);

        // On ajoute l'intérieur rosé des oreilles pour éviter une silhouette trop plate.
        g2d.setColor(new Color(230, 176, 195));
        g2d.fillRoundRect(rabbitCenterX - 9, rabbitCenterY - 19, 3, 10, 4, 4);
        g2d.fillRoundRect(rabbitCenterX + 6, rabbitCenterY - 19, 3, 10, 4, 4);

        // Corps principal.
        g2d.setColor(new Color(252, 248, 250));
        g2d.fillOval(
                rabbitCenterX - RABBIT_BODY_RADIUS,
                rabbitCenterY - RABBIT_BODY_RADIUS,
                RABBIT_BODY_RADIUS * 2,
                RABBIT_BODY_RADIUS * 2
        );

        // Petit contour pour que le lapin reste lisible sur les zones claires du décor.
        g2d.setColor(new Color(210, 200, 206));
        g2d.drawOval(
                rabbitCenterX - RABBIT_BODY_RADIUS,
                rabbitCenterY - RABBIT_BODY_RADIUS,
                RABBIT_BODY_RADIUS * 2,
                RABBIT_BODY_RADIUS * 2
        );

        // Deux points sombres pour les yeux.
        g2d.setColor(new Color(74, 58, 60));
        g2d.fillOval(rabbitCenterX - 5, rabbitCenterY - 3, 3, 3);
        g2d.fillOval(rabbitCenterX + 2, rabbitCenterY - 3, 3, 3);

        // Petit nez rosé.
        g2d.setColor(new Color(214, 126, 145));
        g2d.fillOval(rabbitCenterX - 2, rabbitCenterY + 1, 4, 3);
    }

    /**
     * À chaque frame:
     * 1) on synchronise la taille visible de la zone de jeu (pour que les lapins
     * sachent où sont les bords visibles de l'écran.
     * 2) on valide la sélection courante,
     * 3) on dessine tous les lapins,
     * 4) puis on dessine l'overlay du lapin sélectionné.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // EnemyModel a besoin de la vraie taille visible pour calculer correctement les bords de carte.
        if (getWidth() > 0 && getHeight() > 0) {
            model.setViewportSize(getWidth(), getHeight());
        }

        syncSelectedEnemyState();

        List<EnemyUnit> enemies = model.getEnemyUnits();
        // Les lapins partagent le même repère logique que le centre du champ.
        // On reconstitue donc ici ce point d'ancrage avant de transformer leurs coordonnées en pixels.
        Rectangle fieldBounds = getFieldBoundsInView();
        model.setFieldSize(fieldBounds.width, fieldBounds.height);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        // Toute la vue est rendue en 2D avec antialiasing léger
        // pour garder des formes propres malgré les petites tailles de dessin.
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (EnemyUnit enemy : enemies) {
            // Le lapin n'est pas stocké en coordonnées écran.
            // Sa position est un décalage par rapport au centre du champ.
            // Pour le dessiner dans la fenêtre, on ajoute ce décalage à centerX/centerY.
            int rabbitCenterX = centerX + enemy.getX();
            int rabbitCenterY = centerY + enemy.getY();
            drawRabbit(g2d, rabbitCenterX, rabbitCenterY, enemy == selectedEnemy);
        }

        // L'overlay reste fixe à l'écran pour être lisible,
        // même quand le lapin continue à marcher ou change d'état.
        statusOverlay.paint(g2d, selectedEnemy, getWidth());

        // La hotbar d'inventaire est toujours visible.
        // On la place tout en bas et on lui passe aussi les bornes du champ
        // pour s'assurer qu'elle ne vienne pas mordre sur la grille.
        inventoryOverlay.paint(g2d, inventaire, fieldBounds, getWidth(), getHeight());
    }
}
