package TestShop;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import model.culture.Type;
import model.shop.Seed;

public class TestSeed{

    private Seed tulipe;
    private Seed rose;
    private Seed carotte;

    @BeforeEach
    public void setUp() {
        tulipe = new Seed("Tulipe", 8, 100, Type.TULIPE);
        rose = new Seed("Rose", 10, 100, Type.ROSE);
        carotte = new Seed("Carotte", 5, 50, Type.CAROTTE);
    }

    @Test
    public void testGetters() {
        assertEquals("Tulipe", tulipe.getName());
        assertEquals(8, tulipe.getPrice());
        assertEquals(100, tulipe.getQuantity());

        assertEquals("Rose", rose.getName());
        assertEquals(10, rose.getPrice());
        assertEquals(100, rose.getQuantity());

        assertEquals("Carotte", carotte.getName());
        assertEquals(5, carotte.getPrice());
        assertEquals(50, carotte.getQuantity());
    }

    @Test
    public void testGetTypeAndCategory() {
        assertEquals(Type.TULIPE, tulipe.getType());
        assertTrue(tulipe.isFleur());
        assertFalse(tulipe.isLegume());

        assertEquals(Type.ROSE, rose.getType());
        assertTrue(rose.isFleur());
        assertFalse(rose.isLegume());

        assertEquals(Type.CAROTTE, carotte.getType());
        assertTrue(carotte.isLegume());
        assertFalse(carotte.isFleur());
    }

    @Test
    public void testSetters() {
        tulipe.setPrice(9);
        tulipe.setQuantity(90);
        assertEquals(9, tulipe.getPrice());
        assertEquals(90, tulipe.getQuantity());

        carotte.setPrice(6);
        carotte.setQuantity(60);
        assertEquals(6, carotte.getPrice());
        assertEquals(60, carotte.getQuantity());
    }
}
