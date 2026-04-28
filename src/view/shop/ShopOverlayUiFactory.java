package view.shop;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

/**
 * Petit utilitaire partagé par les overlays de boutique (boutique principale et échoppe).
 * Il regroupe les composants Swing déjà stylisés de la même manière
 * pour éviter de recopier le même habillage dans plusieurs écrans.
 */
public final class ShopOverlayUiFactory {
    private ShopOverlayUiFactory() {}

    /**
     * Crée un scroll discret qui garde le contenu visible sans réintroduire
     * l'apparence standard assez lourde de Swing.
     */
    public static JScrollPane createTransparentScrollPane(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ShopScrollBarUI());
        return scrollPane;
    }

    /**
     * Construit la grande section de catalogue partagée par la boutique et l'atelier.
     * La structure reste toujours la même :
     * un en-tête avec titre et compteur à droite, puis une zone scrollable dessous.
     */
    public static ShopSectionPanel createCatalogSectionPanel(Image woodTexture, JLabel titleLabel,
                                                             JLabel trailingLabel, JComponent content,
                                                             int scrollUnitIncrement) {
        ShopSectionPanel catalogPanel = new ShopSectionPanel(woodTexture);
        catalogPanel.setLayout(new BorderLayout(0, 16));
        catalogPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        trailingLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(trailingLabel, BorderLayout.EAST);

        JScrollPane scrollPane = createTransparentScrollPane(content);
        scrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);

        catalogPanel.add(topRow, BorderLayout.NORTH);
        catalogPanel.add(scrollPane, BorderLayout.CENTER);
        return catalogPanel;
    }
}
