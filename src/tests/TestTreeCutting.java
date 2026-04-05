package tests;

import model.culture.GrilleCulture;
import model.environment.TreeManager;
import model.management.Inventaire;

/**
 * Vérifie la mécanique simple de coupe :
 * 4 clics pour abattre l'arbre, puis 2 unités de bois gagnées.
 */
public class TestTreeCutting {
    public static void main(String[] args) {
        GrilleCulture grilleCulture = new GrilleCulture(null);
        TreeManager treeManager = new TreeManager(grilleCulture);
        Inventaire inventaire = new Inventaire();

        if (!treeManager.placeTree(4, 4, true, false)) {
            System.out.println("Test échoué : impossible d'installer l'arbre de test.");
            return;
        }

        for (int impactIndex = 1; impactIndex < treeManager.getRequiredCutImpactCount(); impactIndex++) {
            boolean treeFelled = treeManager.cutTree(4, 4);
            if (treeFelled) {
                System.out.println("Test échoué : l'arbre est tombé trop tôt au clic " + impactIndex + ".");
                return;
            }
        }

        if (!treeManager.hasTreeAt(4, 4)) {
            System.out.println("Test échoué : l'arbre a disparu avant le dernier clic.");
            return;
        }

        boolean treeFelled = treeManager.cutTree(4, 4);
        if (!treeFelled || treeManager.hasTreeAt(4, 4)) {
            System.out.println("Test échoué : l'arbre aurait dû tomber au 4e clic.");
            return;
        }

        inventaire.ajoutBois(treeManager.getWoodRewardQuantity());
        if (inventaire.getQuantiteBois() != treeManager.getWoodRewardQuantity()) {
            System.out.println("Test échoué : la récompense bois n'a pas été correctement créditée.");
            return;
        }

        if (treeManager.getActiveFellingEffects().isEmpty()) {
            System.out.println("Test échoué : aucun effet visuel de chute n'a été enregistré.");
            return;
        }

        if (treeManager.getActiveWoodRewardEffects().isEmpty()) {
            System.out.println("Test échoué : aucune animation de bois gagné n'a été enregistrée.");
            return;
        }

        System.out.println("Test réussi : l'arbre tombe au 4e clic et déclenche bien la récompense bois.");
    }
}
