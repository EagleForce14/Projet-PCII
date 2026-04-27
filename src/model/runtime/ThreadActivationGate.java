package model.runtime;

/**
 * Verrou pour endormir complètement un thread tant que sa scène
 * n'est pas active afin d'optimiser les performances.

 * Les threads de la grotte s'en servent pour ne faire aucun travail
 * lorsque le joueur reste sur la ferme.
 */
public final class ThreadActivationGate {
    // Verrou interne utilisé pour endormir et réveiller proprement le thread lié.
    private final Object activationLock = new Object();
    // Indique si le thread associé a le droit de tourner ou doit rester bloqué.
    private volatile boolean active;

    /**
     * On prépare la porte d'activation avec son état initial.
     */
    public ThreadActivationGate(boolean initiallyActive) {
        this.active = initiallyActive;
    }

    /**
     * On active ou on coupe la porte, puis on réveille les threads en attente pour qu'ils se recalent.
     */
    public void setActive(boolean active) {
        synchronized (activationLock) {
            this.active = active;
            activationLock.notifyAll();
        }
    }

    /**
     * On dit si la porte laisse actuellement passer le thread.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * On bloque complètement le thread tant que la porte reste inactive.
     */
    public void awaitActivation() throws InterruptedException {
        synchronized (activationLock) {
            while (!active) {
                activationLock.wait();
            }
        }
    }
}
