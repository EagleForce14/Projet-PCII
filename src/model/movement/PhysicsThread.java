package model.movement;

import controller.grotte.GrotteController;
import model.runtime.GamePauseController;
import model.runtime.ThreadActivationGate;

/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends Thread {
    // Modèle de déplacement mis à jour en continu par le thread.
    private final MovementModel movementModel;
    // Contrôleur global de pause partagé avec le reste du jeu.
    private final GamePauseController pauseController;
    // Porte d'activation qui permet de suspendre la physique sans tuer le thread.
    private final ThreadActivationGate activationGate;
    // Délai cible entre deux mises à jour physiques.
    private final int DELAY = 16; // ~60 Hz
    // Contrôleur de grotte utilisé pour vérifier les transitions de scène.
    private GrotteController grotteController;

    /**
     * On crée le thread physique avec une activation immédiate par défaut.
     */
    public PhysicsThread(MovementModel model) {
        this(model, true);
    }

    /**
     * On prépare ici la boucle physique avec son état d'activation initial.
     */
    public PhysicsThread(MovementModel model, boolean initiallyActive) {
        this.movementModel = model;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    /**
     * On active ou on suspend la boucle physique sans détruire le thread.
     */
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

    /**
     * On fait tourner ici la mise à jour physique continue du joueur et des transitions de scène.
     */
    @Override
    public void run() {
        while (true) {
            try {
                // On attend d'abord que la physique soit réellement activée.
                activationGate.awaitActivation();
                // Une pause globale doit aussi figer la boucle physique.
                pauseController.awaitIfPaused();
                // Si l'activation a été coupée pendant l'attente, on saute cette itération.
                if (!activationGate.isActive()) {
                    continue;
                }

                // On met à jour toutes les unités, puis on vérifie si le joueur change de scène.
                movementModel.update();
                if (grotteController != null) {
                    grotteController.checkSceneTransitionFromCurrentPosition();
                }

                // On dort jusqu'au prochain tick cible.
                pauseController.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
