package model.workshop;

import model.management.Inventaire;
import model.shop.FacilityType;

/**
 * Coordonne les constructions lancées depuis la menuiserie.
 * Le manager expose uniquement un petit état de lecture
 * pour que la carte et l'overlay puissent se synchroniser sans dupliquer la logique.
 */
public final class WorkshopConstructionManager {
    public static final int DEFAULT_BRIDGE_WOOD_COST = 10;
    public static final long DEFAULT_BRIDGE_DURATION_MS = 45_000L;

    // Pour le mutex
    private final Object stateLock;
    private final Inventaire inventaire;
    private final int bridgeWoodCost;
    private final long bridgeDurationMs;

    private volatile BridgeConstructionThread activeConstructionThread;
    private volatile long constructionStartedAtMs;
    private volatile long constructionEndsAtMs;
    private volatile boolean shutdownRequested;

    public WorkshopConstructionManager(Inventaire inventaire) {
        this(inventaire, DEFAULT_BRIDGE_WOOD_COST, DEFAULT_BRIDGE_DURATION_MS);
    }

    public WorkshopConstructionManager(Inventaire inventaire, int bridgeWoodCost, long bridgeDurationMs) {
        if (inventaire == null) {
            throw new IllegalArgumentException("L'inventaire de la menuiserie est obligatoire.");
        }
        if (bridgeWoodCost <= 0 || bridgeDurationMs <= 0L) {
            throw new IllegalArgumentException("Les paramètres de construction doivent être strictement positifs.");
        }

        this.stateLock = new Object();
        this.inventaire = inventaire;
        this.bridgeWoodCost = bridgeWoodCost;
        this.bridgeDurationMs = bridgeDurationMs;
    }

    public boolean canStartBridgeConstruction() {
        return !isConstructionInProgress() && inventaire.possedeBois(bridgeWoodCost);
    }

    public boolean startBridgeConstruction() {
        synchronized (stateLock) {
            if (shutdownRequested || activeConstructionThread != null || !inventaire.possedeBois(bridgeWoodCost)) {
                return true;
            }

            inventaire.retirerBois(bridgeWoodCost);
            constructionStartedAtMs = System.currentTimeMillis();
            constructionEndsAtMs = constructionStartedAtMs + bridgeDurationMs;

            BridgeConstructionThread constructionThread = new BridgeConstructionThread(this, bridgeDurationMs);
            activeConstructionThread = constructionThread;
            constructionThread.start();
            return false;
        }
    }

    void completeBridgeConstruction(BridgeConstructionThread sourceThread) {
        synchronized (stateLock) {
            if (shutdownRequested || activeConstructionThread != sourceThread) {
                return;
            }

            inventaire.ajoutInstallation(FacilityType.PONT, 1);
            clearConstructionState();
        }
    }

    public boolean isConstructionInProgress() {
        return activeConstructionThread != null;
    }

    public long getRemainingConstructionMs() {
        if (!isConstructionInProgress()) {
            return 0L;
        }

        return Math.max(0L, constructionEndsAtMs - System.currentTimeMillis());
    }

    public double getConstructionProgressRatio() {
        if (!isConstructionInProgress()) {
            return 0.0;
        }

        long elapsedMs = System.currentTimeMillis() - constructionStartedAtMs;
        return Math.max(0.0, Math.min(1.0, elapsedMs / (double) bridgeDurationMs));
    }

    public int getBridgeWoodCost() {
        return bridgeWoodCost;
    }

    public long getBridgeDurationMs() {
        return bridgeDurationMs;
    }

    public String getConstructionLabel() {
        return "Pont";
    }

    public void shutdown() {
        BridgeConstructionThread threadToStop;
        synchronized (stateLock) {
            shutdownRequested = true;
            threadToStop = activeConstructionThread;
            clearConstructionState();
        }

        if (threadToStop != null) {
            threadToStop.interrupt();
        }
    }

    public static String formatDuration(long durationMs) {
        long safeDurationMs = Math.max(0L, durationMs);
        long totalSeconds = (safeDurationMs + 999L) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void clearConstructionState() {
        activeConstructionThread = null;
        constructionStartedAtMs = 0L;
        constructionEndsAtMs = 0L;
    }
}
