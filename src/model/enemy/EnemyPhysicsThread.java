package model.enemy;

import model.runtime.GamePauseController;
import model.runtime.ThreadActivationGate;

/**
 * Thread dédié à la mise à jour des ennemis.
 */
public class EnemyPhysicsThread extends Thread {
    private final EnemyModel enemyModel;
    private final GamePauseController pauseController;
    private final ThreadActivationGate activationGate;
    private final int DELAY = 16; // ~60 Hz

    public EnemyPhysicsThread(EnemyModel enemyModel) {
        this(enemyModel, true);
    }

    public EnemyPhysicsThread(EnemyModel enemyModel, boolean initiallyActive) {
        this.enemyModel = enemyModel;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    public void setThreadActive(boolean active) {
        activationGate.setActive(active);
    }

    // La méthode principale du thread
    @Override
    public void run() {
        while (true) {
            try {
                activationGate.awaitActivation();
                pauseController.awaitIfPaused();
                if (!activationGate.isActive()) {
                    continue;
                }
                enemyModel.update();
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
