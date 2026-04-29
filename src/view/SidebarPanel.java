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
import model.runtime.GamePauseController;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Sidebar d'actions placée à droite de l'interface.
 */
public class SidebarPanel extends JPanel {
    // Largeur fixe de la colonne latérale.
    public static final int SIDEBAR_WIDTH = 320;
    // Hauteur réservée à la zone centrale des actions.
    private static final int ACTIONS_CONTENT_HEIGHT = 620;
    // Hauteur réservée à la grille de boutons.
    private static final int BUTTONS_GRID_HEIGHT = 250;
    // Taille de police normale du bouton de labour.
    private static final float LABOUR_BUTTON_DEFAULT_FONT_SIZE = 13.5f;
    // Taille de police compacte du bouton de labour quand son texte s'allonge.
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
    // La sidebar relit l'etat de pause global pour adapter le message d'information sur les objectifs.
    private final GamePauseController pauseController;

    // Texture de fond en bois (chargée via la classe utilitaire du projet).
    private final Image woodBackground;

    // Bouton principal de labour ou de désactivation du labour.
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
    // Message d'avertissement lié au labour.
    private final JLabel labourWarningLabel;
    // Indication affichée pendant le placement d'un pont.
    private final JLabel bridgePlacementHintLabel;
    // Ligne dédiée à l'action chemin.
    private final JPanel pathActionRow;
    // Ligne dédiée à l'action compost.
    private final JPanel compostActionRow;
    // Ligne dédiée à l'action couper un arbre.
    private final JPanel cutTreeActionRow;
    // Ligne dédiée à l'action poser un pont.
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


    // Petit cache local de l'état visuel des actions.
    // La sidebar reconstruit souvent l'état désiré à partir du modèle :
    // on mémorise donc la dernière version appliquée pour éviter les mises à jour inutiles.
    private SidebarActionState currentActionButtonsState;
    // Indique si la sidebar doit afficher son comportement spécial de grotte.
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
        this.pauseController = GamePauseController.getInstance();
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
        compostButton = createBoostActionButton();
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

        objectivesInfoLabel = new JLabel(resolveObjectivesInfoText(false, false));
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
        JPanel objectivesCardRow = createObjectivesCardRow(objectivesInfoLabel, objectivesContentPanel);

        // Carte du bilan: style distinct + couleurs dynamiques selon validation du jour.
        dayValidationCardPanel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                ComponentPaintContext paintContext = ComponentPaintContext.create(graphics, this);
                if (paintContext == null) {
                    return;
                }

                Graphics2D g2d = paintContext.graphics();
                int width = paintContext.width();
                int height = paintContext.height();
                try {
                    // Fond dédié "bilan" pour distinguer clairement cette bulle des objectifs standards.
                    paintStandardSidebarCard(g2d, width, height, new Color(46, 44, 24, 238));

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
                    // On la redessine après la bande supérieure pour que cette couleur reste dominante visuellement.
                    Color dynamicBorderColor = caveMode
                            ? new Color(148, 201, 255, 255)
                            : (currentDayValidatedState
                                ? new Color(96, 214, 126, 255)
                                : new Color(232, 98, 98, 255));
                    g2d.setColor(dynamicBorderColor);
                    g2d.drawRoundRect(0, 0, width - 9, height - 9, 16, 16);
                } finally {
                    paintContext.dispose();
                }
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
        applyButtonsEnabledState(SidebarActionState.disabled());
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
     * Assemble la carte des objectifs à partir de ses deux zones dynamiques :
     * le message d'information et la liste des objectifs.
     * Le rendu visuel reste centralisé ici pour que le constructeur garde surtout
     * la lecture globale de la sidebar.
     */
    private JPanel createObjectivesCardRow(JLabel infoLabel, JPanel contentPanel) {
        JPanel objectivesCardPanel = createObjectivesCardPanel();
        objectivesCardPanel.add(infoLabel, BorderLayout.NORTH);
        objectivesCardPanel.add(contentPanel, BorderLayout.CENTER);
        return createSidebarCardRow(objectivesCardPanel);
    }

