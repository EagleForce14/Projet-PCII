package model.movement;

import controller.grotte.GrotteController;
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
    private GrotteController grotteController;

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

    /**
     * Le joueur n'a qu'une seule boucle physique.
     * On lui rattache donc directement la vérification des transitions ferme/grotte,
     * au lieu d'ajouter un second polling parallèle.
     */
    public void setGrotteController(GrotteController grotteController) {
        this.grotteController = grotteController;
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
                if (grotteController != null) {
                    grotteController.checkSceneTransitionFromCurrentPosition();
                }
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
