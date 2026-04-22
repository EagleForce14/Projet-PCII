package tests;

import model.culture.GrilleCulture;
import model.culture.Type;
import model.management.Inventaire;
import model.management.Money;
import model.runtime.Jour;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Seed;
import model.shop.Shop;
import model.shop.ShopKind;

/**
 * Vérifie deux points métier liés à l'échoppe :
 * - les plantes de la zone gauche n'apparaissent pas dans la boutique principale,
 * - chaque type de graine reste limité au bon côté de la rivière.
 */
public class TestShopCatalogAndPlantingZones {
    public static void main(String[] args) {
        testShopCatalogs();
        testShopStockPolicy();
        testPlantingZones();
    }

    private static void testShopCatalogs() {
        Shop mainShop = new Shop(ShopKind.MAIN);
        Shop stallShop = new Shop(ShopKind.STALL);

        boolean mainShopContainsLeftZoneSeed = mainShop.getSeeds().stream()
                .anyMatch(seed -> seed.getType() == Type.NENUPHAR || seed.getType() == Type.IRIS_DES_MARAIS);
        boolean stallHasNenuphar = stallShop.getSeeds().stream()
                .anyMatch(seed -> seed.getType() == Type.NENUPHAR);
        boolean stallHasMarshIris = stallShop.getSeeds().stream()
                .anyMatch(seed -> seed.getType() == Type.IRIS_DES_MARAIS);
        boolean stallOnlySellsLeftZoneSeeds = stallShop.getSeeds().size() == 2
                && stallHasNenuphar
                && stallHasMarshIris
                && stallShop.getFacilities().isEmpty();

        if (!mainShopContainsLeftZoneSeed && stallOnlySellsLeftZoneSeeds) {
            System.out.println("Test réussi : les catalogues des deux shops sont bien séparés.");
            return;
        }

        System.out.println("Test échoué : le catalogue du shop principal et de l'échoppe n'est pas correct.");
    }

    /**
     * Les graines et le chemin ne doivent plus être limités par un stock de boutique.
     * En revanche, clôture et compost gardent leur plafond actuel.
     */
    private static void testShopStockPolicy() {
        Shop mainShop = new Shop(ShopKind.MAIN);
        Shop stallShop = new Shop(ShopKind.STALL);
        Inventaire inventaire = new Inventaire();
        Money money = new Money(10000);

        Seed tulipe = mainShop.getSeeds().stream()
                .filter(seed -> seed.getType() == Type.TULIPE)
                .findFirst()
                .orElse(null);
        Seed nenuphar = stallShop.getSeeds().stream()
                .filter(seed -> seed.getType() == Type.NENUPHAR)
                .findFirst()
                .orElse(null);
        Facility chemin = findFacility(mainShop, FacilityType.CHEMIN);
        Facility cloture = findFacility(mainShop, FacilityType.CLOTURE);
        Facility compost = findFacility(mainShop, FacilityType.COMPOST);

        boolean mainSeedsUnlimited = tulipe != null && mainShop.addToShoppingCard(tulipe, 150);
        boolean stallSeedsUnlimited = nenuphar != null && stallShop.addToShoppingCard(nenuphar, 150);
        boolean pathsUnlimited = chemin != null && mainShop.addToShoppingCard(chemin, 250);
        boolean fencesStillLimited = cloture != null && !mainShop.addToShoppingCard(cloture, cloture.getQuantity() + 1);
        boolean compostStillLimited = compost != null && !mainShop.addToShoppingCard(compost, compost.getQuantity() + 1);

        boolean mainPurchaseSucceeded = mainShop.buyProducts(money, inventaire);
        boolean stallPurchaseSucceeded = stallShop.buyProducts(money, inventaire);
        boolean unlimitedProductsDidNotDecrementStock = tulipe != null
                && nenuphar != null
                && chemin != null
                && tulipe.getQuantity() == 100
                && nenuphar.getQuantity() == 100
                && chemin.getQuantity() == 200;

        if (mainSeedsUnlimited
                && stallSeedsUnlimited
                && pathsUnlimited
                && fencesStillLimited
                && compostStillLimited
                && mainPurchaseSucceeded
                && stallPurchaseSucceeded
                && unlimitedProductsDidNotDecrementStock) {
            System.out.println("Test réussi : seuls clôture et compost gardent un stock limité.");
            return;
        }

        System.out.println("Test échoué : la nouvelle politique de stock des shops est incohérente.");
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
        inventaire.ajoutGraine(new Seed(Type.IRIS_DES_MARAIS.getDisplayName(), 8, 1, Type.IRIS_DES_MARAIS), 1);
        inventaire.ajoutGraine(new Seed(Type.TULIPE.getDisplayName(), 8, 1, Type.TULIPE), 1);

        boolean waterLilyRuleOk = canOnlyPlantOnLeft(grille, inventaire, Type.NENUPHAR);
        boolean marshIrisRuleOk = canOnlyPlantOnLeft(grille, inventaire, Type.IRIS_DES_MARAIS);
        boolean tulipRuleOk = canOnlyPlantOnRight(grille, inventaire, Type.TULIPE);

        if (waterLilyRuleOk && marshIrisRuleOk && tulipRuleOk) {
            System.out.println("Test réussi : la règle de plantation gauche/droite est respectée.");
            return;
        }

        System.out.println("Test échoué : la règle de plantation gauche/droite est incohérente.");
    }

    /**
     * Les plantes vendues à l'échoppe doivent toutes suivre la même règle :
     * zone gauche autorisée, zone droite interdite.
     */
    private static boolean canOnlyPlantOnLeft(GrilleCulture grille, Inventaire inventaire, Type type) {
        return grille.canPlantCulture(2, 0, type, inventaire)
                && !grille.canPlantCulture(5, 0, type, inventaire);
    }

    private static boolean canOnlyPlantOnRight(GrilleCulture grille, Inventaire inventaire, Type type) {
        return !grille.canPlantCulture(2, 0, type, inventaire)
                && grille.canPlantCulture(5, 0, type, inventaire);
    }

    private static Facility findFacility(Shop shop, FacilityType type) {
        return shop.getFacilities().stream()
                .filter(facility -> facility.getType() == type)
                .findFirst()
                .orElse(null);
    }
}
