package model.culture;

/** Classe représentant une zone de culture */
public class ZoneCulture {
    /**
     * Une case peut exister physiquement sur la map sans encore être "prête à cultiver".
     * On sépare donc bien :
     * - le fait que la case soit labourée,
     * - le fait qu'elle contienne ou non une culture.
     */
    private boolean labouree;

    /** Attribut représentant la culture dans la zone */
    private Culture culture;
    // Une zone libre peut vivre seule dans un test, ou appartenir à une vraie grille.
    // Quand elle connaît simplement la carte des rivières et sa position,
    // la culture plantée peut relire le bonus sans callback ni dépendance inutile à toute la grille.
    private final boolean[][] decorativeRiverCells;
    private final int gridX;
    private final int gridY;

    /**
     * Ce constructeur reste pratique pour les tests unitaires
     * qui manipulent une simple parcelle déjà prête à accueillir une plante.
     */
    public ZoneCulture() {
        this(true, null, -1, -1);
    }

    /**
     * Constructeur explicite utilisé par la grille de la map.
     * On peut ainsi créer des cases d'herbe non labourées au démarrage.
     */
    public ZoneCulture(boolean laboureeInitialement) {
        this(laboureeInitialement, null, -1, -1);
    }

    /**
     * Constructeur utilisé par la grille.
     * La zone connaît alors son emplacement logique et peut transmettre ce contexte
     * à la culture pour les bonus liés à l'environnement.
     */
    public ZoneCulture(boolean laboureeInitialement, boolean[][] decorativeRiverCells, int gridX, int gridY) {
        this.labouree = laboureeInitialement;
        this.culture = null;
        this.decorativeRiverCells = decorativeRiverCells;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    /** Getter pour l'attribut culture */
    public Culture getCulture() {
        return culture;
    }

    /**
     * Le labourage est volontairement très simple :
     * il transforme une case d'herbe en case de terre prête à l'emploi.
     * S'il est rejoué sur une case déjà labourée, on ne fait rien.
     */
    public void labourer() {
        labouree = true;
    }

    /**
     * Remet la case à l'état herbe seulement si elle est vide.
     * On évite ainsi toute perte silencieuse d'une culture en place.
     */
    public void remettreEnHerbe() {
        if (culture != null) {
            throw new IllegalStateException("Impossible de remettre en herbe une case qui contient une culture.");
        }
        labouree = false;
    }

    public boolean isLabouree() {
        return labouree;
    }

    /**
     * Méthode qui plante une culture dans la zone
     *
     * @param type : le type concret de la culture à planter
     * @throws IllegalStateException si la zone de culture est déjà occupée par une culture
     **/
    public void planterCulture(Type type) {
        // Règle importante :
        // tant que le joueur n'a pas labouré la case, on reste sur de l'herbe.
        if (!labouree) {
            throw new IllegalStateException("La case doit d'abord etre labourée avant de planter.");
        }

        // Vérifie s'il n'y a pas déjà une culture dans la zone avant de planter une nouvelle culture
        if (culture != null) {
            throw new IllegalStateException("Il y a déjà une culture plantée dans cette zone.");
        } else {
            this.culture = new Culture(type, decorativeRiverCells, gridX, gridY);
        }
    }

    /** Méthode qui récolte la culture de la zone et renvoie son prix de vente */
    public int recolterCulture() {
        // Vérifie s'il y a une culture à récolter dans la zone
        if (culture != null) {
            int prix = culture.recolter();
            this.culture = null;
            return prix;
        } else {
            throw new IllegalStateException("Il n'y a pas de culture à récolter dans cette zone.");
        }
    }

    /** Méthode qui arrose la culture de la zone */
    public void arroserCulture() {
        // Vérifie s'il y a une culture à arroser dans la zone
        if (culture != null) {
            culture.arroser();
        } else {
            throw new IllegalStateException("Il n'y a pas de culture à arroser dans cette zone.");
        }
    }

    /** Méthode qui mange la culture de la zone */
    public boolean mangerCulture() {
        // Vérifie s'il y a une culture à manger dans la zone
        if (culture != null) {
            boolean mangerReussi = culture.manger();
            if (mangerReussi) {
                this.culture = null; // La culture est mangée et retirée de la zone
            }
            return mangerReussi; // Indique que la culture a été mangée avec succès
        } else {
            throw new IllegalStateException("Il n'y a pas de culture à manger dans cette zone.");
        }
    }

    /** Retire la culture seulement si elle est vraiment flétrie. */
    public void nettoyerCultureFletrie() {
        if (culture != null && culture.getStadeCroissance() == Stade.FLETRIE) {
            culture = null;
        }
    }
}
