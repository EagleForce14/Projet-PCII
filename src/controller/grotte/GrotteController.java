package controller.grotte;

import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.grotte.ShrineHazardThread;
import model.movement.MovementModel;
import model.movement.PhysicsThread;
import model.movement.Unit;
import model.runtime.GamePauseController;
import view.SidebarPanel;
import view.MovementView;
import view.grotte.GrotteFieldPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.CardLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Contrôleur minimal de la grotte.
 *
 * Ici on ne gère volontairement que le déplacement :
 * pas d'actions de ferme, pas d'overlay, pas d'interactions de décor.
 */
public final class GrotteController implements KeyListener {
    private final MovementModel movementModel;
    private final GamePauseController pauseController;
    private final GrotteFieldPanel grotteFieldPanel;
    private final CardLayout centerLayout;
    private final JPanel centerPanel;
    private final SidebarPanel sidebarPanel;
    private final Unit farmPlayerUnit;
    private final Unit grottePlayerUnit;
    private final EnemyModel caveEnemyModel;
    private final PhysicsThread cavePhysicsThread;
    private final EnemyPhysicsThread caveEnemyPhysicsThread;
    private final ShrineHazardThread shrineHazardThread;
    private final MovementView farmMovementView;
    private final MovementView grotteMovementView;
    private final String farmCardName;
    private final String grotteCardName;
    private final Timer exitCheckTimer;
    private boolean exitTriggered;

