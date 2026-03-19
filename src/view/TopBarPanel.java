package view;

import model.Money;

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
    // @TODO - Felix : A supprimer / A modifier
    private static final String DAY_TEXT = "Jour 1";

    private final Money playerMoney;
    private final JLabel dayLabel;
    private final JLabel moneyLabel;

    public TopBarPanel(Money playerMoney) {
        this.playerMoney = playerMoney;

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(18, 24, 0, 0));

        dayLabel = createLabel(DAY_TEXT, 14.0f);
        moneyLabel = createLabel("", 18.0f);

        add(dayLabel);
        add(moneyLabel);

        syncMoneyText();
    }

    /**
     * Construit un label simple avec le style de la barre du haut.
     */
    private JLabel createLabel(String text, float fontSize) {
        JLabel label = new JLabel(text);
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

    @Override
    protected void paintComponent(Graphics g) {
        syncMoneyText();
        super.paintComponent(g);
    }
}
