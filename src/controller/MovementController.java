package controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import model.*;
import view.*;

/**
 * Le contrôleur chargé de gérer les intéractions.
 */
public class MovementController implements KeyListener {
    private final MovementModel model;
    private final GrilleCulture grilleCulture;

    public MovementController(MovementModel model, MovementView view, SidebarPanel sidebarPanel, GrilleCulture grilleCulture) {
        this.model = model;
        this.grilleCulture = grilleCulture;
        // On s'abonne aux événements clavier.
        view.addKeyListener(this);

        JButton plantButton = sidebarPanel.getPlantButton();
        plantButton.addActionListener(this::planterSurCaseActive);

        JButton cleanButton = sidebarPanel.getCleanButton();
        cleanButton.addActionListener(this::nettoyerCaseActive);
    }

    /**
     * On plante uniquement sur la case actuellement surlignée.
     * Pour le moment, le bouton utilise un type par défaut unique.
     */
    private void planterSurCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }

        // Tant qu'il n'existe pas encore de sélecteur de graines, on plante toujours le même type.
        if (grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null) {
            grilleCulture.planterCulture(activeFieldCell.x, activeFieldCell.y, Type.FLEURS);
        }
    }

    /**
     * Le bouton nettoyer retire uniquement une culture flétrie sur la case du jardinier.
     */
    private void nettoyerCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }
        grilleCulture.nettoyerCultureFletrie(activeFieldCell.x, activeFieldCell.y);
    }

    // On implémente KeyListener (Gestion des Touches)
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // On récupère l'unité du joueur "actif" à contrôler
        Unit player = model.getPlayerUnit();
        if (player == null) return; // Sécurité si pas de joueur
        
        // Mise à jour des flags selon la touche clavier enfoncée
        if (key == KeyEvent.VK_UP) {
            player.setMoveUp(true);
            player.setMoveDown(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_DOWN) {
            player.setMoveDown(true);
            player.setMoveUp(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_LEFT) {
            player.setMoveLeft(true);
            player.setMoveRight(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
        else if (key == KeyEvent.VK_RIGHT) {
            player.setMoveRight(true);
            player.setMoveLeft(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
    }

    // Désactivation de tous les flags de déplacement de l'unité lors du relâchement d’une touche.
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        Unit player = model.getPlayerUnit();
        if (player == null) return;
        
        // Désactivation des flags sur l'unité
        if (key == KeyEvent.VK_UP) player.setMoveUp(false);
        if (key == KeyEvent.VK_DOWN) player.setMoveDown(false);
        if (key == KeyEvent.VK_LEFT) player.setMoveLeft(false);
        if (key == KeyEvent.VK_RIGHT) player.setMoveRight(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
