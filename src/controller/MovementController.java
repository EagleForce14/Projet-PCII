package controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import model.culture.Culture;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.movement.MovementModel;
import model.movement.Unit;
import model.management.Inventaire;
import model.management.Money;
import model.runtime.GamePauseController;
import model.shop.Shop;
import view.*;
import view.shop.ShopOverlay;

/**
 * Le contrôleur chargé de gérer les intéractions.
 */
public class MovementController implements KeyListener, MouseListener {
    private final MovementModel model;
    private final GrilleCulture grilleCulture;
    private final Money playerMoney;
    private final Shop shop;
    private final Inventaire inventaire;
    private final MovementView movementView;
    private final EnemyView enemyView;
    private final ShopOverlay shopOverlay;
    private final GamePauseController pauseController;

    // Constructeur de la classe
    public MovementController(MovementModel model, MovementView view, EnemyView enemyView, SidebarPanel sidebarPanel,
                              GrilleCulture grilleCulture, Money playerMoney, Shop shop, Inventaire inventaire,
                              ShopOverlay shopOverlay) {
        this.model = model;
        this.grilleCulture = grilleCulture;
        this.playerMoney = playerMoney;
        this.shop = shop;
        this.inventaire = inventaire;
        this.movementView = view;
        this.enemyView = enemyView;
        this.shopOverlay = shopOverlay;
        this.pauseController = GamePauseController.getInstance();
        // On s'abonne aux événements clavier.
        view.addKeyListener(this);
        // On s'abonne aussi a la couche du joueur.
        // Selon l'ordre des couches Swing, c'est parfois elle qui recoit le clic.
        view.addMouseListener(this);
        enemyView.addMouseListener(this);

        JButton plantButton = sidebarPanel.getPlantButton();
        plantButton.addActionListener(this::planterSurCaseActive);

        JButton harvestButton = sidebarPanel.getHarvestButton();
        harvestButton.addActionListener(this::recolterCaseActive);

        JButton waterButton = sidebarPanel.getWaterButton();
        waterButton.addActionListener(this::arroserCaseActive);

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
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }

        // Tant qu'il n'existe pas encore de sélecteur de graines, on plante une tulipe par défaut.
        if (grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null) {
            grilleCulture.planterCulture(activeFieldCell.x, activeFieldCell.y, Type.TULIPE,inventaire);
        }
    }

    /**
     * La récolte ne s'exécute que sur une plante mature, puis on crédite le joueur.
     */
    private void recolterCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

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
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }
        grilleCulture.nettoyerCultureFletrie(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * L'arrosage n'est disponible que sur une case occupée par une culture intermédiaire.
     */
    private void arroserCaseActive(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }
        grilleCulture.arroserCulture(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Ouvre la boutique plein écran et fige le jeu tant que le panneau reste visible.
     */
    private void ouvrirBoutique(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement();
        shopOverlay.openShop();
    }

    // On implémente KeyListener (Gestion des Touches)
    @Override
    public void keyPressed(KeyEvent e) {
        if (pauseController.isPaused()) {
            return;
        }

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
        if (pauseController.isPaused()) {
            stopPlayerMovement();
            return;
        }

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

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (pauseController.isPaused()) {
            return;
        }

        enemyView.handleWorldClick(e.getPoint());
        // On rend immédiatement le focus au panneau de déplacement
        // pour que les flèches continuent de répondre après un clic souris.
        movementView.requestFocusInWindow();
    }

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    /**
     * On remet tous les flags a false pour eviter qu'une direction reste "collee"
     * pendant l'ouverture de la boutique.
     */
    private void stopPlayerMovement() {
        Unit player = model.getPlayerUnit();
        if (player == null) {
            return;
        }

        player.setMoveUp(false);
        player.setMoveDown(false);
        player.setMoveLeft(false);
        player.setMoveRight(false);
    }
}
