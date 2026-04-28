package model.runtime;

/**
 * Base commune des threads de boucle de jeu qui doivent respecter
 * à la fois l'activation locale du thread et la pause globale du jeu.
 * Chaque sous-classe fournit seulement le travail d'un tick ;
 * le rythme, l'attente et l'arrêt sur interruption restent centralisés ici.
 */
public abstract class ActivatableGameLoopThread extends Thread {
    // Contrôleur global de pause partagé avec toutes les boucles temps réel.
    private final GamePauseController pauseController;
    // Porte d'activation permettant d'endormir la boucle sans tuer le thread.
    private final ThreadActivationGate activationGate;
    // Délai cible entre deux ticks consécutifs.
    private final int delayMs;

    /**
     * Prépare une boucle temps réel activable avec sa cadence cible.
     */
    protected ActivatableGameLoopThread(boolean initiallyActive, int delayMs) {
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
        this.delayMs = delayMs;
    }

    /**
     * Active ou désactive la boucle sans recréer le thread.
     */
    public final void setThreadActive(boolean active) {
        activationGate.setActive(active);
    }

    /**
     * Orchestration commune d'une boucle de jeu :
     * attendre son activation, respecter la pause globale, jouer un tick,
     * puis dormir jusqu'au tick suivant.
     */
    @Override
    public final void run() {
        while (true) {
            try {
                activationGate.awaitActivation();
                pauseController.awaitIfPaused();
                if (!activationGate.isActive()) {
                    continue;
                }

                performTick();
                pauseController.sleep(delayMs);
            } catch (InterruptedException exception) {
                interrupt();
                return;
            }
        }
    }

    /**
     * Travail spécifique d'une itération de boucle.
     */
    protected abstract void performTick();
}
