package controller.grotte;

import model.movement.MovementModel;
import model.movement.Unit;
import model.runtime.GamePauseController;
import view.MovementView;
import view.grotte.GrotteFieldPanel;

import javax.swing.Timer;
import java.awt.Point;
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
    private final Runnable onFarmExit;
    private final Timer exitCheckTimer;
    private boolean exitTriggered;

    public GrotteController(
            MovementModel movementModel,
            MovementView movementView,
            GrotteFieldPanel grotteFieldPanel,
            Runnable onFarmExit
    ) {
        this.movementModel = movementModel;
        this.pauseController = GamePauseController.getInstance();
        this.grotteFieldPanel = grotteFieldPanel;
        this.onFarmExit = onFarmExit;
        movementView.addKeyListener(this);
        movementView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                movementView.requestFocusInWindow();
            }
        });

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
        player.setMoveUp(false);
        player.setMoveDown(false);
        player.setMoveLeft(false);
        player.setMoveRight(false);
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
        if (onFarmExit != null) {
            onFarmExit.run();
        }
    }
}
