package view;

import model.objective.GestionnaireObjectifs;
import model.runtime.DefeatCause;
import model.runtime.Jour;
import view.shop.ShopPixelButton;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Overlay qui recouvre la zone de jeu en cas de défaite.
 */
public class GameOverOverlay extends JPanel {
    // Police pixel commune utilisée sur la carte de défaite.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    // Largeur maximale de la carte affichée au centre.
    private static final int CARD_MAX_WIDTH = 620;
    // Hauteur fixe de la carte affichée au centre.
    private static final int CARD_HEIGHT = 300;
    // Marge horizontale minimale laissée autour de la carte.
    private static final int CARD_HORIZONTAL_MARGIN = 80;
    // Largeur du bouton qui relance une partie.
    private static final int REPLAY_BUTTON_WIDTH = 190;
    // Hauteur du bouton qui relance une partie.
    private static final int REPLAY_BUTTON_HEIGHT = 42;

    // On lit l'état de fin de partie et la cause exacte de la défaite.
    private final Jour jour;
    // Bouton affiché uniquement quand la partie est réellement terminée.
    private final ShopPixelButton replayButton;

    /**
     * On prépare l'overlay de défaite et son bouton de relance.
     */
    public GameOverOverlay(Jour jour) {
        this.jour = jour;

        setOpaque(false);
        setLayout(null);
        setAlignmentX(0.5f);
        setAlignmentY(0.5f);

        /*
         * On réutilise le bouton pixel déjà présent ailleurs dans le jeu.
         */
        replayButton = new ShopPixelButton(
                "Rejouer",
                CustomFontLoader.loadFont(FONT_PATH, 12.0f),
                new Color(124, 83, 48, 255),
                new Color(148, 101, 60, 255),
                new Color(50, 30, 10),
                new Color(255, 248, 220)
        );
        replayButton.setFocusable(false);
        replayButton.setPreferredSize(new Dimension(REPLAY_BUTTON_WIDTH, REPLAY_BUTTON_HEIGHT));
        replayButton.setVisible(false);
        add(replayButton);
    }

    /** Méthode qui dessine l'overlay de fin de partie */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        boolean partiePerdue = jour.isPartieTerminee();
        if (replayButton.isVisible() != partiePerdue) {
            replayButton.setVisible(partiePerdue);
        }

        if (!partiePerdue) {
            return;
        }

        // On utilise Graphics2D pour de meilleurs rendus (antialiasing).
        Graphics2D g2d = (Graphics2D) graphics.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        g2d.setColor(new Color(12, 8, 6, 200));
        g2d.fillRect(0, 0, width, height);

        int cardWidth = computeCardWidth();
        int cardHeight = CARD_HEIGHT;
        int cardX = (width - cardWidth) / 2;
        int cardY = (height - cardHeight) / 2;

        g2d.setColor(new Color(45, 26, 18, 240));
        g2d.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);

        g2d.setColor(new Color(214, 159, 126, 255));
        g2d.drawRoundRect(cardX, cardY, cardWidth, cardHeight, 20, 20);

        Font titleFont = CustomFontLoader.loadFont(FONT_PATH, 26.0f);
        Font bodyFont = CustomFontLoader.loadFont(FONT_PATH, 13.0f);
        Font hintFont = CustomFontLoader.loadFont(FONT_PATH, 10.0f);

        DefeatCause defeatCause = jour.getDefeatCause();
        String title = (defeatCause == DefeatCause.CAVE_SHRINE || defeatCause == DefeatCause.CAVE_ENEMY)
                ? "MORT DANS LA GROTTE"
                : "PARTIE PERDUE";
        String body = buildBodyText(defeatCause);
        String hint = "Lancez une nouvelle partie pour rejouer !";

        g2d.setColor(new Color(255, 225, 201));
        g2d.setFont(titleFont);
        drawCenteredLine(g2d, title, width, cardY + 72);

        g2d.setColor(new Color(246, 215, 188));
        g2d.setFont(bodyFont);
        drawCenteredLine(g2d, body, width, cardY + 128);

        g2d.setColor(new Color(214, 188, 166));
        g2d.setFont(hintFont);
        drawCenteredLine(g2d, hint, width, cardY + 172);

        g2d.dispose();
    }

    @Override
    public void doLayout() {
        super.doLayout();

        /*
         * On le place ici pour qu'il reste toujours bien centré en bas de la carte,
         * quelle que soit la taille de la fenêtre.
         */
        int cardWidth = computeCardWidth();
        int cardX = (getWidth() - cardWidth) / 2;
        int cardY = (getHeight() - CARD_HEIGHT) / 2;
        int buttonX = cardX + (cardWidth - REPLAY_BUTTON_WIDTH) / 2;
        int buttonY = cardY + CARD_HEIGHT - REPLAY_BUTTON_HEIGHT - 32;
        replayButton.setBounds(buttonX, buttonY, REPLAY_BUTTON_WIDTH, REPLAY_BUTTON_HEIGHT);
    }

    @Override
    public boolean contains(int x, int y) {
        /*
         * Tant que la partie continue, cet overlay doit être totalement transparent
         * pour la souris. Sans ça, on aurait un panneau invisible au-dessus du jeu
         * qui capterait les clics "pour rien".
         */
        return jour.isPartieTerminee() && super.contains(x, y);
    }

    /** Dessine une ligne centrée sur le composant */
    private void drawCenteredLine(Graphics2D g2d, String text, int fullWidth, int baselineY) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (fullWidth - metrics.stringWidth(text)) / 2;
        g2d.drawString(text, x, baselineY);
    }

    /**
     * On choisit le texte central selon la vraie cause de la défaite.
     */
    private String buildBodyText(DefeatCause defeatCause) {
        if (defeatCause == DefeatCause.CAVE_SHRINE) {
            return "La statue du sanctuaire vous a foudroyé.";
        }
        if (defeatCause == DefeatCause.CAVE_ENEMY) {
            return "Les monstres de la grotte vous ont abattu.";
        }

        GestionnaireObjectifs gestionnaire = jour.getGestionnaireObjectifs();
        int minimum = gestionnaire.getNombreObjectifsAValiderEffectif();
        int atteints = gestionnaire.getNombreObjectifsAtteints();
        return "Objectifs valides : " + atteints + " / " + minimum;
    }

    /** Calcule la largeur de la carte centrale tout en gardant une marge confortable. */
    private int computeCardWidth() {
        return Math.max(320, Math.min(CARD_MAX_WIDTH, getWidth() - CARD_HORIZONTAL_MARGIN));
    }

    /**
     * On expose le bouton pour que le contrôleur puisse y brancher l'action de replay.
     */
    public ShopPixelButton getReplayButton() {
        return replayButton;
    }
}
