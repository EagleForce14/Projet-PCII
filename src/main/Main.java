package main;

import controller.MovementController;

import model.culture.GrilleCulture;
import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.environment.FieldObstacleMap;
import model.environment.TreeManager;
import model.environment.TreeThread;
import model.movement.Barn;
import model.movement.MovementModel;
import model.movement.PhysicsThread;
import model.movement.Unit;
import model.runtime.GamePauseController;
import model.runtime.Jour;
import model.management.Inventaire;
import model.management.Money;
import model.runtime.GameSession;
import model.shop.Shop;
import view.*;
import view.shop.ShopOverlay;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

public class Main {
    private static final Dimension GAME_AREA_MINIMUM_SIZE = new Dimension(960, 690);
    private static final Dimension GAME_AREA_PREFERRED_SIZE = new Dimension(1180, 850);
    private static final int DECORATIVE_RIVER_LENGTH = 11;
    private static final int DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN = 2;
    private static final int DECORATIVE_RIVER_FALLBACK_COLUMN = 4;

    public static void main(String[] args) {
        JFrame frame = createFrame();
        installNewGame(frame, true);
    }

    private static JFrame createFrame() {
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
        return frame;
    }

    private static void installNewGame(JFrame frame, boolean firstLaunch) {
        /*
         * Le contrôleur de pause est un singleton partagé entre les sessions.
         * On le remet donc explicitement en "lecture" avant d'installer la nouvelle
         * partie, sinon la session suivante pourrait démarrer déjà figée.
         */
        GamePauseController.getInstance().setPaused(false);

        Jour jour = new Jour();
        GrilleCulture grilleCulture = new GrilleCulture(jour.getGestionnaireObjectifs());
        TreeManager treeManager = new TreeManager(grilleCulture);
        Money playerMoney = new Money(150);
        Inventaire inventaire = new Inventaire();
        Shop shop = new Shop();
        // Enregistrer shop pour qu'il recoive les notifications de changement de jour
        jour.addDayChangeListener(shop);
        FieldPanel fieldPanel = new FieldPanel(grilleCulture, treeManager);
        FieldObstacleMap fieldObstacleMap = new FieldObstacleMap(treeManager, fieldPanel);
        fieldPanel.setFieldObstacleMap(fieldObstacleMap);

        MovementModel model = new MovementModel();
        // Le joueur démarre hors du champ, près du coin haut-gauche de la grille.
        Point initialPlayerOffset = fieldPanel.getInitialPlayerOffset();
        Unit playerUnit = new Unit(initialPlayerOffset.x, initialPlayerOffset.y);
        playerUnit.setFieldObstacleMap(fieldObstacleMap);
        model.setPlayerUnit(playerUnit);

        EnemyModel enemyModel = new EnemyModel();
        enemyModel.setPlayer(playerUnit);
        enemyModel.setGrilleCulture(grilleCulture);
        enemyModel.setFieldObstacleMap(fieldObstacleMap);
        MovementView movementView = new MovementView(model, fieldPanel);
        movementView.setAlignmentX(0.5f);
        movementView.setAlignmentY(0.5f);

        // Les actions contextuelles sont affichées dans une sidebar dédiée, hors du jeu.
        SidebarPanel actionSidebarPanel = new SidebarPanel(model, grilleCulture, fieldPanel, inventaire);

        EnemyView enemyView = new EnemyView(enemyModel, fieldPanel);
        enemyView.setAlignmentX(0.5f);
        enemyView.setAlignmentY(0.5f);

        InventoryStatusOverlay inventoryStatusOverlay = new InventoryStatusOverlay(fieldPanel, inventaire, model);
        inventoryStatusOverlay.setAlignmentX(0.5f);
        inventoryStatusOverlay.setAlignmentY(0.5f);

        // Cette vue couvre toute la fenêtre et affiche les éléments de décor fixes
        EnvironmentView environmentView = new EnvironmentView(fieldPanel, treeManager);
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

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(gamePanel, BorderLayout.CENTER);
        contentPanel.add(actionSidebarPanel, BorderLayout.EAST);
        frame.setContentPane(contentPanel);

        ShopOverlay shopOverlay = new ShopOverlay(shop, playerMoney, inventaire, movementView);
        frame.setGlassPane(shopOverlay);

        PhysicsThread physicsThread = new PhysicsThread(model);
        EnemyPhysicsThread enemyPhysicsThread = new EnemyPhysicsThread(enemyModel);
        RenderThread renderThread = new RenderThread(contentPanel);
        TreeThread treeThread = new TreeThread(treeManager, fieldObstacleMap, playerUnit, enemyModel);
        GameSession session = new GameSession(jour, grilleCulture, physicsThread, enemyPhysicsThread, renderThread, treeThread);

        GameOverOverlay gameOverOverlay = new GameOverOverlay(jour);
        gamePanel.add(gameOverOverlay);
        gamePanel.setComponentZOrder(gameOverOverlay, 0);
        gamePanel.setComponentZOrder(hudPanel, 1);
        gamePanel.setComponentZOrder(inventoryStatusOverlay, 2);

        new MovementController(
                model,
                movementView,
                enemyModel,
                enemyView,
                actionSidebarPanel,
                grilleCulture,
                playerMoney,
                inventaire,
                fieldPanel,
                inventoryStatusOverlay,
                shopOverlay,
                gameOverOverlay,
                () -> restartCurrentGame(frame, session)
        );

        if (firstLaunch) {
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } else {
            // Même fenêtre, nouvelle partie :
            // on revalide juste le layout au lieu de recréer une JFrame.
            frame.getGlassPane().setVisible(false);
            frame.revalidate();
            frame.repaint();
        }

        frame.validate();
        installDecorativeRiverColumn(grilleCulture, fieldPanel);
        treeThread.installerArbresInitiaux();
        Point safeInitialPlayerOffset = findSafeInitialPlayerOffset(fieldPanel, fieldObstacleMap);
        playerUnit.setPosition(safeInitialPlayerOffset.x, safeInitialPlayerOffset.y);

        enemyModel.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());

