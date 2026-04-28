package model.movement;

import controller.grotte.GrotteController;
import model.runtime.ActivatableGameLoopThread;

/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends ActivatableGameLoopThread {
    // Modèle de déplacement mis à jour en continu par le thread.
    private final MovementModel movementModel;
    // Délai cible entre deux mises à jour physiques.
    private static final int DELAY_MS = 16; // ~60 Hz
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
        super(initiallyActive, DELAY_MS);
        this.movementModel = model;
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
     * Un tick physique met à jour les déplacements puis vérifie
     * si le joueur doit basculer entre la ferme et la grotte.
     */
    @Override
    protected void performTick() {
        movementModel.update();
        if (grotteController != null) {
            grotteController.checkSceneTransitionFromCurrentPosition();
        }
    }
}
