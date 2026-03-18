package model;

/** Classe représentant une zone de culture */
public class ZoneCulture {
    /** Attribut représentant la culture dans la zone */
    private Culture culture;

    /** Constructeur de la classe ZoneCulture qui initialise la culture à null */
    public ZoneCulture() {
        this.culture = null; // La zone de culture est initialement vide
    }

    /** Getter pour l'attribut culture */
    public Culture getCulture() {
        return culture;
    }

    /** Méthode qui plante une culture dans la zone */
    public void planterCulture(Type type) {
        // Vérifie s'il n'y a pas déjà une culture dans la zone avant de planter une nouvelle culture
        if (culture != null) {
            throw new IllegalStateException("Il y a déjà une culture plantée dans cette zone.");
        } else {
            this.culture = new Culture(type); // Plante une nouvelle culture de type spécifié
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

    /** Méthode qui mange la culture de la zone */
    public void mangerCulture() {
        // Vérifie s'il y a une culture à manger dans la zone
        if (culture != null) {
            culture.manger();
            this.culture = null; // La culture est mangée et retirée de la zone
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
