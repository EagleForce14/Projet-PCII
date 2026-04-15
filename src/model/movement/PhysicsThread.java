package model.movement;

import model.runtime.GamePauseController;
import model.runtime.ThreadActivationGate;

/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends Thread {
    private final MovementModel movementModel;
    private final GamePauseController pauseController;
    private final ThreadActivationGate activationGate;
    private final int DELAY = 16; // ~60 Hz

    public PhysicsThread(MovementModel model) {
        this(model, true);
    }

    public PhysicsThread(MovementModel model, boolean initiallyActive) {
        this.movementModel = model;
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
                movementModel.update();
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
