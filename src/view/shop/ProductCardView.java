package view.shop;

import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import view.ProductPixelArt;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Carte visuelle d'un produit dans le catalogue central.
 */
public class ProductCardView extends JPanel {
    // Couleur du texte principal de la carte.
    private static final Color TEXT_PRIMARY = new Color(255, 248, 226);
    // Couleur du texte secondaire de la carte.
    private static final Color TEXT_SECONDARY = new Color(216, 199, 164);
    // Ombre portée du bloc carte.
    private static final Color PANEL_SHADOW = new Color(0, 0, 0, 90);
    // Fond de carte au repos.
    private static final Color CARD_FILL = new Color(67, 49, 34, 238);
    // Fond de carte au survol.
    private static final Color CARD_FILL_HOVER = new Color(83, 61, 42, 244);
    // Fond de carte quand elle est sélectionnée.
    private static final Color CARD_FILL_SELECTED = new Color(102, 72, 46, 248);
    // Bordure douce standard de la carte.
    private static final Color BORDER_SOFT = new Color(138, 110, 73, 180);
    // Bordure accentuée de la carte sélectionnée.
    private static final Color ACCENT = new Color(216, 181, 96);
    // Fond du badge dans son état normal.
    private static final Color BADGE_FILL_DEFAULT = new Color(45, 75, 42, 220);
    // Bordure du badge dans son état normal.
    private static final Color BADGE_BORDER_DEFAULT = new Color(126, 190, 110);
    // Fond du badge quand il signale un manque de bois.
    private static final Color BADGE_FILL_ERROR = new Color(114, 43, 38, 225);
    // Bordure du badge quand il signale un manque de bois.
    private static final Color BADGE_BORDER_ERROR = new Color(232, 112, 101);
    // Fond du bouton intégré quand l'action est disponible.
    private static final Color ACTION_FILL = new Color(92, 166, 196, 225);
    // Fond du bouton intégré au survol.
    private static final Color ACTION_FILL_HOVER = new Color(116, 191, 221, 235);
    // Fond du bouton quand l'action est indisponible.
    private static final Color ACTION_FILL_DISABLED = new Color(74, 78, 86, 220);
    // Bordure du bouton intégré.
    private static final Color ACTION_BORDER = new Color(203, 228, 240, 180);

    // Produit représenté par cette carte.
    private final Product product;
    // Libellé de catégorie affiché en haut de la carte.
    private final String categoryLabel;
    // Détail court affiché au centre quand il existe.
    private final String detailLabel;
    // Valeur ou prix affiché dans la carte.
    private final String valueLabel;
    // Texte de pied de carte, souvent utilisé pour le stock.
    private final String footerLabel;
    // Texte du badge éventuel affiché en bas à droite.
    private final String badgeText;
    // Indique si cette carte représente la sélection courante.
    private final boolean selected;
    // Texture bois légère appliquée sur le fond.
    private final Image woodTexture;
    // Police des petits libellés.
    private final Font labelFont;
    // Police du titre et des valeurs importantes.
    private final Font priceFont;
    // Police du corps de texte.
    private final Font bodyFont;
    // Sert juste à dire quoi faire quand on clique sur la carte.
    // Le callback reste volontairement un Runnable très simple :
    // la carte ne connaît ni la boutique, ni le contrôleur, ni la logique métier.
    // Elle sait seulement déclencher "l'action de sélection" qu'on lui confie au clic.
    private final Runnable onSelect;
    // Libellé du bouton d'action intégré, quand la carte en porte un.
    private final String actionLabel;
    // Indique si l'action intégrée est cliquable.
    private final boolean actionEnabled;
    // Callback exécuté quand on clique sur l'action intégrée.
    private final Runnable onAction;

    // Indique si le pointeur survole actuellement la carte.
    private boolean hovered;
    // Indique si le pointeur survole le bouton intégré.
    private boolean actionHovered;

    /**
     * On prépare une carte purement visuelle pour un produit du catalogue.
     * Le Runnable injecté garde cette vue légère :
     * elle délègue simplement l'action de clic au parent qui l'instancie.
     */
    public ProductCardView(
            Product product,
            String categoryLabel,
            String detailLabel,
            String valueLabel,
            String footerLabel,
            String badgeText,
            boolean selected,
            Image woodTexture,
            Font labelFont,
            Font priceFont,
            Font bodyFont,
            Runnable onSelect
    ) {
        this(
                product,
                categoryLabel,
                detailLabel,
                valueLabel,
                footerLabel,
                badgeText,
                selected,
                woodTexture,
                labelFont,
                priceFont,
                bodyFont,
                onSelect,
                null,
                false,
                null
        );
    }

