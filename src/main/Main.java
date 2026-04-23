package main;

import controller.GameOverController;
import controller.MovementController;
import controller.grotte.GrotteController;

import model.culture.GrilleCulture;
import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.environment.FieldObstacleMap;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import model.environment.TreeThread;
import model.grotte.GrotteMap;
import model.grotte.GrotteObstacleMap;
import model.grotte.ShrineHazardState;
import model.grotte.ShrineHazardThread;
import model.grotte.combat.CaveCombatModel;
import model.grotte.combat.CaveCombatThread;
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
import model.shop.ShopKind;
import model.workshop.WorkshopConstructionManager;
import view.*;
import view.grotte.GrotteCombatView;
import view.grotte.GrotteFieldPanel;
import view.shop.ShopOverlay;
import view.workshop.WorkshopOverlay;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

public class Main {
    private static final Dimension GAME_AREA_MINIMUM_SIZE = new Dimension(960, 690);
    private static final Dimension GAME_AREA_PREFERRED_SIZE = new Dimension(1180, 885);
    private static final String FARM_CARD = "farm";
    private static final String GROTTE_CARD = "grotte";

    public static void main(String[] args) {
        JFrame frame = createFrame();
        installHomeScreen(frame);
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

    // L'écran d'accueil est un panneau simple qui recouvre toute la fenêtre.
    private static void installHomeScreen(JFrame frame) {
        // On passe une référence à la JFrame pour pouvoir lancer une nouvelle partie depuis l'écran d'accueil, sans recréer une nouvelle fenêtre.
        HomeScreenPanel homeScreenPanel = new HomeScreenPanel(
                () -> installNewGame(frame, true),
                frame::dispose
        );

        frame.setContentPane(homeScreenPanel);
        frame.getGlassPane().setVisible(false);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        homeScreenPanel.requestFocusInWindow();
    }

    public static void installNewGame(JFrame frame, boolean firstLaunch) {
        /*
         * Le contrôleur de pause est un singleton partagé entre les sessions.
         * On le remet donc explicitement en "lecture" avant d'installer la nouvelle
         * partie, sinon la session suivante pourrait démarrer déjà figée.
         */
        GamePauseController.getInstance().setPaused(false);

        Jour jour = new Jour();
        GrilleCulture grilleCulture = new GrilleCulture(jour.getGestionnaireObjectifs());
        TreeManager treeManager = new TreeManager(grilleCulture);
        Money playerMoney = new Money(300);
        Inventaire inventaire = new Inventaire();
        Shop shop = new Shop(ShopKind.MAIN);
        Shop stallShop = new Shop(ShopKind.STALL);
        shop.setGestionnaireObjectifs(jour.getGestionnaireObjectifs());
        stallShop.setGestionnaireObjectifs(jour.getGestionnaireObjectifs());
        WorkshopConstructionManager workshopConstructionManager = new WorkshopConstructionManager(inventaire);
        // Enregistrer shop pour qu'il recoive les notifications de changement de jour
        jour.addDayChangeListener(shop);
        jour.addDayChangeListener(stallShop);
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
        configureOverlayAlignment(movementView);

        // Les actions contextuelles sont affichées dans une sidebar dédiée, hors du jeu.
        SidebarPanel actionSidebarPanel = new SidebarPanel(model, grilleCulture, fieldPanel, inventaire, jour);

        EnemyView enemyView = new EnemyView(enemyModel, fieldPanel);
        configureOverlayAlignment(enemyView);

        InventoryStatusOverlay inventoryStatusOverlay = new InventoryStatusOverlay(fieldPanel, inventaire, model);
        configureOverlayAlignment(inventoryStatusOverlay);

        TopBarPanel topBarPanel = new TopBarPanel(playerMoney, jour);

        // Cette vue couvre toute la fenêtre et affiche les éléments de décor fixes
        EnvironmentView environmentView = new EnvironmentView(
                fieldPanel,
                treeManager,
                workshopConstructionManager,
                playerMoney,
                topBarPanel
        );
        configureOverlayAlignment(environmentView);

        JPanel fieldLayer = createFieldLayer(fieldPanel);
        JPanel gamePanel = createOverlayGamePanel(
                movementView,
                enemyView,
                inventoryStatusOverlay,
                environmentView,
                fieldLayer
        );

        // Petit overlay HUD pour garder les infos du joueur visibles sans bouger le layout principal.
        JPanel hudPanel = new JPanel(new BorderLayout());
        hudPanel.setOpaque(false);
        configureOverlayAlignment(hudPanel);
        hudPanel.add(topBarPanel, BorderLayout.NORTH);
        gamePanel.add(hudPanel);

        GrotteMap grotteMap = new GrotteMap();
        ShrineHazardState shrineHazardState = new ShrineHazardState();
        GrotteFieldPanel grotteFieldPanel = new GrotteFieldPanel(grotteMap, shrineHazardState);
        configureOverlayAlignment(grotteFieldPanel);

        GrotteObstacleMap grotteObstacleMap = new GrotteObstacleMap(grotteMap, grotteFieldPanel);

        EnemyModel caveEnemyModel = EnemyModel.createCaveModel(grotteMap);
        caveEnemyModel.setPlayer(playerUnit);
        caveEnemyModel.setMovementCollisionMap(grotteObstacleMap);
        CaveCombatModel caveCombatModel = new CaveCombatModel(
                playerUnit,
                caveEnemyModel,
                grotteObstacleMap,
                jour,
                inventaire,
                grotteMap
        );

        MovementView grotteMovementView = new MovementView(model, grotteFieldPanel);
        configureOverlayAlignment(grotteMovementView);

        EnemyView caveEnemyView = new EnemyView(caveEnemyModel, grotteFieldPanel);
        configureOverlayAlignment(caveEnemyView);

        GrotteCombatView grotteCombatView = new GrotteCombatView(caveCombatModel, grotteFieldPanel);
        configureOverlayAlignment(grotteCombatView);
        InventoryStatusOverlay caveInventoryStatusOverlay = new InventoryStatusOverlay(
                grotteFieldPanel,
                inventaire,
                model
        );
        // En grotte, l'inventaire ne doit pas rester affiché en permanence.
        // On active donc le mode "transient" : apparition brève après un pickup, puis disparition animée.
        caveInventoryStatusOverlay.enableTransientCaveDisplay(caveCombatModel);
        configureOverlayAlignment(caveInventoryStatusOverlay);

        JPanel grotteFieldLayer = createFieldLayer(grotteFieldPanel);
        JPanel grotteGamePanel = createOverlayGamePanel(
                grotteCombatView,
                grotteMovementView,
                caveEnemyView,
                caveInventoryStatusOverlay,
                grotteFieldLayer
        );

        JPanel centerPanel = new JPanel(new CardLayout());
        centerPanel.add(gamePanel, FARM_CARD);
        centerPanel.add(grotteGamePanel, GROTTE_CARD);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(actionSidebarPanel, BorderLayout.EAST);
        frame.setContentPane(contentPanel);

        ShopOverlay shopOverlay = new ShopOverlay(shop, playerMoney, inventaire, movementView);
        ShopOverlay stallShopOverlay = new ShopOverlay(stallShop, playerMoney, inventaire, movementView);
        WorkshopOverlay workshopOverlay = new WorkshopOverlay(inventaire, workshopConstructionManager, movementView);
        frame.setGlassPane(shopOverlay);

        PhysicsThread physicsThread = new PhysicsThread(model);
        EnemyPhysicsThread enemyPhysicsThread = new EnemyPhysicsThread(enemyModel);
        EnemyPhysicsThread caveEnemyPhysicsThread = new EnemyPhysicsThread(caveEnemyModel, false);
        CaveCombatThread caveCombatThread = new CaveCombatThread(caveCombatModel, false);
        RenderThread renderThread = new RenderThread(contentPanel);
        TreeThread treeThread = new TreeThread(treeManager, fieldObstacleMap, playerUnit, enemyModel);
        ShrineHazardThread shrineHazardThread = new ShrineHazardThread(
                shrineHazardState,
                playerUnit,
                grotteFieldPanel,
                jour
        );
        GameSession session = new GameSession(
                jour,
                grilleCulture,
                physicsThread,
                null,
                enemyPhysicsThread,
                caveEnemyPhysicsThread,
                caveCombatThread,
                renderThread,
                treeThread,
                shrineHazardThread,
                workshopConstructionManager
        );

        GameOverOverlay farmGameOverOverlay = installGameOverOverlay(gamePanel, jour);
        GameOverOverlay caveGameOverOverlay = installGameOverOverlay(grotteGamePanel, jour);
        gamePanel.setComponentZOrder(hudPanel, 1);
        gamePanel.setComponentZOrder(inventoryStatusOverlay, 2);
        grotteGamePanel.setComponentZOrder(grotteCombatView, 1);
        grotteGamePanel.setComponentZOrder(caveInventoryStatusOverlay, 2);
        new GameOverController(frame, session, farmGameOverOverlay, caveGameOverOverlay);

        new MovementController(
                model,
                movementView,
                enemyModel,
                enemyView,
                actionSidebarPanel,
                grilleCulture,
                playerMoney,
                inventaire,
                treeManager,
                fieldPanel,
                inventoryStatusOverlay,
                shopOverlay,
                stallShopOverlay,
                workshopOverlay
        );

        GrotteController grotteController = new GrotteController(
                movementView,
                grotteMovementView,
                caveEnemyView,
                grotteCombatView,
                grotteFieldPanel,
                fieldPanel,
                jour,
                centerPanel,
                FARM_CARD,
                GROTTE_CARD,
                actionSidebarPanel,
                playerUnit,
                fieldObstacleMap,
                grotteObstacleMap,
                caveEnemyModel,
                caveCombatModel,
                caveEnemyPhysicsThread,
                caveCombatThread,
                shrineHazardThread
        );
        physicsThread.setGrotteController(grotteController);

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
        PredefinedFieldLayout.apply(fieldPanel);
        treeThread.installerArbresInitiaux();
        Point safeInitialPlayerOffset = findSafeInitialPlayerOffset(fieldPanel, fieldObstacleMap);
        playerUnit.setPosition(safeInitialPlayerOffset.x, safeInitialPlayerOffset.y);

        enemyModel.setViewportSize(gamePanel.getWidth(), gamePanel.getHeight());

        physicsThread.start();
        enemyPhysicsThread.start();
        caveEnemyPhysicsThread.start();
        caveCombatThread.start();
        renderThread.start();
        treeThread.start();
        shrineHazardThread.start();

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

    private static JPanel createGamePanel() {
        JPanel gamePanel = new BackgroundPanel(null);
        gamePanel.setMinimumSize(GAME_AREA_MINIMUM_SIZE);
        gamePanel.setPreferredSize(GAME_AREA_PREFERRED_SIZE);
        return gamePanel;
    }

    /**
     * Toutes les couches superposées utilisent le même centrage dans l'OverlayLayout.
     * On évite ainsi de répéter les deux mêmes affectations partout dans `Main`.
     */
    private static void configureOverlayAlignment(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof JPanel) {
            ((JPanel) component).setAlignmentX(0.5f);
            ((JPanel) component).setAlignmentY(0.5f);
        }
    }

    /**
     * Le fond jouable repose systématiquement sur un panneau transparent en BorderLayout.
     * On factorise cette construction pour la ferme et la grotte.
     */
    private static JPanel createFieldLayer(Component mapComponent) {
        JPanel fieldLayer = new JPanel(new BorderLayout());
        fieldLayer.setOpaque(false);
        configureOverlayAlignment(fieldLayer);
        fieldLayer.add(mapComponent, BorderLayout.CENTER);
        return fieldLayer;
    }

    /**
     * Petit helper de composition :
     * on garde l'ordre des couches explicite à l'appel,
     * tout en évitant de recopier le même boilerplate pour chaque scène.
     */
    private static JPanel createOverlayGamePanel(Component... layers) {
        JPanel gamePanel = createGamePanel();
        gamePanel.setLayout(new OverlayLayout(gamePanel));
        if (layers == null) {
            return gamePanel;
        }

        for (Component layer : layers) {
            gamePanel.add(layer);
        }
        return gamePanel;
    }

    private static GameOverOverlay installGameOverOverlay(JPanel gamePanel, Jour jour) {
        GameOverOverlay gameOverOverlay = new GameOverOverlay(jour);
        gamePanel.add(gameOverOverlay);
        gamePanel.setComponentZOrder(gameOverOverlay, 0);
        return gameOverOverlay;
    }
}
