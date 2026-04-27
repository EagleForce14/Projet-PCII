package view.shop;

import model.culture.Type;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import model.shop.Seed;

/**
 * Petit enum de filtres pour la boutique.
 * Chaque valeur ne connait qu'une seule chose:
 * dire si un produit doit apparaitre ou non.
 */
public enum ShopFilterCategory {
    ALL("Tout voir"),
    FLOWERS("Fleurs"),
    VEGETABLES("Legumes"),
    FACILITIES("Installations"),
    DECOR("Décor / Boosts");

    // Libellé lisible affiché dans la colonne des filtres.
    private final String label;

    /**
     * On associe à chaque filtre son texte d'affichage.
     */
    ShopFilterCategory(String label) {
        this.label = label;
    }

    /**
     * On renvoie le libellé affiché dans l'interface.
     */
    public String getLabel() {
        return label;
    }

    /**
     * On dit si le produit reçu doit apparaître dans cette catégorie de filtre.
     */
    public boolean matches(Product product) {
        if (this == ALL) {
            return true;
        }
        if (this == FACILITIES) {
            if (!(product instanceof Facility)) {
                return false;
            }
            FacilityType type = ((Facility) product).getType();
            return type != FacilityType.CHEMIN && type != FacilityType.COMPOST;
        }
        if (this == DECOR) {
            if (!(product instanceof Facility)) {
                return false;
            }
            FacilityType type = ((Facility) product).getType();
            return type == FacilityType.CHEMIN || type == FacilityType.COMPOST;
        }
        if (!(product instanceof Seed)) {
            return false;
        }

        Type type = ((Seed) product).getType();
        if (this == FLOWERS) {
            return type.isFleur();
        }
        return type.isLegume();
    }
}
