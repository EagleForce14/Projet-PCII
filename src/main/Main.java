package main;

import controller.MovementController;
import model.EnemyPhysicsThread;
import model.EnemyModel;
import model.GrilleCulture;
import model.MovementModel;
import model.PhysicsThread;
import model.Unit;
import view.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;
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

        // Image de fond globale de la fenêtre.
        Image backgroundImage = ImageLoader.load("/assets/Main_Background.png");

        GrilleCulture grilleCulture = new GrilleCulture();
        FieldPanel fieldPanel = new FieldPanel(grilleCulture);

        MovementModel model = new MovementModel();
        // Le joueur démarre hors du champ, près du coin haut-gauche de la grille.
        Point initialPlayerOffset = fieldPanel.getInitialPlayerOffset();
        Unit playerUnit = new Unit(initialPlayerOffset.x, initialPlayerOffset.y);
        model.setPlayerUnit(playerUnit);

        EnemyModel enemyModel = new EnemyModel();
        enemyModel.setPlayer(playerUnit);
        MovementView movementView = new MovementView(model, fieldPanel);
        movementView.setAlignmentX(0.5f);
        movementView.setAlignmentY(0.5f);

        // Les actions contextuelles sont affichées dans une sidebar dédiée, hors du jeu.
        SidebarPanel actionSidebarPanel = new SidebarPanel(model);

        EnemyView enemyView = new EnemyView(enemyModel, fieldPanel);
        enemyView.setAlignmentX(0.5f);
        enemyView.setAlignmentY(0.5f);

        // Cette vue couvre toute la fenêtre et affiche les éléments de décor fixes
        EnvironmentView environmentView = new EnvironmentView(fieldPanel);
        environmentView.setAlignmentX(0.5f);
        environmentView.setAlignmentY(0.5f);

        JPanel fieldLayer = new JPanel(new GridBagLayout());
        fieldLayer.setOpaque(false);
        fieldLayer.setAlignmentX(0.5f);
        fieldLayer.setAlignmentY(0.5f);
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        // On repousse le champ vers le bas pour qu'il soit moins centré
        gbc.insets = new java.awt.Insets(100, 0, 0, 0); 
        fieldLayer.add(fieldPanel, gbc);

        JPanel gamePanel = createGamePanel(backgroundImage);
        gamePanel.setLayout(new OverlayLayout(gamePanel));
        gamePanel.add(movementView);
        gamePanel.add(enemyView);
        gamePanel.add(environmentView); // S'affiche derrière les ennemis
        gamePanel.add(fieldLayer); // S'affiche derrière l'environnement

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(gamePanel, BorderLayout.CENTER);
        contentPanel.add(actionSidebarPanel, BorderLayout.EAST);
        frame.setContentPane(contentPanel);

        new MovementController(model, movementView, actionSidebarPanel, grilleCulture);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        enemyModel.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());

        (new PhysicsThread(model)).start();
        (new EnemyPhysicsThread(enemyModel)).start();
        (new RenderThread(contentPanel)).start();

        movementView.requestFocusInWindow();
    }

    private static JPanel createGamePanel(Image backgroundImage) {
        JPanel gamePanel = new BackgroundPanel(backgroundImage);
        gamePanel.setMinimumSize(GAME_AREA_MINIMUM_SIZE);
        gamePanel.setPreferredSize(GAME_AREA_PREFERRED_SIZE);
        return gamePanel;
    }
}
