package model.runtime;

import model.objective.GestionnaireObjectifs;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread qui fait avancer le temps dans le jeu */
public class Jour extends Thread {

    /** Constante représente le délai entre deux jours */
    private static final int DELAI_JOUR = 60000; // 1 minute

    /** Attribut représentant le jour */
    private int jour;

    /** Attribut représentant l'état du thread */
    private boolean actif;
    private volatile boolean partieTerminee;
    private final GamePauseController pauseController;

    /** Attribut représentant le gestionnaire d'objectifs */
    private GestionnaireObjectifs gestionnaireObjectifs;

    // Listeners pour le changement de jour
    private final List<DayChangeListener> dayChangeListeners = new CopyOnWriteArrayList<>();

    /** Constructeur de la classe Jour */
    public Jour() {
        this.jour = 1; // Le jeu commence au jour 1
        this.actif = true;
        this.partieTerminee = false;
        this.pauseController = GamePauseController.getInstance();
        this.gestionnaireObjectifs = new GestionnaireObjectifs(this);

    }

    /** Méthode run qui fait avancer le temps dans le jeu */
    @Override
    public void run() {
        while (actif) {
            try {
                pauseController.sleep(DELAI_JOUR); // Attendre le délai entre deux jours
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
            jour++; // Incrémenter le jour

            // Notifier les listeners avant de vérifier la validité du jour, afin qu'ils
            // puissent ajuster leur état si besoin (par ex. changer les prix du shop).
            notifyDayChangeListeners();

            if (!notifierChangementJour()) {
                partieTerminee = true;
                pauseController.setPaused(true);
                arreter();
            }
        }  
    }

    /** Getter sur le jour */
    public int getJour() {
        return jour;
    }

    /** Méthode pour arrêter le thread */
    public void arreter() {
        actif = false;
    }

    /** Getter sur l'état de fin de partie */
    public boolean isPartieTerminee() {
        return partieTerminee;
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

    /** Retrait d'un listener */
    public void removeDayChangeListener(DayChangeListener listener) {
        dayChangeListeners.remove(listener);
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
}
