package view;

import model.management.Money;
import model.objective.ObjectifJournalier;
import model.objective.TypeObjectif;
import model.runtime.Jour;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Petite barre d'information en haut à gauche de l'écran
 */
public class TopBarPanel extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    private final Money playerMoney;
    private final JLabel dayLabel;
    private final JLabel moneyLabel;
    private final JButton objectiveButton;
    private final ImageIcon completedObjectiveIcon;
    private final ImageIcon pendingObjectiveIcon;
    private JPopupMenu objectivesPopup;
    private JPanel objectivesListPanel;
    private final Jour jour;

    // Le constructeur de la classe
    public TopBarPanel(Money playerMoney, Jour jour) {
        this.playerMoney = playerMoney;
        this.jour = jour;
        jour.start();

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 0));

        dayLabel = createLabel(14.0f);
        moneyLabel = createLabel(18.0f);
        objectiveButton = createObjectiveButton();
        completedObjectiveIcon = createObjectiveStateIcon(true);
        pendingObjectiveIcon = createObjectiveStateIcon(false);

        add(dayLabel);
        add(moneyLabel);
        add(objectiveButton);

        syncMoneyText();
        syncDayText(jour.getJour());
    }

    /**
     * Construit un label simple avec le style de la barre du haut.
     */
    private JLabel createLabel(float fontSize) {
        JLabel label = new JLabel("");
        label.setForeground(new Color(255, 248, 220));
        label.setFont(CustomFontLoader.loadFont(FONT_PATH, fontSize));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    /**
     * Crée le bouton d'accès aux objectifs avec une petite icône dédiée.
     */
    private JButton createObjectiveButton() {
        JButton button = new JButton(createObjectiveIcon());
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(new Color(255, 248, 220));
        button.setFont(CustomFontLoader.loadFont(FONT_PATH, 12.0f));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(44, 44));
        button.setMaximumSize(new Dimension(44, 44));
        button.setToolTipText("Afficher les objectifs du jour");
        button.addActionListener(event -> showObjectivesPopup());
        return button;
    }

    /**
     * Ouvre une bulle ancrée au bouton qui liste les objectifs du jour.
     */
    private void showObjectivesPopup() {
        if (objectivesPopup == null) {
            objectivesPopup = createObjectivesPopup();
        }

        if (objectivesPopup.isVisible()) {
            objectivesPopup.setVisible(false);
            return;
        }

        refreshObjectivesPopup();
        objectivesPopup.show(objectiveButton, objectiveButton.getWidth() + 8, 0);
    }

    /**
     * Construit une popup non modale avec le thème visuel du HUD.
     */
    private JPopupMenu createObjectivesPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.setOpaque(false);

        JPanel root = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2d = (Graphics2D) graphics.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(0, 0, 0, 70));
                g2d.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 18, 18);

                g2d.setColor(new Color(57, 41, 24, 232));
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 18, 18);

                g2d.setColor(new Color(123, 90, 53, 245));
                g2d.fillRoundRect(0, 0, getWidth() - 8, 30, 18, 18);
                g2d.fillRect(0, 15, getWidth() - 8, 15);

                g2d.setColor(new Color(230, 214, 157, 255));
                g2d.drawRoundRect(0, 0, getWidth() - 9, getHeight() - 9, 18, 18);
                g2d.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JLabel titleLabel = new JLabel("Objectifs du jour", SwingConstants.LEFT);
        titleLabel.setForeground(new Color(255, 248, 220));
        titleLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 10.0f));

        JButton closeButton = new JButton("x");
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(new Color(255, 248, 220));
        closeButton.setFont(CustomFontLoader.loadFont(FONT_PATH, 10.0f));
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(event -> popup.setVisible(false));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);

        objectivesListPanel = new JPanel();
        objectivesListPanel.setOpaque(false);
        objectivesListPanel.setLayout(new BoxLayout(objectivesListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(objectivesListPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setPreferredSize(new Dimension(360, 190));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);

        popup.add(root);
        return popup;
    }

    /**
     * Synchronise la liste affichée dans la popup avec les objectifs actifs.
     */
    private void refreshObjectivesPopup() {
        if (objectivesListPanel == null) {
            return;
        }

        objectivesListPanel.removeAll();

        Map<TypeObjectif, ObjectifJournalier> objectifsByType = jour.getGestionnaireObjectifs().getObjectifs();
        List<TypeObjectif> orderedTypes = new ArrayList<>(objectifsByType.keySet());
        orderedTypes.sort(Comparator.comparingInt(Enum::ordinal));
        int validatedObjectives = 0;

        if (orderedTypes.isEmpty()) {
            JLabel emptyLabel = new JLabel("Aucun objectif actif");
            emptyLabel.setForeground(new Color(236, 229, 212));
            emptyLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 8.0f));
            objectivesListPanel.add(emptyLabel);
        }

        for (TypeObjectif type : orderedTypes) {
            ObjectifJournalier objectif = objectifsByType.get(type);
            String progression = objectif == null ? "Progression indisponible" : objectif.getProgressionString();
            boolean isReached = objectif != null && objectif.estAtteint();
            if (isReached) {
                validatedObjectives++;
            }

            JLabel objectiveLabel = new JLabel(TypeObjectif.getIntitule(type));
            objectiveLabel.setForeground(new Color(255, 248, 220));
            objectiveLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 8.0f));

            JLabel progressionLabel = new JLabel(progression);
            progressionLabel.setForeground(new Color(236, 229, 212));
            progressionLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 8.0f));

            JLabel stateIconLabel = new JLabel(isReached ? completedObjectiveIcon : pendingObjectiveIcon);
            stateIconLabel.setPreferredSize(new Dimension(14, 14));
            stateIconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JPanel rightInfoPanel = new JPanel();
            rightInfoPanel.setOpaque(false);
            rightInfoPanel.setLayout(new BoxLayout(rightInfoPanel, BoxLayout.X_AXIS));
            rightInfoPanel.add(progressionLabel);
            rightInfoPanel.add(Box.createHorizontalStrut(8));
            rightInfoPanel.add(stateIconLabel);

            JPanel titleRow = new JPanel(new BorderLayout());
            titleRow.setOpaque(false);
            titleRow.add(objectiveLabel, BorderLayout.WEST);
            titleRow.add(rightInfoPanel, BorderLayout.EAST);

            objectivesListPanel.add(titleRow);
            objectivesListPanel.add(Box.createVerticalStrut(8));
        }

        int minimumToValidate = jour.getGestionnaireObjectifs().getNombreMinimumObjectifsAValider();
        int effectiveMinimum = Math.min(minimumToValidate, orderedTypes.size());
        boolean isDayValidated = validatedObjectives >= effectiveMinimum;

        objectivesListPanel.add(Box.createVerticalStrut(6));

        JLabel dayValidationLabel = new JLabel("Validation du jour");
        dayValidationLabel.setForeground(new Color(255, 236, 170));
        dayValidationLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 8.0f));

        JLabel dayProgressLabel = new JLabel(validatedObjectives + " / " + effectiveMinimum);
        dayProgressLabel.setForeground(new Color(255, 236, 170));
        dayProgressLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 8.0f));

        JLabel dayStateIconLabel = new JLabel(isDayValidated ? completedObjectiveIcon : pendingObjectiveIcon);
        dayStateIconLabel.setPreferredSize(new Dimension(14, 14));
        dayStateIconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel dayRightInfoPanel = new JPanel();
        dayRightInfoPanel.setOpaque(false);
        dayRightInfoPanel.setLayout(new BoxLayout(dayRightInfoPanel, BoxLayout.X_AXIS));
        dayRightInfoPanel.add(dayProgressLabel);
        dayRightInfoPanel.add(Box.createHorizontalStrut(8));
        dayRightInfoPanel.add(dayStateIconLabel);

        JPanel dayValidationRow = new JPanel(new BorderLayout());
        dayValidationRow.setOpaque(false);
        dayValidationRow.add(dayValidationLabel, BorderLayout.WEST);
        dayValidationRow.add(dayRightInfoPanel, BorderLayout.EAST);
        objectivesListPanel.add(dayValidationRow);

        objectivesListPanel.revalidate();
        objectivesListPanel.repaint();

        if (objectivesPopup != null) {
            objectivesPopup.revalidate();
            objectivesPopup.repaint();
        }
    }

    /**
     * Génère une icône simple de type parchemin avec coche pour représenter les objectifs.
     */
    private ImageIcon createObjectiveIcon() {
        int size = 36;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(215, 198, 143));
        g2d.fillRoundRect(3, 3, 27, 30, 6, 6);
        g2d.setColor(new Color(121, 96, 54));
        g2d.drawRoundRect(3, 3, 27, 30, 6, 6);

        g2d.setColor(new Color(121, 96, 54));
        g2d.drawLine(9, 14, 21, 14);
        g2d.drawLine(9, 20, 18, 20);

        g2d.setColor(new Color(86, 171, 96));
        g2d.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(20, 24, 23, 27);
        g2d.drawLine(23, 27, 32, 16);

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Génère le petit pictogramme d'état affiché à droite de chaque objectif.
     */
    private ImageIcon createObjectiveStateIcon(boolean completed) {
        int size = 12;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (completed) {
            g2d.setColor(new Color(92, 204, 104));
            g2d.fillOval(1, 1, 9, 9);
            g2d.setColor(new Color(50, 125, 59));
            g2d.drawOval(1, 1, 9, 9);
        } else {
            g2d.setColor(new Color(104, 88, 63, 160));
            g2d.drawOval(1, 1, 9, 9);
        }

        g2d.dispose();
        return new ImageIcon(image);
    }

    /**
     * Relit la solde actuelle pour garder l'affichage synchro avec le modèle.
     */
    private void syncMoneyText() {
        String nextText = "Solde : " + playerMoney.getAmount() + " €";
        if (!nextText.equals(moneyLabel.getText())) {
            moneyLabel.setText(nextText);
        }
    }

    /**
     * Relit le jour actuel pour garder l'affichage synchro avec le modèle.
     */
    private void syncDayText(int day) {
        String nextText = "Jour " + day;
        if (!nextText.equals(dayLabel.getText())) {
            dayLabel.setText(nextText);
        }
    }

    /**
     * Donne aux overlays un point d'arrivée exact vers la zone d'argent du HUD.
     * On évite ainsi de recalculer à la main un ancrage fragile dans plusieurs vues.
     */
    public Rectangle getMoneyLabelBoundsIn(Component target) {
        if (target == null || moneyLabel == null || moneyLabel.getParent() == null) {
            return null;
        }

        return javax.swing.SwingUtilities.convertRectangle(
                moneyLabel.getParent(),
                moneyLabel.getBounds(),
                target
        );
    }

    @Override
    protected void paintComponent(Graphics g) {
        syncMoneyText();
        syncDayText(jour.getJour());
        super.paintComponent(g);
    }
}