    /**
     * Variante enrichie avec un bouton intégré dans la carte.
     */
    public ProductCardView(
            Product product,
            String categoryLabel,
            String detailLabel,
            String valueLabel,
            String footerLabel,
            String badgeText,
            boolean selected,
            Image woodTexture,
            Font labelFont,
            Font priceFont,
            Font bodyFont,
            Runnable onSelect,
            String actionLabel,
            boolean actionEnabled,
            Runnable onAction
    ) {
        this.product = product;
        this.categoryLabel = categoryLabel;
        this.detailLabel = detailLabel == null ? "" : detailLabel.trim();
        this.valueLabel = valueLabel == null ? "" : valueLabel.trim();
        this.footerLabel = footerLabel == null ? "" : footerLabel.trim();
        this.badgeText = badgeText == null ? "" : badgeText.trim();
        this.selected = selected;
        this.woodTexture = woodTexture;
        this.labelFont = labelFont;
        this.priceFont = priceFont;
        this.bodyFont = bodyFont;
        this.onSelect = onSelect;
        this.actionLabel = actionLabel == null ? "" : actionLabel.trim();
        this.actionEnabled = actionEnabled;
        this.onAction = onAction;

        setOpaque(false);
        setPreferredSize(new Dimension(270, getCardHeight()));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // On ne garde qu'un retour visuel minimal:
        // survol pour donner envie de cliquer, puis callback de selection.
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                actionHovered = isInsideActionButton(e);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                actionHovered = false;
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasActionHovered = actionHovered;
                actionHovered = isInsideActionButton(e);
                if (wasActionHovered != actionHovered) {
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isInsideActionButton(e) && actionEnabled && onAction != null) {
                    onAction.run();
                    return;
                }
                if (onSelect != null) {
                    onSelect.run();
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    /**
     * On dessine l'ensemble de la carte produit, du fond jusqu'au badge éventuel.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = selected ? CARD_FILL_SELECTED : hovered ? CARD_FILL_HOVER : CARD_FILL;

        // La carte superpose ombre, fond et bordure.
        // Le rendu reste simple pour laisser respirer le pixel art au centre.
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 20, 20);

        g2d.setColor(fill);
        g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        if (woodTexture != null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.08f));
            g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        g2d.setColor(selected ? ACCENT : BORDER_SOFT);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

        // Le cartouche sombre derriere l'illustration aide a garder
        // une lecture propre meme quand la texture du bois est visible.
        g2d.setColor(new Color(92, 68, 46, 220));
        g2d.fillRoundRect(16, 16, 88, 88, 18, 18);

        int pixelSize = getCardArtPixelSize();
        int artWidth = ProductPixelArt.getProductArtWidth(product, pixelSize);
        int artHeight = ProductPixelArt.getProductArtHeight(product, pixelSize);
        int artX = 16 + ((88 - artWidth) / 2);
        int artY = 16 + ((88 - artHeight) / 2);
        ProductPixelArt.drawProduct(g2d, product, artX, artY, pixelSize);

        g2d.setFont(labelFont);
        g2d.setColor(TEXT_SECONDARY);
        g2d.drawString(categoryLabel, 120, 30);

        g2d.setFont(priceFont);
        g2d.setColor(TEXT_PRIMARY);
        g2d.drawString(product.getName(), 120, 58);

        g2d.setFont(bodyFont);
        if (hasDetailLabel()) {
            /*
             * Le texte d'effet est affiché directement sur la carte du catalogue.
             * On accepte plusieurs lignes pour les règles un peu plus longues,
             * afin d'éviter les débordements horizontaux.
             */
            g2d.setColor(new Color(176, 226, 136));
            String[] detailLines = getDetailLines();
            int detailY = 84;
            for (String line : detailLines) {
                g2d.drawString(line, 120, detailY);
                detailY += 16;
            }
            g2d.setColor(TEXT_SECONDARY);
            if (!valueLabel.isBlank()) {
                g2d.drawString(valueLabel, 120, detailY + 8);
            }
            if (!footerLabel.isBlank()) {
                g2d.drawString(footerLabel, 120, detailY + 32);
            }
        } else {
            g2d.setColor(TEXT_SECONDARY);
            if (!valueLabel.isBlank()) {
                g2d.drawString(valueLabel, 120, 86);
            }
            if (!footerLabel.isBlank()) {
                g2d.drawString(footerLabel, 120, 110);
            }
        }

        if (!badgeText.isBlank()) {
            // Le badge ne s'affiche que si l'article est deja dans le panier.
            // Ca evite d'ajouter du bruit sur toutes les cartes en permanence.
            java.awt.FontMetrics badgeMetrics = g2d.getFontMetrics();
            int badgeWidth = badgeMetrics.stringWidth(badgeText) + 18;
            int badgeX = getWidth() - badgeWidth - 14;
            int badgeY = hasActionButton() ? getHeight() - 74 : getHeight() - 38;

            g2d.setColor(getBadgeFillColor());
            g2d.fillRoundRect(badgeX, badgeY, badgeWidth, 24, 12, 12);
            g2d.setColor(getBadgeBorderColor());
            g2d.drawRoundRect(badgeX, badgeY, badgeWidth, 24, 12, 12);
            g2d.setColor(TEXT_PRIMARY);
            int badgeTextX = badgeX + ((badgeWidth - badgeMetrics.stringWidth(badgeText)) / 2);
            int badgeTextY = badgeY + ((24 - badgeMetrics.getHeight()) / 2) + badgeMetrics.getAscent();
            g2d.drawString(badgeText, badgeTextX, badgeTextY);
        }

        paintActionButton(g2d);

        g2d.dispose();
    }

