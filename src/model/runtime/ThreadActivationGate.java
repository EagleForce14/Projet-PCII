package model.runtime;

/**
 * Verrou pour endormir complètement un thread tant que sa scène
 * n'est pas active afin d'optimiser les performances.

 * Les threads de la grotte s'en servent pour ne faire aucun travail
 * lorsque le joueur reste sur la ferme.
 */
public final class ThreadActivationGate {
    private final Object activationLock = new Object();
    private volatile boolean active;

    public ThreadActivationGate(boolean initiallyActive) {
        this.active = initiallyActive;
    }

    public void setActive(boolean active) {
        synchronized (activationLock) {
            this.active = active;
            activationLock.notifyAll();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void awaitActivation() throws InterruptedException {
        synchronized (activationLock) {
            while (!active) {
                activationLock.wait();
            }
        }
    }
}
