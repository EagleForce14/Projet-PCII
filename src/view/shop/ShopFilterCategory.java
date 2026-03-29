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
    DECOR("Decor / Boosts");

    private final String label;

    // Le constructeur
    ShopFilterCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

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
