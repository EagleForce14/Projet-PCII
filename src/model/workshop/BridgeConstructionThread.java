package model.workshop;

/**
 * Thread dédié à une seule construction de pont.
 * Il dort pendant la durée de fabrication, puis prévient le manager
 * pour finaliser proprement l'objet dans l'inventaire.
 */
public final class BridgeConstructionThread extends Thread {
    private final WorkshopConstructionManager manager;
    private final long durationMs;

    public BridgeConstructionThread(WorkshopConstructionManager manager, long durationMs) {
        super("bridge-construction-thread");
        this.manager = manager;
        this.durationMs = durationMs;
        setDaemon(true);
    }

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
