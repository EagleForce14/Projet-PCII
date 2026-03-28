package main;

import controller.MovementController;

import model.culture.GrilleCulture;
import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.movement.MovementModel;
import model.movement.PhysicsThread;
import model.movement.Unit;
import model.runtime.Jour;
import model.management.Inventaire;
import model.management.Money;
import model.shop.Shop;
import view.*;
import view.shop.ShopOverlay;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

public class Main {
    private static final Dimension GAME_AREA_MINIMUM_SIZE = new Dimension(960, 690);
    private static final Dimension GAME_AREA_PREFERRED_SIZE = new Dimension(1180, 850);

    public static void main(String[] args) {
        JFrame frame = new JFrame("Projet PCII");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(
                GAME_AREA_MINIMUM_SIZE.width + SidebarPanel.SIDEBAR_WIDTH,
                GAME_AREA_MINIMUM_SIZE.height
        ));
        frame.setPreferredSize(new Dimension(
                GAME_AREA_PREFERRED_SIZE.width + SidebarPanel.SIDEBAR_WIDTH,
                GAME_AREA_PREFERRED_SIZE.height
        ));

        Jour jour = new Jour();
        GrilleCulture grilleCulture = new GrilleCulture(jour.getGestionnaireObjectifs());
        Money playerMoney = new Money(50);
        Inventaire inventaire = new Inventaire();
        Shop shop = new Shop();
        FieldPanel fieldPanel = new FieldPanel(grilleCulture);

        MovementModel model = new MovementModel();
        // Le joueur démarre hors du champ, près du coin haut-gauche de la grille.
        Point initialPlayerOffset = fieldPanel.getInitialPlayerOffset();
        Unit playerUnit = new Unit(initialPlayerOffset.x, initialPlayerOffset.y);
        model.setPlayerUnit(playerUnit);

        EnemyModel enemyModel = new EnemyModel();
        enemyModel.setPlayer(playerUnit);
        enemyModel.setGrilleCulture(grilleCulture);
        MovementView movementView = new MovementView(model, fieldPanel);
        movementView.setAlignmentX(0.5f);
        movementView.setAlignmentY(0.5f);

        // Les actions contextuelles sont affichées dans une sidebar dédiée, hors du jeu.
        SidebarPanel actionSidebarPanel = new SidebarPanel(model, grilleCulture, fieldPanel, shop, inventaire);

        EnemyView enemyView = new EnemyView(enemyModel, fieldPanel);
        enemyView.setAlignmentX(0.5f);
        enemyView.setAlignmentY(0.5f);

        InventoryStatusOverlay inventoryStatusOverlay = new InventoryStatusOverlay(fieldPanel, inventaire, model);
        inventoryStatusOverlay.setAlignmentX(0.5f);
        inventoryStatusOverlay.setAlignmentY(0.5f);

        // Cette vue couvre toute la fenêtre et affiche les éléments de décor fixes
        EnvironmentView environmentView = new EnvironmentView(fieldPanel);
        environmentView.setAlignmentX(0.5f);
        environmentView.setAlignmentY(0.5f);

        JPanel fieldLayer = new JPanel(new BorderLayout());
        fieldLayer.setOpaque(false);
        fieldLayer.setAlignmentX(0.5f);
        fieldLayer.setAlignmentY(0.5f);
        fieldLayer.add(fieldPanel, BorderLayout.CENTER);

        JPanel gamePanel = createGamePanel();
        gamePanel.setLayout(new OverlayLayout(gamePanel));
        gamePanel.add(movementView);
        gamePanel.add(enemyView);
        gamePanel.add(inventoryStatusOverlay);
        gamePanel.add(environmentView); // S'affiche derrière les ennemis
        gamePanel.add(fieldLayer); // S'affiche derrière l'environnement

        // Petit overlay HUD pour garder les infos du joueur visibles sans bouger le layout principal.
        JPanel hudPanel = new JPanel(new BorderLayout());
        hudPanel.setOpaque(false);
        hudPanel.setAlignmentX(0.5f);
        hudPanel.setAlignmentY(0.5f);
        hudPanel.add(new TopBarPanel(playerMoney, jour), BorderLayout.NORTH);
        gamePanel.add(hudPanel);
        gamePanel.setComponentZOrder(hudPanel, 0);
        gamePanel.setComponentZOrder(inventoryStatusOverlay, 1);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(gamePanel, BorderLayout.CENTER);
        contentPanel.add(actionSidebarPanel, BorderLayout.EAST);
        frame.setContentPane(contentPanel);

        ShopOverlay shopOverlay = new ShopOverlay(shop, playerMoney, inventaire, movementView);
        frame.setGlassPane(shopOverlay);

        new MovementController(
                model,
                movementView,
                enemyView,
                actionSidebarPanel,
                grilleCulture,
                playerMoney,
                inventaire,
                fieldPanel,
                inventoryStatusOverlay,
                shopOverlay
        );

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        enemyModel.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());

        (new PhysicsThread(model)).start();
        (new EnemyPhysicsThread(enemyModel)).start();
        (new RenderThread(contentPanel)).start();

        movementView.requestFocusInWindow();
    }

    private static JPanel createGamePanel() {
        JPanel gamePanel = new BackgroundPanel(null);
        gamePanel.setMinimumSize(GAME_AREA_MINIMUM_SIZE);
        gamePanel.setPreferredSize(GAME_AREA_PREFERRED_SIZE);
        return gamePanel;
    }
}
