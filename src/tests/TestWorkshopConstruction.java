package tests;

import model.management.Inventaire;
import model.shop.FacilityType;
import model.workshop.WorkshopConstructionManager;

/**
 * Vérifie le cycle minimal de fabrication d'un pont :
 * le bois est consommé au lancement, puis le pont rejoint l'inventaire à la fin.
 */
public class TestWorkshopConstruction {
    public static void main(String[] args) throws InterruptedException {
        Inventaire inventaire = new Inventaire();
        inventaire.ajoutBois(10);

        WorkshopConstructionManager constructionManager = new WorkshopConstructionManager(inventaire, 10, 40L);
        if (constructionManager.startBridgeConstruction()) {
            System.out.println("Test échoué : la construction du pont aurait dû démarrer.");
            return;
        }

        if (inventaire.getQuantiteBois() != 0) {
            System.out.println("Test échoué : le bois n'a pas été réservé au lancement.");
            return;
        }

        Thread.sleep(80L);

        if (constructionManager.isConstructionInProgress()) {
            System.out.println("Test échoué : la construction aurait dû être terminée.");
            return;
        }

        if (inventaire.getQuantiteInstallation(FacilityType.PONT) != 1) {
            System.out.println("Test échoué : le pont n'a pas été ajouté à l'inventaire.");
            return;
        }

        System.out.println("Test réussi : le pont est construit après le délai et rejoint l'inventaire.");
    }
}
