package model.runtime;

/** Thread qui fait avancer le temps dans le jeu */
public class Jour extends Thread {

    /** Constante représente le délai entre deux jours */
    private static final int DELAI_JOUR = 60000; // 1 minute

    /** Attribut représentant le jour */
    private int jour;

    /** Attribut représentant l'état du thread */
    private boolean actif;
    private final GamePauseController pauseController;

    /** Constructeur de la classe Jour */
    public Jour() {
        this.jour = 1; // Le jeu commence au jour 1
        this.actif = true;
        this.pauseController = GamePauseController.getInstance();
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
}
