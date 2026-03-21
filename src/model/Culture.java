package model;

/** Classe représentant une culture */
public class Culture {

    /** Attribut représentant le stade de croissance de la culture */
    private Stade stadeCroissance;

    /** Attribut stockant le thread gérant la croissance de la culture */
    private final Croissance threadCroissance;

    /** Attribut représentant le type concret de la culture */
    private final Type type;

    /** Attribut stockant si la culture a été arrosée */
    private boolean arrosee;

    /** Constructeur de la classe Culture qui initialise le stade de croissance et démarre le thread de croissance */
    public Culture(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de culture ne peut pas être null.");
        }

        this.type = type;
        this.stadeCroissance = Stade.JEUNE_POUSSE;
        this.threadCroissance = new Croissance(this);
        this.threadCroissance.start(); // Démarrer le thread de croissance
        this.arrosee = false;
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

    /** Méthode qui arrose la culture et la fait grandir plus vite */
    public void arroser() {
        // Vérifie si la culture est à un stade intermédiaire avant de l'arroser
        if (stadeCroissance == Stade.INTERMEDIAIRE) {
            if (!arrosee) {
                arrosee = true;
                threadCroissance.reveillerPourRecalculDelai(); // Réveiller le thread de croissance pour recalculer le délai
            } else {
                throw new IllegalStateException("La culture a déjà été arrosée et ne peut pas être arrosée à nouveau.");
            }
        } else {
            throw new IllegalStateException("La culture n'est pas à un stade intermédiaire et ne peut pas être arrosée.");
        }
    }

    /** Getter pour l'attribut type */
    public Type getType() {
        return type;
    }

    /** Getter pour l'attribut arrosee */
    public boolean isArrosee() {
        return arrosee;
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
