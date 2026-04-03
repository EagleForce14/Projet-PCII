package view;

import model.culture.Culture;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.movement.MovementModel;
import model.management.Inventaire;
import model.shop.FacilityType;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Point;

/**
 * Sidebar d'actions placée à droite de l'interface.
 */
public class SidebarPanel extends JPanel {
    public static final int SIDEBAR_WIDTH = 320;
    private static final int ACTIONS_CONTENT_HEIGHT = 360;

    // Le chemin pour accéder à la police personnalisée
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // On référence au modèle car la vue lit uniquement un état booléen d'activation.
    private final MovementModel movementModel;
    private final GrilleCulture grilleCulture;
    private final FieldPanel fieldPanel;
    private final Inventaire inventaire;


    // Texture de fond en bois (chargée via la classe utilitaire du projet).
    private final Image woodBackground;

    private final JButton labourButton;
    // Les 4 boutons de contrôle des actions des unités déplaçables.
    private final JButton plantButton;
    private final JButton harvestButton;
    private final JButton waterButton;
    private final JButton cleanButton;
    private final JButton pathButton;
    private final JButton compostButton;
    private final JLabel labourWarningLabel;
    private final JPanel pathActionRow;
    private final JPanel compostActionRow;


    // Petit cache local pour éviter d'appliquer setEnabled en boucle inutilement.
    private boolean currentLabourEnabledState;
    private boolean currentPlantEnabledState;
    private boolean currentHarvestEnabledState;
    private boolean currentCleanEnabledState;
    private boolean currentWaterEnabledState;
    private boolean currentPathEnabledState;
    private boolean currentPathVisibleState;
    private boolean currentCompostEnabledState;
    private boolean currentCompostVisibleState;
    private String currentCompostButtonLabel;
    private boolean currentLabourWarningVisibleState;

