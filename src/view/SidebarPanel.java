package view;

import model.culture.Culture;
import model.culture.GrilleCulture;
import model.culture.Stade;
import model.culture.Type;
import model.environment.FieldObstacleMap;
import model.environment.TreeInstance;
import model.movement.MovementModel;
import model.movement.Unit;
import model.management.Inventaire;
import model.objective.ObjectifCompteur;
import model.objective.ObjectifJournalier;
import model.objective.TypeObjectif;
import model.runtime.Jour;
import model.shop.FacilityType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar d'actions placée à droite de l'interface.
 */
public class SidebarPanel extends JPanel {
    public static final int SIDEBAR_WIDTH = 320;
    private static final int ACTIONS_CONTENT_HEIGHT = 620;
    private static final int BUTTONS_GRID_HEIGHT = 250;
    private static final float LABOUR_BUTTON_DEFAULT_FONT_SIZE = 13.5f;
    private static final float LABOUR_BUTTON_COMPACT_FONT_SIZE = 11.5f;
    // Largeur de wrapping des intitulés d'objectifs
    private static final int OBJECTIVE_TITLE_WRAP_WIDTH = 270;

    // Le chemin pour accéder à la police personnalisée
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // On référence au modèle car la vue lit uniquement un état booléen d'activation.
    private final MovementModel movementModel;
    private final GrilleCulture grilleCulture;
    private final FieldPanel fieldPanel;
    private final Inventaire inventaire;
    private final Jour jour;

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
    private final JButton cutTreeButton;
    private final JButton bridgeButton;
    private final JButton caveButton;
    private final JLabel labourWarningLabel;
    private final JLabel bridgePlacementHintLabel;
    private final JPanel pathActionRow;
    private final JPanel compostActionRow;
    private final JPanel cutTreeActionRow;
    private final JPanel bridgeActionRow;

    // Blocs UI dédiés à la zone objectifs et au bilan du jour.
    private final JPanel objectivesContentPanel; // Conteneur dynamique de la liste des objectifs.
    private final JPanel dayValidationContentPanel; // Conteneur dynamique du bilan du jour (objectifs validés, non validés, etc).
    private final JPanel dayValidationCardPanel; // Panneau de fond du bilan du jour, avec une peinture personnalisée pour les couleurs dynamiques selon validation ou non du jour.
    private final JLabel objectivesInfoLabel; // Label d'information sur les objectifs, affiché uniquement dans la grotte pour expliquer le fonctionnement particulier des objectifs dans ce lieu.

    // Références directes vers les widgets objectifs pour mises à jour incrémentales.
    private final Map<TypeObjectif, JTextArea> objectiveTitleLabelsByType;
    private final Map<TypeObjectif, JLabel> objectiveProgressLabelsByType;

    // Labels du panneau "Bilan du jour" réutilisés sans recréation.
    private final JLabel dayValidationTitleLabel;
    private final JLabel dayValidationProgressLabel;
    private final JLabel dayValidationStatusLabel;


    // Petit cache local pour éviter d'appliquer setEnabled en boucle inutilement.
    private boolean currentLabourEnabledState;
    private boolean currentPlantEnabledState;
    private boolean currentHarvestEnabledState;
    private boolean currentCleanEnabledState;
    private boolean currentWaterEnabledState;
    private boolean currentPathEnabledState;
    private boolean currentPathVisibleState;
    private String currentPathButtonLabel;
    private boolean currentCompostEnabledState;
    private boolean currentCompostVisibleState;
    private boolean currentCutTreeEnabledState;
    private boolean currentCutTreeVisibleState;
    private boolean currentBridgeEnabledState;
    private boolean currentBridgeVisibleState;
    private boolean currentBridgeHintVisibleState;
    private String currentLabourButtonLabel;
    private String currentCompostButtonLabel;
    private boolean currentLabourWarningVisibleState;
    private boolean caveMode;

    // Cache de contenu objectifs pour éviter de redessiner inutilement et supprimer le clignotement.
    private String currentObjectivesSnapshot;
    private String currentObjectivesStructureSignature;
    private boolean currentDayValidatedState;

