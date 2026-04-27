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
    // Pas maximal entre deux mises à jour du compte à rebours.
    private static final long UPDATE_STEP_MS = 50L;

    // État partagé que la vue lit pour afficher le cycle du sanctuaire.
    private final ShrineHazardState shrineHazardState;
    // Joueur à surveiller pour savoir si l'onde doit provoquer la défaite.
    private final Unit playerUnit;
    // Vue de grotte utilisée pour convertir la position du joueur en case logique.
    private final GrotteFieldPanel grotteFieldPanel;
    // Référence de session utilisée pour terminer proprement la partie.
    private final Jour jour;
    // Contrôleur global de pause partagé avec le reste du jeu.
    private final GamePauseController pauseController;
    // Porte d'activation qui permet de suspendre ce cycle sans tuer le thread.
    private final ThreadActivationGate activationGate;

    // Indique si le thread doit continuer à tourner.
    private volatile boolean running = true;

    /**
     * On crée le thread du sanctuaire avec un état d'activation initial par défaut.
     */
    public ShrineHazardThread(ShrineHazardState shrineHazardState, Unit playerUnit, GrotteFieldPanel grotteFieldPanel, Jour jour) {
        this(shrineHazardState, playerUnit, grotteFieldPanel, jour, false);
    }

    /**
     * On prépare ici toute la boucle temporelle de la statue.
     */
    public ShrineHazardThread(ShrineHazardState shrineHazardState, Unit playerUnit, GrotteFieldPanel grotteFieldPanel, Jour jour, boolean initiallyActive) {
        this.shrineHazardState = shrineHazardState;
        this.playerUnit = playerUnit;
        this.grotteFieldPanel = grotteFieldPanel;
        this.jour = jour;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    /**
     * On demande l'arrêt complet du thread du sanctuaire.
     */
    public void shutdown() {
        running = false;
        interrupt();
    }

    /**
     * On active ou on coupe le cycle, en repartant toujours d'un compte à rebours propre.
     */
    public void setThreadActive(boolean active) {
        shrineHazardState.resetCycle();
        activationGate.setActive(active);
    }

    /**
     * On fait tourner ici le cycle complet attente puis déclenchement de l'onde du sanctuaire.
     */
    @Override
    public void run() {
        shrineHazardState.resetCycle();

        while (running) {
            try {
                // On attend d'abord que la grotte soit réellement active avant de lancer un cycle.
                activationGate.awaitActivation();
                // Chaque nouvelle activation repart d'un cycle remis à zéro.
                shrineHazardState.resetCycle();
                waitForNextPulse();
            } catch (InterruptedException exception) {
                interrupt();
                return;
            }

            // Si l'arrêt a été demandé pendant l'attente, on sort proprement.
            if (!running) {
                break;
            }

            // Si la grotte a été désactivée entre-temps, on saute l'impulsion.
            if (!activationGate.isActive()) {
                continue;
            }

            // Sinon on déclenche l'onde, puis on remet l'état visuel au début du cycle suivant.
            triggerShrinePulse();
            shrineHazardState.resetCycle();
        }
    }

    /**
     * On fait avancer le compte à rebours par petits pas pour garder un affichage fluide.
     */
    private void waitForNextPulse() throws InterruptedException {
        long elapsedInCycleMs = 0L;
        shrineHazardState.setElapsedInCycleMs(0L);

        while (running && activationGate.isActive() && elapsedInCycleMs < ShrineHazardState.CYCLE_DURATION_MS) {
            // La pause globale doit figer entièrement l'écoulement du sanctuaire.
            pauseController.awaitIfPaused();
            // Si la grotte a été désactivée pendant la pause, on arrête ce cycle ici.
            if (!activationGate.isActive()) {
                return;
            }

            long remainingMs = ShrineHazardState.CYCLE_DURATION_MS - elapsedInCycleMs;
            // On dort par petits morceaux pour pouvoir mettre à jour la vue régulièrement.
            long chunkMs = Math.min(remainingMs, UPDATE_STEP_MS);
            long startMs = System.currentTimeMillis();
            Thread.sleep(chunkMs);
            // On mesure le temps réellement écoulé pour garder un chrono fidèle,
            // même si le sleep n'est pas parfaitement exact.
            long elapsedChunkMs = Math.max(1L, System.currentTimeMillis() - startMs);
            elapsedInCycleMs = Math.min(ShrineHazardState.CYCLE_DURATION_MS, elapsedInCycleMs + elapsedChunkMs);
            shrineHazardState.setElapsedInCycleMs(elapsedInCycleMs);
        }
    }

    /**
     * On applique l'onde du sanctuaire uniquement si le joueur se trouve réellement dans sa zone dangereuse.
     */
    private void triggerShrinePulse() {
        // Hors grotte, l'onde n'a aucun effet même si le thread existe encore.
        if (playerUnit == null || !playerUnit.isInCave()) {
            return;
        }

        Point playerCell = grotteFieldPanel.getLogicalGridPositionAt(playerUnit.getX(), playerUnit.getY());
        // Si la position ne se convertit pas en case logique, ou si cette case n'est pas dangereuse,
        // l'onde ne fait rien cette fois-ci.
        if (playerCell == null || !grotteFieldPanel.getGrotteMap().isShrineDangerCell(playerCell.x, playerCell.y)) {
            return;
        }

        // Si le joueur est bien sur une case létale, on coupe son mouvement
        // puis on termine immédiatement la partie avec la cause dédiée.
        playerUnit.stopMovement();
        if (jour != null) {
            jour.terminerPartie(DefeatCause.CAVE_SHRINE);
        }
    }
}
