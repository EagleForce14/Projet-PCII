package model.objective;

/** Énumération représentant les différents types d'objectifs */
public enum TypeObjectif {
    PLANTER_CULTURES,
    PLANTER_TYPES_CULTURE,
    RECOLTER_CULTURES,
    RECOLTER_TYPES_CULTURE,
    ARROSER_CULTURES,
    TAUX_RECOLTE_CULTURES,
    CULTURES_MANGEES,
    REPOUSSER_LAPINS;

    /** Méthode statique qui renvoie l'intitulé de l'objectif */
    public static String getIntitule(TypeObjectif type) {
        switch (type) {
            case PLANTER_CULTURES:
                return "Planter des cultures";
            case PLANTER_TYPES_CULTURE:
                return "Planter différents types de cultures";
            case RECOLTER_CULTURES:
                return "Récolter des cultures";
            case RECOLTER_TYPES_CULTURE:
                return "Récolter différents types de cultures";
            case ARROSER_CULTURES:
                return "Arroser des cultures";
            case TAUX_RECOLTE_CULTURES:
                return "Taux de récolte des cultures";
            case CULTURES_MANGEES:
                return "Cultures mangées";
            case REPOUSSER_LAPINS:
                return "Repousser les lapins";
            default:
                throw new IllegalArgumentException("Type d'objectif non reconnu");
        }
    }
}


