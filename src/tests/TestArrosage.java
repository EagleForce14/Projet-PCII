package tests;

import model.Culture;
import model.Stade;
import model.Type;

/**
 * Classe de test pour l'arrosage des cultures
 */
public class TestArrosage {
    public static void main(String[] args) {
        // Créer une culture de type TULIPE
        Culture culture = new Culture(Type.TULIPE);

        // Essayer d'arroser la culture avant qu'elle ne soit à un stade intermédiaire pour vérifier que l'exception est levée
        try {
            culture.arroser();
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue lors de l'arrosage d'une culture jeune pousse : " + e.getMessage());
        }

        // Faire grandir la culture jusqu'au stade intermédiaire
        while (culture.getStadeCroissance() != Stade.INTERMEDIAIRE) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde avant de vérifier à nouveau
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Arroser la culture
        System.out.println("Arrosage de la culture...");
        culture.arroser();

        // Essayer d'arroser à nouveau pour vérifier que l'exception est levée
        try {
            culture.arroser();
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue lors du deuxième arrosage : " + e.getMessage());
        }

        // Vérifier que le délai de croissance a été réduit
        long startTime = System.currentTimeMillis();
        while (culture.getStadeCroissance() != Stade.MATURE) {
            try {
                Thread.sleep(500); // Attendre 0.5 seconde avant de vérifier à nouveau
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("Temps écoulé pour atteindre le stade mature après arrosage : " + elapsedTime + " ms");

        // Essayer d'arroser une culture à maturité pour vérifier que l'exception est levée
        try {
            culture.arroser();
        } catch (IllegalStateException e) {
            System.out.println("Exception attendue lors de l'arrosage d'une culture à maturité : " + e.getMessage());
        }
    }
}
