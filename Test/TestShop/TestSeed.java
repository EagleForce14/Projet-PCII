/*package TestShop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import model.Seed;





public class TestSeed{

    private Seed tulipe;
    private Seed rose;

    @BeforeEach
    public void setUp() {
        tulipe = new Seed("Tulipe", 8, 100, 16, Seed.SeedType.FLOWER);
        rose = new Seed("Rose", 10, 100, 20, Seed.SeedType.FLOWER); 
    }

    // teste de la récupération du prix de vente de la culture une fois récoltée
    @Test
    public void testGetPrixDeVente() {
        assertEquals(16, tulipe.getPrixDeVente());
        assertEquals(20, rose.getPrixDeVente());
    }

    // teste de la récupération du type de la graine (fleur ou légume)
    @Test
    public void testGetType() {
        assertEquals(Seed.SeedType.FLOWER, tulipe.getType());
        assertEquals(Seed.SeedType.FLOWER, rose.getType());
    }

    // teste de la modification du prix de vente de la culture une fois récoltée
    @Test
    public void testSetPrixDeVente() {
        tulipe.setPrixDeVente(18);
        rose.setPrixDeVente(22);
        assertEquals(18, tulipe.getPrixDeVente());
        assertEquals(22, rose.getPrixDeVente());
    }

    //teste de la récupération du nom de la graine
    @Test
    public void testGetName() {
        assertEquals("Tulipe", tulipe.getName());
        assertEquals("Rose", rose.getName());
    }

    //teste de la récupération du prix de la graine
    @Test
    public void testGetPrice() {    
        assertEquals(8, tulipe.getPrice());
        assertEquals(10, rose.getPrice());
    }

    //teste de la récupération de la quantité de la graine
    @Test
    public void testGetQuantity() {
        assertEquals(100, tulipe.getQuantity());
        assertEquals(100, rose.getQuantity());
    }

    //teste de la modification du prix de la graine
    @Test
    public void testSetPrice() {
        tulipe.setPrice(9);
        rose.setPrice(11);
        assertEquals(9, tulipe.getPrice());
        assertEquals(11, rose.getPrice());
    }

    //teste de la modification de la quantité de la graine
    @Test
    public void testSetQuantity() {
        tulipe.setQuantity(90);
        rose.setQuantity(90);
        assertEquals(90, tulipe.getQuantity());
        assertEquals(90, rose.getQuantity());
    }

    
    


}
*/
