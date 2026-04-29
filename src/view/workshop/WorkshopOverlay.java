package view.workshop;

import model.management.Inventaire;
import model.runtime.GamePauseController;
import model.shop.Facility;
import model.shop.FacilityType;
import model.workshop.WorkshopConstructionManager;
import view.CustomFontLoader;
import view.ImageLoader;
import view.shop.ProductCardView;
import view.shop.ShopOverlayUiFactory;
import view.shop.ShopPixelButton;
import view.shop.ShopSectionPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;

/**
 * Overlay dédié à la menuiserie.
 * On y retrouve la même base visuelle que la boutique,
 * mais recentrée sur les constructions et sur l'état du chantier.
 */
public class WorkshopOverlay extends JPanel {
    // Chemin de la police pixel utilisée par la menuiserie.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // Couleur principale des textes importants.
    private static final Color TEXT_PRIMARY = new Color(255, 248, 226);
    // Couleur des textes secondaires.
    private static final Color TEXT_SECONDARY = new Color(216, 199, 164);
    // Couleur des textes peu prioritaires.
    private static final Color TEXT_MUTED = new Color(169, 151, 124);
    // Couleur d'accent du bouton principal.
    private static final Color ACCENT = new Color(92, 166, 196);
    // Variante de survol de la couleur d'accent.
    private static final Color ACCENT_HOVER = new Color(116, 191, 221);
    // Couleur des messages de succès.
    private static final Color SUCCESS = new Color(163, 216, 130);
    // Couleur des messages d'erreur.
    private static final Color ERROR = new Color(255, 151, 126);

    // Inventaire sur lequel on lit le bois et les ponts fabriqués.
    private final Inventaire inventaire;
    // Manager qui pilote la fabrication réelle du pont.
    private final WorkshopConstructionManager constructionManager;
    // Composant auquel on rend le focus quand on ferme la menuiserie.
    private final JComponent focusReturnTarget;
    // Contrôleur global de pause partagé avec le reste du jeu.
    private final GamePauseController pauseController;
    // Texture bois légère utilisée dans l'habillage.
    private final Image woodTexture;
    // Produit factice utilisé pour réutiliser la carte produit de la boutique.
    private final Facility bridgeProduct;

    // Police du grand titre.
    private final Font titleFont;
    // Police des petits sous-titres.
    private final Font subtitleFont;
    // Police des petits libellés.
    private final Font labelFont;
    // Police du corps de texte.
    private final Font bodyFont;
    // Police des valeurs importantes.
    private final Font priceFont;

    // Grille centrale qui reçoit les cartes de plans disponibles.
    private final JPanel productsGrid;
    // Aperçu visuel dédié à la ressource bois.
    private final WoodPreviewPanel woodPreviewPanel;
    // Libellé d'information de stock dans la colonne de gauche.
    private final JLabel infoWoodLabel;
    // Libellé d'état du chantier dans la colonne de gauche.
    private final JLabel infoQueueLabel;
    // Temps restant ou durée totale affichée.
    private final JLabel remainingTimeLabel;
    // Message court affiché dans la colonne de gauche.
    private final JLabel messageLabel;
    // Timer de rafraîchissement périodique de l'overlay.
    private final Timer refreshTimer;

    // Opacité globale de l'overlay.
    private float overlayAlpha;
    // Mémorise l'ancien état du chantier pour détecter une fin de fabrication.
    private boolean wasConstructionInProgress;

    /**
     * On prépare toute l'interface de la menuiserie une bonne fois pour toutes.
     */
    public WorkshopOverlay(Inventaire inventaire, WorkshopConstructionManager constructionManager, JComponent focusReturnTarget) {
        this.inventaire = inventaire;
        this.constructionManager = constructionManager;
        this.focusReturnTarget = focusReturnTarget;
        this.pauseController = GamePauseController.getInstance();
        this.woodTexture = ImageLoader.load("/assets/bois.png");
        this.bridgeProduct = new Facility("Pont", 0, Integer.MAX_VALUE, FacilityType.PONT);

        this.titleFont = CustomFontLoader.loadFont(FONT_PATH, 26.0f);
        this.subtitleFont = CustomFontLoader.loadFont(FONT_PATH, 11.0f);
        this.labelFont = CustomFontLoader.loadFont(FONT_PATH, 10.0f);
        this.bodyFont = CustomFontLoader.loadFont(FONT_PATH, 13.0f);
        this.priceFont = CustomFontLoader.loadFont(FONT_PATH, 16.0f);

        this.productsGrid = new JPanel(new GridBagLayout());
        this.productsGrid.setOpaque(false);

        this.woodPreviewPanel = new WoodPreviewPanel();
        this.infoWoodLabel = createSecondaryLabel(bodyFont);
        this.infoQueueLabel = createSecondaryLabel(bodyFont);
        this.remainingTimeLabel = createSecondaryLabel(bodyFont);
        this.messageLabel = createSecondaryLabel(bodyFont);
        this.refreshTimer = new Timer(250, event -> syncFromState());

        setOpaque(false);
        setVisible(false);
        setFocusable(true);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        add(buildContent(), BorderLayout.CENTER);
        setMessage(null, TEXT_MUTED);
    }