    /**
     * Fond décoratif partagé par la zone des objectifs.
     * Cette carte accueille un contenu variable, mais son habillage reste toujours le même.
     */
    private JPanel createObjectivesCardPanel() {
        JPanel objectivesCardPanel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                ComponentPaintContext paintContext = ComponentPaintContext.create(graphics, this);
                if (paintContext == null) {
                    return;
                }

                try {
                    paintStandardSidebarCard(
                            paintContext.graphics(),
                            paintContext.width(),
                            paintContext.height(),
                            new Color(57, 41, 24, 232)
                    );
                } finally {
                    paintContext.dispose();
                }
            }
        };
        objectivesCardPanel.setOpaque(false);
        objectivesCardPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 14));
        objectivesCardPanel.setAlignmentX(LEFT_ALIGNMENT);
        return objectivesCardPanel;
    }

    /**
     * Dessine la base commune des cartes de la sidebar.
     * Toutes les cartes partagent la même ombre, le même arrondi et la même bordure,
     * puis chaque zone peut ajouter ses propres accents par-dessus si elle en a besoin.
     */
    private void paintStandardSidebarCard(Graphics2D g2d, int width, int height, Color fillColor) {
        g2d.setColor(new Color(0, 0, 0, 70));
        g2d.fillRoundRect(4, 4, width - 8, height - 8, 16, 16);

        g2d.setColor(fillColor);
        g2d.fillRoundRect(0, 0, width - 8, height - 8, 16, 16);

        g2d.setColor(new Color(230, 214, 157, 255));
        g2d.drawRoundRect(0, 0, width - 9, height - 9, 16, 16);
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
    private JButton createBoostActionButton() {
        JButton button = createStyledButton("Poser compost", new Color(66, 111, 57, 255), 12.0f);
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
            SidebarActionState disabledState = SidebarActionState.disabled();
            if (disabledState.differsFrom(currentActionButtonsState)) {
                applyButtonsEnabledState(disabledState);
            }
            return;
        }

        SidebarActionState desiredState = buildFieldActionButtonsState();
        if (desiredState.differsFrom(currentActionButtonsState)) {
            applyButtonsEnabledState(desiredState);
        }

        refreshObjectivesDisplay();
    }

    /**
     * Construit l'état complet attendu pour la zone d'actions du champ.
     * Tout ce qui dépend de la case active ou de l'outil sélectionné est rassemblé ici,
     * ce qui évite d'éparpiller la logique métier dans la mise à jour visuelle.
     */
    private SidebarActionState buildFieldActionButtonsState() {
        boolean shouldShowBridgeAction = movementModel.getSelectedFacilityType() == FacilityType.PONT;
        boolean shouldShowCutTreeAction = shouldShowCutTreeAction();
        return new SidebarActionState(
                canUseLabourButtonOnActiveCell(),
                canPlantActiveCell(),
                canHarvestActiveCell(),
                canCleanActiveCell(),
                canWaterActiveCell(),
                canUsePathButtonOnActiveCell(),
                shouldShowPathAction(),
                shouldShowStorePathAction()
                        ? "<html><center>Remise ce chemin<br>dans l'inventaire</center></html>"
                        : "Poser chemin",
                canUseCompostButtonOnActiveCell(),
                shouldShowCompostAction(),
                shouldShowCutTreeAction,
                shouldShowCutTreeAction,
                shouldShowBridgeAction && canPlaceBridgeActiveCell(),
                shouldShowBridgeAction,
                shouldShowBridgeAction,
                shouldShowRemettreEnHerbeAction() ? "Remettre en herbe" : "Labourer",
                shouldShowRemiserCompostAction() ? "Remiser compost" : "Poser compost",
                shouldShowAdjacentFenceLabourWarning()
        );
    }

    /**
     * Active ou masque les éléments d'interface en fonction d'un état déjà calculé.
     * Cette méthode ne décide rien : elle traduit simplement l'état en apparence écran.
     */
    private void applyButtonsEnabledState(SidebarActionState state) {
        currentActionButtonsState = state;

        labourButton.setEnabled(state.labourEnabled);
        boolean compactLabourLabel = "Remettre en herbe".equals(state.labourButtonLabel);
        labourButton.setFont(CustomFontLoader.loadFont(
                FONT_PATH,
                compactLabourLabel ? LABOUR_BUTTON_COMPACT_FONT_SIZE : LABOUR_BUTTON_DEFAULT_FONT_SIZE
        ));
        labourButton.setText(compactLabourLabel
                ? "<html><center>Remettre<br>en herbe</center></html>"
                : state.labourButtonLabel);
        plantButton.setEnabled(state.plantEnabled);
        harvestButton.setEnabled(state.harvestEnabled);
        waterButton.setEnabled(state.waterEnabled);
        cleanButton.setEnabled(state.cleanEnabled);
        pathButton.setEnabled(state.pathEnabled);
        pathButton.setText(state.pathButtonLabel);
        pathActionRow.setVisible(state.pathVisible);
        compostButton.setEnabled(state.compostEnabled);
        compostActionRow.setVisible(state.compostVisible);
        cutTreeButton.setEnabled(state.cutTreeEnabled);
        cutTreeActionRow.setVisible(state.cutTreeVisible);
        bridgeButton.setEnabled(state.bridgeEnabled);
        bridgeActionRow.setVisible(state.bridgeVisible);
        bridgePlacementHintLabel.setVisible(state.bridgeHintVisible);
        compostButton.setText(state.compostButtonLabel);
        labourWarningLabel.setVisible(state.labourWarningVisible);
    }

    /**
     * Photographie complète de la zone d'actions.
     * En regroupant tous les drapeaux et libellés au même endroit,
     * la comparaison entre "état souhaité" et "état affiché" reste simple à suivre.
     */
    private static final class SidebarActionState {
        private final boolean labourEnabled;
        private final boolean plantEnabled;
        private final boolean harvestEnabled;
        private final boolean cleanEnabled;
        private final boolean waterEnabled;
        private final boolean pathEnabled;
        private final boolean pathVisible;
        private final String pathButtonLabel;
        private final boolean compostEnabled;
        private final boolean compostVisible;
        private final boolean cutTreeEnabled;
        private final boolean cutTreeVisible;
        private final boolean bridgeEnabled;
        private final boolean bridgeVisible;
        private final boolean bridgeHintVisible;
        private final String labourButtonLabel;
        private final String compostButtonLabel;
        private final boolean labourWarningVisible;

        private SidebarActionState(boolean labourEnabled, boolean plantEnabled,
                                   boolean harvestEnabled, boolean cleanEnabled, boolean waterEnabled,
                                   boolean pathEnabled, boolean pathVisible, String pathButtonLabel,
                                   boolean compostEnabled, boolean compostVisible,
                                   boolean cutTreeEnabled, boolean cutTreeVisible,
                                   boolean bridgeEnabled, boolean bridgeVisible, boolean bridgeHintVisible,
                                   String labourButtonLabel, String compostButtonLabel,
                                   boolean labourWarningVisible) {
            this.labourEnabled = labourEnabled;
            this.plantEnabled = plantEnabled;
            this.harvestEnabled = harvestEnabled;
            this.cleanEnabled = cleanEnabled;
            this.waterEnabled = waterEnabled;
            this.pathEnabled = pathEnabled;
            this.pathVisible = pathVisible;
            this.pathButtonLabel = pathButtonLabel;
            this.compostEnabled = compostEnabled;
            this.compostVisible = compostVisible;
            this.cutTreeEnabled = cutTreeEnabled;
            this.cutTreeVisible = cutTreeVisible;
            this.bridgeEnabled = bridgeEnabled;
            this.bridgeVisible = bridgeVisible;
            this.bridgeHintVisible = bridgeHintVisible;
            this.labourButtonLabel = labourButtonLabel;
            this.compostButtonLabel = compostButtonLabel;
            this.labourWarningVisible = labourWarningVisible;
        }

        /**
         * État neutre utilisé quand aucune action de champ ne doit rester interactive,
         * par exemple pendant le mode grotte.
         */
        private static SidebarActionState disabled() {
            return new SidebarActionState(
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
         * Compare deux instantanés d'interface sans dépendre de l'identité des objets.
         * Ce qui compte ici est uniquement ce qui sera visible ou cliquable pour le joueur.
         */
        private boolean differsFrom(SidebarActionState other) {
            return other == null
                    || labourEnabled != other.labourEnabled
                    || plantEnabled != other.plantEnabled
                    || harvestEnabled != other.harvestEnabled
                    || cleanEnabled != other.cleanEnabled
                    || waterEnabled != other.waterEnabled
                    || pathEnabled != other.pathEnabled
                    || pathVisible != other.pathVisible
                    || !pathButtonLabel.equals(other.pathButtonLabel)
                    || compostEnabled != other.compostEnabled
                    || compostVisible != other.compostVisible
                    || cutTreeEnabled != other.cutTreeEnabled
                    || cutTreeVisible != other.cutTreeVisible
                    || bridgeEnabled != other.bridgeEnabled
                    || bridgeVisible != other.bridgeVisible
                    || bridgeHintVisible != other.bridgeHintVisible
                    || !labourButtonLabel.equals(other.labourButtonLabel)
                    || !compostButtonLabel.equals(other.compostButtonLabel)
                    || labourWarningVisible != other.labourWarningVisible;
        }
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
                .append("PAUSE|")
                .append(pauseController.isPaused())
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

        boolean frozenTimeInfoVisible = caveMode || pauseController.isPaused();
        objectivesInfoLabel.setText(resolveObjectivesInfoText(caveMode, pauseController.isPaused()));
        objectivesInfoLabel.setVisible(frozenTimeInfoVisible);

        // Mise à jour du bilan du jour (texte + couleur d'état).
        // Synthèse métier: le jour est validé si le seuil minimal est atteint.
        boolean isDayValidated = validatedObjectives >= effectiveMinimum;
        currentDayValidatedState = isDayValidated;

        if (caveMode) {
            dayValidationTitleLabel.setText("Bilan du jour figé");
            dayValidationProgressLabel.setText(
                    "Progression conservée : " + validatedObjectives + " / " + effectiveMinimum
            );
            dayValidationStatusLabel.setText("Statut : temps figé");
            dayValidationStatusLabel.setForeground(new Color(182, 222, 255));
        } else {
            String dayValidationState = isDayValidated ? "Validée" : "En cours";
            dayValidationTitleLabel.setText("Bilan du jour");
            dayValidationProgressLabel.setText(
                    "Objectifs valides : " + validatedObjectives + " / " + effectiveMinimum
            );
            dayValidationStatusLabel.setText("Statut : " + dayValidationState);
            dayValidationStatusLabel.setForeground(isDayValidated ? new Color(172, 227, 143) : new Color(255, 196, 118));
        }

        // Aucune demande de repaint ici :
        // le RenderThread redessinera la sidebar après les mises à jour de texte et de couleur.
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
        if (type == TypeObjectif.ACHETER_ITEMS_BOUTIQUE) {
            return "Acheter des items\ndans la boutique";
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

    /**
     * Le panneau d'objectifs garde une seule bulle d'explication.
     * Son contenu change juste selon la raison pour laquelle le temps est actuellement figé.
     */
    private String resolveObjectivesInfoText(boolean caveMode, boolean paused) {
        if (caveMode) {
            return "<html>Dans la grotte, le temps est figé.<br>"
                    + "Les objectifs non réalisés n'entraînent aucune pénalité tant que vous y restez.</html>";
        }
        if (paused) {
            return "<html>Le jeu est en pause, le temps est figé.</html>";
        }
        return "";
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

    /** Définit le mode grotte. */
    public void setCaveMode(boolean caveMode) {
        if (this.caveMode == caveMode) {
            return; // Pas de changement : on évite les opérations coûteuses de refresh/repaint.
        }

        this.caveMode = caveMode;
        refreshObjectivesDisplay();
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
