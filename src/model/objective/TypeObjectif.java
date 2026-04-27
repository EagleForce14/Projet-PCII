package model.objective;

/** Énumération représentant les différents types d'objectifs */
public enum TypeObjectif {
    PLANTER_CULTURES, // Objectif de planter un certain nombre de cultures
    PLANTER_TYPES_CULTURE, // Objectif de planter un certain nombre de types de cultures différents
    RECOLTER_CULTURES, // Objectif de récolter un certain nombre de cultures
    RECOLTER_TYPES_CULTURE, // Objectif de récolter un certain nombre de types de cultures différents
    LABOURER_CHAMPS, // Objectif de labourer un certain nombre de champs
    ARROSER_CULTURES, // Objectif d'arroser un certain nombre de cultures
    COUPER_ARBRES, // Objectif de couper un certain nombre d'arbres
    ACHETER_ITEMS_BOUTIQUE, // Objectif d'acheter un certain nombre d'items dans la boutique
    CULTURES_MANGEES, // Objectif de ne pas dépasser la limite de cultures mangées
    REPOUSSER_LAPINS; // Objectif de repousser les lapins

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
                return "Ne pas dépasser la limite de cultures mangées";
            case REPOUSSER_LAPINS:
                return "Repousser les lapins";
            default:
                throw new IllegalArgumentException("Type d'objectif non reconnu");
        }
    }
}
