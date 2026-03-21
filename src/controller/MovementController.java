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
    private final Money playerMoney;
    private final Shop shop;
    private final Inventaire inventaire;
    private ShopThread shopThread;

    // Constructeur de la classe

    public MovementController(MovementModel model, MovementView view, SidebarPanel sidebarPanel,
                              GrilleCulture grilleCulture, Money playerMoney, Shop shop, Inventaire inventaire) {
        this.model = model;
        this.grilleCulture = grilleCulture;
        this.playerMoney = playerMoney;
        this.shop = shop;
        this.inventaire = inventaire;
        // On s'abonne aux événements clavier.
        view.addKeyListener(this);

        JButton plantButton = sidebarPanel.getPlantButton();
        plantButton.addActionListener(this::planterSurCaseActive);

        JButton harvestButton = sidebarPanel.getHarvestButton();
        harvestButton.addActionListener(this::recolterCaseActive);

        JButton cleanButton = sidebarPanel.getCleanButton();
        cleanButton.addActionListener(this::nettoyerCaseActive);

        JButton shopButton = sidebarPanel.getShopButton();
        shopButton.addActionListener(this::ouvrirBoutique);
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

        // Tant qu'il n'existe pas encore de sélecteur de graines, on plante une tulipe par défaut.
        if (grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null) {
            grilleCulture.planterCulture(activeFieldCell.x, activeFieldCell.y, Type.TULIPE);
        }
    }

    /**
     * La récolte ne s'exécute que sur une plante mature, puis on crédite le joueur.
     */
    private void recolterCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        // Si le jardinier n'est sur aucune case du champ, il n'y a rien à récolter.
        if (activeFieldCell == null) {
            return;
        }

        Culture culture = grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y);
        // On garde la vérification ici même si le bouton est déjà filtré par la vue.
        // Comme ça, la règle reste vraie même si cette méthode est réutilisée ailleurs plus tard.
        if (culture == null || culture.getStadeCroissance() != Stade.MATURE) {
            return;
        }

        // On calcule le gain associé.
        int gain = grilleCulture.recolterCulture(activeFieldCell.x, activeFieldCell.y);

        // Le portefeuille du joueur est mis à jour à part pour garder une logique bien découpée.
        playerMoney.credit(gain);
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

    /** Ouverture de la boutique dans la console POUR l'INSTANT
     * Affichage de tout ce que la boutique contient (graines et installations)
     * Affichage des prix de chaque objet, de la quantité dispo et de l'argent du joueur
     * et affichage du panier
     **/
    private void ouvrirBoutique(ActionEvent event) {
        if (shopThread != null && shopThread.isAlive()) {
            System.out.println("La boutique est deja ouverte dans le terminal.");
            return;
        }

        (new ShopThread(shop, playerMoney)).start();
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
