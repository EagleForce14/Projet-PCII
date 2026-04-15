package model.grotte;

/**
 * État du cycle de la statue du sanctuaire.
 *
 * La vue lit uniquement cet état pour dessiner le compte à rebours
 * et le clignotement des cases dangereuses.
 * Le thread dédié reste le seul endroit qui le fait avancer.
 */
public final class ShrineHazardState {
    public static final long CYCLE_DURATION_MS = 30_000L;
    public static final long WARNING_DURATION_MS = 10_000L;
    private static final long WARNING_BLINK_INTERVAL_MS = 420L;

    private volatile long elapsedInCycleMs;

    // Le constructeur
    public ShrineHazardState() {
        resetCycle();
    }

    public synchronized void resetCycle() {
        elapsedInCycleMs = 0L;
    }

    public synchronized void setElapsedInCycleMs(long elapsedInCycleMs) {
        this.elapsedInCycleMs = Math.max(0L, Math.min(CYCLE_DURATION_MS, elapsedInCycleMs));
    }

    public long getRemainingMs() {
        return Math.max(0L, CYCLE_DURATION_MS - elapsedInCycleMs);
    }

    public int getRemainingSeconds() {
        return (int) Math.max(0L, Math.ceil(getRemainingMs() / 1000.0));
    }

    /**
     * La barre représente le temps restant avant la prochaine onde létale.
     * 1.0 = cycle tout juste relancé, 0.0 = déclenchement imminent.
     */
    public double getRemainingRatio() {
        return Math.max(0.0, Math.min(1.0, getRemainingMs() / (double) CYCLE_DURATION_MS));
    }

    public boolean isWarningPhase() {
        return getRemainingMs() > WARNING_DURATION_MS;
    }

    /**
     * Le clignotement est déterministe et dépend uniquement du temps écoulé dans la zone d'alerte.
     * La vue n'a ainsi aucune logique temporelle parallèle à maintenir.
     */
    public boolean isWarningBlinkVisible() {
        if (isWarningPhase()) {
            return false;
        }

        long warningElapsedMs = WARNING_DURATION_MS - getRemainingMs();
        long blinkIndex = warningElapsedMs / WARNING_BLINK_INTERVAL_MS;
        return blinkIndex % 2L == 0L;
    }
}