    // Constructeur de la classe
    public SidebarPanel(MovementModel movementModel, GrilleCulture grilleCulture, FieldPanel fieldPanel,
                        Inventaire inventaire, Jour jour) {
        this.movementModel = movementModel;
        this.grilleCulture = grilleCulture;
        this.fieldPanel = fieldPanel;
        this.inventaire = inventaire;
        this.jour = jour;
        this.woodBackground = ImageLoader.load("/assets/bois.png");
        // Ces maps servent d'index rapide vers les widgets par type d'objectif.
        this.objectiveTitleLabelsByType = new EnumMap<>(TypeObjectif.class);
        this.objectiveProgressLabelsByType = new EnumMap<>(TypeObjectif.class);

        // Le panneau est transparent hors de sa zone peinte personnalisée.
        setOpaque(false);

        // La sidebar garde une largeur fixe pour ne jamais empiéter sur le jeu.
        setPreferredSize(new Dimension(SIDEBAR_WIDTH, ACTIONS_CONTENT_HEIGHT));
        setMinimumSize(new Dimension(SIDEBAR_WIDTH, 0));

        setLayout(new BorderLayout());

        // Le contenu utile garde une organisation claire:
        // actions ancrées en haut, objectifs dans la zone restante.
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, ACTIONS_CONTENT_HEIGHT));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 0));

        // On s'occupe du titre principal (avec la police personnalisée).
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 8));
        titleRow.setAlignmentX(LEFT_ALIGNMENT);

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
        buttonsGrid.setAlignmentX(LEFT_ALIGNMENT);
        buttonsGrid.setPreferredSize(new Dimension(SIDEBAR_WIDTH, BUTTONS_GRID_HEIGHT));
        buttonsGrid.setMinimumSize(new Dimension(0, BUTTONS_GRID_HEIGHT));
        buttonsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, BUTTONS_GRID_HEIGHT));

        // On crée les boutons en appliquant le style visuel.
        labourButton = createStyledButton(
                "Labourer",
                new Color(124, 83, 48, 255),
                LABOUR_BUTTON_DEFAULT_FONT_SIZE
        );
        plantButton = createStyledButton("Planter", new Color(139, 69, 19, 255), 13.5f);
        harvestButton = createStyledButton("Recolter", new Color(160, 82, 45, 255), 13.5f);
        waterButton = createStyledButton("Arroser", new Color(205, 133, 63, 255), 13.5f);
        cleanButton = createStyledButton("Nettoyer", new Color(101, 67, 33, 255), 13.5f);
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
        pathButton = createStyledButton("Poser chemin", new Color(91, 97, 112, 255), 13.0f);
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

        /*
         * La coupe de bois est une action contextuelle pure :
         * on ne veut surtout pas l'afficher en permanence,
         * seulement quand le joueur est réellement au contact d'un arbre.
         */
        cutTreeButton = createStyledButton("Couper l'arbre", new Color(120, 84, 45, 255), 12.5f);
        cutTreeActionRow = createSpecialActionRow(cutTreeButton);
        cutTreeActionRow.setVisible(false);

        /*
         * Le pont fonctionne comme le chemin :
         * on l'équipe d'abord dans l'inventaire,
         * puis on l'active via un bouton dédié uniquement quand cet outil est sélectionné.
         */
        bridgeButton = createStyledButton("Poser pont", new Color(73, 105, 136, 255), 13.0f);
        bridgeActionRow = createSpecialActionRow(bridgeButton);
        bridgeActionRow.setVisible(false);

        // Bouton temporaire de navigation : il sert uniquement à afficher la grotte pendant le développement.
        caveButton = createStyledButton("Aller grotte", new Color(64, 70, 86, 255), 11.5f);

        labourWarningLabel = new JLabel(
                "<html>Le labourage n'est pas autorisé sur une case adjacente à une clôture.</html>"
        );
        labourWarningLabel.setOpaque(false);
        labourWarningLabel.setForeground(new Color(255, 116, 96));
        labourWarningLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 10.0f));
        labourWarningLabel.setVisible(false);
        labourWarningLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));

        bridgePlacementHintLabel = new JLabel(
                "<html>Déplacez-vous sur une case surlignée collée à la rivière pour poser le pont.</html>"
        );
        bridgePlacementHintLabel.setOpaque(false);
        bridgePlacementHintLabel.setForeground(new Color(199, 235, 255));
        bridgePlacementHintLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 9.5f));
        bridgePlacementHintLabel.setVisible(false);
        bridgePlacementHintLabel.setBorder(BorderFactory.createEmptyBorder(8, 2, 0, 2));

        JPanel specialActionsPanel = new JPanel();
        specialActionsPanel.setOpaque(false);
        specialActionsPanel.setLayout(new BoxLayout(specialActionsPanel, BoxLayout.Y_AXIS));
        specialActionsPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));
        specialActionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        specialActionsPanel.add(pathActionRow);
        specialActionsPanel.add(compostActionRow);
        specialActionsPanel.add(bridgeActionRow);
        specialActionsPanel.add(cutTreeActionRow);
        specialActionsPanel.add(bridgePlacementHintLabel);
        specialActionsPanel.add(labourWarningLabel);
        specialActionsPanel.add(createSpecialActionRow(caveButton));

        // Titre de la section objectifs (même hiérarchie visuelle que "Actions").
        JPanel objectivesTitleRow = new JPanel(new BorderLayout());
        objectivesTitleRow.setOpaque(false);
        objectivesTitleRow.setBorder(BorderFactory.createEmptyBorder(4, 16, 15, 8));
        objectivesTitleRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel objectivesTitleLabel = new JLabel("Objectifs");
        objectivesTitleLabel.setForeground(Color.WHITE);
        objectivesTitleLabel.setHorizontalAlignment(JLabel.LEFT);
        objectivesTitleLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 18.0f));
        objectivesTitleRow.add(objectivesTitleLabel, BorderLayout.WEST);

        // Carte d'objectifs inspirée du popup de la top bar.
        JPanel objectivesCardPanel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                if (width <= 0 || height <= 0) {
                    g2d.dispose();
                    return;
                }

                g2d.setColor(new Color(0, 0, 0, 70));
                g2d.fillRoundRect(4, 4, width - 8, height - 8, 16, 16);

                g2d.setColor(new Color(57, 41, 24, 232));
                g2d.fillRoundRect(0, 0, width - 8, height - 8, 16, 16);

                g2d.setColor(new Color(230, 214, 157, 255));
                g2d.drawRoundRect(0, 0, width - 9, height - 9, 16, 16);
                g2d.dispose();
            }
        };
        objectivesCardPanel.setOpaque(false);
        objectivesCardPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 14));
        objectivesCardPanel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel objectivesCardRow = createSidebarCardRow(objectivesCardPanel);

        objectivesInfoLabel = new JLabel(
                "<html>Dans la grotte, le temps est figé.<br>"
                        + "Les objectifs non réalisés n'entraînent aucune pénalité tant que vous y restez.</html>"
        );
        objectivesInfoLabel.setForeground(new Color(196, 230, 255));
        objectivesInfoLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 10.5f));
        objectivesInfoLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 2));
        objectivesInfoLabel.setVisible(false);
        objectivesInfoLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Panel contenant la liste des objectifs.
        objectivesContentPanel = new JPanel();
        objectivesContentPanel.setOpaque(false);
        objectivesContentPanel.setLayout(new BoxLayout(objectivesContentPanel, BoxLayout.Y_AXIS));
        objectivesContentPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 2));
        objectivesContentPanel.setAlignmentX(LEFT_ALIGNMENT);

        objectivesCardPanel.add(objectivesInfoLabel, BorderLayout.NORTH);
        objectivesCardPanel.add(objectivesContentPanel, BorderLayout.CENTER);

        // Carte du bilan: style distinct + couleurs dynamiques selon validation du jour.
        dayValidationCardPanel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();
                if (width <= 0 || height <= 0) {
                    g2d.dispose();
                    return;
                }

                g2d.setColor(new Color(0, 0, 0, 70));
                g2d.fillRoundRect(4, 4, width - 8, height - 8, 16, 16);

                // Fond dédié "bilan" pour distinguer clairement cette bulle des objectifs standards.
                g2d.setColor(new Color(46, 44, 24, 238));
                g2d.fillRoundRect(0, 0, width - 8, height - 8, 16, 16);

                // Bande supérieure qui change de couleur selon l'état de validation du jour.
                Color dynamicBandColor = caveMode
                        ? new Color(74, 116, 162, 230)
                        : (currentDayValidatedState
                            ? new Color(72, 150, 83, 230)
                            : new Color(166, 68, 68, 230));
                g2d.setColor(dynamicBandColor);
                g2d.fillRoundRect(0, 0, width - 8, 18, 16, 16);
                g2d.fillRect(0, 8, width - 8, 10);

                // Bordure dynamique verte (validé) ou rouge (non validé).
                Color dynamicBorderColor = caveMode
                        ? new Color(148, 201, 255, 255)
                        : (currentDayValidatedState
                            ? new Color(96, 214, 126, 255)
                            : new Color(232, 98, 98, 255));
                g2d.setColor(dynamicBorderColor);
                g2d.drawRoundRect(0, 0, width - 9, height - 9, 16, 16);
                g2d.dispose();
            }
        };
        dayValidationCardPanel.setOpaque(false);
        dayValidationCardPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 14));
        dayValidationCardPanel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel dayValidationCardRow = createSidebarCardRow(dayValidationCardPanel);

        // Conteneur texte du bilan avec padding haut pour décoller du bandeau coloré.
        dayValidationContentPanel = new JPanel();
        dayValidationContentPanel.setOpaque(false);
        dayValidationContentPanel.setLayout(new BoxLayout(dayValidationContentPanel, BoxLayout.Y_AXIS));
        dayValidationContentPanel.setBorder(BorderFactory.createEmptyBorder(16, 8, 6, 2));
        dayValidationContentPanel.setAlignmentX(LEFT_ALIGNMENT);

        // Labels persistants: mis à jour en place pour éviter les remove/add à chaque tick.
        dayValidationTitleLabel = new JLabel("Bilan du jour");
        dayValidationTitleLabel.setForeground(new Color(255, 247, 196));
        dayValidationTitleLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 13.0f));

        dayValidationProgressLabel = new JLabel("");
        dayValidationProgressLabel.setForeground(new Color(255, 236, 170));
        dayValidationProgressLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.0f));

        dayValidationStatusLabel = new JLabel("");
        dayValidationStatusLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.0f));

        // Organisation verticale des éléments du bilan du jour, avec alignement à gauche.
        JPanel dayValidationRow = new JPanel();
        dayValidationRow.setOpaque(false);
        dayValidationRow.setLayout(new BoxLayout(dayValidationRow, BoxLayout.Y_AXIS));
        dayValidationRow.setAlignmentX(LEFT_ALIGNMENT);
        dayValidationRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        dayValidationTitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        dayValidationProgressLabel.setAlignmentX(LEFT_ALIGNMENT);
        dayValidationStatusLabel.setAlignmentX(LEFT_ALIGNMENT);
        dayValidationRow.add(dayValidationTitleLabel);
        dayValidationRow.add(Box.createVerticalStrut(3));
        dayValidationRow.add(dayValidationProgressLabel);
        dayValidationRow.add(Box.createVerticalStrut(2));
        dayValidationRow.add(dayValidationStatusLabel);

        dayValidationContentPanel.add(dayValidationRow); // On ajoute d'abord le row de base, puis on met à jour son contenu en place au fil du temps.

        dayValidationCardPanel.add(dayValidationContentPanel, BorderLayout.CENTER); // Le card panel gère le fond et la bordure, le content panel gère le contenu dynamique du bilan du jour.

        // Ordonnancement final de la sidebar: actions -> objectifs -> bilan.
        JPanel actionsSectionPanel = new JPanel();
        actionsSectionPanel.setOpaque(false);
        actionsSectionPanel.setLayout(new BoxLayout(actionsSectionPanel, BoxLayout.Y_AXIS));
        actionsSectionPanel.add(titleRow);
        actionsSectionPanel.add(buttonsGrid);
        actionsSectionPanel.add(specialActionsPanel);
        actionsSectionPanel.add(objectivesTitleRow);
        actionsSectionPanel.add(objectivesCardRow);
        actionsSectionPanel.add(Box.createVerticalStrut(8));
        actionsSectionPanel.add(dayValidationCardRow);

        contentPanel.add(actionsSectionPanel, BorderLayout.NORTH);
        contentPanel.add(Box.createVerticalGlue(), BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        refreshObjectivesDisplay();

        // Au démarrage, les boutons sont désactivés tant que l'unité déplaçable n'est pas
        // sur une case valide du champ.
        applyButtonsEnabledState(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "Poser chemin",
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                "Labourer",
                "Poser compost",
                false
        );
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
     * Ajoute un inset externe aux cartes pour aligner leur bordure visible
     * avec le retrait du titre de section "Objectifs".
     */
    private JPanel createSidebarCardRow(JPanel cardPanel) {
        JPanel cardRow = new JPanel(new BorderLayout());
        cardRow.setOpaque(false);
        cardRow.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 8));
        cardRow.setAlignmentX(LEFT_ALIGNMENT);
        cardRow.add(cardPanel, BorderLayout.CENTER);
        return cardRow;
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
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
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
        if (caveMode) {
            refreshObjectivesDisplay();
            if (currentLabourEnabledState
                    || currentPlantEnabledState
                    || currentHarvestEnabledState
                    || currentCleanEnabledState
                    || currentWaterEnabledState
                    || currentPathEnabledState
                    || currentPathVisibleState
                    || currentCompostEnabledState
                    || currentCompostVisibleState
                    || currentCutTreeEnabledState
                    || currentCutTreeVisibleState
                    || currentBridgeEnabledState
                    || currentBridgeVisibleState
                    || currentBridgeHintVisibleState
                    || currentLabourWarningVisibleState
                    || !"Poser compost".equals(currentCompostButtonLabel)) {
                applyButtonsEnabledState(
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "Poser chemin",
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        "Labourer",
                        "Poser compost",
                        false
                );
            }
            return;
        }

        boolean shouldEnablePlant = canPlantActiveCell();
        boolean shouldEnableHarvest = canHarvestActiveCell();
        boolean shouldEnableClean = canCleanActiveCell();
        boolean shouldEnableWater = canWaterActiveCell();
        boolean shouldShowPathAction = shouldShowPathAction();
        boolean shouldEnablePath = canUsePathButtonOnActiveCell();
        boolean shouldDisplayCompostAction = shouldShowCompostAction();
        boolean shouldEnableCompost = canUseCompostButtonOnActiveCell();
        boolean shouldShowBridgeAction = movementModel.getSelectedFacilityType() == FacilityType.PONT;
        boolean shouldEnableBridge = shouldShowBridgeAction && canPlaceBridgeActiveCell();
        boolean shouldShowCutTreeAction = shouldShowCutTreeAction();
        boolean shouldEnableCutTree = shouldShowCutTreeAction;
        boolean shouldShowBridgeHint = shouldShowBridgeAction;
        String labourButtonLabel = shouldShowRemettreEnHerbeAction() ? "Remettre en herbe" : "Labourer";
        String pathButtonLabel = shouldShowStorePathAction()
                ? "<html><center>Remise ce chemin<br>dans l'inventaire</center></html>"
                : "Poser chemin";
        boolean shouldEnableLabourAction = canUseLabourButtonOnActiveCell();
        String compostButtonLabel = shouldShowRemiserCompostAction() ? "Remiser compost" : "Poser compost";
        boolean shouldShowLabourWarning = shouldShowAdjacentFenceLabourWarning();
        if (shouldEnableLabourAction != currentLabourEnabledState
                || shouldEnablePlant != currentPlantEnabledState
                || shouldEnableHarvest != currentHarvestEnabledState
                || shouldEnableClean != currentCleanEnabledState
                || shouldEnableWater != currentWaterEnabledState
                || shouldEnablePath != currentPathEnabledState
                || shouldShowPathAction != currentPathVisibleState
                || !pathButtonLabel.equals(currentPathButtonLabel)
                || shouldEnableCompost != currentCompostEnabledState
                || shouldDisplayCompostAction != currentCompostVisibleState
                || shouldEnableCutTree != currentCutTreeEnabledState
                || shouldShowCutTreeAction != currentCutTreeVisibleState
                || shouldEnableBridge != currentBridgeEnabledState
                || shouldShowBridgeAction != currentBridgeVisibleState
                || shouldShowBridgeHint != currentBridgeHintVisibleState
                || !labourButtonLabel.equals(currentLabourButtonLabel)
                || !compostButtonLabel.equals(currentCompostButtonLabel)
                || shouldShowLabourWarning != currentLabourWarningVisibleState) {
            applyButtonsEnabledState(
                    shouldEnableLabourAction,
                    shouldEnablePlant,
                    shouldEnableHarvest,
                    shouldEnableClean,
                    shouldEnableWater,
                    shouldEnablePath,
                    shouldShowPathAction,
                    pathButtonLabel,
                    shouldEnableCompost,
                    shouldDisplayCompostAction,
                    shouldEnableCutTree,
                    shouldShowCutTreeAction,
                    shouldEnableBridge,
                    shouldShowBridgeAction,
                    shouldShowBridgeHint,
                    labourButtonLabel,
                    compostButtonLabel,
                    shouldShowLabourWarning
            );
        }

        refreshObjectivesDisplay();
    }

    /**
     * Active/désactive les boutons.
     */
    private void applyButtonsEnabledState(boolean labourEnabled, boolean plantEnabled,
                                          boolean harvestEnabled, boolean cleanEnabled, boolean waterEnabled,
                                          boolean pathEnabled, boolean pathVisible, String pathButtonLabel,
                                          boolean compostEnabled, boolean compostVisible,
                                          boolean cutTreeEnabled, boolean cutTreeVisible,
                                          boolean bridgeEnabled, boolean bridgeVisible, boolean bridgeHintVisible,
                                          String labourButtonLabel,
                                          String compostButtonLabel,
                                          boolean labourWarningVisible) {
        currentLabourEnabledState = labourEnabled;
        currentPlantEnabledState = plantEnabled;
        currentHarvestEnabledState = harvestEnabled;
        currentCleanEnabledState = cleanEnabled;
        currentWaterEnabledState = waterEnabled;
        currentPathEnabledState = pathEnabled;
        currentPathVisibleState = pathVisible;
        currentPathButtonLabel = pathButtonLabel;
        currentCompostEnabledState = compostEnabled;
        currentCompostVisibleState = compostVisible;
        currentCutTreeEnabledState = cutTreeEnabled;
        currentCutTreeVisibleState = cutTreeVisible;
        currentBridgeEnabledState = bridgeEnabled;
        currentBridgeVisibleState = bridgeVisible;
        currentBridgeHintVisibleState = bridgeHintVisible;
        currentLabourButtonLabel = labourButtonLabel;
        currentCompostButtonLabel = compostButtonLabel;
        currentLabourWarningVisibleState = labourWarningVisible;

        labourButton.setEnabled(labourEnabled);
        boolean compactLabourLabel = "Remettre en herbe".equals(labourButtonLabel);
        labourButton.setFont(CustomFontLoader.loadFont(
                FONT_PATH,
                compactLabourLabel ? LABOUR_BUTTON_COMPACT_FONT_SIZE : LABOUR_BUTTON_DEFAULT_FONT_SIZE
        ));
        labourButton.setText(compactLabourLabel
                ? "<html><center>Remettre<br>en herbe</center></html>"
                : labourButtonLabel);
        plantButton.setEnabled(plantEnabled);
        harvestButton.setEnabled(harvestEnabled);
        waterButton.setEnabled(waterEnabled);
        cleanButton.setEnabled(cleanEnabled);
        pathButton.setEnabled(pathEnabled);
        pathButton.setText(pathButtonLabel);
        pathActionRow.setVisible(pathVisible);
        compostButton.setEnabled(compostEnabled);
        compostActionRow.setVisible(compostVisible);
        cutTreeButton.setEnabled(cutTreeEnabled);
        cutTreeActionRow.setVisible(cutTreeVisible);
        bridgeButton.setEnabled(bridgeEnabled);
        bridgeActionRow.setVisible(bridgeVisible);
        bridgePlacementHintLabel.setVisible(bridgeHintVisible);
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

    /** Vérifie si la case active peut être remise en herbe */
    private boolean canRemettreEnHerbeActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        if (activeFieldCell == null || !fieldPanel.isFarmableCell(activeFieldCell)) {
            return false; // Pas de case active ou case non cultivable : on ne peut pas remettre en herbe.
        }
        return grilleCulture.canRemettreEnHerbeCell(activeFieldCell.x, activeFieldCell.y);
    }

    /**
     * Vérifie si l'action de remettre en herbe doit être affichée.
     */
    private boolean shouldShowRemettreEnHerbeAction() {
        return canRemettreEnHerbeActiveCell();
    }

    /**
     * Le bouton de labourage doit être actif si l'une ou l'autre des actions est possible :
     * - soit labourer la case active,
     * - soit remettre en herbe la case active.
     */
    private boolean canUseLabourButtonOnActiveCell() {
        return canLabourActiveCell() || canRemettreEnHerbeActiveCell();
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
                && !grilleCulture.hasBridgeAnchorAt(activeFieldCell.x, activeFieldCell.y)
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
                || selectedSeedType == null
                || !inventaire.possedeGraine(selectedSeedType)) {
            return false;
        }

        return grilleCulture.canPlantCulture(activeFieldCell.x, activeFieldCell.y, selectedSeedType, inventaire);
    }

    /**
     * Le chemin se pose uniquement sur de l'herbe:
     * pas sur la boutique principale (à droite), pas sur une case deja labourée,
     * pas sur une case deja occupee.
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

    /** Vérifie si l'action de placement de chemin doit être affichée. */
    private boolean shouldShowPathAction() {
        return movementModel.getSelectedFacilityType() == FacilityType.CHEMIN || isPlayerStandingOnPath();
    }

    /** Vérifie si l'action de placement de chemin dans la boutique doit être affichée. */
    private boolean shouldShowStorePathAction() {
        return isPlayerStandingOnPath();
    }

    /** Vérifie si le bouton de placement de chemin peut être utilisé sur la case active. */
    private boolean canUsePathButtonOnActiveCell() {
        if (shouldShowStorePathAction()) {
            return true;
        }
        return movementModel.getSelectedFacilityType() == FacilityType.CHEMIN && canPlacePathActiveCell();
    }

    /** Vérifie si le joueur est positionné sur un chemin. */
    private boolean isPlayerStandingOnPath() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null
                && fieldPanel.isFarmableCell(activeFieldCell)
                && grilleCulture.hasPath(activeFieldCell.x, activeFieldCell.y);
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
     * Le pont ne peut être posé que sur la colonne de berge droite
     * collée à la rivière. La validation visuelle et métier passe donc
     * par un helper unique porté par le FieldPanel.
     */
    private boolean canPlaceBridgeActiveCell() {
        Point activeFieldCell = movementModel.getActiveFieldCell();
        return activeFieldCell != null && fieldPanel.isBridgePlacementCandidateCell(activeFieldCell);
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
     * La coupe se base sur la vraie géométrie de collision des arbres,
     * pas sur la simple case active du champ.
     *
     * C'est important car, près d'un arbre mature, plusieurs cases peuvent être
     * visuellement recouvertes ou désactivées sans que le joueur soit exactement
     * "sur" la case racine de cet arbre.
     */
    private boolean shouldShowCutTreeAction() {
        return getInteractableTreeNearPlayer() != null;
    }

    /** Retourne l'arbre interagissable le plus proche du joueur. */
    private TreeInstance getInteractableTreeNearPlayer() {
        Unit playerUnit = movementModel.getPlayerUnit(); // On se base sur l'unité du joueur, pas sur la case active, pour trouver l'arbre le plus proche.
        FieldObstacleMap obstacleMap = fieldPanel.getFieldObstacleMap();
        if (playerUnit == null || obstacleMap == null) {
            return null;
        }

        // On cherche un arbre interagissable à la position du joueur, avec une tolérance de la taille d'une case pour compenser les différences de géométrie.
        return obstacleMap.findInteractableTree(
                playerUnit.getX(),
                playerUnit.getY(),
                Unit.SIZE,
                Unit.SIZE
        );
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

    /**
     * Rafraîchit l'affichage des objectifs en limitant les opérations coûteuses:
     * 1) rebuild structure uniquement si la liste de types a changé,
     * 2) mise à jour incrémentale des textes/couleurs,
     * 3) repaint ciblé de la carte bilan si son état change.
     */
    private void refreshObjectivesDisplay() {
        Map<TypeObjectif, ObjectifJournalier> objectifsByType = jour.getGestionnaireObjectifs().getObjectifs();
        List<TypeObjectif> orderedTypes = new ArrayList<>(objectifsByType.keySet());
        orderedTypes.sort(Comparator.comparingInt(Enum::ordinal));

        // Signature structurelle (types + ordre): permet d'éviter de recréer les composants.
        String structureSignature = buildObjectivesStructureSignature(orderedTypes);
        if (!structureSignature.equals(currentObjectivesStructureSignature)) {
            rebuildObjectivesRows(orderedTypes);
            currentObjectivesStructureSignature = structureSignature;
        }

        int minimumToValidate = jour.getGestionnaireObjectifs().getNombreMinimumObjectifsAValider();
        int effectiveMinimum = Math.min(minimumToValidate, orderedTypes.size());
        int validatedObjectivesForSnapshot = 0;

        // Snapshot de contenu: si inchangé, on sort immédiatement pour éviter tout repaint inutile.
        StringBuilder snapshotBuilder = new StringBuilder();
        // Le snapshot encode progression + état atteint pour détecter toute vraie variation visuelle.
        for (TypeObjectif type : orderedTypes) {
            ObjectifJournalier objectif = objectifsByType.get(type);
            String progression = objectif == null ? "Progression indisponible" : objectif.getProgressionString();
            boolean isReached = objectif != null && objectif.estAtteint();
            if (isReached) {
                validatedObjectivesForSnapshot++;
            }
            snapshotBuilder
                    .append(type.name())
                    .append('|')
                    .append(progression)
                    .append('|')
                    .append(isReached)
                    .append(';');
        }
        snapshotBuilder
                .append("CAVE|")
                .append(caveMode)
                .append(';')
                .append("INFO|")
                .append(caveMode)
                .append(';')
                .append("DAY_STATE|")
                .append(caveMode)
                .append(';')
                .append("DAY|")
                .append(validatedObjectivesForSnapshot)
                .append('/')
                .append(effectiveMinimum);

        String nextSnapshot = snapshotBuilder.toString();
        if (nextSnapshot.equals(currentObjectivesSnapshot)) {
            return;
        }
        currentObjectivesSnapshot = nextSnapshot;

        // Mise à jour des lignes objectifs existantes (pas de recréation des widgets).
        int validatedObjectives = 0;
        for (TypeObjectif type : orderedTypes) {
            ObjectifJournalier objectif = objectifsByType.get(type);
            String progression = objectif == null ? "Progression indisponible" : objectif.getProgressionString();
            boolean isReached = objectif != null && objectif.estAtteint();
            if (isReached) {
                validatedObjectives++;
            }

            JTextArea objectiveLabel = objectiveTitleLabelsByType.get(type);
            JLabel progressionLabel = objectiveProgressLabelsByType.get(type);
            if (objectiveLabel == null || progressionLabel == null) {
                // Sécurité défensive: ne rien casser si la structure change en cours de frame.
                continue;
            }

            objectiveLabel.setText(resolveObjectiveTitle(type, objectif));

            String progressionWithState = progression + "   " + (isReached ? "Atteint" : "En cours");
            progressionLabel.setText(progressionWithState);
            progressionLabel.setForeground(isReached ? new Color(172, 227, 143) : new Color(236, 229, 212));
        }

        objectivesInfoLabel.setVisible(caveMode);

        // Mise à jour du bilan du jour (texte + couleur d'état).
        // Synthèse métier: le jour est validé si le seuil minimal est atteint.
        boolean isDayValidated = validatedObjectives >= effectiveMinimum;
        boolean shouldRepaintDayValidationCard = isDayValidated != currentDayValidatedState;
        currentDayValidatedState = isDayValidated;

        if (caveMode) {
            dayValidationTitleLabel.setText("Bilan du jour figé");
            dayValidationProgressLabel.setText(
                    "Progression conservée : " + validatedObjectives + " / " + effectiveMinimum
            );
            dayValidationStatusLabel.setText("Statut : temps figé");
            dayValidationStatusLabel.setForeground(new Color(182, 222, 255));
        } else {
            String dayValidationState = isDayValidated ? "Validee" : "En cours";
            dayValidationTitleLabel.setText("Bilan du jour");
            dayValidationProgressLabel.setText(
                    "Objectifs valides : " + validatedObjectives + " / " + effectiveMinimum
            );
            dayValidationStatusLabel.setText("Statut : " + dayValidationState);
            dayValidationStatusLabel.setForeground(isDayValidated ? new Color(172, 227, 143) : new Color(255, 196, 118));
        }

        // Repaint minimal pour garder l'UI fluide sans clignotement.
        // repaint (sans revalidate) suffit ici: la structure n'a pas changé, seul le contenu textuel évolue.
        objectivesContentPanel.repaint();
        dayValidationContentPanel.repaint();
        if (shouldRepaintDayValidationCard) {
            // Repaint de la carte seulement si ses couleurs dynamiques doivent changer.
            dayValidationCardPanel.repaint();
        }
    }

    /**
     * Recrée la structure des lignes objectifs uniquement si la liste des types actifs change.
     */
    private void rebuildObjectivesRows(List<TypeObjectif> orderedTypes) {
        // On reconstruit proprement les maps pour conserver la correspondance type -> widgets.
        objectiveTitleLabelsByType.clear();
        objectiveProgressLabelsByType.clear();
        objectivesContentPanel.removeAll();

        // Etat vide explicite: aucun objectif actif pour ce jour.
        if (orderedTypes.isEmpty()) {
            JLabel emptyLabel = new JLabel("Aucun objectif actif");
            emptyLabel.setForeground(new Color(236, 229, 212));
            emptyLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.5f));
            objectivesContentPanel.add(emptyLabel);
        } else {
            for (TypeObjectif type : orderedTypes) {
                // Intitulé multi-ligne natif Swing (sans HTML).
                JTextArea objectiveLabel = createWrappedObjectiveTitleArea();
                objectiveLabel.setForeground(new Color(255, 248, 220));
                objectiveLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.5f));

                // Ligne de progression/état (reste en JLabel simple).
                JLabel progressionLabel = new JLabel("");
                progressionLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.5f));

                JPanel objectiveRow = new JPanel();
                objectiveRow.setOpaque(false);
                objectiveRow.setLayout(new BoxLayout(objectiveRow, BoxLayout.Y_AXIS));
                objectiveRow.setAlignmentX(LEFT_ALIGNMENT);
                objectiveRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
                objectiveRow.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
                objectiveLabel.setAlignmentX(LEFT_ALIGNMENT);
                progressionLabel.setAlignmentX(LEFT_ALIGNMENT);
                objectiveRow.add(objectiveLabel);
                objectiveRow.add(Box.createVerticalStrut(2));
                objectiveRow.add(progressionLabel);

                // Séparateur visuel entre objectifs.
                JPanel divider = new JPanel();
                divider.setOpaque(true);
                divider.setBackground(new Color(123, 90, 53, 120));
                divider.setPreferredSize(new Dimension(1, 1));
                divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

                objectivesContentPanel.add(objectiveRow);
                objectivesContentPanel.add(Box.createVerticalStrut(4));
                objectivesContentPanel.add(divider);
                objectivesContentPanel.add(Box.createVerticalStrut(4));

                // Enregistrement des références pour les updates incrémentaux futurs.
                objectiveTitleLabelsByType.put(type, objectiveLabel);
                objectiveProgressLabelsByType.put(type, progressionLabel);
            }
        }

        objectivesContentPanel.revalidate();
        objectivesContentPanel.repaint();
    }

    /**
     * Crée un JTextArea non éditable configuré pour le retour à la ligne automatique.
     */
    private JTextArea createWrappedObjectiveTitleArea() {
        JTextArea area = new JTextArea() {
            @Override
            public Dimension getPreferredSize() {
                // Largeur fixée: la hauteur s'adapte selon le nombre de lignes nécessaires.
                setSize(OBJECTIVE_TITLE_WRAP_WIDTH, Short.MAX_VALUE);
                Dimension preferred = super.getPreferredSize();
                return new Dimension(OBJECTIVE_TITLE_WRAP_WIDTH, preferred.height);
            }

            @Override
            public Dimension getMaximumSize() {
                Dimension preferred = getPreferredSize();
                // Garder la même largeur que celle utilisée pour le calcul du wrap,
                // sinon Swing peut afficher le texte sur moins de lignes que prévu
                // et laisser un blanc vertical inutile.
                return new Dimension(OBJECTIVE_TITLE_WRAP_WIDTH, preferred.height);
            }
        };
        area.setOpaque(false);
        area.setEditable(false);
        area.setFocusable(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        // Wrap mot à mot pour éviter de couper brutalement au milieu d'un mot.
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setAlignmentX(LEFT_ALIGNMENT);
        return area;
    }

    private String resolveObjectiveTitle(TypeObjectif type, ObjectifJournalier objectif) {
        if (type == TypeObjectif.CULTURES_MANGEES && objectif instanceof ObjectifCompteur compteur) {
            return "Ne pas dépasser " + compteur.getValeurCible() + " cultures mangées";
        }
        return TypeObjectif.getIntitule(type);
    }

    /**
     * Génère une signature compacte de la structure objectifs (types + ordre).
     */
    private String buildObjectivesStructureSignature(List<TypeObjectif> orderedTypes) {
        StringBuilder signature = new StringBuilder();
        for (TypeObjectif type : orderedTypes) {
            signature.append(type.name()).append(';');
        }
        return signature.toString();
    }

    /** Retourne le bouton de plantation. */
    public JButton getPlantButton() {
        return plantButton;
    }

    /** Retourne le bouton de labourage. */
    public JButton getLabourButton() {
        return labourButton;
    }

    /** Retourne le bouton de récolte. */
    public JButton getHarvestButton() {
        return harvestButton;
    }

    /** Retourne le bouton d'arrosage. */
    public JButton getWaterButton() {
        return waterButton;
    }

    /** Retourne le bouton de nettoyage. */
    public JButton getCleanButton() {
        return cleanButton;
    }

    /** Retourne le bouton de placement de chemin. */
    public JButton getPathButton() {
        return pathButton;
    }

    /** Retourne le bouton de compostage. */
    public JButton getCompostButton() {
        return compostButton;
    }

    /** Retourne le bouton de coupe d'arbre. */
    public JButton getCutTreeButton() {
        return cutTreeButton;
    }

    /** Retourne le bouton de construction de pont. */
    public JButton getBridgeButton() {
        return bridgeButton;
    }

    /** Retourne le bouton de la grotte. */
    public JButton getCaveButton() {
        return caveButton;
    }

    /** Définit le mode grotte. */
    public void setCaveMode(boolean caveMode) {
        if (this.caveMode == caveMode) {
            return; // Pas de changement : on évite les opérations coûteuses de refresh/repaint.
        }

        this.caveMode = caveMode;
        caveButton.setText(caveMode ? "Retour ferme" : "Aller grotte");
        refreshObjectivesDisplay();
        repaint();
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
