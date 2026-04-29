package model.runtime;

import model.objective.GestionnaireObjectifs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread qui fait avancer le temps dans le jeu */
public class Jour extends Thread {

    /** Constante représente le délai entre deux jours */
    private static final int DELAI_JOUR = 60000; // 1 minute
    private static final long PAS_MISE_A_JOUR_PROGRESSION_MS = 50L;

    /** Attribut représentant le jour */
    private int jour;

    /** Attribut représentant l'état du thread */
    private boolean actif;
    private volatile boolean partieTerminee;
    private volatile DefeatCause defeatCause;
    private long elapsedInCurrentDayMs;
    private volatile boolean tempsFige;
    private final GamePauseController pauseController;
    private final Object freezeLock;

    /** Attribut représentant le gestionnaire d'objectifs */
    private final GestionnaireObjectifs gestionnaireObjectifs;

    // Listeners pour le changement de jour
    private final List<DayChangeListener> dayChangeListeners = new CopyOnWriteArrayList<>();

    /** Constructeur de la classe Jour */
    public Jour() {
        this.jour = 1; // Le jeu commence au jour 1
        this.actif = true;
        this.partieTerminee = false;
        this.defeatCause = DefeatCause.NONE;
        this.elapsedInCurrentDayMs = 0L;
        this.tempsFige = false;
        this.pauseController = GamePauseController.getInstance();
        this.freezeLock = new Object();
        this.gestionnaireObjectifs = new GestionnaireObjectifs(this);

    }

    /** Méthode run qui fait avancer le temps dans le jeu */
    @Override
    public void run() {
        while (actif) {
            try {
                waitUntilNextDayBoundary();
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
            if (!actif) {
                return;
            }

            /*
             * On remet la jauge a zero juste avant de lancer le nouveau jour.
             * Ainsi, le HUD repart immédiatement d'un cycle propre pour le jour suivant.
             */
            resetElapsedInCurrentDayMs();
            jour++; // Incrémenter le jour

            // Notifier les listeners avant de vérifier la validité du jour, afin qu'ils
            // puissent ajuster leur état si besoin (par ex. changer les prix du shop).
            notifyDayChangeListeners();

            if (!notifierChangementJour()) {
                terminerPartie(DefeatCause.OBJECTIVES);
            }
        }  
    }

    /** Getter sur le jour */
    public int getJour() {
        return jour;
    }

    /**
     * Expose la progression du chrono du jour courant pour le HUD.
     * 0.0 = début du jour, 1.0 = passage imminent au jour suivant.
     */
    public double getProgressionVersJourSuivant() {
        return Math.max(0.0, Math.min(1.0, getElapsedInCurrentDayMs() / (double) DELAI_JOUR));
    }

    /**
     * Renvoie le temps restant avant l'évaluation du jour.
     * Le résultat reste figé pendant une pause, car le chrono de jeu s'arrête lui aussi.
     */
    public long getTempsRestantAvantProchainJourMs() {
        return Math.max(0L, DELAI_JOUR - getElapsedInCurrentDayMs());
    }

    /**
     * Dans la grotte, le chrono journalier doit s'immobiliser :
     * aucun nouveau jour ne peut être évalué tant que le joueur y reste.
     */
    public void setTempsFige(boolean tempsFige) {
        synchronized (freezeLock) {
            this.tempsFige = tempsFige;
            if (!tempsFige) {
                freezeLock.notifyAll();
            }
        }
    }

    /** Méthode pour arrêter le thread */
    public void arreter() {
        actif = false;
    }

    /** Getter sur l'état de fin de partie */
    public boolean isPartieTerminee() {
        return partieTerminee;
    }

    public DefeatCause getDefeatCause() {
        return defeatCause;
    }

    /**
     * Permet à un danger hors du cycle normal des jours
     * de déclencher une vraie défaite immédiatement.
     */
    public synchronized void terminerPartie(DefeatCause defeatCause) {
        if (partieTerminee) {
            return;
        }

        partieTerminee = true;
        this.defeatCause = defeatCause == null ? DefeatCause.NONE : defeatCause;
        pauseController.setPaused(true);
        arreter();
        interrupt();
    }

    /** Getter sur le gestionnaire d'objectifs */
    public GestionnaireObjectifs getGestionnaireObjectifs() {
        return gestionnaireObjectifs;
    }

    /** Méthode qui notifie le gestionnaire d'objectifs du changement de jour */
    public boolean notifierChangementJour() {
        return gestionnaireObjectifs.appliquerChangementsJour();
    }

    /** Enregistrement d'un listener pour le changement de jour */
    public void addDayChangeListener(DayChangeListener listener) {
        if (listener != null) {
            dayChangeListeners.add(listener);
        }
    }

    private void notifyDayChangeListeners() {
        for (DayChangeListener listener : dayChangeListeners) {
            try {
                listener.onNewDay(jour);
            } catch (Exception ignored) {
                // Un listener défaillant ne doit pas interrompre la boucle de jour
            }
        }
    }

    /**
     * Attend la fin du jour courant en mettant régulièrement à jour la progression visible.
     * On ne mesure ici que le temps réellement joué : si la partie est en pause,
     * la jauge s'immobilise naturellement jusqu'à la reprise.
     */
    private void waitUntilNextDayBoundary() throws InterruptedException {
        while (actif && getElapsedInCurrentDayMs() < DELAI_JOUR) {
            pauseController.awaitIfPaused();
            attendreSiTempsFige();
            if (!actif) {
                return;
            }

            long remainingMs = DELAI_JOUR - getElapsedInCurrentDayMs();
            long chunkMs = Math.min(remainingMs, PAS_MISE_A_JOUR_PROGRESSION_MS);
            long startMs = System.currentTimeMillis();
            Thread.sleep(chunkMs);
            long elapsedChunkMs = Math.max(1L, System.currentTimeMillis() - startMs);
            advanceElapsedInCurrentDayMs(elapsedChunkMs);
        }
    }

    private synchronized long getElapsedInCurrentDayMs() {
        return elapsedInCurrentDayMs;
    }

    private synchronized void resetElapsedInCurrentDayMs() {
        elapsedInCurrentDayMs = 0L;
    }

    private synchronized void advanceElapsedInCurrentDayMs(long elapsedChunkMs) {
        elapsedInCurrentDayMs = Math.min(DELAI_JOUR, elapsedInCurrentDayMs + elapsedChunkMs);
    }

    private void attendreSiTempsFige() throws InterruptedException {
        synchronized (freezeLock) {
            while (actif && tempsFige) {
                freezeLock.wait();
            }
        }
    }
}
