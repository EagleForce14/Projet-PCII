package tests;

import model.culture.GrilleCulture;
import model.environment.PredefinedFieldLayout;
import model.environment.TreeManager;
import view.FieldPanel;

/**
 * Vérifie que la zone protégée à gauche de la rivière
 * couvre aussi les cases où un arbre mature bloquerait les bandes labourées par défaut.
 */
public class TestPredefinedFieldLayout {
    public static void main(String[] args) {
        GrilleCulture grilleCulture = new GrilleCulture(null);
        TreeManager treeManager = new TreeManager(grilleCulture);
        FieldPanel fieldPanel = new FieldPanel(grilleCulture, treeManager);
        fieldPanel.setSize(1180, 850);

        PredefinedFieldLayout.apply(fieldPanel);

        if (!PredefinedFieldLayout.blocksTreeSpawnInLeftRiverSection(fieldPanel, 3, 13)) {
            System.out.println("Test echoue : un arbre pourrait encore bloquer la derniere bande labourée par défaut.");
            return;
        }

        if (PredefinedFieldLayout.blocksTreeSpawnInLeftRiverSection(fieldPanel, 3, 14)) {
            System.out.println("Test echoue : la zone protegee depasse inutilement sous les bandes labourées.");
            return;
        }

        System.out.println("Test reussi : la protection des bandes labourées couvre bien le blocage des arbres matures.");
    }
}
