package TestShop;

import model.Facility;
import model.FacilityType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TestFacility {

    @Test
    public void constructeurEtGetters() {
        Facility f = new Facility("Cloture", 50, 20, FacilityType.CLOTURE);
        assertEquals("Cloture", f.getName());
        assertEquals(50, f.getPrice());
        assertEquals(20, f.getQuantity());
        assertEquals(FacilityType.CLOTURE, f.getType());
    }

    @Test
    public void settersMettreAJour() {
        Facility f = new Facility("Engrais", 30, 50, FacilityType.ENGRAIS);
        f.setPrice(45);
        f.setQuantity(80);
        assertEquals(45, f.getPrice());
        assertEquals(80, f.getQuantity());
    }

    @Test
    public void settersAcceptentValeursNegatives() {
        Facility f = new Facility("Test", 10, 5, FacilityType.JARDINIER);
        f.setPrice(-1);
        f.setQuantity(-3);
        assertEquals(-1, f.getPrice());
        assertEquals(-3, f.getQuantity());
    }

    @Test
    public void typeResteImmuable() {
        Facility f = new Facility("Cloture", 50, 20, FacilityType.CLOTURE);
        f.setPrice(60);
        f.setQuantity(25);
        assertEquals(FacilityType.CLOTURE, f.getType());
    }

    @Test
    public void equalsEstReferenceOnly() {
        Facility f1 = new Facility("Cloture", 50, 20, FacilityType.CLOTURE);
        Facility f2 = new Facility("Cloture", 50, 20, FacilityType.CLOTURE);
        Facility f3 = f1;
        assertNotEquals(f1, f2);   // objets distincts
        assertEquals(f1, f3);      // même instance
    }
}
