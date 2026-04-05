package tests;

import model.management.Money;

/**
 * Vérifie qu'un gain d'argent alimente bien le modèle
 * et enregistre un effet graphique consultable par la vue.
 */
public class TestMoneyRewardEffects {
    public static void main(String[] args) {
        Money money = new Money(150);
        money.creditFromWorld(25, 32, -18);

        if (money.getAmount() != 175) {
            System.out.println("Test échoué : le gain d'argent n'a pas été appliqué.");
            return;
        }

        if (money.getActiveRewardEffects().isEmpty()) {
            System.out.println("Test échoué : aucun effet de gain d'argent n'a été enregistré.");
            return;
        }

        if (!money.getActiveRewardEffects().get(0).hasExplicitSource()) {
            System.out.println("Test échoué : la source du gain n'a pas été conservée.");
            return;
        }

        System.out.println("Test réussi : le gain d'argent crée bien un effet animable.");
    }
}
