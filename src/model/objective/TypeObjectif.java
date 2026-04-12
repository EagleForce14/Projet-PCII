package model.objective;

/** Énumération représentant les différents types d'objectifs */
public enum TypeObjectif {
    PLANTER_CULTURES,
    PLANTER_TYPES_CULTURE,
    RECOLTER_CULTURES,
    RECOLTER_TYPES_CULTURE,
    LABOURER_CHAMPS,
    ARROSER_CULTURES,
    COUPER_ARBRES,
    ACHETER_ITEMS_BOUTIQUE,
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
            case LABOURER_CHAMPS:
                return "Labourer des champs";
            case ARROSER_CULTURES:
                return "Arroser des cultures";
            case COUPER_ARBRES:
                return "Couper des arbres";
            case ACHETER_ITEMS_BOUTIQUE:
                return "Acheter des items dans la boutique";
            case CULTURES_MANGEES:
                return "Cultures mangées";
            case REPOUSSER_LAPINS:
                return "Repousser les lapins";
            default:
                throw new IllegalArgumentException("Type d'objectif non reconnu");
        }
    }
}


