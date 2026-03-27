package view;

import model.management.Money;
import model.runtime.Jour;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Petite barre d'information en haut à gauche de l'écran
 */
public class TopBarPanel extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    private final Money playerMoney;
    private final JLabel dayLabel;
    private final JLabel moneyLabel;
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

        add(dayLabel);
        add(moneyLabel);

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

    @Override
    protected void paintComponent(Graphics g) {
        syncMoneyText();
        syncDayText(jour.getJour());
        super.paintComponent(g);
    }
}