    public GrotteController(
            MovementModel movementModel,
            MovementView farmMovementView,
            MovementView movementView,
            GrotteFieldPanel grotteFieldPanel,
            JPanel centerPanel,
            String farmCardName,
            String grotteCardName,
            SidebarPanel sidebarPanel,
            Unit farmPlayerUnit,
            Unit grottePlayerUnit,
            EnemyModel caveEnemyModel,
            PhysicsThread cavePhysicsThread,
            EnemyPhysicsThread caveEnemyPhysicsThread,
            ShrineHazardThread shrineHazardThread
    ) {
        this.movementModel = movementModel;
        this.pauseController = GamePauseController.getInstance();
        this.farmMovementView = farmMovementView;
        this.grotteMovementView = movementView;
        this.grotteFieldPanel = grotteFieldPanel;
        this.centerPanel = centerPanel;
        this.centerLayout = centerPanel == null ? null : (CardLayout) centerPanel.getLayout();
        this.farmCardName = farmCardName;
        this.grotteCardName = grotteCardName;
        this.sidebarPanel = sidebarPanel;
        this.farmPlayerUnit = farmPlayerUnit;
        this.grottePlayerUnit = grottePlayerUnit;
        this.caveEnemyModel = caveEnemyModel;
        this.cavePhysicsThread = cavePhysicsThread;
        this.caveEnemyPhysicsThread = caveEnemyPhysicsThread;
        this.shrineHazardThread = shrineHazardThread;
        movementView.addKeyListener(this);
        movementView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                movementView.requestFocusInWindow();
            }
        });
        bindCaveButton();
        markActiveCard(farmCardName);

        this.exitCheckTimer = new Timer(35, event -> checkFarmExit());
        this.exitCheckTimer.start();
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        Unit player = movementModel.getPlayerUnit();
        if (player == null) {
            return;
        }

        int key = event.getKeyCode();
        if (key == KeyEvent.VK_UP) {
            player.setMoveUp(true);
            player.setMoveDown(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        } else if (key == KeyEvent.VK_DOWN) {
            player.setMoveDown(true);
            player.setMoveUp(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        } else if (key == KeyEvent.VK_LEFT) {
            player.setMoveLeft(true);
            player.setMoveRight(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        } else if (key == KeyEvent.VK_RIGHT) {
            player.setMoveRight(true);
            player.setMoveLeft(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        Unit player = movementModel.getPlayerUnit();
        if (player == null) {
            return;
        }

        if (pauseController.isPaused()) {
            stopPlayerMovement(player);
            return;
        }

        int key = event.getKeyCode();
        if (key == KeyEvent.VK_UP) {
            player.setMoveUp(false);
        }
        if (key == KeyEvent.VK_DOWN) {
            player.setMoveDown(false);
        }
        if (key == KeyEvent.VK_LEFT) {
            player.setMoveLeft(false);
        }
        if (key == KeyEvent.VK_RIGHT) {
            player.setMoveRight(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    private void stopPlayerMovement(Unit player) {
        player.stopMovement();
    }

    private void bindCaveButton() {
        if (sidebarPanel == null) {
            return;
        }

        JButton caveButton = sidebarPanel.getCaveButton();
        if (caveButton != null) {
            caveButton.addActionListener(this::toggleCaveScene);
        }
    }

    private void toggleCaveScene(ActionEvent event) {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement(farmPlayerUnit);
        stopPlayerMovement(grottePlayerUnit);
        if (isCaveSceneActive()) {
            returnToFarm();
            return;
        }

        showCave();
    }

    private void checkFarmExit() {
        if (pauseController.isPaused()) {
            return;
        }

        Unit player = movementModel.getPlayerUnit();
        if (player == null) {
            return;
        }

        Point playerCenterCell = grotteFieldPanel.getLogicalGridPositionAt(player.getX(), player.getY());
        if (playerCenterCell == null) {
            return;
        }

        if (!grotteFieldPanel.getGrotteMap().isFarmExitCell(playerCenterCell.x, playerCenterCell.y)) {
            exitTriggered = false;
            return;
        }

        if (exitTriggered) {
            return;
        }

        exitTriggered = true;
        stopPlayerMovement(player);
        Point spawnOffset = grotteFieldPanel.getInitialPlayerOffset();
        player.setPosition(spawnOffset.x, spawnOffset.y);
        returnToFarm();
    }

    private void showCave() {
        if (centerLayout == null || centerPanel == null) {
            return;
        }

        centerLayout.show(centerPanel, grotteCardName);
        markActiveCard(grotteCardName);
        if (sidebarPanel != null) {
            sidebarPanel.setCaveMode(true);
        }

        if (farmPlayerUnit != null) {
            farmPlayerUnit.exitCave();
        }
        if (grottePlayerUnit != null) {
            grottePlayerUnit.enterCave();
        }
        if (caveEnemyModel != null) {
            caveEnemyModel.enterCave();
        }
        setCaveThreadsActive(true);
        if (grotteMovementView != null) {
            grotteMovementView.requestFocusInWindow();
        }
    }

    private void returnToFarm() {
        if (centerLayout == null || centerPanel == null) {
            return;
        }

        centerLayout.show(centerPanel, farmCardName);
        markActiveCard(farmCardName);
        if (sidebarPanel != null) {
            sidebarPanel.setCaveMode(false);
        }

        setCaveThreadsActive(false);
        if (caveEnemyModel != null) {
            caveEnemyModel.exitCave();
        }
        if (grottePlayerUnit != null) {
            grottePlayerUnit.exitCave();
        }
        if (farmPlayerUnit != null) {
            farmPlayerUnit.exitCave();
        }
        if (farmMovementView != null) {
            farmMovementView.requestFocusInWindow();
        }
    }

    private void setCaveThreadsActive(boolean active) {
        if (cavePhysicsThread != null) {
            cavePhysicsThread.setThreadActive(active);
        }
        if (caveEnemyPhysicsThread != null) {
            caveEnemyPhysicsThread.setThreadActive(active);
        }
        if (shrineHazardThread != null) {
            shrineHazardThread.setThreadActive(active);
        }
    }

    private boolean isCaveSceneActive() {
        return grotteCardName != null && grotteCardName.equals(centerPanel.getClientProperty("activeCard"));
    }

    private void markActiveCard(String cardName) {
        if (centerPanel != null) {
            centerPanel.putClientProperty("activeCard", cardName);
        }
    }
}
