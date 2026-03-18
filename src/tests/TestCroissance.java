package tests;
import model.Culture;

/** Classe de test pour la fonctionnalité de croissance */
public class TestCroissance {

    public static void main(String[] args) {
        Culture culture = new Culture(model.Type.FLEURS); // Créer une culture de type FLEURS

        // Affiche dans la console le stade de croissance de la culture toutes les 3 secondes
        while (true) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde
                System.out.println("Stade de croissance : " + culture.getStadeCroissance());

                // Arrêter le test lorsque la culture est flétrie
                if (culture.getStadeCroissance() == model.Stade.FLETRIE) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Vérifie que le thread de croissance s'est arrêté lorsque la culture est flétrie
        if (!culture.isThreadCroissanceAlive()) {
            System.out.println("Le thread de croissance s'est arrêté correctement.");
        } else {
            System.out.println("Le thread de croissance est toujours actif, il y a un problème.");
        }
    }
}
