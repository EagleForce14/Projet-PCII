package tests;

import model.objective.GestionnaireObjectifs;
import model.runtime.Jour;

/** Classe de test pour la génération des objectifs */
public class TestGenerationObjectifs {
    public static void main(String[] args) {
        // Test de la génération des objectifs au fil des jours
        Jour jour = new Jour();
        GestionnaireObjectifs gestionnaireObjectifs = new GestionnaireObjectifs(jour);
        jour.start(); // Démarre le thread du jour pour faire avancer le temps
        for (int i = 0; i < 10; i++) { // Test sur les 10 premiers jours
            try {
                Thread.sleep(6000); // Attendre un peu plus de 5 secondes pour s'assurer que le jour a avancé
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gestionnaireObjectifs.genererObjectifs(); // Génère les objectifs pour le jour actuel
            System.out.println("Jour " + jour.getJour() + " :");
            gestionnaireObjectifs.getProgressionObjectifs().forEach((type, progression) -> {
                System.out.println(progression);
            });
            System.out.println("Jour validé : " + gestionnaireObjectifs.estJourValide());
            System.out.println();
        }
        
    }
}
