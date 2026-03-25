package model.culture;

/** Enumération représentant les différents stades de croissance d'une culture */
public enum Stade {
    JEUNE_POUSSE, // Stade initial de la culture
    INTERMEDIAIRE, // Stade intermédiaire de la culture
    MATURE, // Stade de récolte de la culture
    FLETRIE; // Stade où la culture est détruite


    /** Redéfinition de la méthode toString pour afficher le nom du stade */
    @Override
    public String toString() {
        return this.name();
    }
}