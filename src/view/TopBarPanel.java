package view;

import model.management.Money;
import model.runtime.GamePauseController;
import model.runtime.Jour;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

/**
 * Petite barre d'information en haut à gauche de l'écran
 */
public class TopBarPanel extends JPanel {
    // Chemin de la police pixel utilisée dans la barre du haut.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    // Largeur fixe de la jauge de progression du jour.
    private static final int DAY_PROGRESS_BAR_WIDTH = 156;
    // Hauteur fixe de la jauge de progression du jour.
    private static final int DAY_PROGRESS_BAR_HEIGHT = 18;
    // Cadre de la jauge de progression du jour.
    private static final Color DAY_PROGRESS_FRAME = new Color(244, 215, 125, 245);
    // Fond de la jauge de progression du jour.
    private static final Color DAY_PROGRESS_BACKGROUND = new Color(46, 30, 14, 235);
    // Remplissage de la jauge de progression du jour.
    private static final Color DAY_PROGRESS_FILL = new Color(238, 191, 62, 250);
    // Reflet de la jauge de progression du jour.
    private static final Color DAY_PROGRESS_HIGHLIGHT = new Color(255, 244, 194, 240);
    // Fond du bouton de pause quand le jeu tourne normalement.
    private static final Color PAUSE_BUTTON_BACKGROUND = new Color(86, 52, 18, 238);
    // Fond du bouton quand le jeu est deja fige.
    private static final Color PAUSE_BUTTON_BACKGROUND_ACTIVE = new Color(128, 66, 22, 246);
    // Variante légèrement plus claire affichée au survol.
    private static final Color PAUSE_BUTTON_BACKGROUND_HOVER = new Color(104, 63, 24, 244);
    // Ombre légère posée sous le bouton pour l'intégrer au HUD sans l'alourdir.
    private static final Color PAUSE_BUTTON_SHADOW = new Color(0, 0, 0, 82);
    // Bordure chaude du bouton de pause.
    private static final Color PAUSE_BUTTON_BORDER = new Color(244, 215, 125, 245);
    // Reflet très léger sur la partie haute du bouton.
    private static final Color PAUSE_BUTTON_HIGHLIGHT = new Color(255, 241, 188, 56);
    // Couleur du texte du bouton.
    private static final Color PAUSE_BUTTON_TEXT = new Color(255, 248, 220);
    // Ombre courte du symbole central pour améliorer la lisibilité.
    private static final Color PAUSE_BUTTON_TEXT_SHADOW = new Color(73, 48, 22, 220);
    // Couleur du texte affiché dans la jauge.
    private static final Color DAY_PROGRESS_TEXT = new Color(255, 248, 220);
    // Ombre du texte affiché dans la jauge.
    private static final Color DAY_PROGRESS_TEXT_SHADOW = new Color(73, 48, 22, 220);

    // Réserve d'argent du joueur affichée dans le HUD.
    private final Money playerMoney;
    // Controleur global de pause partage avec toutes les boucles de jeu.
    private final GamePauseController pauseController;
    // Libellé qui affiche le numéro du jour courant.
    private final JLabel dayLabel;
    // Libellé qui affiche le solde actuel.
    private final JLabel moneyLabel;
    // Police utilisée dans la jauge de progression du jour.
    private final Font dayProgressTextFont;
    // Composant graphique de la jauge de progression du jour.
    private final JComponent dayProgressBar;
    // Bouton unique qui bascule entre pause et reprise sans changer le reste du HUD.
    private final JButton pauseButton;
    // Police du bouton de pause.
    private final Font pauseButtonFont;
    // Référence au cycle de journée en cours.
    private final Jour jour;