    /**
     * On ouvre la menuiserie, on met le jeu en pause et on lance le rafraîchissement périodique.
     */
    public void openWorkshop() {
        if (isVisible()) {
            requestFocusInWindow();
            return;
        }

        pauseController.setPaused(true);
        overlayAlpha = 1f;
        wasConstructionInProgress = constructionManager.isConstructionInProgress();
        setMessage(null, TEXT_MUTED);
        syncFromState();
        refreshTimer.start();
        setVisible(true);
        requestFocusInWindow();
    }

    /**
     * On ferme la menuiserie, on coupe le rafraîchissement puis on rend le focus au jeu.
     */
    public void closeWorkshop() {
        if (!isVisible()) {
            return;
        }

        refreshTimer.stop();
        overlayAlpha = 0f;
        setVisible(false);
        pauseController.setPaused(false);
        if (focusReturnTarget != null) {
            focusReturnTarget.requestFocusInWindow();
        }
    }

    /**
     * On applique ici l'opacité globale de l'overlay à tout son sous-arbre Swing.
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, overlayAlpha))));
        super.paint(g2d);
        g2d.dispose();
    }

    /**
     * On dessine le fond d'ambiance de la menuiserie derrière tous les composants.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setPaint(new GradientPaint(
                0, 0, new Color(15, 13, 14, 236),
                0, getHeight(), new Color(28, 33, 39, 246)
        ));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (woodTexture != null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.06f));
            g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        g2d.setColor(new Color(67, 135, 165, 34));
        g2d.fillOval(-180, -140, 540, 320);
        g2d.setColor(new Color(203, 157, 87, 28));
        g2d.fillOval(getWidth() - 360, getHeight() - 300, 560, 320);
        g2d.dispose();
    }

    /**
     * On assemble la structure racine de l'overlay : en-tête puis zone de travail.
     */
    private JComponent buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildColumns(), BorderLayout.CENTER);
        return root;
    }

    /**
     * On construit la barre haute avec le titre de la menuiserie et le bouton de fermeture.
     */
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = createPrimaryLabel(titleFont);
        title.setText("Menuiserie");

        ShopPixelButton closeButton = new ShopPixelButton(
                "X",
                labelFont,
                new Color(86, 59, 39),
                new Color(112, 74, 49),
                new Color(52, 33, 20),
                TEXT_PRIMARY
        );
        closeButton.setPreferredSize(new Dimension(40, 32));
        closeButton.setMinimumSize(new Dimension(40, 32));
        closeButton.setMaximumSize(new Dimension(40, 32));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.addActionListener(event -> closeWorkshop());

        header.add(title, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);
        return header;
    }

    /**
     * On construit les deux colonnes de la menuiserie.
     */
    private JComponent buildColumns() {
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 18);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        gbc.gridx = 0;
        gbc.weightx = 0.0;
        columns.add(buildInfoPanel(), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        columns.add(buildCatalogPanel(), gbc);
        return columns;
    }

    /**
     * On construit la colonne d'information de gauche.
     */
    private JComponent buildInfoPanel() {
        ShopSectionPanel infoPanel = createWorkshopSectionPanel(new BorderLayout(), new Insets(18, 18, 18, 18), new Dimension(286, 0));
        JPanel content = createTransparentVerticalPanel();

        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Atelier");
        alignLeft(infoQueueLabel);
        alignLeft(remainingTimeLabel);
        alignLeft(messageLabel);

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(buildWoodStockPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createLeftAlignedGroup(new int[] {6, 8}, infoQueueLabel, remainingTimeLabel, messageLabel));
        content.add(Box.createVerticalGlue());

        infoPanel.add(content, BorderLayout.CENTER);
        return infoPanel;
    }

    /**
     * On construit la colonne centrale qui liste les plans disponibles.
     */
    private JComponent buildCatalogPanel() {
        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Plans disponibles");

        JLabel subtitle = createSecondaryLabel(bodyFont);
        subtitle.setText("1 construction");
        return ShopOverlayUiFactory.createCatalogSectionPanel(
                woodTexture,
                title,
                subtitle,
                productsGrid,
                24
        );
    }

    /**
     * On relit l'état du chantier puis on rafraîchit toutes les zones visibles de l'overlay.
     */
    private void syncFromState() {
        boolean constructionInProgress = constructionManager.isConstructionInProgress();
        if (wasConstructionInProgress && !constructionInProgress) {
            setMessage("Pont terminé. L'objet est ajouté à l'inventaire.", SUCCESS);
        }
        wasConstructionInProgress = constructionInProgress;

        refreshInfoPanel();
        refreshCatalog();
        revalidate();
        repaint();
    }

    /**
     * On met à jour la colonne d'information de gauche.
     */
    private void refreshInfoPanel() {
        int woodQuantity = inventaire.getQuantiteBois();
        infoWoodLabel.setText(toHtmlLines(woodQuantity + " unites", "disponibles"));
        infoQueueLabel.setText(constructionManager.isConstructionInProgress()
                ? "Pont en fabrication"
                : "Atelier prêt");
        remainingTimeLabel.setText(constructionManager.isConstructionInProgress()
                ? "Temps restant : " + WorkshopConstructionManager.formatDuration(constructionManager.getRemainingConstructionMs())
                : "Durée d'un pont : " + WorkshopConstructionManager.formatDuration(constructionManager.getBridgeDurationMs()));
    }

    /**
     * On reconstruit la liste des plans disponibles dans la colonne centrale.
     */
    private void refreshCatalog() {
        productsGrid.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        productsGrid.add(new ProductCardView(
                bridgeProduct,
                "Construction",
                getBridgeCardDetailLabel(),
                formatWoodUnits(constructionManager.getBridgeWoodCost()),
                "Duree : " + getFormattedDuration(),
                getBridgeCardBadgeText(),
                true,
                woodTexture,
                labelFont,
                priceFont,
                bodyFont,
                null,
                getBridgeActionLabel(),
                isBridgeActionEnabled(),
                this::startBridgeConstruction
        ), gbc);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 1;
        filler.weightx = 1.0;
        filler.weighty = 1.0;
        filler.fill = GridBagConstraints.BOTH;
        productsGrid.add(Box.createGlue(), filler);
    }

    /**
     * Uniformise l'affichage des coûts en bois dans toute la menuiserie.
     */
    private String formatWoodUnits(int quantity) {
        return quantity + " unités de bois";
    }

    /**
     * Libellé du CTA directement intégré à la carte du pont.
     */
    private String getBridgeActionLabel() {
        if (constructionManager.isConstructionInProgress()) {
            return "Construction en cours";
        }
        if (!inventaire.possedeBois(constructionManager.getBridgeWoodCost())) {
            return "Bois insuffisant";
        }
        return "Construire";
    }

    /**
     * Le CTA n'est actif que si l'atelier est libre et que le bois suffit.
     */
    private boolean isBridgeActionEnabled() {
        return !constructionManager.isConstructionInProgress()
                && inventaire.possedeBois(constructionManager.getBridgeWoodCost());
    }

    /**
     * On tente de lancer une nouvelle fabrication de pont.
     */
    private void startBridgeConstruction() {
        if (constructionManager.isConstructionInProgress()) {
            setMessage("La menuiserie est déjà occupée par un pont.", ERROR);
            syncFromState();
            return;
        }

        if (!inventaire.possedeBois(constructionManager.getBridgeWoodCost())) {
            setMessage("Bois insuffisant pour lancer la construction.", ERROR);
            syncFromState();
            return;
        }

        if (constructionManager.startBridgeConstruction()) {
            setMessage("Le chantier n'a pas pu être lancé.", ERROR);
            syncFromState();
            return;
        }

        wasConstructionInProgress = true;
        setMessage(
                "Pont lancé pour " + WorkshopConstructionManager.formatDuration(constructionManager.getBridgeDurationMs()) + ".",
                SUCCESS
        );
        syncFromState();
    }

    /**
     * On renvoie le texte d'aide court affiché sur la carte du pont.
     */
    private String getBridgeCardDetailLabel() {
        int bridgeQuantity = inventaire.getQuantiteInstallation(FacilityType.PONT);
        if (constructionManager.isConstructionInProgress()) {
            return "Permet de franchir la rivière\nPonts en inventaire : " + bridgeQuantity;
        }
        return "Permet de franchir la rivière\nPonts en inventaire : " + bridgeQuantity;
    }

    /**
     * On renvoie le badge court affiché sur la carte du pont.
     */
    private String getBridgeCardBadgeText() {
        if (constructionManager.isConstructionInProgress()) {
            return "En cours";
        }
        if (inventaire.possedeBois(constructionManager.getBridgeWoodCost())) {
            return "Prêt";
        }
        return "Bois requis";
    }

    /**
     * On renvoie la durée pertinente à afficher selon qu'un chantier est actif ou non.
     */
    private String getFormattedDuration() {
        long durationMs = constructionManager.isConstructionInProgress()
                ? constructionManager.getRemainingConstructionMs()
                : constructionManager.getBridgeDurationMs();
        return WorkshopConstructionManager.formatDuration(durationMs);
    }

    /**
     * On affiche ou masque le message d'état dans la colonne de gauche.
     */
    private void setMessage(String text, Color color) {
        boolean hasMessage = text != null && !text.isBlank();
        messageLabel.setText(hasMessage ? text : "");
        messageLabel.setForeground(color);
        messageLabel.setVisible(hasMessage);
    }

    /**
     * On construit le bloc visuel qui montre le stock de bois disponible.
     */
    private JComponent buildWoodStockPanel() {
        ShopSectionPanel stockPanel = createWorkshopSectionPanel(new BorderLayout(12, 0), new Insets(12, 12, 12, 12), new Dimension(250, 154));
        stockPanel.setAlignmentX(LEFT_ALIGNMENT);
        stockPanel.setMinimumSize(new Dimension(250, 154));
        stockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 154));
        JPanel meta = createTransparentVerticalPanel();

        JLabel stockTitle = createLeftAlignedSecondaryLabel(subtitleFont);
        infoWoodLabel.setAlignmentX(LEFT_ALIGNMENT);

        meta.add(stockTitle);
        meta.add(Box.createVerticalStrut(10));
        meta.add(infoWoodLabel);

        stockPanel.add(createPreviewMetaPanel(woodPreviewPanel, meta), BorderLayout.CENTER);
        return stockPanel;
    }

    /**
     * Crée l'habillage standard des sections de la menuiserie.
     * Les trois colonnes et les encarts internes partagent la même matière visuelle,
     * puis chaque méthode se concentre seulement sur son contenu propre.
     */
    private ShopSectionPanel createWorkshopSectionPanel(LayoutManager layout, Insets padding, Dimension preferredSize) {
        ShopSectionPanel panel = new ShopSectionPanel(woodTexture);
        panel.setLayout(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));
        if (preferredSize != null) {
            panel.setPreferredSize(preferredSize);
        }
        return panel;
    }

    /**
     * Crée une pile verticale transparente.
     * Ce format revient partout dans l'overlay dès qu'on empile du texte ou plusieurs contrôles.
     */
    private JPanel createTransparentVerticalPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    /**
     * Assemble un aperçu à gauche et son bloc descriptif à droite.
     * Cela garde le même squelette visuel pour le panneau de sélection et pour le stock de bois.
     */
    private JPanel createPreviewMetaPanel(JComponent preview, JComponent metaPanel) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.add(preview, BorderLayout.WEST);
        panel.add(metaPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * On force un composant à rester bien ancré à gauche dans une pile verticale.
     */
    private JComponent createLeftAlignedRow(JComponent component) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(component, BorderLayout.WEST);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return row;
    }

    /**
     * Empile plusieurs lignes déjà alignées à gauche avec des espacements définis entre elles.
     * On l'utilise pour les petits blocs de statut afin d'éviter de répéter la même séquence
     * "ligne + espace + ligne" dans chaque colonne.
     */
    private JComponent createLeftAlignedGroup(int[] gapsAfterRows, JComponent... components) {
        JPanel group = createTransparentVerticalPanel();
        for (int index = 0; index < components.length; index++) {
            group.add(createLeftAlignedRow(components[index]));
            if (index < gapsAfterRows.length) {
                group.add(Box.createVerticalStrut(gapsAfterRows[index]));
            }
        }
        return group;
    }

    /**
     * Positionne explicitement un label sur la gauche.
     * Les BoxLayout gardent ainsi un alignement cohérent même quand les polices changent.
     */
    private void alignLeft(JLabel label) {
        label.setAlignmentX(LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
    }

    /**
     * Variante pratique pour créer un titre secondaire déjà calé à gauche.
     */
    private JLabel createLeftAlignedSecondaryLabel(Font font) {
        JLabel label = createSecondaryLabel(font);
        label.setText("STOCK BOIS");
        alignLeft(label);
        return label;
    }

    /**
     * On crée un label au style principal de la menuiserie.
     */
    private JLabel createPrimaryLabel(Font font) {
        JLabel label = new JLabel();
        label.setForeground(TEXT_PRIMARY);
        label.setFont(font);
        return label;
    }

    /**
     * On crée un label au style secondaire de la menuiserie.
     */
    private JLabel createSecondaryLabel(Font font) {
        JLabel label = new JLabel();
        label.setForeground(TEXT_SECONDARY);
        label.setFont(font);
        return label;
    }

    /**
     * On transforme plusieurs lignes logiques en petit bloc HTML Swing.
     */
    private String toHtmlLines(String... lines) {
        StringBuilder builder = new StringBuilder("<html>");
        for (int index = 0; index < lines.length; index++) {
            if (index > 0) {
                builder.append("<br>");
            }
            builder.append(lines[index]);
        }
        builder.append("</html>");
        return builder.toString();
    }

}
