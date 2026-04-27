package view.workshop;

import model.management.Inventaire;
import model.runtime.GamePauseController;
import model.shop.Facility;
import model.shop.FacilityType;
import model.workshop.WorkshopConstructionManager;
import view.CustomFontLoader;
import view.ImageLoader;
import view.shop.ProductCardView;
import view.shop.ProductPreview;
import view.shop.ShopPixelButton;
import view.shop.ShopScrollBarUI;
import view.shop.ShopSectionPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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
    // Libellé du stock de bois dans la colonne de droite.
    private final JLabel woodValueLabel;
    // Libellé d'information de stock dans la colonne de gauche.
    private final JLabel infoWoodLabel;
    // Libellé d'état du chantier dans la colonne de gauche.
    private final JLabel infoQueueLabel;
    // Nom du plan sélectionné.
    private final JLabel selectedNameLabel;
    // Coût du plan sélectionné.
    private final JLabel selectedCostLabel;
    // Nombre de ponts déjà présents dans l'inventaire.
    private final JLabel selectedBridgeStockLabel;
    // État général du chantier en cours.
    private final JLabel constructionStateLabel;
    // Temps restant ou durée totale affichée.
    private final JLabel remainingTimeLabel;
    // Message court affiché en bas de la colonne de droite.
    private final JLabel messageLabel;
    // Aperçu visuel du plan sélectionné.
    private final ProductPreview productPreview;
    // Bouton principal qui lance la fabrication.
    private final ShopPixelButton constructButton;
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
        this.woodValueLabel = createPrimaryLabel(priceFont);
        this.infoWoodLabel = createSecondaryLabel(bodyFont);
        this.infoQueueLabel = createSecondaryLabel(bodyFont);
        this.selectedNameLabel = createPrimaryLabel(priceFont);
        this.selectedCostLabel = createPrimaryLabel(bodyFont);
        this.selectedBridgeStockLabel = createSecondaryLabel(bodyFont);
        this.constructionStateLabel = createPrimaryLabel(bodyFont);
        this.remainingTimeLabel = createSecondaryLabel(bodyFont);
        this.messageLabel = createSecondaryLabel(bodyFont);
        this.productPreview = new ProductPreview();
        this.constructButton = new ShopPixelButton(
                "Construire",
                bodyFont,
                ACCENT,
                ACCENT_HOVER,
                new Color(37, 72, 92),
                TEXT_PRIMARY
        );
        this.refreshTimer = new Timer(250, event -> syncFromState());

        setOpaque(false);
        setVisible(false);
        setFocusable(true);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        add(buildContent(), BorderLayout.CENTER);
        wireActions();
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
     * On construit les trois grandes colonnes de la menuiserie.
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
        columns.add(buildCatalogPanel(), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        columns.add(buildSummaryPanel(), gbc);
        return columns;
    }

    /**
     * On construit la colonne d'information de gauche.
     */
    private JComponent buildInfoPanel() {
        ShopSectionPanel infoPanel = new ShopSectionPanel(woodTexture);
        infoPanel.setPreferredSize(new Dimension(286, 0));
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Atelier");

        JLabel queueTitle = createSecondaryLabel(subtitleFont);
        queueTitle.setText("CHANTIER");

        content.add(title);
        content.add(Box.createVerticalStrut(10));
        content.add(buildWoodStockPanel());
        content.add(Box.createVerticalStrut(18));
        content.add(createLeftAlignedRow(queueTitle));
        content.add(Box.createVerticalStrut(6));
        content.add(createLeftAlignedRow(infoQueueLabel));
        content.add(Box.createVerticalGlue());

        infoPanel.add(content, BorderLayout.CENTER);
        return infoPanel;
    }

    /**
     * On construit la colonne centrale qui liste les plans disponibles.
     */
    private JComponent buildCatalogPanel() {
        ShopSectionPanel catalogPanel = new ShopSectionPanel(woodTexture);
        catalogPanel.setLayout(new BorderLayout(0, 16));
        catalogPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Plans disponibles");

        JLabel subtitle = createSecondaryLabel(bodyFont);
        subtitle.setText("1 construction");
        subtitle.setHorizontalAlignment(SwingConstants.RIGHT);

        topRow.add(title, BorderLayout.WEST);
        topRow.add(subtitle, BorderLayout.EAST);

        JScrollPane scrollPane = createScrollPane(productsGrid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(24);

        catalogPanel.add(topRow, BorderLayout.NORTH);
        catalogPanel.add(scrollPane, BorderLayout.CENTER);
        return catalogPanel;
    }

    /**
     * On construit la colonne de droite avec la sélection, le chantier et l'action principale.
     */
    private JComponent buildSummaryPanel() {
        ShopSectionPanel summaryPanel = new ShopSectionPanel(woodTexture);
        summaryPanel.setPreferredSize(new Dimension(350, 0));
        summaryPanel.setLayout(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel topBlock = new JPanel();
        topBlock.setOpaque(false);
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));

        JLabel woodTitle = createSecondaryLabel(subtitleFont);
        woodTitle.setText("STOCK BOIS");
        woodTitle.setAlignmentX(LEFT_ALIGNMENT);
        woodTitle.setHorizontalAlignment(SwingConstants.LEFT);
        woodValueLabel.setAlignmentX(LEFT_ALIGNMENT);
        woodValueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        topBlock.add(createLeftAlignedRow(woodTitle));
        topBlock.add(Box.createVerticalStrut(4));
        topBlock.add(createLeftAlignedRow(woodValueLabel));
        topBlock.add(Box.createVerticalStrut(16));
        topBlock.add(buildSelectionPanel());
        topBlock.add(Box.createVerticalStrut(16));

        JLabel chantierTitle = createPrimaryLabel(priceFont);
        chantierTitle.setText("Chantier");
        chantierTitle.setAlignmentX(LEFT_ALIGNMENT);
        chantierTitle.setHorizontalAlignment(SwingConstants.LEFT);
        constructionStateLabel.setHorizontalAlignment(SwingConstants.LEFT);
        remainingTimeLabel.setHorizontalAlignment(SwingConstants.LEFT);

        topBlock.add(createLeftAlignedRow(chantierTitle));
        topBlock.add(Box.createVerticalStrut(8));
        topBlock.add(createLeftAlignedRow(constructionStateLabel));
        topBlock.add(Box.createVerticalStrut(6));
        topBlock.add(createLeftAlignedRow(remainingTimeLabel));

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.add(Box.createVerticalStrut(12));
        footer.add(constructButton);
        footer.add(Box.createVerticalStrut(10));
        footer.add(messageLabel);

        summaryPanel.add(topBlock, BorderLayout.NORTH);
        summaryPanel.add(Box.createGlue(), BorderLayout.CENTER);
        summaryPanel.add(footer, BorderLayout.SOUTH);
        return summaryPanel;
    }

    /**
     * On construit le petit panneau qui décrit le plan actuellement affiché.
     */
    private JComponent buildSelectionPanel() {
        ShopSectionPanel selectionPanel = new ShopSectionPanel(woodTexture);
        selectionPanel.setLayout(new BorderLayout(0, 14));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(productPreview, BorderLayout.WEST);

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(selectedNameLabel);
        meta.add(Box.createVerticalStrut(6));
        meta.add(selectedCostLabel);
        meta.add(Box.createVerticalStrut(4));
        meta.add(selectedBridgeStockLabel);

        top.add(meta, BorderLayout.CENTER);
        selectionPanel.add(top, BorderLayout.NORTH);
        return selectionPanel;
    }

    /**
     * On branche ici les clics principaux de la menuiserie.
     */
    private void wireActions() {
        constructButton.addActionListener(event -> startBridgeConstruction());
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
        refreshSummary();
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
                ? toHtmlLines("Pont en", "fabrication")
                : toHtmlLines("Aucune construction", "active"));
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
                this::selectBridgeProduct
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
     * On met à jour les valeurs affichées dans la colonne de droite.
     */
    private void refreshSummary() {
        int woodQuantity = inventaire.getQuantiteBois();
        int bridgeQuantity = inventaire.getQuantiteInstallation(FacilityType.PONT);
        boolean constructionInProgress = constructionManager.isConstructionInProgress();

        woodValueLabel.setText(woodQuantity + " unités");
        productPreview.setProduct(bridgeProduct);
        selectedNameLabel.setText(bridgeProduct.getName());
        selectedCostLabel.setText("Coût : " + formatWoodUnits(constructionManager.getBridgeWoodCost()));
        selectedBridgeStockLabel.setText("Ponts en inventaire : " + bridgeQuantity);

        constructionStateLabel.setText(constructionInProgress ? "Construction active" : "Prêt à lancer");
        remainingTimeLabel.setText(constructionInProgress
                ? "Temps restant : " + WorkshopConstructionManager.formatDuration(constructionManager.getRemainingConstructionMs())
                : "Durée d'un pont : " + WorkshopConstructionManager.formatDuration(constructionManager.getBridgeDurationMs()));

        constructButton.setText(constructionInProgress ? "Construction en cours" : "Construire");
        constructButton.setEnabled(!constructionInProgress && inventaire.possedeBois(constructionManager.getBridgeWoodCost()));
    }

    /**
     * On force simplement la sélection visuelle du plan de pont.
     */
    private void selectBridgeProduct() {
        productPreview.setProduct(bridgeProduct);
        repaint();
    }

    /**
     * Uniformise l'affichage des coûts en bois dans toute la menuiserie.
     */
    private String formatWoodUnits(int quantity) {
        return quantity + " unités de bois";
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
        if (constructionManager.isConstructionInProgress()) {
            return "Permet de franchir la rivière\nChantier en cours";
        }
        return "Permet de franchir la rivière";
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
     * On affiche ou masque le message d'état en bas de la colonne de droite.
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
        ShopSectionPanel stockPanel = new ShopSectionPanel(woodTexture);
        stockPanel.setLayout(new BorderLayout(12, 0));
        stockPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        stockPanel.setAlignmentX(LEFT_ALIGNMENT);
        stockPanel.setPreferredSize(new Dimension(250, 154));
        stockPanel.setMinimumSize(new Dimension(250, 154));
        stockPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 154));
        stockPanel.add(woodPreviewPanel, BorderLayout.WEST);

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));

        JLabel stockTitle = createSecondaryLabel(subtitleFont);
        stockTitle.setText("STOCK BOIS");
        stockTitle.setAlignmentX(LEFT_ALIGNMENT);
        infoWoodLabel.setAlignmentX(LEFT_ALIGNMENT);

        meta.add(stockTitle);
        meta.add(Box.createVerticalStrut(10));
        meta.add(infoWoodLabel);

        stockPanel.add(meta, BorderLayout.CENTER);
        return stockPanel;
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
     * On crée un label secondaire déjà formaté sur plusieurs lignes HTML.
     */
    private JLabel createWrappedSecondaryLabel(Font font, String... lines) {
        JLabel label = createSecondaryLabel(font);
        label.setText(toHtmlLines(lines));
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

    /**
     * On crée un scroll transparent avec la scrollbar visuelle partagée avec la boutique.
     */
    private JScrollPane createScrollPane(JComponent content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ShopScrollBarUI());
        return scrollPane;
    }
}
