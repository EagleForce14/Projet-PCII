package tests;

import model.culture.Stade;
import model.culture.Type;
import model.culture.ZoneCulture;

/** Classe de test pour la fonctionnalité de récolte des cultures */
public class TestRecolterCulture {
    public static void main(String[] args) {

        /** Test si la culture est à maturité */
        // Créer une zone de culture et planter une culture de type TULIPE
        ZoneCulture zone1 = new ZoneCulture();
        zone1.planterCulture(Type.TULIPE);
        
        // Attendre que la culture atteigne le stade de récolte
        while (zone1.getCulture().getStadeCroissance() != Stade.MATURE) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            // Récolter la culture et afficher le prix de vente
            int prix = zone1.recolterCulture();
            System.out.println("Test réussi : Prix de vente de la culture récoltée : " + prix);
        } catch (IllegalStateException e) {
            System.out.println("Erreur : " + e.getMessage());
        }  
        
        try {
        // Essayer de récolter à nouveau pour vérifier que la culture a été retirée de la zone
            zone1.recolterCulture();
            System.out.println("Erreur : La culture a déjà été récoltée.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }

        /** Test si la culture n'est pas à maturité */
        ZoneCulture zone2 = new ZoneCulture();
        zone2.planterCulture(Type.TULIPE);

        try {
            zone2.recolterCulture();
            System.out.println("Erreur : La culture n'est pas à maturité.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }

        /** Test s'il n'y a pas de culture à récolter */
        ZoneCulture zone3 = new ZoneCulture();
        try {
            zone3.recolterCulture();
            System.out.println("Erreur : Il n'y a pas de culture à récolter.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }
    }
}
