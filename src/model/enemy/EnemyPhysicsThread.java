package model.enemy;

import model.runtime.GamePauseController;
import model.runtime.ThreadActivationGate;

/**
 * Thread dédié à la mise à jour des ennemis.
 */
public class EnemyPhysicsThread extends Thread {
    // Modèle ennemi que la boucle doit faire avancer.
    private final EnemyModel enemyModel;
    // Contrôleur global de pause partagé avec le reste du jeu.
    private final GamePauseController pauseController;
    // Porte d'activation qui permet d'endormir ce thread sans le supprimer.
    private final ThreadActivationGate activationGate;
    // Délai cible entre deux mises à jour pour rester proche de 60 Hz.
    private final int DELAY = 16; // ~60 Hz

    /**
     * On crée le thread ennemi en le laissant actif par défaut.
     */
    public EnemyPhysicsThread(EnemyModel enemyModel) {
        this(enemyModel, true);
    }

    /**
     * On prépare ici la boucle physique avec son état d'activation initial.
     */
    public EnemyPhysicsThread(EnemyModel enemyModel, boolean initiallyActive) {
        this.enemyModel = enemyModel;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    /**
     * On active ou on coupe la boucle sans détruire le thread.
     */
    public void setThreadActive(boolean active) {
        activationGate.setActive(active);
    }

    /**
     * On fait tourner ici la boucle de mise à jour des ennemis tant que le thread vit.
     */
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
