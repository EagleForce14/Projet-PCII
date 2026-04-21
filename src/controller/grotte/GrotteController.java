package controller.grotte;

import model.enemy.EnemyModel;
import model.enemy.EnemyPhysicsThread;
import model.grotte.ShrineHazardThread;
import model.grotte.combat.CaveCombatModel;
import model.grotte.combat.CaveCombatThread;
import model.movement.MovementModel;
import model.movement.PhysicsThread;
import model.movement.Unit;
import model.runtime.GamePauseController;
import view.EnemyView;
import view.MovementView;
import view.SidebarPanel;
import view.grotte.GrotteCombatView;
import view.grotte.GrotteFieldPanel;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Contrôleur dédié à la grotte.
 *
 * Toute la gestion des listeners de cette zone est centralisée ici :
 * déplacement, tir, focus clavier, clic de sélection des monstres
 * et bascule ferme <-> grotte.
 */
public final class GrotteController implements KeyListener, MouseListener, ActionListener {
    private static final int EXIT_CHECK_DELAY_MS = 35;

    private final MovementModel movementModel;
    private final GamePauseController pauseController;
    private final GrotteFieldPanel grotteFieldPanel;
    private final EnemyView caveEnemyView;
    private final GrotteCombatView grotteCombatView;
    private final CardLayout centerLayout;
    private final JPanel centerPanel;
    private final SidebarPanel sidebarPanel;
    private final Unit farmPlayerUnit;
    private final Unit grottePlayerUnit;
    private final EnemyModel caveEnemyModel;
    private final CaveCombatModel caveCombatModel;
    private final PhysicsThread cavePhysicsThread;
    private final EnemyPhysicsThread caveEnemyPhysicsThread;
    private final CaveCombatThread caveCombatThread;
    private final ShrineHazardThread shrineHazardThread;
    private final MovementView farmMovementView;
    private final MovementView grotteMovementView;
    private final String farmCardName;
    private final String grotteCardName;
    private final Timer exitCheckTimer;

    private JButton caveButton;
    private boolean exitTriggered;

    public GrotteController(
            MovementModel movementModel,
            MovementView farmMovementView,
            MovementView grotteMovementView,
            EnemyView caveEnemyView,
            GrotteCombatView grotteCombatView,
            GrotteFieldPanel grotteFieldPanel,
            JPanel centerPanel,
            String farmCardName,
            String grotteCardName,
            SidebarPanel sidebarPanel,
            Unit farmPlayerUnit,
            Unit grottePlayerUnit,
            EnemyModel caveEnemyModel,
            CaveCombatModel caveCombatModel,
            PhysicsThread cavePhysicsThread,
            EnemyPhysicsThread caveEnemyPhysicsThread,
            CaveCombatThread caveCombatThread,
            ShrineHazardThread shrineHazardThread
    ) {
        this.movementModel = movementModel;
        this.pauseController = GamePauseController.getInstance();
        this.farmMovementView = farmMovementView;
        this.grotteMovementView = grotteMovementView;
        this.caveEnemyView = caveEnemyView;
        this.grotteCombatView = grotteCombatView;
        this.grotteFieldPanel = grotteFieldPanel;
        this.centerPanel = centerPanel;
        this.centerLayout = centerPanel == null ? null : (CardLayout) centerPanel.getLayout();
        this.farmCardName = farmCardName;
        this.grotteCardName = grotteCardName;
        this.sidebarPanel = sidebarPanel;
        this.farmPlayerUnit = farmPlayerUnit;
        this.grottePlayerUnit = grottePlayerUnit;
        this.caveEnemyModel = caveEnemyModel;
        this.caveCombatModel = caveCombatModel;
        this.cavePhysicsThread = cavePhysicsThread;
        this.caveEnemyPhysicsThread = caveEnemyPhysicsThread;
        this.caveCombatThread = caveCombatThread;
        this.shrineHazardThread = shrineHazardThread;

        registerInputLayers();
        bindCaveButton();
        markActiveCard(farmCardName);

        this.exitCheckTimer = new Timer(EXIT_CHECK_DELAY_MS, this);
        this.exitCheckTimer.start();
    }

    private void registerInputLayers() {
        if (grotteMovementView != null) {
            grotteMovementView.addKeyListener(this);
            grotteMovementView.addMouseListener(this);
        }
        if (caveEnemyView != null) {
            caveEnemyView.addMouseListener(this);
        }
        if (grotteCombatView != null) {
            grotteCombatView.addMouseListener(this);
        }
    }

    private void bindCaveButton() {
        if (sidebarPanel == null) {
            return;
        }

        caveButton = sidebarPanel.getCaveButton();
        if (caveButton != null) {
            caveButton.addActionListener(this);
        }
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == exitCheckTimer) {
            checkFarmExit();
            return;
        }

