/*package TestShop;
import model.Facility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class TestFacility {

    private Facility cloture;
    private Facility engrais;
    private Facility jardinier;

    @BeforeEach
    public void setUp() {
        cloture = new Facility("Cloture", 50, 20, Facility.FacilityType.FENCE);
        engrais = new Facility("Engrais", 30, 50, Facility.FacilityType.FERTILIZER);
        jardinier = new Facility("Jardinier", 100, 10, Facility.FacilityType.GARDENER);
    }

    //teste de la récupération du nom de l'installation
    @Test   
    public void testGetName() {

        assertEquals("Cloture", cloture.getName());
        assertEquals("Engrais", engrais.getName());
        assertEquals("Jardinier", jardinier.getName());
    }

    //teste de la récupération du type de l'installation
    @Test
    public void testGetType() {
        assertEquals(Facility.FacilityType.FENCE, cloture.getType());
        assertEquals(Facility.FacilityType.FERTILIZER, engrais.getType());
        assertEquals(Facility.FacilityType.GARDENER, jardinier.getType());
    }

    //teste de la récupération du prix de l'installation
    @Test
    public void testGetPrice() {
        assertEquals(50, cloture.getPrice());
        assertEquals(30, engrais.getPrice());
        assertEquals(100, jardinier.getPrice());
    }
    
    //teste de la récupération de la quantité de l'installation
    @Test
    public void testGetQuantity() {
        assertEquals(20, cloture.getQuantity());
        assertEquals(50, engrais.getQuantity());
        assertEquals(10, jardinier.getQuantity());
    }

    // teste de la modification du prix de l'installation
    @Test
    public void testSetPrice() {
        cloture.setPrice(60);
        engrais.setPrice(40);
        jardinier.setPrice(110);
        assertEquals(60, cloture.getPrice());
        assertEquals(40, engrais.getPrice());
        assertEquals(110, jardinier.getPrice());
    }

    //teste de la modification de la quantité de l'installation
    @Test
    public void testSetQuantity() {
        cloture.setQuantity(25);
        engrais.setQuantity(55);
        jardinier.setQuantity(15);
        assertEquals(25, cloture.getQuantity());
        assertEquals(55, engrais.getQuantity());
        assertEquals(15, jardinier.getQuantity());
    }




}
*/