    /**
     * Dessine le CTA intégré au bas de la carte quand il existe.
     */
    private void paintActionButton(Graphics2D g2d) {
        if (!hasActionButton()) {
            return;
        }

        Rectangle actionBounds = getActionButtonBounds();
        if (actionBounds == null) {
            return;
        }

        Color fill = !actionEnabled
                ? ACTION_FILL_DISABLED
                : actionHovered
                ? ACTION_FILL_HOVER
                : ACTION_FILL;

        g2d.setColor(fill);
        g2d.fillRoundRect(actionBounds.x, actionBounds.y, actionBounds.width, actionBounds.height, 14, 14);
        g2d.setColor(ACTION_BORDER);
        g2d.drawRoundRect(actionBounds.x, actionBounds.y, actionBounds.width, actionBounds.height, 14, 14);

        g2d.setFont(bodyFont);
        java.awt.FontMetrics metrics = g2d.getFontMetrics();
        g2d.setColor(TEXT_PRIMARY);
        int textX = actionBounds.x + ((actionBounds.width - metrics.stringWidth(actionLabel)) / 2);
        int textY = actionBounds.y + ((actionBounds.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g2d.drawString(actionLabel, textX, textY);
    }

    /**
     * On dit si cette carte doit afficher un bloc de détail supplémentaire.
     */
    private boolean hasDetailLabel() {
        return !detailLabel.isBlank();
    }

    /**
     * Indique si cette carte embarque un CTA visible.
     */
    private boolean hasActionButton() {
        return !actionLabel.isBlank();
    }

    /**
     * Le pont utilise un sprite plus massif que les autres objets.
     * On le réduit un peu dans la carte centrale pour garder un meilleur équilibre visuel.
     */
    private int getCardArtPixelSize() {
        if (product instanceof Facility && ((Facility) product).getType() == FacilityType.PONT) {
            return 11;
        }
        return 7;
    }

    /**
     * On choisit la couleur de fond du badge selon son message.
     */
    private Color getBadgeFillColor() {
        return "Bois requis".equals(badgeText) ? BADGE_FILL_ERROR : BADGE_FILL_DEFAULT;
    }

    /**
     * On choisit la couleur de bordure du badge selon son message.
     */
    private Color getBadgeBorderColor() {
        return "Bois requis".equals(badgeText) ? BADGE_BORDER_ERROR : BADGE_BORDER_DEFAULT;
    }

    /**
     * Découpe le texte d'aide sur les retours à la ligne demandés par la boutique.
     */
    private String[] getDetailLines() {
        return detailLabel.split("\\n");
    }

    /**
     * Calcule la zone cliquable du CTA intégré.
     */
    private Rectangle getActionButtonBounds() {
        if (!hasActionButton()) {
            return null;
        }

        int horizontalInset = 14;
        int buttonHeight = 32;
        int bottomInset = 14;
        return new Rectangle(
                horizontalInset,
                getHeight() - buttonHeight - bottomInset,
                getWidth() - (horizontalInset * 2),
                buttonHeight
        );
    }

    /**
     * On teste si la souris est bien dans la zone du CTA intégré.
     */
    private boolean isInsideActionButton(MouseEvent event) {
        Rectangle actionBounds = getActionButtonBounds();
        return actionBounds != null && actionBounds.contains(event.getPoint());
    }

    /**
     * Une carte avec plusieurs lignes d'aide doit être un peu plus haute,
     * sinon le prix et le stock viennent se coller au badge du panier.
     */
    private int getCardHeight() {
        int actionHeight = hasActionButton() ? 46 : 0;
        if (!hasDetailLabel()) {
            return 154 + actionHeight;
        }
        return 162 + (getDetailLines().length * 18) + actionHeight;
    }
}
