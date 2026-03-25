package model.culture;

import model.runtime.GamePauseController;

/** Thread gérant automatiquement la croissance d'une culture */
public class Croissance extends Thread {

    /** Constante représentant le délai de croissance entre chaque stade */
    private static final long DELAI_CROISSANCE = 7000; // 7 secondes

    /** Constante représentant le délai avant le flétrissement à partir du stade mature */
    private static final long DELAI_AVANT_FLETRISSEMENT = 10000; // 10 secondes

    /** Constante représentant le délai réduit après un arrosage */
    private static final long DELAI_CROISSANCE_ARROSE = 4000; // 4 secondes

    /** Attribut représentant la culture associée à ce thread de croissance */
    private final Culture culture;

    /** Attribut représentant l'état actif du thread de croissance */
    private volatile boolean actif = true;
    private final GamePauseController pauseController;

    /** Constructeur de la classe Croissance qui prend en argument la culture à gérer */
    public Croissance(Culture culture) {
        this.culture = culture;
        this.pauseController = GamePauseController.getInstance();
    }

    /** Méthode pour arrêter le thread de croissance */
    public void arreter() {
        actif = false;
        interrupt(); // Réveille immédiatement si le thread dort
    }

    /** 
     * Méthode pour réveiller le thread de croissance lors de l'arrosage pour recalculer le délai 
     */
    public void reveillerPourRecalculDelai() {
        this.interrupt();
    }

    /** Méthode qui calcule le délai pour le stade courant */
    private long delaiPourEtatActuel() {
        if (culture.getStadeCroissance() == Stade.MATURE) {
            System.out.println("delai fletrissement : " + DELAI_AVANT_FLETRISSEMENT);
            return DELAI_AVANT_FLETRISSEMENT;
        } else if (culture.isArrosee()) {
            System.out.println("delai arrosage : " + DELAI_CROISSANCE_ARROSE);
            return DELAI_CROISSANCE_ARROSE;
        } else {
            System.out.println("delai normal : " + DELAI_CROISSANCE);
            return DELAI_CROISSANCE;
        }
    }

    /** Méthode run qui gère la croissance de la culture en fonction du temps */
    @Override
    public void run() {
        // Attribut représentant l'instant de départ du stade courant
        long debutStadeMs = System.currentTimeMillis();

        while (actif) {
            try {
                // Calcul du délai à dormir pour le stade actuel
                long delaiCible = delaiPourEtatActuel();
                // Calcul du temps écoulé et du temps restant à dormir
                long delaiEcoule = System.currentTimeMillis() - debutStadeMs;
                long delaiRestant = delaiCible - delaiEcoule;

                if (delaiRestant > 0) {
                    pauseController.sleep(delaiRestant);
                }

                Stade nouveauStade = culture.grandir();
                debutStadeMs = System.currentTimeMillis(); // Nouveau stade => nouveau chrono

                if (nouveauStade == Stade.FLETRIE) {
                    arreter();
                }
            } catch (InterruptedException e) {
                if (!actif) {
                    break; // Interruption d'arrêt
                }
                // Interruption "normale" : arrosage => on reboucle et recalcule le delai restant.
            }
        }
    }

}
