package view;

import model.MovementModel;

import javax.swing.BorderFactory;
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

/**
 * Overlay d'actions placé en bas à gauche de l'interface.
 */
public class ActionOverlayPanel extends JPanel {
    private static final int OVERLAY_WIDTH = 320;
    private static final int OVERLAY_HEIGHT = 230;

    // Le chemin pour accéder à la police personnalisée
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // Référence au modèle: la vue lit uniquement un état booléen d'activation.
    private final MovementModel movementModel;

    // Texture de fond en bois (chargée via la classe utilitaire du projet).
    private final Image woodBackground;

    // les 4 boutons de contrôle des actions des unités déplaçables.
    private final JButton plantButton;
    private final JButton harvestButton;
    private final JButton waterButton;
    private final JButton cleanButton;

    // Petit cache local pour éviter d'appliquer setEnabled en boucle inutilement.
    private boolean currentEnabledState;

    // Constructeur de la classe
    public ActionOverlayPanel(MovementModel movementModel) {
        this.movementModel = movementModel;
        this.woodBackground = ImageLoader.load("/assets/bois.png");

        // Le panneau est transparent hors de sa zone peinte personnalisée.
        setOpaque(false);

        // On souhaite faire un overlay placé en bas à gauche.
        setAlignmentX(0.0f);
        setAlignmentY(1.0f);

        // On fixe la taille.
        setPreferredSize(new Dimension(OVERLAY_WIDTH, OVERLAY_HEIGHT));
        setMinimumSize(new Dimension(OVERLAY_WIDTH, OVERLAY_HEIGHT));
        setMaximumSize(new Dimension(OVERLAY_WIDTH, OVERLAY_HEIGHT));

        // Petite marge depuis le bord de la fenêtre pour ne pas coller aux bords.
        setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 0));

        // On utilise un BorderLayout pour séparer clairement
        // le titre (haut) et la grille de boutons (centre).
        setLayout(new BorderLayout());

        // On s'occupe du titre principal (avec la police personnalisée).
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(10, 16, 4, 8));

        JLabel titleLabel = new JLabel("Actions");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(JLabel.LEFT);
        titleLabel.setFont(CustomFontLoader.loadFont(FONT_PATH, 18.0f));
        titleRow.add(titleLabel, BorderLayout.WEST);

        // On crée la grille 2x2 demandée avec deux boutons par ligne.
        JPanel buttonsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonsGrid.setOpaque(false);
        buttonsGrid.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));

        // On crée les boutons en appliquant le style visuel.
        plantButton = createStyledButton("Planter", new Color(139, 69, 19, 255), 12.5f);
        harvestButton = createStyledButton("Recolter", new Color(160, 82, 45, 255), 12.5f);
        waterButton = createStyledButton("Arroser", new Color(205, 133, 63, 255), 12.5f);
        cleanButton = createStyledButton("Nettoyer", new Color(101, 67, 33, 255), 12.5f);

        // On ajoute les boutons
        buttonsGrid.add(plantButton);
        buttonsGrid.add(harvestButton);
        buttonsGrid.add(waterButton);
        buttonsGrid.add(cleanButton);

        add(titleRow, BorderLayout.NORTH);
        add(buttonsGrid, BorderLayout.CENTER);

        // Au démarrage, les boutons sont désactivés tant que l'unité déplaçable n'est pas
        // sur une case valide du champ.
        applyButtonsEnabledState(false);
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
     * Lit l'état dans le modèle et applique visuellement l'activation si nécessaire.
     */
    private void syncFromModel() {
        boolean shouldEnable = movementModel.isActionOverlayEnabled();
        if (shouldEnable != currentEnabledState) {
            applyButtonsEnabledState(shouldEnable);
        }
    }

    /**
     * Active/désactive les boutons.
     */
    private void applyButtonsEnabledState(boolean enabled) {
        currentEnabledState = enabled;

        plantButton.setEnabled(enabled);
        harvestButton.setEnabled(enabled);
        waterButton.setEnabled(enabled);
        cleanButton.setEnabled(enabled);

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
