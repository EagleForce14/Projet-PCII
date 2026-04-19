package model.culture;

/** Classe représentant une culture */
public class Culture {
    /*
     * La rivière accélère la pousse de 30 % :
     * on conserve donc 70 % du temps normal pour les stades de croissance.
     */
    private static final double RIVER_GROWTH_DELAY_MULTIPLIER = 0.70;

    /*
     * À l'inverse, une plante proche de l'eau reste viable plus longtemps.
     * 130 % signifie donc "30 % de temps en plus avant le flétrissement".
     */
    private static final double RIVER_WILT_DELAY_MULTIPLIER = 1.30;

    /** Attribut représentant le stade de croissance de la culture */
    private Stade stadeCroissance;

    /** Attribut stockant le thread gérant la croissance de la culture */
    private final Croissance threadCroissance;

    /** Attribut représentant le type concret de la culture */
    private final Type type;

    /** Attribut stockant si la culture a été arrosée */
    private boolean arrosee;

    /*
     * On ne fige pas le bonus rivière au moment de la plantation.
     * La culture relit directement la carte des cases rivière autour d'elle,
     * ce qui garde le modèle simple et explicite.
     */
    private final boolean[][] decorativeRiverCells;
    private final int gridX;
    private final int gridY;

    /** Constructeur de la classe Culture qui initialise le stade de croissance et démarre le thread de croissance */
    public Culture(Type type) {
        this(type, null, -1, -1);
    }

    /**
     * Variante utilisée par la grille quand une culture a besoin
     * de relire dynamiquement son environnement.
     */
    public Culture(Type type, boolean[][] decorativeRiverCells, int gridX, int gridY) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de culture ne peut pas être null.");
        }

        this.type = type;
        this.stadeCroissance = Stade.JEUNE_POUSSE;
        this.arrosee = false;
        this.decorativeRiverCells = decorativeRiverCells;
        this.gridX = gridX;
        this.gridY = gridY;
        this.threadCroissance = new Croissance(this);
        this.threadCroissance.start(); // Démarrer le thread de croissance
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

    /**
     * Utilisé pendant un redémarrage complet de partie.
     * On demande explicitement au thread de croissance de s'arrêter,
     * sinon l'ancienne plante continuerait à "exister" en arrière-plan.
     */
    public void arreterCroissance() {
        threadCroissance.arreter();
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

    /**
     * Le bonus rivière n'a rien d'absolu :
     * il dépend de l'état actuel des cases voisines.
     * Cette lecture reste donc volontairement dynamique.
     */
    public double getGrowthDelayMultiplier() {
        return isBoostedByRiver() ? RIVER_GROWTH_DELAY_MULTIPLIER : 1.0;
    }

    /**
     * Même idée pour le temps avant flétrissement.
     * On veut que la culture profite d'une rivière ajoutée ensuite,
     * sans avoir à la replanter.
     */
    public double getWiltDelayMultiplier() {
        return isBoostedByRiver() ? RIVER_WILT_DELAY_MULTIPLIER : 1.0;
    }

    /**
     * Quand le contexte change autour de la culture,
     * on réveille simplement le thread pour qu'il recalcule son temps restant.
     * C'est plus léger et plus sûr que de bricoler directement son chrono depuis la grille.
     */
    public void notifierContexteCroissanceModifie() {
        threadCroissance.reveillerPourRecalculDelai();
    }

    /** Méthode qui permet de manger la culture seulement si elle est à maturité */
    public boolean manger() {
        // Vérifie si la culture est à maturité avant de la manger
        if (stadeCroissance == Stade.MATURE) {
            threadCroissance.arreter(); // Arrêter le thread de croissance
            return true; // Indique que la culture a été mangée avec succès
        } else {
            throw new IllegalStateException("La culture n'est pas à maturité et ne peut pas être mangée.");
        }
    }

    private boolean isBoostedByRiver() {
        return GrilleCulture.isCellBoostedByRiver(decorativeRiverCells, gridX, gridY);
    }
}
