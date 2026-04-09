package model.culture;

/**
 * Enumération unique des cultures du jeu.
 * Chaque valeur représente une culture concrète et embarque sa catégorie
 * afin d'indiquer simplement si c'est une fleur ou un légume.
 */
public enum Type {
    TULIPE(Categorie.FLEUR),
    ROSE(Categorie.FLEUR),
    MARGUERITE(Categorie.FLEUR),
    ORCHIDEE(Categorie.FLEUR),
    CAROTTE(Categorie.LEGUME),
    TOMATE(Categorie.LEGUME),
    CHOUFLEUR(Categorie.LEGUME),
    COURGETTE(Categorie.LEGUME);

    /** Petite catégorie embarquée pour éviter les anciens sous-types. */
    public enum Categorie {
        FLEUR,
        LEGUME
    }

    private final Categorie categorie;

    Type(Categorie categorie) {
        this.categorie = categorie;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public boolean isFleur() {
        return categorie == Categorie.FLEUR;
    }

    public boolean isLegume() {
        return categorie == Categorie.LEGUME;
    }
}