        physicsThread.start();
        enemyPhysicsThread.start();
        renderThread.start();
        treeThread.start();

        movementView.requestFocusInWindow();
    }

    /**
     * Les arbres initiaux étant posés après la création du joueur,
     * on choisit ici la case libre la plus proche du centre avant de lancer les threads.
     */
    private static Point findSafeInitialPlayerOffset(FieldPanel fieldPanel, FieldObstacleMap fieldObstacleMap) {
        Point preferredOffset = fieldPanel.getInitialPlayerOffset();
        Point bestOffset = preferredOffset;
        long bestDistanceSquared = Long.MAX_VALUE;

        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            for (int row = 0; row < fieldPanel.getRowCount(); row++) {
                Point candidateOffset = fieldPanel.getLogicalCellCenter(column, row);
                if (!canSpawnPlayerAt(candidateOffset, fieldObstacleMap)) {
                    continue;
                }

                long distanceSquared = squaredDistance(candidateOffset, preferredOffset);
                if (distanceSquared < bestDistanceSquared) {
                    bestDistanceSquared = distanceSquared;
                    bestOffset = candidateOffset;
                }
            }
        }

        return bestOffset;
    }

    private static boolean canSpawnPlayerAt(Point position, FieldObstacleMap fieldObstacleMap) {
        return position != null
                && Barn.canOccupyCenteredBox(position.x, position.y, Unit.SIZE, Unit.SIZE)
                && (fieldObstacleMap == null || fieldObstacleMap.canOccupyCenteredBox(position.x, position.y, Unit.SIZE, Unit.SIZE));
    }

    private static long squaredDistance(Point a, Point b) {
        long deltaX = (long) a.x - b.x;
        long deltaY = (long) a.y - b.y;
        return (deltaX * deltaX) + (deltaY * deltaY);
    }

    /**
     * Ajoute la colonne fixe de 11 cases de rivière demandee
     * avant l'installation des arbres initiaux.
     */
    private static void installDecorativeRiverColumn(GrilleCulture grilleCulture, FieldPanel fieldPanel) {
        int riverColumn = resolveDecorativeRiverColumn(fieldPanel);
        if (riverColumn < 0 || riverColumn >= grilleCulture.getLargeur()) {
            return;
        }

        int riverLength = Math.min(DECORATIVE_RIVER_LENGTH, grilleCulture.getHauteur());
        for (int row = 0; row < riverLength; row++) {
            grilleCulture.placeDecorativeRiver(riverColumn, row);
        }
    }

    /**
     * On repere la premiere colonne touchee par l'image de la boutique/grange,
     * puis on se decale de deux cases vers la gauche.
     * Un fallback fixe garde un resultat stable si le layout n'est pas encore resolu.
     */
    private static int resolveDecorativeRiverColumn(FieldPanel fieldPanel) {
        Rectangle barnBounds = fieldPanel.getBarnLogicalDrawBounds();
        Rectangle fieldBounds = fieldPanel.getFieldLogicalBounds();
        int leftmostBarnColumn = -1;

        for (int column = 0; column < fieldPanel.getColumnCount(); column++) {
            Rectangle firstCellBounds = fieldPanel.getLogicalCellBounds(column, 0);
            if (firstCellBounds == null) {
                continue;
            }

            Rectangle columnBounds = new Rectangle(
                    firstCellBounds.x,
                    fieldBounds.y,
                    firstCellBounds.width,
                    fieldBounds.height
            );
            if (columnBounds.intersects(barnBounds)) {
                leftmostBarnColumn = column;
                break;
            }
        }

        if (leftmostBarnColumn < 0) {
            return DECORATIVE_RIVER_FALLBACK_COLUMN;
        }

        return Math.max(0, leftmostBarnColumn - DECORATIVE_RIVER_COLUMNS_LEFT_OF_BARN);
    }

    private static JPanel createGamePanel() {
        JPanel gamePanel = new BackgroundPanel(null);
        gamePanel.setMinimumSize(GAME_AREA_MINIMUM_SIZE);
        gamePanel.setPreferredSize(GAME_AREA_PREFERRED_SIZE);
        return gamePanel;
    }

    private static void restartCurrentGame(JFrame frame, GameSession session) {
        if (session == null) {
            return;
        }

        /*
         * L'idée du bouton est vraiment "comme si on relançait l'app",
         * mais sans faire apparaître une deuxième fenêtre.
         * On arrête donc tout ce qui appartient à l'ancienne session,
         * puis on réinstalle un nouvel arbre de composants dans la même frame.
         */
        session.shutdown();
        installNewGame(frame, false);
    }
}