        if (source == caveButton) {
            toggleCaveScene();
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (pauseController.isPaused() || !isCaveSceneActive()) {
            return;
        }

        Unit player = movementModel.getPlayerUnit();
        if (player == null) {
            return;
        }

        int key = event.getKeyCode();
        if (isShootKey(key)) {
            if (caveCombatModel != null) {
                caveCombatModel.setPlayerFiring(true);
            }
            return;
        }

        if (isUpKey(key)) {
            activateSingleDirection(player, true, false, false, false);
        } else if (isDownKey(key)) {
            activateSingleDirection(player, false, true, false, false);
        } else if (isLeftKey(key)) {
            activateSingleDirection(player, false, false, true, false);
        } else if (isRightKey(key)) {
            activateSingleDirection(player, false, false, false, true);
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
            stopPlayerFire();
            return;
        }

        int key = event.getKeyCode();
        if (isShootKey(key)) {
            stopPlayerFire();
            return;
        }

        if (isUpKey(key)) {
            player.setMoveUp(false, false);
        }
        if (isDownKey(key)) {
            player.setMoveDown(false, false);
        }
        if (isLeftKey(key)) {
            player.setMoveLeft(false, false);
        }
        if (isRightKey(key)) {
            player.setMoveRight(false, false);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (grotteMovementView != null) {
            grotteMovementView.requestFocusInWindow();
        }
        syncEnemySelection(event);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    private void syncEnemySelection(MouseEvent event) {
        if (caveEnemyView == null || event == null) {
            return;
        }

        Component source = event.getComponent();
        if (source == null) {
            return;
        }

        Point translatedPoint = SwingUtilities.convertPoint(source, event.getPoint(), caveEnemyView);
        caveEnemyView.handleWorldClick(translatedPoint);
    }

    private void activateSingleDirection(Unit player, boolean moveUp, boolean moveDown, boolean moveLeft, boolean moveRight) {
        if (player == null) {
            return;
        }

        player.setMoveUp(moveUp, moveUp);
        player.setMoveDown(moveDown, moveDown);
        player.setMoveLeft(moveLeft, moveLeft);
        player.setMoveRight(moveRight, moveRight);
    }

    private void stopPlayerMovement(Unit player) {
        if (player != null) {
            player.stopMovement();
        }
    }

    private void stopPlayerFire() {
        if (caveCombatModel != null) {
            caveCombatModel.setPlayerFiring(false);
        }
    }

    private void toggleCaveScene() {
        if (pauseController.isPaused()) {
            return;
        }

        stopPlayerMovement(farmPlayerUnit);
        stopPlayerMovement(grottePlayerUnit);
        stopPlayerFire();
        if (isCaveSceneActive()) {
            returnToFarm();
            return;
        }

        showCave();
    }

    private void checkFarmExit() {
        if (pauseController.isPaused() || !isCaveSceneActive()) {
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

        if (!grotteFieldPanel.getGrotteMap().isActualFarmExitCell(playerCenterCell.x, playerCenterCell.y)) {
            exitTriggered = false;
            return;
        }

        if (exitTriggered) {
            return;
        }

        exitTriggered = true;
        stopPlayerMovement(player);
        stopPlayerFire();
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
        if (caveCombatModel != null) {
            caveCombatModel.enterCave();
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

        stopPlayerFire();
        setCaveThreadsActive(false);
        if (caveCombatModel != null) {
            caveCombatModel.exitCave();
        }
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
        if (caveCombatThread != null) {
            caveCombatThread.setThreadActive(active);
        }
        if (shrineHazardThread != null) {
            shrineHazardThread.setThreadActive(active);
        }
    }

    private boolean isUpKey(int keyCode) {
        return keyCode == KeyEvent.VK_UP;
    }

    private boolean isDownKey(int keyCode) {
        return keyCode == KeyEvent.VK_DOWN;
    }

    private boolean isLeftKey(int keyCode) {
        return keyCode == KeyEvent.VK_LEFT;
    }

    private boolean isRightKey(int keyCode) {
        return keyCode == KeyEvent.VK_RIGHT;
    }

    private boolean isShootKey(int keyCode) {
        return keyCode == KeyEvent.VK_SPACE;
    }

    private boolean isCaveSceneActive() {
        return centerPanel != null
                && grotteCardName != null
                && grotteCardName.equals(centerPanel.getClientProperty("activeCard"));
    }

    private void markActiveCard(String cardName) {
        if (centerPanel != null) {
            centerPanel.putClientProperty("activeCard", cardName);
        }
    }
}
