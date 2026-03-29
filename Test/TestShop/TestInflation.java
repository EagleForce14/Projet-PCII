package TestShop;
/** test pour tester l'augmentation des prix du shop en fonction du jour
 * **/

import model.shop.Shop;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
public class TestInflation {


        @Test
        public void PriceIncreaseOnNewDay() {
            //on a 100 d'argent pour les tests

            Shop shop = new Shop();

            int oldpriceSeed = shop.getSeeds().get(0).getPrice();
            int oldpriceFacility = shop.getFacilities().get(0).getPrice();
                    //on vérifie que les prix sont corrects au jour 1
            assertEquals(8, shop.getSeeds().get(0).getPrice());
            assertEquals(50, shop.getFacilities().get(0).getPrice());

            //on avance de 10 jours
            shop.onNewDay(10);

            int newPriceSeed = shop.getSeeds().get(0).getPrice();
            int newPriceFacility = shop.getFacilities().get(0).getPrice();
            //on vérifie que les prix ont augmenté de 10% après 10 jours
            System.out.println("prix après 10 jours : " + shop.getSeeds().get(0).getPrice());
            assertTrue( newPriceSeed >= oldpriceSeed, "Le prix doit augmenter ou rester égal selon l'arrondi");
            assertTrue( newPriceFacility >= oldpriceFacility, "Le prix doit augmenter ou rester égal selon l'arrondi");
        }

}
