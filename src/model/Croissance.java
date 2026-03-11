package model;

/** Thread gérant automatiquement la croissance d'une culture */
public class Croissance extends Thread {

    /** Attribut représentant la culture associée à ce thread de croissance */
    private Culture culture;

    /** Attribut représentant l'état actif du thread de croissance */
    private boolean actif = true;

    /** Attribut représentant le délai à attendre entre chaque stade de croissance */
    private static final int DELAI_CROISSANCE = 2000; // Délai de croissance en millisecondes (exemple : 2 secondes)

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
                Thread.sleep(DELAI_CROISSANCE); // Attendre le délai de croissance
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