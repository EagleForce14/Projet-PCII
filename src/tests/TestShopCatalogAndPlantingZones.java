package tests;

import model.culture.GrilleCulture;
import model.culture.Type;
import model.management.Inventaire;
import model.runtime.Jour;
import model.shop.Seed;
import model.shop.Shop;
import model.shop.ShopKind;

/**
 * Vérifie deux points métier liés à l'échoppe :
 * - le nénuphar n'apparaît pas dans la boutique principale,
 * - chaque type de graine reste limité au bon côté de la rivière.
 */
public class TestShopCatalogAndPlantingZones {
    public static void main(String[] args) {
        testShopCatalogs();
        testPlantingZones();
    }

    private static void testShopCatalogs() {
        Shop mainShop = new Shop(ShopKind.MAIN);
        Shop stallShop = new Shop(ShopKind.STALL);

        boolean mainShopContainsWaterLily = mainShop.getSeeds().stream()
                .anyMatch(seed -> seed.getType() == Type.NENUPHAR);
        boolean stallOnlySellsWaterLily = stallShop.getSeeds().size() == 1
                && stallShop.getSeeds().get(0).getType() == Type.NENUPHAR
                && stallShop.getFacilities().isEmpty();

        if (!mainShopContainsWaterLily && stallOnlySellsWaterLily) {
            System.out.println("Test réussi : les catalogues des deux shops sont bien séparés.");
            return;
        }

        System.out.println("Test échoué : le catalogue du shop principal et de l'échoppe n'est pas correct.");
    }

    private static void testPlantingZones() {
        Jour jour = new Jour();
        GrilleCulture grille = new GrilleCulture(jour.getGestionnaireObjectifs());
        Inventaire inventaire = new Inventaire();

        for (int row = 0; row < grille.getHauteur(); row++) {
            grille.placeDecorativeRiver(4, row);
        }

        grille.labourerCase(2, 0);
        grille.labourerCase(5, 0);

        inventaire.ajoutGraine(new Seed(Type.NENUPHAR.getDisplayName(), 8, 1, Type.NENUPHAR), 1);
        inventaire.ajoutGraine(new Seed(Type.TULIPE.getDisplayName(), 8, 1, Type.TULIPE), 1);

        boolean waterLilyLeftOk = grille.canPlantCulture(2, 0, Type.NENUPHAR, inventaire);
        boolean waterLilyRightBlocked = !grille.canPlantCulture(5, 0, Type.NENUPHAR, inventaire);
        boolean tulipLeftBlocked = !grille.canPlantCulture(2, 0, Type.TULIPE, inventaire);
        boolean tulipRightOk = grille.canPlantCulture(5, 0, Type.TULIPE, inventaire);

        if (waterLilyLeftOk && waterLilyRightBlocked && tulipLeftBlocked && tulipRightOk) {
            System.out.println("Test réussi : la règle de plantation gauche/droite est respectée.");
            return;
        }

        System.out.println("Test échoué : la règle de plantation gauche/droite est incohérente.");
    }
}
