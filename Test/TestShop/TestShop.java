import model.culture.Type;
import model.management.Inventaire;
import model.management.Money;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Seed;
import model.shop.Shop;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestShop {

    private Shop shop;
    private Money money;
    private Inventaire inventaire;

    @BeforeEach
    void setUp() {
        shop = new Shop();
        money = new Money(200);
        inventaire = new Inventaire();
    }

    @Test
    void testGetSeedsInitialisesDeuxGraines() {
        assertNotNull(shop.getSeeds());
        assertEquals(2, shop.getSeeds().size());
        assertEquals("Tulipe", shop.getSeeds().get(0).getName());
        assertEquals(100, shop.getSeeds().get(0).getQuantity());
        assertEquals(Type.TULIPE, shop.getSeeds().get(0).getType());
    }

    @Test
    void testGetFacilitiesInitialisesTroisInstallations() {
        assertNotNull(shop.getFacilities());
        assertEquals(3, shop.getFacilities().size());
        assertEquals(FacilityType.CLOTURE, shop.getFacilities().get(0).getType());
        assertEquals(20, shop.getFacilities().get(0).getQuantity());
    }

    @Test
    void testShoppingCardCommenceVide() {
        assertNotNull(shop.getShoppingCard());
        assertEquals(0, shop.getShoppingCard().size());
        assertEquals(0, shop.getShoppingCardTotalPrice());
    }

    @Test
    void testAddToShoppingCardAjouteSansChangerLeStock() {
        Seed tulipe = shop.getSeeds().get(0);
        Facility cloture = shop.getFacilities().get(0);

        assertTrue(shop.addToShoppingCard(tulipe, 2));
        assertTrue(shop.addToShoppingCard(cloture, 1));

        assertEquals(2, shop.getShoppingCard().size());
        assertEquals(100, tulipe.getQuantity()); // le stock n'est pas décrémenté avant achat
        assertEquals(20, cloture.getQuantity());
        assertEquals(66, shop.getShoppingCardTotalPrice()); // 2*8 + 1*50
    }

    @Test
    void testAddToShoppingCardRefuseQuantiteSuperieureAuStock() {
        Facility jardinier = shop.getFacilities().get(2); // stock 10
        assertFalse(shop.addToShoppingCard(jardinier, 15));
        assertEquals(0, shop.getShoppingCard().size());
    }

    @Test
    void testRemoveFromShoppingCardRetirePartiellementPuisCompletement() {
        Seed tulipe = shop.getSeeds().get(0);
        shop.addToShoppingCard(tulipe, 3);

        shop.removeFromShoppingCard(tulipe, 1);
        assertEquals(1, shop.getShoppingCard().size());
        assertEquals(2, shop.getShoppingCard().get(0).getQuantity());

        shop.removeFromShoppingCard(tulipe, 2);
        assertEquals(0, shop.getShoppingCard().size());
    }

    @Test
    void testClearShoppingCardVideLePanier() {
        shop.addToShoppingCard(shop.getSeeds().get(0), 2);
        shop.addToShoppingCard(shop.getFacilities().get(0), 1);
        shop.clearShoppingCard();
        assertEquals(0, shop.getShoppingCard().size());
        assertEquals(0, shop.getShoppingCardTotalPrice());
    }

    @Test
    void testBuyProductsReussiMetAJourArgentStockEtInventaire() {
        Seed tulipe = shop.getSeeds().get(0); // prix 8, stock 100
        Facility cloture = shop.getFacilities().get(0); // prix 50, stock 20

        shop.addToShoppingCard(tulipe, 2); // 16
        shop.addToShoppingCard(cloture, 1); // +50 => 66

        boolean resultat = shop.buyProducts(money, inventaire);

        assertTrue(resultat);
        assertEquals(new Money(134), money); // 200 - 66
        assertEquals(0, shop.getShoppingCard().size());
        assertEquals(98, tulipe.getQuantity());
        assertEquals(19, cloture.getQuantity());
        assertEquals(2, inventaire.getGraines().get(Type.TULIPE));
        assertEquals(1, inventaire.getInstallations().get(FacilityType.CLOTURE));
    }

    @Test
    void testBuyProductsEchoueSiArgentInsuffisant() {
        money = new Money(20);
        Facility jardinier = shop.getFacilities().get(2); // prix 100, stock 10

        shop.addToShoppingCard(jardinier, 1);
        boolean resultat = shop.buyProducts(money, inventaire);

        assertFalse(resultat);
        assertEquals(new Money(20), money); // pas de débit
        assertEquals(1, shop.getShoppingCard().size()); // panier intact
        assertEquals(10, jardinier.getQuantity()); // stock intact
        assertTrue(inventaire.getInstallations().isEmpty());
    }
}
