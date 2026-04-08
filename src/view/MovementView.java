package view;
import model.movement.MovementModel;
import model.movement.Unit;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * On réalise une interface graphique très simple afin de mieux tester la fonctionnalité
 */
public class MovementView extends JPanel {
    // Le modèle contenant la liste des unités à afficher
    private final MovementModel model;
    private final PlayableMapPanel mapPanel;

    public MovementView(MovementModel model, PlayableMapPanel mapPanel) {
        this.model = model;
        this.mapPanel = mapPanel;
        this.setOpaque(false);
        this.setDoubleBuffered(true); // Petite optimisation pour éviter les clignotements
        this.setFocusable(true); // Pour recevoir les événements clavier
    }

    // On boucle sur toutes les unités du modèle pour les dessiner.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Toujours appeler super pour nettoyer le fond

        List<Unit> units = model.getUnits();
        Unit playerUnit = model.getPlayerUnit();
        Point highlightedCell = null;

        // On convertit les bornes du champ dans le repère de cette vue pour dessiner l'unité au centre du champ
        Rectangle fieldBounds = SwingUtilities.convertRectangle(mapPanel.getMapComponent(), mapPanel.getFieldBounds(), this);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        // On calcule ici les positions minimales et maximales autorisées pour que
        // l'unité reste entièrement visible dans la fenêtre.
        int minX = -centerX + (Unit.SIZE / 2);
        int maxX = getWidth() - centerX - (Unit.SIZE / 2);
        int minY = -centerY + (Unit.SIZE / 2);
        int maxY = getHeight() - centerY - (Unit.SIZE / 2);

        // On dessine chaque unité présente dans le modèle
        for (Unit u : units) {
            // On borne la position avant le dessin pour empêcher l'unité de sortir de l'écran.
            u.clampPosition(minX, maxX, minY, maxY);

            // Dessin de la zone d'influence
            g.setColor(new Color(0, 0, 255, 50)); // Bleu semi-transparent
            int radius = Unit.INFLUENCE_RADIUS;
            int circleX = centerX + u.getX() - radius;
            int circleY = centerY + u.getY() - radius;
            g.fillOval(circleX, circleY, radius * 2, radius * 2);
            g.setColor(Color.BLUE);
            g.drawOval(circleX, circleY, radius * 2, radius * 2);

            // La couleur du rectangle
            g.setColor(Color.RED);
            
            // Position relative au centre de la fenêtre
            // Rappel : La position (0,0) de l'unité correspond au centre de l'écran
            int drawX = centerX - (Unit.SIZE / 2) + u.getX(); 
            int drawY = centerY - (Unit.SIZE / 2) + u.getY();

            // La case active est calculée à partir du rectangle réel du joueur,
            // sans tenir compte du cercle d'influence. Un chevauchement entre deux
            // cases annule donc la surbrillance.
            if (u == playerUnit) {
                Rectangle playerBounds = new Rectangle(drawX, drawY, Unit.SIZE, Unit.SIZE);
                Rectangle playerBoundsInField = SwingUtilities.convertRectangle(this, playerBounds, mapPanel.getMapComponent());
                highlightedCell = mapPanel.getFullyOccupiedCell(playerBoundsInField);

                // Une case recouverte par la grange ou par un arbre
                // ne doit jamais devenir la case active du gameplay.
                if (!mapPanel.isFarmableCell(highlightedCell)) {
                    highlightedCell = null;
                }

                /*
                 * Le bonus de vitesse du chemin ne doit pas dependre
                 * de la surbrillance de gameplay.
                 *
                 * Pourquoi ?
                 * La case active impose que tout le rectangle du joueur
                 * soit contenu dans une seule case.
                 * C'est tres bien pour labourer / planter,
                 * mais trop strict pour une sensation de deplacement fluide.
                 *
                 * Pour la vitesse, on lit donc simplement la case situee
                 * sous le centre du joueur.
                 * Des que son centre entre sur un chemin, il accelere.
                 */
                Point playerCenterInField = SwingUtilities.convertPoint(
                        this,
                        drawX + (Unit.SIZE / 2),
                        drawY + (Unit.SIZE / 2),
                        mapPanel.getMapComponent()
                );
                Point movementCell = mapPanel.getGridPositionAt(playerCenterInField.x, playerCenterInField.y);
                u.setCurrentSpeed(mapPanel.resolveMovementSpeed(movementCell));
            }
            
            g.fillRect(drawX, drawY, Unit.SIZE, Unit.SIZE);
        }

        // La condition d'activation des actions est liée à la présence
        // de l'unité déplaçable sur une case valide highlightée du champ.
        model.setActiveFieldCell(highlightedCell);
        mapPanel.setHighlightedCell(highlightedCell);
    }
}
