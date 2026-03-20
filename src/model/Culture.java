package model;

/** Classe représentant une culture */
public class Culture {

    /** Attribut représentant le stade de croissance de la culture */
    private Stade stadeCroissance;

    /** Attribut stockant le thread gérant la croissance de la culture */
    private final Croissance threadCroissance;

    /** Attribut représentant le type générique de la culture */
    private final Type type;
    /** Attribut représentant le type spécifique de la culture (fleur ou légume) */
    private final Object typeSpecific;

    /** Constructeur de la classe Culture qui initialise le stade de croissance et démarre le thread de croissance */
    public Culture(Object typeSpe) {
        this.typeSpecific = typeSpe;
        this.stadeCroissance = Stade.JEUNE_POUSSE;
        this.threadCroissance = new Croissance(this);
        this.threadCroissance.start(); // Démarrer le thread de croissance
        
        // initialisation de type 
        if (typeSpe instanceof FleurType) {
            this.type = Type.FLEURS;
        } else if (typeSpe instanceof LegumeType) {
            this.type = Type.LEGUMES;
        } else {
            throw new IllegalArgumentException("Le type spécifique doit être une instance de FleurType ou LegumeType.");
        }
    }

    /** Méthode qui fait grandir la culture et renvoie le nouveau stade de croissance */
    public Stade grandir() {
        if (stadeCroissance == Stade.JEUNE_POUSSE) {
            stadeCroissance = Stade.INTERMEDIAIRE;
        } else if (stadeCroissance == Stade.INTERMEDIAIRE) {
            stadeCroissance = Stade.MATURE;
        } else if (stadeCroissance == Stade.MATURE) {
            stadeCroissance = Stade.FLETRIE;
        }
        return stadeCroissance;
    }

    /** Getter pour l'attribut stadeCroissance */
    public Stade getStadeCroissance() {
        return stadeCroissance;
    }

    /** Getter sur l'état du thread de croissance */
    public boolean isThreadCroissanceAlive() {
        return threadCroissance.isAlive();
    }

    /** Méthode qui récolte la culture et renvoie son prix de vente */
    public int recolter() {
        // Vérifie si la culture est à maturité avant de la récolter
        if (stadeCroissance == Stade.MATURE) {
            // Arrêter le thread de croissance
            threadCroissance.arreter();
            return GrilleCulture.getPrixVente(type);
        } else {
            throw new IllegalStateException("La culture n'est pas à maturité et ne peut pas être récoltée.");
        }
    }

    /** Getter pour l'attribut type */
    public Type getType() {
        return type;
    }

    /** Méthode qui permet de manger la culture seulement si elle est à maturité */
    public void manger() {
        // Vérifie si la culture est à maturité avant de la manger
        if (stadeCroissance == Stade.MATURE) {
            threadCroissance.arreter(); // Arrêter le thread de croissance
        } else {
            throw new IllegalStateException("La culture n'est pas à maturité et ne peut pas être mangée.");
        }
    }
}