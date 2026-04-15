package model.grotte;

import model.movement.Unit;
import model.runtime.DefeatCause;
import model.runtime.GamePauseController;
import model.runtime.Jour;
import model.runtime.ThreadActivationGate;
import view.grotte.GrotteFieldPanel;

import java.awt.Point;

/**
 * Thread dédié au cycle de la statue.
 *
 * Le rôle de ce thread est :
 * - faire avancer le compte à rebours
 * - déclencher l'onde létale toutes les 30 secondes
 * - déclencher la défaite si le joueur se trouve dans une des cases concernées par l'onde.
 */
public final class ShrineHazardThread extends Thread {
    private static final long UPDATE_STEP_MS = 50L;

    private final ShrineHazardState shrineHazardState;
    private final Unit playerUnit;
    private final GrotteFieldPanel grotteFieldPanel;
    private final Jour jour;
    private final GamePauseController pauseController;
    private final ThreadActivationGate activationGate;

    private volatile boolean running = true;

    public ShrineHazardThread(ShrineHazardState shrineHazardState, Unit playerUnit, GrotteFieldPanel grotteFieldPanel, Jour jour) {
        this(shrineHazardState, playerUnit, grotteFieldPanel, jour, false);
    }

    public ShrineHazardThread(ShrineHazardState shrineHazardState, Unit playerUnit, GrotteFieldPanel grotteFieldPanel, Jour jour, boolean initiallyActive) {
        this.shrineHazardState = shrineHazardState;
        this.playerUnit = playerUnit;
        this.grotteFieldPanel = grotteFieldPanel;
        this.jour = jour;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    public void setThreadActive(boolean active) {
        shrineHazardState.resetCycle();
        activationGate.setActive(active);
    }

    @Override
    public void run() {
        shrineHazardState.resetCycle();

        while (running) {
            try {
                activationGate.awaitActivation();
                shrineHazardState.resetCycle();
                waitForNextPulse();
            } catch (InterruptedException exception) {
                interrupt();
                return;
            }

            if (!running) {
                break;
            }

            if (!activationGate.isActive()) {
                continue;
            }

            triggerShrinePulse();
            shrineHazardState.resetCycle();
        }
    }

    private void waitForNextPulse() throws InterruptedException {
        long elapsedInCycleMs = 0L;
        shrineHazardState.setElapsedInCycleMs(0L);

        while (running && activationGate.isActive() && elapsedInCycleMs < ShrineHazardState.CYCLE_DURATION_MS) {
            pauseController.awaitIfPaused();
            if (!activationGate.isActive()) {
                return;
            }

            long remainingMs = ShrineHazardState.CYCLE_DURATION_MS - elapsedInCycleMs;
            long chunkMs = Math.min(remainingMs, UPDATE_STEP_MS);
            long startMs = System.currentTimeMillis();
            Thread.sleep(chunkMs);
            long elapsedChunkMs = Math.max(1L, System.currentTimeMillis() - startMs);
            elapsedInCycleMs = Math.min(ShrineHazardState.CYCLE_DURATION_MS, elapsedInCycleMs + elapsedChunkMs);
            shrineHazardState.setElapsedInCycleMs(elapsedInCycleMs);
        }
    }

    private void triggerShrinePulse() {
        if (playerUnit == null || !playerUnit.isInCave()) {
            return;
        }

        Point playerCell = grotteFieldPanel.getLogicalGridPositionAt(playerUnit.getX(), playerUnit.getY());
        if (playerCell == null || !grotteFieldPanel.getGrotteMap().isShrineDangerCell(playerCell.x, playerCell.y)) {
            return;
        }

        playerUnit.stopMovement();
        if (jour != null) {
            jour.terminerPartie(DefeatCause.CAVE_SHRINE);
        }
    }
}