    /**
     * On prépare la barre haute avec le jour courant, l'argent et la jauge temporelle.
     */
    public TopBarPanel(Money playerMoney, Jour jour) {
        this.playerMoney = playerMoney;
        this.pauseController = GamePauseController.getInstance();
        this.jour = jour;
        jour.start();

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 0));

        dayLabel = createLabel(14.0f);
        moneyLabel = createLabel(18.0f);
        dayProgressTextFont = CustomFontLoader.loadFont(FONT_PATH, 8.0f);
        pauseButtonFont = CustomFontLoader.loadFont(FONT_PATH, 13.0f);
        dayProgressBar = createDayProgressBar();

        syncMoneyText();
        syncDayText(jour.getJour());
        JPanel infoColumn = createInfoColumn();
        pauseButton = createPauseButton(infoColumn.getPreferredSize().height);

        infoColumn.setAlignmentY(Component.TOP_ALIGNMENT);
        pauseButton.setAlignmentY(Component.TOP_ALIGNMENT);

        add(infoColumn);
        add(Box.createHorizontalStrut(10));
        add(pauseButton);

        syncPauseButtonState();
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
     * Le bloc d'informations garde ses deux lignes d'origine :
     * la première pour le jour et sa jauge,
     * la seconde pour le solde.
     * Le bouton de pause vient ensuite se caler à droite sur toute cette hauteur.
     */
    private JPanel createInfoColumn() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setAlignmentX(LEFT_ALIGNMENT);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JPanel dayInfoRow = createDayInfoRow();
        dayInfoRow.setAlignmentX(LEFT_ALIGNMENT);
        moneyLabel.setAlignmentX(LEFT_ALIGNMENT);

        column.add(dayInfoRow);
        column.add(moneyLabel);
        return column;
    }

    /**
     * Regroupe le libellé du jour et sa jauge temporelle sur la même ligne.
     * Le joueur lit ainsi immédiatement "où on en est" dans le jour en cours.
     */
    private JPanel createDayInfoRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        dayLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        dayProgressBar.setAlignmentY(Component.CENTER_ALIGNMENT);

        row.add(dayLabel);
        row.add(Box.createHorizontalStrut(12));
        row.add(dayProgressBar);
        return row;
    }

    /**
     * Le bouton ne porte qu'une seule responsabilite :
     * geler ou relancer tout le temps de jeu partage par les threads.
     * On s'appuie donc directement sur le controleur global de pause
     * au lieu d'introduire une logique parallele dans le HUD.
     */
    private JButton createPauseButton(int buttonSize) {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics graphics) {
                ComponentPaintContext paintContext = ComponentPaintContext.create(graphics, this);
                if (paintContext == null) {
                    return;
                }

                Graphics2D g2 = paintContext.graphics();
                int width = paintContext.width();
                int height = paintContext.height();
                try {
                    // Le bouton reste grand pour être confortable à cliquer,
                    // mais son dessin utile est volontairement rentré à l'intérieur
                    // pour qu'il se fonde mieux dans la barre du haut.
                    int inset = Math.max(3, Math.min(width, height) / 12);
                    int drawWidth = width - (inset * 2);
                    int drawHeight = height - (inset * 2);
                    int arc = Math.max(12, drawHeight / 3);

                    Color background = getBackground();
                    if (getModel().isRollover() && !pauseController.isPaused()) {
                        background = PAUSE_BUTTON_BACKGROUND_HOVER;
                    }

                    g2.setColor(PAUSE_BUTTON_SHADOW);
                    g2.fillRoundRect(inset + 2, inset + 2, drawWidth, drawHeight, arc, arc);

                    g2.setColor(background);
                    g2.fillRoundRect(inset, inset, drawWidth, drawHeight, arc, arc);

                    g2.setColor(PAUSE_BUTTON_HIGHLIGHT);
                    g2.fillRoundRect(inset, inset, drawWidth, Math.max(8, drawHeight / 4), arc, arc);

                    g2.setColor(PAUSE_BUTTON_BORDER);
                    g2.drawRoundRect(inset, inset, drawWidth - 1, drawHeight - 1, arc, arc);

                    String symbol = getText();
                    g2.setFont(getFont());
                    int textWidth = g2.getFontMetrics().stringWidth(symbol);
                    int textX = inset + ((drawWidth - textWidth) / 2);
                    int textY = inset + ((drawHeight - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent() - 1;

                    g2.setColor(PAUSE_BUTTON_TEXT_SHADOW);
                    g2.drawString(symbol, textX + 1, textY + 1);
                    g2.setColor(PAUSE_BUTTON_TEXT);
                    g2.drawString(symbol, textX, textY);
                } finally {
                    paintContext.dispose();
                }
            }
        };
        button.setFocusable(false);
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(PAUSE_BUTTON_TEXT);
        button.setBackground(PAUSE_BUTTON_BACKGROUND);
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setFont(pauseButtonFont);
        button.setRolloverEnabled(true);
        button.setPreferredSize(new Dimension(buttonSize, buttonSize));
        button.setMinimumSize(new Dimension(buttonSize, buttonSize));
        button.setMaximumSize(new Dimension(buttonSize, buttonSize));

        // Le meme bouton sert toujours :
        // quand le jeu tourne il lance la pause,
        // et quand le jeu est fige il relance exactement la meme session.
        button.addActionListener(event -> {
            pauseController.setPaused(!pauseController.isPaused());
            syncPauseButtonState();
        });
        return button;
    }

    /**
     * Petite jauge dédiée au chrono du jour.
     * Elle avance avec le temps réellement joué, pas avec les objectifs atteints :
     * le but est de montrer quand le jeu évaluera la validation du jour courant.
     */
    private JComponent createDayProgressBar() {
        JComponent progressBar = new JComponent() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);

                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                double progressRatio = jour.getProgressionVersJourSuivant();

                /*
                 * On pose d'abord une ombre portée sous la jauge pour qu'elle reste visible
                 * même quand le décor du jeu est très contrasté derrière le HUD.
                 */
                g2.setColor(new Color(0, 0, 0, 95));
                g2.fillRoundRect(2, 2, Math.max(1, getWidth() - 2), Math.max(1, getHeight() - 1), getHeight() + 2, getHeight() + 2);

                HudProgressBarPainter.paint(
                        g2,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        progressRatio,
                        DAY_PROGRESS_FRAME,
                        DAY_PROGRESS_BACKGROUND,
                        DAY_PROGRESS_FILL,
                        DAY_PROGRESS_HIGHLIGHT
                );

                /*
                 * On affiche le temps restant directement dans la barre.
                 * Le joueur n'a donc pas besoin d'interpréter uniquement la jauge visuelle :
                 * l'information "combien de secondes avant le prochain jour ?" est explicite.
                 */
                long remainingMs = jour.getTempsRestantAvantProchainJourMs();
                int remainingSeconds = (int) Math.max(0L, Math.ceil(remainingMs / 1000.0));
                String remainingText = remainingSeconds + "s";
                g2.setFont(dayProgressTextFont);
                int textWidth = g2.getFontMetrics().stringWidth(remainingText);
                int textX = (getWidth() - textWidth) / 2;
                int textY = ((getHeight() - g2.getFontMetrics().getHeight()) / 2) + g2.getFontMetrics().getAscent() - 1;

                g2.setColor(DAY_PROGRESS_TEXT_SHADOW);
                g2.drawString(remainingText, textX + 1, textY + 1);
                g2.setColor(DAY_PROGRESS_TEXT);
                g2.drawString(remainingText, textX, textY);
                g2.dispose();
            }

            /**
             * On renvoie le temps restant complet dans l'infobulle de la jauge.
             */
            @Override
            public String getToolTipText(MouseEvent event) {
                long remainingMs = jour.getTempsRestantAvantProchainJourMs();
                int remainingSeconds = (int) Math.max(0L, Math.ceil(remainingMs / 1000.0));
                return "Prochain jour dans " + remainingSeconds + " s";
            }
        };

        progressBar.setOpaque(false);
        progressBar.setToolTipText("");
        progressBar.setPreferredSize(new Dimension(DAY_PROGRESS_BAR_WIDTH, DAY_PROGRESS_BAR_HEIGHT));
        progressBar.setMinimumSize(new Dimension(DAY_PROGRESS_BAR_WIDTH, DAY_PROGRESS_BAR_HEIGHT));
        progressBar.setMaximumSize(new Dimension(DAY_PROGRESS_BAR_WIDTH, DAY_PROGRESS_BAR_HEIGHT));
        return progressBar;
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
     * Le libelle et l'infobulle restent cales sur l'etat global de pause.
     * Ainsi, si une autre interface met temporairement le jeu en pause,
     * le bouton continue d'expliquer clairement ce qu'un nouveau clic fera.
     */
    private void syncPauseButtonState() {
        boolean paused = pauseController.isPaused();
        pauseButton.setText(paused ? ">" : "||");
        pauseButton.setToolTipText(paused ? "Relancer le jeu" : "Mettre le jeu en pause");
        pauseButton.setBackground(paused ? PAUSE_BUTTON_BACKGROUND_ACTIVE : PAUSE_BUTTON_BACKGROUND);
        pauseButton.setForeground(PAUSE_BUTTON_TEXT);
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

    /**
     * On resynchronise le texte du HUD avant de laisser Swing dessiner la barre.
     */
    @Override
    protected void paintComponent(Graphics g) {
        syncMoneyText();
        syncDayText(jour.getJour());
        syncPauseButtonState();
        super.paintComponent(g);
    }
}
