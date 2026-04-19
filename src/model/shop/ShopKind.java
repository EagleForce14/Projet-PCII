package model.shop;

/**
 * Décrit les deux points de vente du jeu.
 *
 * La boutique centrale et l'échoppe réutilisent la même logique,
 * mais elles n'exposent pas le même catalogue.
 */
public enum ShopKind {
    MAIN("Boutique", true),
    STALL("Échoppe", false);

    private final String displayName;
    private final boolean sellsFacilities;

    ShopKind(String displayName, boolean sellsFacilities) {
        this.displayName = displayName;
        this.sellsFacilities = sellsFacilities;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean sellsFacilities() {
        return sellsFacilities;
    }

    /**
     * On affiche les filtres de catégories que pour l'échoppe.
     */
    public boolean usesCategoryFilters() {
        return this == MAIN;
    }
}
