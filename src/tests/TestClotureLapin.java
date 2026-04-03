package tests;

import model.culture.CellSide;
import model.culture.GrilleCulture;
import model.management.Inventaire;
import model.shop.FacilityType;

/**
 * Vérifie que les clôtures sont bien destructibles par les lapins au bout de 900ms.
 */
public class TestClotureLapin {
    public static void main(String[] args) {
        GrilleCulture grille = new GrilleCulture(null);
        Inventaire inventaire = new Inventaire();
        inventaire.ajoutInstallation(FacilityType.CLOTURE, 1);

        grille.labourerCase(1, 1);
        grille.placeFence(1, 1, CellSide.TOP, inventaire);

        if (grille.canLabourCell(1, 0)) {
            System.out.println("Test échoué : la case adjacente à la clôture ne devrait pas pouvoir être labourée.");
            return;
        }

        for (int attempt = 1; attempt < GrilleCulture.FENCE_HIT_POINTS; attempt++) {
            boolean destroyed = grille.damageFence(1, 1, CellSide.TOP);
            if (destroyed) {
                System.out.println("Test échoué : la clôture a cédé trop tôt au coup " + attempt + ".");
                return;
            }
        }

        if (!grille.hasFence(1, 1, CellSide.TOP)) {
            System.out.println("Test échoué : la clôture a disparu avant le 4e impact.");
            return;
        }

        if (grille.getFenceRemainingHitPoints(1, 1, CellSide.TOP) != 1) {
            System.out.println("Test échoué : le compteur de PV de la clôture est incohérent après 3 coups.");
            return;
        }

        boolean destroyed = grille.damageFence(1, 1, CellSide.TOP);
        if (!destroyed || grille.hasFence(1, 1, CellSide.TOP)) {
            System.out.println("Test échoué : la clôture aurait dû céder au 4e impact.");
            return;
        }

        if (grille.getActiveFenceDestructionEffects().isEmpty()) {
            System.out.println("Test échoué : aucun effet de destruction n'a été enregistré.");
            return;
        }

        System.out.println("Test réussi : la clôture tient 3 coups et casse au 4e avec un effet visuel.");
    }
}
