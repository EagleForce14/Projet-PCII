package TestShop;


import model.culture.Type;
import model.management.Inventaire;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Seed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// classe de test pour l'inventaire
public class TestInventaire {

    private Inventaire inventaire;
    private Seed tulipe;
    private Facility cloture;

    @BeforeEach
    public void setUp() {
        inventaire = new Inventaire();
        tulipe = new Seed("Tulipe", 8, 100, Type.TULIPE);
        cloture = new Facility("Cloture", 50, 1, FacilityType.CLOTURE);
    }

    @Test
    public void testConstructeurEtVide() {
        assertTrue(inventaire.estVide(), "L'inventaire doit être vide après construction");
    }

    @Test
    public void testAjoutGraineEtPossession() {
        inventaire.ajoutGraine(tulipe, 5);
        assertFalse(inventaire.estVide(), "L'inventaire ne doit plus être vide après ajout");
        assertNotNull(inventaire.getGraines().get(Type.TULIPE));
        assertEquals(5, inventaire.getGraines().get(Type.TULIPE).intValue());
        assertTrue(inventaire.possedeGraine(Type.TULIPE));
    }

    @Test
    public void testAjoutGraineIncremente() {
        inventaire.ajoutGraine(tulipe, 3);
        inventaire.ajoutGraine(tulipe, 2);
        assertEquals(5, inventaire.getGraines().get(Type.TULIPE).intValue());
    }

    @Test
    public void testUseGraineDecrementeEtNeDevientPasNegatif() {
        inventaire.ajoutGraine(tulipe, 2);
        inventaire.UseGraineFleure(Type.TULIPE);
        assertEquals(1, inventaire.getGraines().get(Type.TULIPE).intValue());
        inventaire.UseGraineFleure(Type.TULIPE);
        assertEquals(0, inventaire.getGraines().get(Type.TULIPE).intValue());
        // utilisation supplémentaire ne doit pas passer en négatif
        inventaire.UseGraineFleure(Type.TULIPE);
        assertEquals(0, inventaire.getGraines().get(Type.TULIPE).intValue());
        assertFalse(inventaire.possedeGraine(Type.TULIPE));
    }

    @Test
    public void testAjoutEtUseInstallation() {
        inventaire.ajoutInstallation(cloture, 3);
        assertNotNull(inventaire.getInstallations().get(FacilityType.CLOTURE));
        assertEquals(3, inventaire.getInstallations().get(FacilityType.CLOTURE).intValue());

        inventaire.UseInstallation(FacilityType.CLOTURE);
        assertEquals(2, inventaire.getInstallations().get(FacilityType.CLOTURE).intValue());

        // utilisation jusqu'à zéro puis utilisation supplémentaire
        inventaire.UseInstallation(FacilityType.CLOTURE);
        inventaire.UseInstallation(FacilityType.CLOTURE);
        assertEquals(0, inventaire.getInstallations().get(FacilityType.CLOTURE).intValue());
        inventaire.UseInstallation(FacilityType.CLOTURE);
        assertEquals(0, inventaire.getInstallations().get(FacilityType.CLOTURE).intValue());
    }
}