    // Constructeur de la classe
    public SidebarPanel(MovementModel movementModel, GrilleCulture grilleCulture, FieldPanel fieldPanel,
                        Inventaire inventaire) {
        this.movementModel = movementModel;
        this.grilleCulture = grilleCulture;
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.woodBackground = ImageLoader.load("/assets/bois.png");

        // Le panneau est transparent hors de sa zone peinte personnalisée.
        setOpaque(false);

        // La sidebar garde une largeur fixe pour ne jamais empiéter sur le jeu.
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, ACTIONS_CONTENT_HEIGHT));
        setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));

        setLayout(new BorderLayout());

        // Le contenu utile garde la même hauteur que l'ancien overlay afin de
        // préserver la taille visuelle des boutons, sans étirer la grille.
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, ACTIONS_CONTENT_HEIGHT));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 0));

        // On s'occupe du titre principal (avec la police personnalisée).
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 8));

        JLabel titleLabel = new JLabel("Actions");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.LEFT);
        titleLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 18.0f));
        titleRow.add(titleLabel, BorderLayout.WEST);

        // 3 lignes de 2 boutons :
        // cela laisse une place claire au bouton "Labourer"
        // sans rendre la colonne plus compliquée à comprendre.
        JPanel buttonsGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        buttonsGrid.setOpaque(false);
        buttonsGrid.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));

        // On crée les boutons en appliquant le style visuel.
        labourButton = createStyledButton("Labourer", new Color(124, 83, 48, 255), 12.5f);
        plantButton = createStyledButton("Planter", new Color(139, 69, 19, 255), 12.5f);
        harvestButton = createStyledButton("Recolter", new Color(160, 82, 45, 255), 12.5f);
        waterButton = createStyledButton("Arroser", new Color(205, 133, 63, 255), 12.5f);
        cleanButton = createStyledButton("Nettoyer", new Color(101, 67, 33, 255), 12.5f);
        // On ajoute les boutons
        buttonsGrid.add(labourButton);
        buttonsGrid.add(plantButton);
        buttonsGrid.add(harvestButton);
        buttonsGrid.add(waterButton);
        buttonsGrid.add(cleanButton);
        buttonsGrid.add(createGridSpacer());

        /*
         * Le bouton de pose du chemin est volontairement separé :
         * il ne doit apparaitre que quand l'objet chemin est l'outil actif.
         * Ainsi, la barre principale reste sobre la plupart du temps.
         */
        pathButton = createStyledButton("Poser chemin", new Color(91, 97, 112, 255), 12.0f);
        pathActionRow = createSpecialActionRow(pathButton);
        pathActionRow.setVisible(false);

        /*
         * Le compost est un boost important et rare.
         * On lui donne donc un bouton plus "spécial" visuellement
         * pour qu'on sente immédiatement qu'il ne joue pas le même rôle qu'un simple chemin.
         */
        compostButton = createBoostActionButton("Poser compost");
        compostActionRow = createSpecialActionRow(compostButton);
        compostActionRow.setVisible(false);

        labourWarningLabel = new JLabel(
                "<html>Le labourage n'est pas autorisé sur une case adjacente à une clôture.</html>"
        );
        labourWarningLabel.setOpaque(false);
        labourWarningLabel.setForeground(new Color(255, 116, 96));
        labourWarningLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 10.0f));
        labourWarningLabel.setVisible(false);
        labourWarningLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));

        JPanel specialActionsPanel = new JPanel();
        specialActionsPanel.setOpaque(false);
        specialActionsPanel.setLayout(new BoxLayout(specialActionsPanel, BoxLayout.Y_AXIS));
        specialActionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 16, 16));
        specialActionsPanel.add(pathActionRow);
        specialActionsPanel.add(compostActionRow);
        specialActionsPanel.add(labourWarningLabel);

        contentPanel.add(titleRow, BorderLayout.NORTH);
        contentPanel.add(buttonsGrid, BorderLayout.CENTER);
        contentPanel.add(specialActionsPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.NORTH);

        // Au démarrage, les boutons sont désactivés tant que l'unité déplaçable n'est pas
        // sur une case valide du champ.
        applyButtonsEnabledState(false, false, false, false, false, false, false, false, false, "Poser compost", false);
    }

    /**
     * Case vide utilisée pour garder la grille de boutons bien alignée
     */
    private JPanel createGridSpacer() {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        return spacer;
    }

    /**
     * Construit un bouton avec son interface
     */
    private JButton createStyledButton(String text, Color color, float fontSize) {
        JButton button = new JButton(text);

        // Style de fond inspiré du panneau de référence.
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(CustomFontLoader.loadFont(FONT_PATH, fontSize));
        button.setFocusPainted(false);
        button.setOpaque(true);

        // Le jeu est commandé au clavier, on évite que les boutons capturent le focus.
        button.setFocusable(false);

        // Bordure composée: contour + padding interne pour garder un style "jeu".
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 30, 10), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        button.setBorderPainted(true);

        return button;
    }

    /**
     * Variante visuelle utilisée pour les objets "boost".
     * Le fond est plus vert et la bordure plus claire,
     * pour qu'on lise tout de suite un objet spécial différent des actions classiques.
     */
    private JButton createBoostActionButton(String text) {
        JButton button = createStyledButton(text, new Color(66, 111, 57, 255), 12.0f);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 214, 123), 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return button;
    }

    /**
     * Petit wrapper partagé pour les actions contextuelles.
     * Elles n'apparaissent pas en permanence :
     * seulement quand un état de jeu particulier le justifie.
     */
    private JPanel createSpecialActionRow(JButton button) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(button, BorderLayout.CENTER);
        return row;
    }

    /**
     * Lit l'état dans le modèle et applique visuellement l'activation si nécessaire.
     */
    private void syncFromModel() {
        boolean shouldEnableLabour = canLabourActiveCell();
        boolean shouldEnablePlant = canPlantActiveCell();
        boolean shouldEnableHarvest = canHarvestActiveCell();
        boolean shouldEnableClean = canCleanActiveCell();
        boolean shouldEnableWater = canWaterActiveCell();
        boolean shouldShowPathAction = movementModel.getSelectedFacilityType() == FacilityType.CHEMIN;
        boolean shouldEnablePath = shouldShowPathAction && canPlacePathActiveCell();
        boolean shouldDisplayCompostAction = shouldShowCompostAction();
        boolean shouldEnableCompost = canUseCompostButtonOnActiveCell();
        String compostButtonLabel = shouldShowRemiserCompostAction() ? "Remiser compost" : "Poser compost";
        boolean shouldShowLabourWarning = shouldShowAdjacentFenceLabourWarning();
        if (shouldEnableLabour != currentLabourEnabledState
                || shouldEnablePlant != currentPlantEnabledState
                || shouldEnableHarvest != currentHarvestEnabledState
                || shouldEnableClean != currentCleanEnabledState
                || shouldEnableWater != currentWaterEnabledState
                || shouldEnablePath != currentPathEnabledState
                || shouldShowPathAction != currentPathVisibleState
                || shouldEnableCompost != currentCompostEnabledState
                || shouldDisplayCompostAction != currentCompostVisibleState
                || !compostButtonLabel.equals(currentCompostButtonLabel)
                || shouldShowLabourWarning != currentLabourWarningVisibleState) {
            applyButtonsEnabledState(
                    shouldEnableLabour,
                    shouldEnablePlant,
                    shouldEnableHarvest,
                    shouldEnableClean,
                    shouldEnableWater,
                    shouldEnablePath,
                    shouldShowPathAction,
                    shouldEnableCompost,
                    shouldDisplayCompostAction,
                    compostButtonLabel,
                    shouldShowLabourWarning
            );
        }
    }

    /**
     * Active/désactive les boutons.
     */
    private void applyButtonsEnabledState(boolean labourEnabled, boolean plantEnabled,
                                          boolean harvestEnabled, boolean cleanEnabled, boolean waterEnabled,
                                          boolean pathEnabled, boolean pathVisible,
                                          boolean compostEnabled, boolean compostVisible,
                                          String compostButtonLabel,
                                          boolean labourWarningVisible) {
        currentLabourEnabledState = labourEnabled;
        currentPlantEnabledState = plantEnabled;
        currentHarvestEnabledState = harvestEnabled;
        currentCleanEnabledState = cleanEnabled;
        currentWaterEnabledState = waterEnabled;
        currentPathEnabledState = pathEnabled;
        currentPathVisibleState = pathVisible;
        currentCompostEnabledState = compostEnabled;
        currentCompostVisibleState = compostVisible;
        currentCompostButtonLabel = compostButtonLabel;
        currentLabourWarningVisibleState = labourWarningVisible;

        labourButton.setEnabled(labourEnabled);
        plantButton.setEnabled(plantEnabled);
        harvestButton.setEnabled(harvestEnabled);
        waterButton.setEnabled(waterEnabled);
        cleanButton.setEnabled(cleanEnabled);
        pathButton.setEnabled(pathEnabled);
        pathActionRow.setVisible(pathVisible);
        compostButton.setEnabled(compostEnabled);
        compostActionRow.setVisible(compostVisible);
        compostButton.setText(compostButtonLabel);
        labourWarningLabel.setVisible(labourWarningVisible);

        repaint();
    }

    /**
     * Le labourage n'est possible que sur une case de map exploitable
     * qui est encore en herbe.
     */
    private boolean canLabourActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return false;
        }

        return grilleCulture.canLabourCell(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Le message rouge n'apparaît que pour cette nouvelle règle précise :
     * une case encore "labourable" en apparence, mais collée à une clôture.
     */
    private boolean shouldShowAdjacentFenceLabourWarning() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null
                && fieldPanel.isFarmableCell(activeFieldCell)
                && !grilleCulture.isLabouree(activeFieldCell.x, activeFieldCell.y)
                && !grilleCulture.hasPath(activeFieldCell.x, activeFieldCell.y)
                && !grilleCulture.hasCompostAt(activeFieldCell.x, activeFieldCell.y)
                && !grilleCulture.hasRiver(activeFieldCell.x, activeFieldCell.y)
                && grilleCulture.isLabourBlockedByAdjacentFence(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * On ne peut planter que si une graine précise est sélectionnée,
     * qu'il en reste au moins une, que la case est bien labourée,
     * et qu'aucune culture n'y pousse déjà.
     */
    private boolean canPlantActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        Type selectedSeedType = movementModel.getSelectedSeedType();
        if (activeFieldCell == null
                || !fieldPanel.isFarmableCell(activeFieldCell)
                || !grilleCulture.isLabouree(activeFieldCell.x, activeFieldCell.y)
                || grilleCulture.hasPath(activeFieldCell.x, activeFieldCell.y)
                || selectedSeedType == null
                || !inventaire.possedeGraine(selectedSeedType)) {
            return false;
        }

        return grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null;
    }

    /**
     * Le chemin se pose uniquement sur de l'herbe:
     * pas sur la grange, pas sur une case deja labourée, pas sur une case deja occupee.
     *
     * On garde cette regle dans la sidebar aussi
     * pour que le bouton explique visuellement quand l'action est possible.
     */
    private boolean canPlacePathActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null
                && fieldPanel.isFarmableCell(activeFieldCell)
                && grilleCulture.canPlacePath(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Le compost se pose lui aussi sur une case d'herbe libre,
     * avec une limite simple à retenir :
     * pas plus de deux composts sur la map en même temps.
     */
    private boolean canPlaceCompostActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null
                && fieldPanel.isFarmableCell(activeFieldCell)
                && grilleCulture.canPlaceCompost(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Le bouton compost doit rester visible si le compost est sélectionné,
     * mais aussi quand le joueur est déjà debout dessus pour pouvoir le remiser.
     */
    private boolean shouldShowCompostAction() {
        return movementModel.getSelectedFacilityType() == FacilityType.COMPOST || isPlayerStandingOnCompost();
    }

    /**
     * Même composant visuel, mais deux libellés possibles.
     * Si la case active contient déjà le compost, on affiche clairement l'action inverse.
     */
    private boolean shouldShowRemiserCompostAction() {
        return isPlayerStandingOnCompost();
    }

    /**
     * Active le bouton dans le mode pertinent :
     * - toujours actif pour remiser un compost sur la case du joueur,
     * - sinon actif seulement si le compost sélectionné peut être posé ici.
     */
    private boolean canUseCompostButtonOnActiveCell() {
        if (shouldShowRemiserCompostAction()) {
            return true;
        }
        return movementModel.getSelectedFacilityType() == FacilityType.COMPOST && canPlaceCompostActiveCell();
    }

    /**
     * Helper de lecture très simple pour éviter de répéter partout
     * le même test "case active + compost".
     */
    private boolean isPlayerStandingOnCompost() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null
                && fieldPanel.isFarmableCell(activeFieldCell)
                && grilleCulture.hasCompostAt(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * La récolte n'est disponible que sur une case occupée par une culture mature.
     */
    private boolean canHarvestActiveCell() {
        Culture culture = getCultureOnActiveCell();
        return culture != null && culture.getStadeCroissance() == Stade.MATURE;
    }

    /**
     * L'arrosage n'est disponible que sur une case occupée par une culture intermédiaire et qui n'a pas encore été arrosée.
     */
    private boolean canWaterActiveCell() {
        Culture culture = getCultureOnActiveCell();
        return culture != null && culture.getStadeCroissance() == Stade.INTERMEDIAIRE && !culture.isArrosee();
    }

    /**
     * Le nettoyage n'est disponible que sur une case occupée par une culture flétrie.
     */
    private boolean canCleanActiveCell() {
        Culture culture = getCultureOnActiveCell();
        return culture != null && culture.getStadeCroissance() == Stade.FLETRIE;
    }

    /**
     * Centralise la lecture de la case active pour garder les règles au même endroit.
     */
    private Culture getCultureOnActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        if (activeFieldCell == null) {
            return null;
        }

        return grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y);
    }

    public JButton getPlantButton() {
        return plantButton;
    }

    public JButton getLabourButton() {
        return labourButton;
    }

    public JButton getHarvestButton() {
        return harvestButton;
    }

    public JButton getWaterButton() {
        return waterButton;
    }

    public JButton getCleanButton() {
        return cleanButton;
    }

    public JButton getPathButton() {
        return pathButton;
    }

    public JButton getCompostButton() {
        return compostButton;
    }



    /**
     * Dessine le fond de l'overlay avec l'image, puis un léger contour.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // On regarde si les boutons doivent être activés ou pas.
        syncFromModel();
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();

        // Le contenu utile du panneau exclut l'EmptyBorder.
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;

        if (w > 0 && h > 0) {
            if (woodBackground != null) {
                g2.drawImage(woodBackground, x, y, w, h, this);
            } else {
                // Fallback si l'image n'est pas disponible.
                g2.setColor(new Color(102, 71, 45));
                g2.fillRect(x, y, w, h);
            }

            // Voile léger pour améliorer la lisibilité des boutons.
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRect(x, y, w, h);

            // Bordure simple pour mieux délimiter le panneau.
            g2.setColor(new Color(60, 35, 20, 180));
            g2.drawRect(x, y, w - 1, h - 1);
        }

        g2.dispose();
    }
}
