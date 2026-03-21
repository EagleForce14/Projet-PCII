package tests;

/** Classe de test pour la fonctionnalité qui permet au lapin de manger une culture */
public class TestMangerCulture {
    public static void main(String[] args) {
        // Créer une grille de culture
        model.GrilleCulture grille = new model.GrilleCulture();

        // Planter une culture dans la grille de culture
        grille.planterCulture(0, 0, model.Type.TULIPE);

        /* Test pour une culture qui peut être mangée */
        // Attendre que la culture atteigne le stade de maturité
        while (grille.getCulture(0, 0).getStadeCroissance() != model.Stade.MATURE) {
            try {
                Thread.sleep(1000); // Attendre 1 seconde
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            grille.mangerCulture(0, 0);
            System.out.println("Test réussi : La culture a été mangée correctement.");
        } catch (IllegalStateException e) {
            System.out.println("Test échoué : " + e.getMessage());
        }

        // Test pour une culture qui n'existe pas
        try {
            grille.mangerCulture(1, 1);
            System.out.println("Test échoué : Il n'y a pas de culture à manger.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }

        // Test pour une culture qui n'est pas à maturité
        grille.planterCulture(2, 2, model.Type.TULIPE);
        try {
            grille.mangerCulture(2, 2);
            System.out.println("Test échoué : La culture n'est pas à maturité.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }
    }
}
