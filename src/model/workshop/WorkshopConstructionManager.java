package model.workshop;

import model.management.Inventaire;
import model.shop.FacilityType;

/**
 * Coordonne les constructions lancées depuis la menuiserie.
 * Le manager expose uniquement un petit état de lecture
 * pour que la carte et l'overlay puissent se synchroniser sans dupliquer la logique.
 */
public final class WorkshopConstructionManager {
    // Coût en bois appliqué par défaut pour lancer une fabrication de pont.
    public static final int DEFAULT_BRIDGE_WOOD_COST = 10;
    // Durée par défaut d'une fabrication de pont.
    public static final long DEFAULT_BRIDGE_DURATION_MS = 45_000L;

    // Verrou interne qui protège l'état de construction.
    private final Object stateLock;
    // Inventaire sur lequel on prélève le bois et on ajoute le pont terminé.
    private final Inventaire inventaire;
    // Coût réel en bois de la fabrication gérée par ce manager.
    private final int bridgeWoodCost;
    // Durée réelle de fabrication gérée par ce manager.
    private final long bridgeDurationMs;

    // Thread actuellement chargé de la fabrication en cours, s'il y en a une.
    private volatile BridgeConstructionThread activeConstructionThread;
    // Instant exact de démarrage de la fabrication courante.
    private volatile long constructionStartedAtMs;
    // Instant exact auquel la fabrication courante doit se terminer.
    private volatile long constructionEndsAtMs;
    // Indique si le manager est en train de s'arrêter définitivement.
    private volatile boolean shutdownRequested;

    /**
     * On crée un manager avec les paramètres de fabrication par défaut.
     */
    public WorkshopConstructionManager(Inventaire inventaire) {
        this(inventaire, DEFAULT_BRIDGE_WOOD_COST, DEFAULT_BRIDGE_DURATION_MS);
    }

    /**
     * On prépare ici toute la gestion d'une fabrication de pont avec son coût et sa durée.
     */
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

    /**
     * On dit si une nouvelle fabrication peut démarrer tout de suite.
     */
    public boolean canStartBridgeConstruction() {
        return !isConstructionInProgress() && inventaire.possedeBois(bridgeWoodCost);
    }

    /**
     * On lance une fabrication de pont si rien ne l'empêche déjà.
     */
    public boolean startBridgeConstruction() {
        synchronized (stateLock) {
            // Si le manager s'arrête, si un pont est déjà en cours,
            // ou si le bois manque, on ne démarre rien.
            if (shutdownRequested || activeConstructionThread != null || !inventaire.possedeBois(bridgeWoodCost)) {
                return true;
            }

            // On prélève le bois immédiatement, puis on mémorise la fenêtre temporelle
            // de la construction pour que l'UI puisse afficher un vrai compte à rebours.
            inventaire.retirerBois(bridgeWoodCost);
            constructionStartedAtMs = System.currentTimeMillis();
            constructionEndsAtMs = constructionStartedAtMs + bridgeDurationMs;

            // Enfin, on crée le thread dédié à cette seule fabrication et on le lance.
            BridgeConstructionThread constructionThread = new BridgeConstructionThread(this, bridgeDurationMs);
            activeConstructionThread = constructionThread;
            constructionThread.start();
            return false;
        }
    }

    /**
     * On finalise la fabrication seulement si le thread qui répond est bien celui encore attendu.
     */
    void completeBridgeConstruction(BridgeConstructionThread sourceThread) {
        synchronized (stateLock) {
            if (shutdownRequested || activeConstructionThread != sourceThread) {
                return;
            }

            inventaire.ajoutInstallation(FacilityType.PONT, 1);
            clearConstructionState();
        }
    }

    /**
     * On dit si une fabrication de pont est actuellement en cours.
     */
    public boolean isConstructionInProgress() {
        return activeConstructionThread != null;
    }

    /**
     * On renvoie le temps restant avant la fin de la fabrication courante.
     */
    public long getRemainingConstructionMs() {
        if (!isConstructionInProgress()) {
            return 0L;
        }

        return Math.max(0L, constructionEndsAtMs - System.currentTimeMillis());
    }

    /**
     * On renvoie l'avancement normalisé de la fabrication entre 0 et 1.
     */
    public double getConstructionProgressRatio() {
        if (!isConstructionInProgress()) {
            return 0.0;
        }

        long elapsedMs = System.currentTimeMillis() - constructionStartedAtMs;
        return Math.max(0.0, Math.min(1.0, elapsedMs / (double) bridgeDurationMs));
    }

    /**
     * On expose le coût en bois demandé pour fabriquer un pont.
     */
    public int getBridgeWoodCost() {
        return bridgeWoodCost;
    }

    /**
     * On expose la durée totale de fabrication d'un pont.
     */
    public long getBridgeDurationMs() {
        return bridgeDurationMs;
    }

    /**
     * On renvoie le libellé court de l'objet fabriqué par ce manager.
     */
    public String getConstructionLabel() {
        return "Pont";
    }

    /**
     * On arrête proprement le manager et toute fabrication encore en cours.
     */
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

    /**
     * On transforme une durée en millisecondes en texte `mm:ss` lisible par l'interface.
     */
    public static String formatDuration(long durationMs) {
        long safeDurationMs = Math.max(0L, durationMs);
        long totalSeconds = (safeDurationMs + 999L) / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * On remet à zéro tout l'état temporaire lié à une fabrication en cours.
     */
    private void clearConstructionState() {
        activeConstructionThread = null;
        constructionStartedAtMs = 0L;
        constructionEndsAtMs = 0L;
    }
}
