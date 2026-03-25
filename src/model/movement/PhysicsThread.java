package model.movement;

import model.runtime.GamePauseController;

/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends Thread {
    private final MovementModel movementModel;
    private final GamePauseController pauseController;
    private final int DELAY = 16; // ~60 Hz

    public PhysicsThread(MovementModel model) {
        this.movementModel = model;
        this.pauseController = GamePauseController.getInstance();
    }

    // La méthode principale du thread
    @Override
    public void run() {
        while (true) {
            try {
                pauseController.awaitIfPaused();
                movementModel.update();
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
