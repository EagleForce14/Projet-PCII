/*package TestShop;
import model.Inventaire;
import model.Money;
import model.Shop;
import model.Inventaire;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class TestShop {

    Shop shop;
    Money money;
    Invenaire inventaire;

    @BeforeEach
    public void setUp() {
        shop = new Shop(inventaire);
        money = new Money(100);
    }

    //teste de la récupération de la liste des graines disponibles dans le magasin
    @Test
    public void testGetSeeds() {
        Assertions.assertNotNull(shop.getSeeds());
        // on a initialisé 3 graines dans le constructeur de la classe Shop
        Assertions.assertEquals(3, shop.getSeeds().size());
    }

    //teste de la récupération de la liste des installations disponibles dans le magasin
    @Test
    public void testGetFacilities() {
        Assertions.assertNotNull(shop.getFacilities());
        Assertions.assertEquals(3, shop.getFacilities().size());
    }

    //teste de la récupération de la liste des produits que le joueur souhaite acheter
    @Test
    public void testGetShoppingCard() {
        Assertions.assertNotNull(shop.getShoppingCard());
        Assertions.assertEquals(0, shop.getShoppingCard().size());
    }

    //teste de l'ajout d'un produit à la liste des produits que le joueur souhaite acheter
    @Test
    public void testAddToShoppingCard() {
        shop.addToShoppingCard(shop.getSeeds().get(0),1);
        shop.addToShoppingCard(shop.getFacilities().get(0),1);
        Assertions.assertEquals(2, shop.getShoppingCard().size());
        Assertions.assertEquals(1,shop.getSeeds().get(0).getQuantity());
        Assertions.assertEquals(1,shop.getFacilities().get(0).getQuantity());
    }

    //teste du retrait d'un produit de la liste des produits que le joueur souhaite acheter
    @Test
    public void testRemoveFromShoppingCard() {
        shop.addToShoppingCard(shop.getSeeds().get(0),1);
        shop.removeFromShoppingCard(shop.getSeeds().get(0));
        Assertions.assertEquals(0, shop.getShoppingCard().size());
    }

    //teste du vidage de la liste des produits que le joueur souhaite acheter
    @Test
    public void testClearShoppingCard() {
        shop.addToShoppingCard(shop.getSeeds().get(0),4);
        shop.clearShoppingCard();
        Assertions.assertEquals(0, shop.getShoppingCard().size());
    }   

     //teste de l'achat des produits du panier
     @Test
     public void testBuyProducts() {
        // on ajoute un produit au panier et on achète les produits du panier
        // ajout de graine de tulipe au panier
        shop.addToShoppingCard(shop.getSeeds().get(0),1);
        shop.buyProducts(money);

        // on vérifie que l'argent du joueur a été mis à jour et que le panier est vide
        Assertions.assertEquals(new Money(90),  money); 
        Assertions.assertEquals(0, shop.getShoppingCard().size());

        shop.addToShoppingCard(shop.getSeeds().get(0), 2);
        shop.addToShoppingCard(shop.getSeeds().get(1), 1);
        shop.addToShoppingCard(shop.getFacilities().get(0), 1);
        shop.buyProducts(money);
        Assertions.assertEquals(new Money(12),     money);
        Assertions.assertEquals(0, shop.getShoppingCard().size());

        // cas d'achat échoué : le joueur n'a pas assez d'argent pour acheter les produits du panier
        shop.addToShoppingCard(shop.getFacilities().get(2), 1);
        shop.buyProducts(money);
        Assertions.assertEquals(new Money(12), money);
        Assertions.assertEquals(1, shop.getShoppingCard().size());
     }

    


}
*/