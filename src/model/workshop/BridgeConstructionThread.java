package model.workshop;

/**
 * Thread dédié à une seule construction de pont.
 * Il dort pendant la durée de fabrication, puis prévient le manager
 * pour finaliser proprement l'objet dans l'inventaire.
 */
public final class BridgeConstructionThread extends Thread {
    // Manager à prévenir une fois la fabrication du pont terminée.
    private final WorkshopConstructionManager manager;
    // Durée totale de fabrication à attendre avant de livrer le pont.
    private final long durationMs;

    /**
     * On prépare un thread dédié à une seule fabrication de pont.
     */
    public BridgeConstructionThread(WorkshopConstructionManager manager, long durationMs) {
        super("bridge-construction-thread");
        this.manager = manager;
        this.durationMs = durationMs;
        setDaemon(true);
    }

    /**
     * On attend la fin du temps de fabrication, puis on signale au manager que le pont est prêt.
     */
    @Override
    public void run() {
        try {
            Thread.sleep(durationMs);
            manager.completeBridgeConstruction(this);
        } catch (InterruptedException interruptedException) {
            interrupt();
        }
    }
}
