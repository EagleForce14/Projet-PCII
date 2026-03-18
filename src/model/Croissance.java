package model;

/** Thread gérant automatiquement la croissance d'une culture */
public class Croissance extends Thread {
    private static final int DELAI_CROISSANCE = 7000;
    private static final int DELAI_AVANT_FLETRISSEMENT = 10000;

    /** Attribut représentant la culture associée à ce thread de croissance */
    private final Culture culture;

    /** Attribut représentant l'état actif du thread de croissance */
    private boolean actif = true;

    /** Constructeur de la classe Croissance qui prend en argument la culture à gérer */
    public Croissance(Culture culture) {
        this.culture = culture;
    }

    /** Méthode pour arrêter le thread de croissance */
    public void arreter() {
        actif = false;
    }

    /** Méthode run qui gère la croissance de la culture en fonction du temps */
    @Override
    public void run() {
        while (actif) {
            try {
                // On allonge uniquement le temps passé au stade mature.
                int delai = DELAI_CROISSANCE;
                if (culture.getStadeCroissance() == Stade.MATURE) {
                    delai = DELAI_AVANT_FLETRISSEMENT;
                }
                Thread.sleep(delai);
                Stade nouveauStade = culture.grandir(); // Faire grandir la culture et récupérer le nouveau stade
                if (nouveauStade == Stade.FLETRIE) {
                    this.arreter(); // Arrêter le thread si la culture est flétrie
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
