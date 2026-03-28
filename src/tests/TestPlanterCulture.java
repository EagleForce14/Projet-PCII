package tests;

import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.management.Inventaire;
import model.objective.GestionnaireObjectifs;
import model.shop.Seed;

/** Classe de test pour la fonctionnalité de plantation des cultures */

public class TestPlanterCulture {
    public static void main(String[] args) {
        GestionnaireObjectifs gestionnaireObjectifs = new GestionnaireObjectifs(null); // On peut passer null car on ne teste pas la génération d'objectifs ici
        // Créer une grille de culture
        GrilleCulture grille = new GrilleCulture(gestionnaireObjectifs);
        Inventaire inventaire = new Inventaire();
        inventaire.ajoutGraine(new Seed("Tulipe", 10, 1, Type.TULIPE), 2);

        // Avec la nouvelle règle de gameplay, la case doit d'abord être labourée.
        grille.labourerCase(0, 0);
        grille.planterCulture(0, 0, Type.TULIPE, inventaire);

        // Vérifier que la culture a été plantée correctement
        if (grille.getCulture(0, 0) != null && grille.getCulture(0, 0).getType() == Type.TULIPE
            && grille.getCulture(0, 0).getStadeCroissance() == Stade.JEUNE_POUSSE) {
            System.out.println("Test réussi : La culture a été plantée correctement.");
        } else {
            System.out.println("Test échoué : La culture n'a pas été plantée correctement.");
        }

        // Essayer de planter une culture dans une zone déjà occupée pour vérifier que l'exception est levée
        try {
            grille.planterCulture(0, 0, Type.TULIPE, inventaire);
            System.out.println("Test échoué : Il est possible de planter une culture dans une zone déjà occupée.");
        } catch (IllegalStateException e) {
            System.out.println("Test réussi : " + e.getMessage());
        }
    }
}
