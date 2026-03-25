package model.enemy;

import model.runtime.GamePauseController;

/**
 * Thread dédié à la mise à jour des ennemis.
 */
public class EnemyPhysicsThread extends Thread {
    private final EnemyModel enemyModel;
    private final GamePauseController pauseController;
    private final int DELAY = 16; // ~60 Hz

    public EnemyPhysicsThread(EnemyModel enemyModel) {
        this.enemyModel = enemyModel;
        this.pauseController = GamePauseController.getInstance();
    }

    // La méthode principale du thread
    @Override
    public void run() {
        while (true) {
            try {
                pauseController.awaitIfPaused();
                enemyModel.update();
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
