package model.runtime;

/**
 * Classe permettant de mettre en pause le jeu.
 * Les threads viennent tous s'y synchroniser pour geler le temps
 * tant que la boutique, est ouverte.
 */
public final class GamePauseController {
    private static final GamePauseController INSTANCE = new GamePauseController();

    private final Object pauseLock = new Object();
    private volatile boolean paused;

    private GamePauseController() {
    }

    public static GamePauseController getInstance() {
        return INSTANCE;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        synchronized (pauseLock) {
            this.paused = paused;
            if (!paused) {
                pauseLock.notifyAll();
            }
        }
    }

    /**
     * Bloque le thread appelant tant que la pause est active.
     */
    public void awaitIfPaused() throws InterruptedException {
        synchronized (pauseLock) {
            while (paused) {
                pauseLock.wait();
            }
        }
    }

    /**
     * Dort en petits segments pour que le temps ne continue pas a s'ecouler
     * quand le jeu est mis en pause.
     */
    public void sleep(long durationMs) throws InterruptedException {
        long remainingMs = durationMs;
        while (remainingMs > 0L) {
            awaitIfPaused();

            long chunkMs = Math.min(remainingMs, 50L);
            long startMs = System.currentTimeMillis();
            Thread.sleep(chunkMs);
            long elapsedMs = System.currentTimeMillis() - startMs;
            remainingMs -= Math.max(1L, elapsedMs);
        }
    }
}
