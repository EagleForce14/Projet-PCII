package view.shop;

import model.management.Inventaire;
import model.management.Money;
import model.runtime.GamePauseController;
import model.shop.CartItem;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import model.shop.Seed;
import model.shop.Shop;
import view.CustomFontLoader;
import view.ImageLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Classe permettant d'afficher la boutique en plein ecran dans le jeu.
 * Elle gêle l'état actuel du jeu tant qu'elle reste ouverte.

 * L'idée générale:
 * - une colonne fine pour les filtres,
 * - une grande zone centrale pour le catalogue,
 * - une colonne droite pour la selection courante et le panier.
 */
public class ShopOverlay extends JPanel {
    // Chemin de la police pixel utilisée par la boutique.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    // Couleur principale des textes importants.
    private static final Color TEXT_PRIMARY = new Color(255, 248, 226);
    // Couleur des textes secondaires.
    private static final Color TEXT_SECONDARY = new Color(216, 199, 164);
    // Couleur des textes peu prioritaires.
    private static final Color TEXT_MUTED = new Color(169, 151, 124);
    // Couleur d'accent des boutons d'action.
    private static final Color ACCENT = new Color(216, 181, 96);
    // Variante de survol de la couleur d'accent.
    private static final Color ACCENT_HOVER = new Color(236, 202, 114);
    // Couleur des messages de succès.
    private static final Color SUCCESS = new Color(163, 216, 130);
    // Couleur des messages d'erreur.
    private static final Color ERROR = new Color(255, 151, 126);

    // Boutique réelle que l'overlay pilote.
    private final Shop shop;
    // Argent du joueur affiché et débité au moment du paiement.
    private final Money playerMoney;
    // Inventaire du joueur à remplir lors des achats validés.
    private final Inventaire inventaire;
    // Composant auquel on rend le focus quand on ferme la boutique.
    private final JComponent focusReturnTarget;
    // Contrôleur global de pause partagé avec le reste du jeu.
    private final GamePauseController pauseController;
    // Texture bois légère utilisée dans tout l'habillage.
    private final Image woodTexture;

    // Police du grand titre.
    private final Font titleFont;
    // Police des petits sous-titres en capitales.
    private final Font subtitleFont;
    // Police des petits boutons et labels compacts.
    private final Font labelFont;
    // Police du corps de texte.
    private final Font bodyFont;
    // Police des prix et valeurs importantes.
    private final Font priceFont;

    // Ces deux panneaux sont les deux zones qui bougent le plus souvent.
    // On les garde en references pour pouvoir les reconstruire facilement.
    private final JPanel productsGrid;
    private final JPanel cartItemsPanel;
    // Libellé qui affiche combien d'articles sont visibles dans le catalogue.
    private final JLabel catalogCountLabel;
    // Libellé qui affiche le solde courant du joueur.
    private final JLabel balanceValueLabel;
    // Libellé du nom de l'article sélectionné.
    private final JLabel selectedNameLabel;
    // Libellé de catégorie de l'article sélectionné.
    private final JLabel selectedMetaLabel;
    // Libellé de prix de l'article sélectionné.
    private final JLabel selectedPriceLabel;
    // Libellé de stock disponible de l'article sélectionné.
    private final JLabel selectedStockLabel;
    // Libellé qui rappelle combien d'exemplaires sont déjà dans le panier.
    private final JLabel selectedCartLabel;
    // Libellé numérique de la quantité voulue.
    private final JLabel desiredQuantityLabel;
    // Libellé du total du panier.
    private final JLabel totalValueLabel;
    // Zone de message courte affichée en bas à droite.
    private final JLabel messageLabel;
    // Le panneau de droite a volontairement son propre petit focus visuel:
    // une preview plus grande de l'article selectionné.
    private final ProductPreview productPreview;
    // Bouton qui ajoute la sélection courante au panier.
    private final ShopPixelButton addToCartButton;
    // Bouton qui baisse la quantité désirée.
    private final ShopPixelButton decreaseDesiredButton;
    // Bouton qui augmente la quantité désirée.
    private final ShopPixelButton increaseDesiredButton;
    // Bouton qui vide complètement le panier.
    private final ShopPixelButton clearCartButton;
    // Bouton qui valide l'achat du panier.
    private final ShopPixelButton checkoutButton;
    // Table des boutons de filtre affichés à gauche.
    private final Map<ShopFilterCategory, ShopFilterChip> filterButtons;

    // État local purement visuel de la boutique.
    // Filtre actuellement actif dans le catalogue.
    private ShopFilterCategory activeFilter = ShopFilterCategory.ALL;
    // Produit actuellement sélectionné dans le catalogue.
    private Product selectedProduct;
    // Quantité actuellement demandée pour le produit sélectionné.
    private int desiredQuantity = 0;
    // Opacité globale de l'overlay.
    private float overlayAlpha = 0f;

    /**
     * On prépare toute l'interface de boutique une bonne fois pour toutes.
     */
    public ShopOverlay(Shop shop, Money playerMoney, Inventaire inventaire, JComponent focusReturnTarget) {
        this.shop = shop;
        this.playerMoney = playerMoney;
        this.inventaire = inventaire;
        this.focusReturnTarget = focusReturnTarget;
        this.pauseController = GamePauseController.getInstance();
        this.woodTexture = ImageLoader.load("/assets/bois.png");

        // On garde peu de tailles de typo pour que l'ensemble reste calme.
        this.titleFont = CustomFontLoader.loadFont(FONT_PATH, 26.0f);
        this.subtitleFont = CustomFontLoader.loadFont(FONT_PATH, 11.0f);
        this.labelFont = CustomFontLoader.loadFont(FONT_PATH, 10.0f);
        this.bodyFont = CustomFontLoader.loadFont(FONT_PATH, 13.0f);
        this.priceFont = CustomFontLoader.loadFont(FONT_PATH, 16.0f);

        // GridBagLayout est un peu verbeux, mais il nous laisse une vraie grille
        // de cartes sans devoir bricoler les tailles a la main.
        this.productsGrid = new JPanel(new GridBagLayout());
        this.productsGrid.setOpaque(false);

        // Le panier est une pile verticale simple:
        // une ligne par article
        this.cartItemsPanel = new JPanel();
        this.cartItemsPanel.setOpaque(false);
        this.cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));

        this.catalogCountLabel = createSecondaryLabel(bodyFont);
        this.balanceValueLabel = createPrimaryLabel(priceFont);
        this.selectedNameLabel = createPrimaryLabel(priceFont);
        this.selectedMetaLabel = createSecondaryLabel(bodyFont);
        this.selectedPriceLabel = createPrimaryLabel(priceFont);
        this.selectedStockLabel = createSecondaryLabel(bodyFont);
        this.selectedCartLabel = createSecondaryLabel(bodyFont);
        this.desiredQuantityLabel = createPrimaryLabel(priceFont);
        this.totalValueLabel = createPrimaryLabel(priceFont);
        this.messageLabel = createSecondaryLabel(bodyFont);
        this.productPreview = new ProductPreview();

        this.decreaseDesiredButton = createControlButton("-");
        this.increaseDesiredButton = createControlButton("+");
        this.addToCartButton = new ShopPixelButton("Ajouter", bodyFont, ACCENT, ACCENT_HOVER, new Color(60, 42, 17), TEXT_PRIMARY);
        this.clearCartButton = new ShopPixelButton("Vider", bodyFont, new Color(95, 72, 55), new Color(120, 89, 67), new Color(53, 35, 24), TEXT_PRIMARY);
        this.checkoutButton = new ShopPixelButton("Valider", bodyFont, new Color(119, 164, 84), new Color(142, 189, 100), new Color(50, 77, 31), TEXT_PRIMARY);
        this.filterButtons = new EnumMap<>(ShopFilterCategory.class);

        setOpaque(false);
        setVisible(false);
        setFocusable(true);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(22, 24, 24, 24));

        // Toute la boutique vit dans un seul grand conteneur transparent
        // pose sur le glassPane de la fenêtre.
        add(buildContent(), BorderLayout.CENTER);
        wireActions();
        setMessage(null, TEXT_MUTED);
    }

    /**
     * On ouvre la boutique, on met le jeu en pause et on resynchronise tout l'affichage.
     */
    public void openShop() {
        if (isVisible()) {
            requestFocusInWindow();
            return;
        }

        pauseController.setPaused(true);
        setMessage(null, TEXT_MUTED);
        syncFromModel();
        overlayAlpha = 1f;
        setVisible(true);
        requestFocusInWindow();
    }

    /**
     * On ferme la boutique, on relance le jeu puis on rend le focus au terrain.
     */
    public void closeShop() {
        if (!isVisible()) {
            return;
        }

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
        // On passe par paint() plutot que paintComponent() pour que tout le sous-arbre
        // herite de la meme opacite si jamais on remet une animation plus tard.
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.SrcOver.derive(Math.max(0f, Math.min(1f, overlayAlpha))));
        super.paint(g2d);
        g2d.dispose();
    }

    /**
     * On dessine le fond d'ambiance de la boutique derrière tous les composants Swing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fond volontairement sobre:
        // un voile sombre + un peu de texture + deux taches chaudes.
        // Ca donne de la presence sans concurrencer les cartes produit.
        g2d.setPaint(new GradientPaint(
                0, 0, new Color(17, 12, 9, 235),
                0, getHeight(), new Color(36, 27, 20, 245)
        ));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (woodTexture != null) {
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.08f));
            g2d.drawImage(woodTexture, 0, 0, getWidth(), getHeight(), this);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        g2d.setColor(new Color(201, 156, 84, 24));
        g2d.fillOval(-200, -140, 620, 340);
        g2d.fillOval(getWidth() - 320, getHeight() - 280, 520, 300);
        g2d.dispose();
    }

    /**
     * On assemble la structure racine de l'overlay : en-tête puis zone de travail.
     */
    private JComponent buildContent() {
        // Un header, puis la zone de travail. Rien de plus.
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setOpaque(false);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildColumns(), BorderLayout.CENTER);
        return root;
    }

    /**
     * On construit la barre haute avec le titre de la boutique et le bouton de fermeture.
     */
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = createPrimaryLabel(titleFont);
        title.setText(shop.getDisplayName());

        // Le bouton de fermeture reste compact:
        // il doit etre evident, pas devenir un gros call-to-action.
        ShopPixelButton closeButton = new ShopPixelButton("X", labelFont, new Color(86, 59, 39), new Color(112, 74, 49), new Color(52, 33, 20), TEXT_PRIMARY);
        closeButton.setFont(labelFont);
        closeButton.setPreferredSize(new Dimension(40, 32));
        closeButton.setMinimumSize(new Dimension(40, 32));
        closeButton.setMaximumSize(new Dimension(40, 32));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.addActionListener(event -> closeShop());

        header.add(title, BorderLayout.WEST);
        header.add(closeButton, BorderLayout.EAST);
        return header;
    }

    /**
     * On construit les grandes colonnes de la boutique selon le type de shop affiché.
     */
    private JComponent buildColumns() {
        // La boutique principale garde 3 colonnes,
        // mais l'échoppe n'en a besoin que de 2.
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.weighty = 1.0;

        int nextColumn = 0;
        if (shop.usesCategoryFilters()) {
            gbc.gridx = nextColumn++;
            gbc.weightx = 0.0;
            gbc.insets = new Insets(0, 0, 0, 18);
            columns.add(buildFiltersPanel(), gbc);
        }

        gbc.gridx = nextColumn++;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 0, 18);
        columns.add(buildCatalogPanel(), gbc);

        gbc.gridx = nextColumn;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        columns.add(buildSummaryPanel(), gbc);
        return columns;
    }

    /**
     * On construit la colonne de filtres affichée seulement pour la boutique principale.
     */
    private JComponent buildFiltersPanel() {
        ShopSectionPanel filtersPanel = new ShopSectionPanel(woodTexture);
        filtersPanel.setPreferredSize(new Dimension(210, 0));
        filtersPanel.setLayout(new BorderLayout());
        filtersPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Categories");

        content.add(title);
        content.add(Box.createVerticalStrut(18));

        // Chaque filtre se contente de changer l'etat local,
        // puis on reconstruit le catalogue visible.
        for (ShopFilterCategory category : ShopFilterCategory.values()) {
            ShopFilterChip chip = new ShopFilterChip(category.getLabel(), bodyFont);
            chip.addActionListener(event -> {
                activeFilter = category;
                syncFromModel();
            });
            filterButtons.put(category, chip);
            content.add(chip);
            content.add(Box.createVerticalStrut(10));
        }

        content.add(Box.createVerticalGlue());

        filtersPanel.add(content, BorderLayout.CENTER);
        return filtersPanel;
    }

    /**
     * On construit la zone centrale du catalogue avec son compteur et sa liste scrollable.
     */
    private JComponent buildCatalogPanel() {
        JLabel title = createPrimaryLabel(priceFont);
        title.setText("Articles");
        return ShopOverlayUiFactory.createCatalogSectionPanel(
                woodTexture,
                title,
                catalogCountLabel,
                productsGrid,
                24
        );
    }

    /**
     * On construit la colonne de droite avec la sélection courante, le panier et les actions.
     */
    private JComponent buildSummaryPanel() {
        ShopSectionPanel summaryPanel = new ShopSectionPanel(woodTexture);
        summaryPanel.setPreferredSize(new Dimension(330, 0));
        summaryPanel.setLayout(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        // La colonne droite est volontairement coupee en trois:
        // - haut: solde + article selectionne
        // - centre: liste du panier
        // - bas: total + actions
        // Cette decoupe evite que les boutons se retrouvent ejectes hors cadre
        // quand la liste du panier grandit.
        JPanel topBlock = new JPanel();
        topBlock.setOpaque(false);
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));

        JLabel walletTitle = createSecondaryLabel(subtitleFont);
        walletTitle.setText("SOLDE");
        walletTitle.setAlignmentX(LEFT_ALIGNMENT);
        walletTitle.setHorizontalAlignment(SwingConstants.LEFT);
        balanceValueLabel.setText("0 EUR");
        balanceValueLabel.setAlignmentX(LEFT_ALIGNMENT);
        balanceValueLabel.setHorizontalAlignment(SwingConstants.LEFT);

        topBlock.add(createLeftAlignedRow(walletTitle));
        topBlock.add(Box.createVerticalStrut(4));
        topBlock.add(createLeftAlignedRow(balanceValueLabel));
        topBlock.add(Box.createVerticalStrut(16));
        topBlock.add(buildSelectionPanel());
        topBlock.add(Box.createVerticalStrut(16));

        JLabel cartTitle = createPrimaryLabel(priceFont);
        cartTitle.setText("Panier");
        cartTitle.setAlignmentX(LEFT_ALIGNMENT);
        cartTitle.setHorizontalAlignment(SwingConstants.LEFT);
        topBlock.add(createLeftAlignedRow(cartTitle));
        topBlock.add(Box.createVerticalStrut(10));

        // La liste du panier occupe l'espace restant.
        // C'est la seule zone de cette colonne qui doit "respirer" verticalement.
        JScrollPane cartScroll = ShopOverlayUiFactory.createTransparentScrollPane(cartItemsPanel);
        cartScroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);

        JLabel totalLabel = createSecondaryLabel(bodyFont);
        totalLabel.setText("Total");
        totalValueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalValueLabel, BorderLayout.EAST);

        footer.add(Box.createVerticalStrut(12));
        footer.add(totalRow);
        footer.add(Box.createVerticalStrut(12));

        JPanel buttonRow = new JPanel(new BorderLayout(8, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(clearCartButton, BorderLayout.WEST);
        buttonRow.add(checkoutButton, BorderLayout.CENTER);

        footer.add(buttonRow);
        footer.add(Box.createVerticalStrut(10));
        footer.add(messageLabel);

        summaryPanel.add(topBlock, BorderLayout.NORTH);
        summaryPanel.add(cartScroll, BorderLayout.CENTER);
        summaryPanel.add(footer, BorderLayout.SOUTH);
        return summaryPanel;
    }

    /**
     * On construit le panneau de détail du produit actuellement sélectionné.
     */
    private JComponent buildSelectionPanel() {
        ShopSectionPanel selectionPanel = new ShopSectionPanel(woodTexture);
        selectionPanel.setLayout(new BorderLayout(0, 14));
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        top.add(productPreview, BorderLayout.WEST);

        // Le bloc meta reste volontairement compact:
        // nom, categorie, prix, éventuel stock limité, deja-present-dans-le-panier.
        // Le texte d'effet des boosts est maintenant visible directement
        // sur les cartes du catalogue central pour faciliter la comparaison.
        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(selectedNameLabel);
        meta.add(Box.createVerticalStrut(6));
        meta.add(selectedMetaLabel);
        meta.add(Box.createVerticalStrut(6));
        meta.add(selectedPriceLabel);
        meta.add(Box.createVerticalStrut(4));
        meta.add(selectedStockLabel);
        meta.add(Box.createVerticalStrut(4));
        meta.add(selectedCartLabel);

        top.add(meta, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setOpaque(false);

        // Petit stepper classique: simple, lisible, sans widget exotique.
        JPanel stepper = new JPanel(new BorderLayout(6, 0));
        stepper.setOpaque(false);

        desiredQuantityLabel.setHorizontalAlignment(SwingConstants.CENTER);
        desiredQuantityLabel.setPreferredSize(new Dimension(46, 36));

        stepper.add(decreaseDesiredButton, BorderLayout.WEST);
        stepper.add(desiredQuantityLabel, BorderLayout.CENTER);
        stepper.add(increaseDesiredButton, BorderLayout.EAST);

        bottom.add(stepper, BorderLayout.WEST);
        bottom.add(addToCartButton, BorderLayout.CENTER);

        selectionPanel.add(top, BorderLayout.NORTH);
        selectionPanel.add(bottom, BorderLayout.SOUTH);
        return selectionPanel;
    }

    /**
     * On branche ici tous les clics principaux de la boutique.
     */
    private void wireActions() {
        // Toute la logique de clic reste ici pour eviter de disperser
        // les comportements de la boutique dans plusieurs classes internes.
        decreaseDesiredButton.addActionListener(event -> updateDesiredQuantity(desiredQuantity - 1));
        increaseDesiredButton.addActionListener(event -> updateDesiredQuantity(incrementQuantity(desiredQuantity)));
        addToCartButton.addActionListener(event -> addSelectedProductToCart());
        clearCartButton.addActionListener(event -> {
            shop.clearShoppingCard();
            setMessage(null, TEXT_MUTED);
            syncFromModel();
        });
        checkoutButton.addActionListener(event -> checkout());
    }

    /**
     * On relit tout l'état du modèle puis on reconstruit les zones visibles concernées.
     */
    private void syncFromModel() {
        // Ici on assume qu'une reconstruction est plus simple qu'une synchro fine.
        // On recalcule tout l'etat visible a partir du modele courant.
        List<Product> visibleProducts = getVisibleProducts();
        ensureSelection(visibleProducts);
        refreshFilterButtons();
        refreshCatalog(visibleProducts);
        refreshSelectionPanel();
        refreshCartPanel();
        refreshSummary();
        revalidate();
        repaint();
    }

    /**
     * On met à jour l'état visuel de chaque bouton de filtre.
     */
    private void refreshFilterButtons() {
        for (Map.Entry<ShopFilterCategory, ShopFilterChip> entry : filterButtons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == activeFilter);
        }
    }

    /**
     * On reconstruit la grille des cartes produit à partir de la liste actuellement visible.
     */
    private void refreshCatalog(List<Product> visibleProducts) {
        productsGrid.removeAll();

        if (visibleProducts.isEmpty()) {
            // Petit etat vide tres sobre pour eviter une grande zone "cassee".
            JPanel emptyState = new JPanel();
            emptyState.setOpaque(false);
            emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));
            emptyState.setBorder(BorderFactory.createEmptyBorder(40, 10, 10, 10));

            JLabel emptyTitle = createPrimaryLabel(priceFont);
            emptyTitle.setText("Aucun article");

            JLabel emptyText = createSecondaryLabel(bodyFont);
            emptyText.setText("Ce filtre ne retourne encore rien.");

            emptyState.add(emptyTitle);
            emptyState.add(Box.createVerticalStrut(8));
            emptyState.add(emptyText);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            productsGrid.add(emptyState, gbc);
        } else {
            // On pose les cartes 2 par 2. Le filler final pousse simplement tout vers le haut.
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 0.5;
            gbc.insets = new Insets(0, 0, 16, 16);

            for (int index = 0; index < visibleProducts.size(); index++) {
                Product product = visibleProducts.get(index);
                gbc.gridx = index % 2;
                gbc.gridy = index / 2;
                gbc.insets = new Insets(0, 0, 16, gbc.gridx == 0 ? 16 : 0);
                productsGrid.add(new ProductCardView(
                        product,
                        getProductCategoryLabel(product),
                        getProductCatalogDetailLabel(product),
                        product.getPrice() + " EUR",
                        getProductFooterLabel(product),
                        getCatalogBadgeText(product),
                        product == selectedProduct,
                        woodTexture,
                        labelFont,
                        priceFont,
                        bodyFont,
                        () -> selectProduct(product)
                ), gbc);
            }

            GridBagConstraints filler = new GridBagConstraints();
            filler.gridx = 0;
            filler.gridy = (visibleProducts.size() + 1) / 2;
            filler.weightx = 1.0;
            filler.weighty = 1.0;
            filler.gridwidth = 2;
            filler.fill = GridBagConstraints.BOTH;
            productsGrid.add(Box.createGlue(), filler);
        }

        String suffix = visibleProducts.size() > 1 ? "articles" : "article";
        catalogCountLabel.setText(visibleProducts.size() + " " + suffix);
    }

    /**
     * On met à jour le panneau de droite qui décrit le produit sélectionné.
     */
    private void refreshSelectionPanel() {
        // Le panneau de selection est strictement drive par selectedProduct.
        // S'il n'y a rien, on montre un etat neutre tres simple.
        productPreview.setProduct(selectedProduct);

        if (selectedProduct == null) {
            selectedNameLabel.setText("Aucune selection");
            selectedMetaLabel.setText("Choisissez une carte au centre.");
            selectedPriceLabel.setText("--");
            selectedStockLabel.setText("");
            selectedStockLabel.setVisible(false);
            selectedCartLabel.setText("Dans le panier : 0");
            desiredQuantityLabel.setText("0");
            addToCartButton.setEnabled(false);
            decreaseDesiredButton.setEnabled(false);
            increaseDesiredButton.setEnabled(false);
            return;
        }

        int remainingStock = shop.getRemainingStock(selectedProduct);
        int cartQuantity = shop.getShoppingCardQuantity(selectedProduct);

        selectedNameLabel.setText(selectedProduct.getName());
        selectedMetaLabel.setText(getProductCategoryLabel(selectedProduct));
        selectedPriceLabel.setText(selectedProduct.getPrice() + " EUR");
        if (shop.usesManagedStock(selectedProduct)) {
            selectedStockLabel.setText("Stock libre : " + remainingStock);
            selectedStockLabel.setVisible(true);
        } else {
            selectedStockLabel.setText("");
            selectedStockLabel.setVisible(false);
        }
        selectedCartLabel.setText("Dans le panier : " + cartQuantity);
        desiredQuantityLabel.setText(String.valueOf(desiredQuantity));

        // Le bouton Ajouter n'est actif que si la quantite courante est realiste.
        boolean canAdd = hasAvailableQuantity(selectedProduct) && desiredQuantity > 0;
        addToCartButton.setEnabled(canAdd);
        decreaseDesiredButton.setEnabled(desiredQuantity > 1);
        increaseDesiredButton.setEnabled(canIncreaseDesiredQuantity(selectedProduct, desiredQuantity));
    }

    /**
     * On reconstruit la liste des lignes du panier.
     */
    private void refreshCartPanel() {
        cartItemsPanel.removeAll();

        List<CartItem> cartItems = shop.getShoppingCard();
        if (cartItems.isEmpty()) {
            JLabel emptyLabel = createSecondaryLabel(bodyFont);
            emptyLabel.setText("Le panier est vide.");
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
            cartItemsPanel.add(emptyLabel);
        } else {
            for (CartItem item : cartItems) {
                cartItemsPanel.add(createCartRow(item));
                cartItemsPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Ce glue est important:
        // sans lui, Swing etire les dernieres lignes du panier pour remplir la hauteur.
        cartItemsPanel.add(Box.createVerticalGlue());
    }

    /**
     * On met à jour le total du panier et l'état des boutons globaux.
     */
    private void refreshSummary() {
        // Le total et l'etat des boutons suivent le vrai panier du modele.
        balanceValueLabel.setText(playerMoney.getAmount() + " EUR");
        totalValueLabel.setText(shop.getShoppingCardTotalPrice() + " EUR");

        boolean hasCart = !shop.getShoppingCard().isEmpty();
        clearCartButton.setEnabled(hasCart);
        checkoutButton.setEnabled(hasCart);
    }

    /**
     * On construit une ligne visuelle autonome pour un article déjà présent dans le panier.
     */
    private JComponent createCartRow(CartItem item) {
        ShopSectionPanel row = createCartRowPanel();

        Product product = item.getProduct();

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));

        JLabel name = createPrimaryLabel(bodyFont);
        name.setText(product.getName());

        JLabel details = createSecondaryLabel(bodyFont);
        details.setText(item.getQuantity() + " x " + product.getPrice() + " EUR");

        meta.add(name);
        meta.add(Box.createVerticalStrut(4));
        meta.add(details);

        // La zone de contrôles reste compacte et stable,
        // même si le texte a gauche prend plus de place.
        JPanel controls = new JPanel(new BorderLayout(8, 0));
        controls.setOpaque(false);
        controls.setPreferredSize(new Dimension(150, 32));
        controls.setMinimumSize(new Dimension(150, 32));
        controls.setMaximumSize(new Dimension(150, 32));

        ShopPixelButton minus = createCompactControlButton("-");
        ShopPixelButton plus = createCompactControlButton("+");
        ShopPixelButton remove = createCompactDangerButton();
        JLabel quantity = createPrimaryLabel(bodyFont);
        quantity.setHorizontalAlignment(SwingConstants.CENTER);
        quantity.setPreferredSize(new Dimension(28, 24));
        quantity.setMinimumSize(new Dimension(28, 24));
        quantity.setMaximumSize(new Dimension(28, 24));
        quantity.setText(String.valueOf(item.getQuantity()));

        minus.addActionListener(event -> {
            shop.setShoppingCardQuantity(product, item.getQuantity() - 1);
            syncFromModel();
        });
        plus.addActionListener(event -> {
            shop.setShoppingCardQuantity(product, incrementQuantity(item.getQuantity()));
            syncFromModel();
        });
        remove.addActionListener(event -> {
            // Ici on retire tout l'article d'un coup.
            // C'est plus confortable que de marteler le bouton "-".
            shop.setShoppingCardQuantity(product, 0);
            setMessage(null, TEXT_MUTED);
            syncFromModel();
        });

        minus.setEnabled(item.getQuantity() > 1);
        plus.setEnabled(shop.canIncreaseShoppingCardQuantity(product));

        JPanel quantityControls = new JPanel(new BorderLayout(6, 0));
        quantityControls.setOpaque(false);
        quantityControls.setPreferredSize(new Dimension(112, 32));
        quantityControls.setMinimumSize(new Dimension(112, 32));
        quantityControls.setMaximumSize(new Dimension(112, 32));

        quantityControls.add(minus, BorderLayout.WEST);
        quantityControls.add(quantity, BorderLayout.CENTER);
        quantityControls.add(plus, BorderLayout.EAST);

        controls.add(quantityControls, BorderLayout.CENTER);
        controls.add(remove, BorderLayout.EAST);

        row.add(meta, BorderLayout.CENTER);
        row.add(controls, BorderLayout.EAST);
        return row;
    }

    /**
     * Prépare le conteneur standard d'une ligne du panier.
     * Le contenu de la ligne varie, mais son enveloppe visuelle reste stable
     * pour garder un panier homogène et compact.
     */
    private ShopSectionPanel createCartRowPanel() {
        ShopSectionPanel row = new ShopSectionPanel(woodTexture);
        row.setLayout(new BorderLayout(8, 0));
        row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(0, 84));
        row.setMinimumSize(new Dimension(0, 84));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        return row;
    }

    /**
     * On garde une sélection cohérente quand le filtre ou le panier modifie le catalogue visible.
     */
    private void ensureSelection(List<Product> visibleProducts) {
        // Quand le filtre change, on essaie de garder la selection courante.
        // Si elle disparait, on retombe proprement sur le premier article visible.
        if (visibleProducts.isEmpty()) {
            selectedProduct = null;
            desiredQuantity = 0;
            return;
        }

        if (selectedProduct == null || !visibleProducts.contains(selectedProduct)) {
            selectedProduct = visibleProducts.get(0);
        }

        if (!hasAvailableQuantity(selectedProduct)) {
            desiredQuantity = 0;
            return;
        }

        // On corrige aussi la quantite desiree si elle n'a plus de sens
        // apres un changement de filtre ou de panier.
        if (desiredQuantity <= 0
                || (shop.usesManagedStock(selectedProduct) && desiredQuantity > shop.getRemainingStock(selectedProduct))) {
            desiredQuantity = 1;
        }
    }

    /**
     * On recale la quantité demandée dans des bornes encore valides pour le produit courant.
     */
    private void updateDesiredQuantity(int nextQuantity) {
        // Pour les articles limités, on reste borné par le stock libre.
        // Pour les autres, on laisse simplement monter la quantité demandée.
        if (selectedProduct == null) {
            desiredQuantity = 0;
            syncFromModel();
            return;
        }

        if (!hasAvailableQuantity(selectedProduct)) {
            desiredQuantity = 0;
            syncFromModel();
            return;
        }

        if (shop.usesManagedStock(selectedProduct)) {
            int remainingStock = shop.getRemainingStock(selectedProduct);
            desiredQuantity = Math.max(1, Math.min(nextQuantity, remainingStock));
        } else {
            desiredQuantity = Math.max(1, nextQuantity);
        }
        refreshSelectionPanel();
        repaint();
    }

    /**
     * On change le produit sélectionné puis on remet sa quantité de départ à une valeur saine.
     */
    private void selectProduct(Product product) {
        selectedProduct = product;
        desiredQuantity = getInitialDesiredQuantity(product);
        setMessage(null, TEXT_MUTED);
        syncFromModel();
    }

    /**
     * On ajoute la quantité demandée du produit sélectionné dans le panier.
     */
    private void addSelectedProductToCart() {
        // Ici on ajoute la quantite choisie d'un coup.
        // Shop fait encore la vraie validation, la vue ne fait qu'anticiper.
        if (selectedProduct == null || desiredQuantity <= 0) {
            return;
        }

        if (!shop.addToShoppingCard(selectedProduct, desiredQuantity)) {
            setMessage("Quantité demandee indisponible.", ERROR);
            syncFromModel();
            return;
        }

        setMessage(null, TEXT_MUTED);
        // Une fois l'ajout fait, on repasse sur 1 pour faciliter les achats suivants.
        desiredQuantity = getInitialDesiredQuantity(selectedProduct);
        syncFromModel();
    }

    /**
     * On tente de finaliser l'achat de tout le panier.
     */
    private void checkout() {
        // La validation reste volontairement simple:
        // si le modele refuse, on affiche juste une raison courte.
        if (shop.getShoppingCard().isEmpty()) {
            setMessage("Ajoutez d'abord un article.", TEXT_MUTED);
            syncFromModel();
            return;
        }

        boolean purchaseSucceeded = shop.buyProducts(playerMoney, inventaire);
        if (!purchaseSucceeded) {
            setMessage("Solde insuffisant pour valider ce panier.", ERROR);
            syncFromModel();
            return;
        }

        setMessage("Achat valide. Retour au jeu.", SUCCESS);
        syncFromModel();
        closeShop();
    }

    /**
     * On reconstruit la liste des produits visibles en appliquant le filtre actif.
     */
    private List<Product> getVisibleProducts() {
        // On reconstruit la liste visible a la volée:
        // peu d'articles, donc aucun interêt à maintenir un cache.
        List<Product> products = new ArrayList<>();

        for (Seed seed : shop.getSeeds()) {
            if (activeFilter.matches(seed)) {
                products.add(seed);
            }
        }

        for (Facility facility : shop.getFacilities()) {
            if (activeFilter.matches(facility)) {
                products.add(facility);
            }
        }

        return products;
    }

    /**
     * On affiche ou masque le message d'état en bas de la colonne droite.
     */
    private void setMessage(String text, Color color) {
        // Quand il n'y a rien a dire, on cache vraiment le label
        // pour eviter un trou visuel gratuit.
        boolean hasMessage = text != null && !text.isBlank();
        messageLabel.setText(hasMessage ? text : "");
        messageLabel.setForeground(color);
        messageLabel.setVisible(hasMessage);
    }

    /**
     * On renvoie le grand libellé de catégorie affiché sur une carte produit.
     */
    private String getProductCategoryLabel(Product product) {
        if (product instanceof Seed) {
            return "Graine";
        }
        if (product instanceof Facility) {
            FacilityType type = ((Facility) product).getType();
            if (type == FacilityType.CHEMIN || type == FacilityType.COMPOST) {
                return "Décor / Boosts";
            }
            return "Installation";
        }
        return "Article";
    }

    /**
     * Petit texte court affiché directement sur la carte produit du catalogue.

     * On le réserve surtout aux objets "décor / boosts".
     */
    private String getProductCatalogDetailLabel(Product product) {
        if (!(product instanceof Facility)) {
            return "";
        }

        FacilityType type = ((Facility) product).getType();
        switch (type) {
            case CHEMIN:
                return "Deplacement plus rapide";
            case COMPOST:
                if (shop.isCompostRestockUnlocked()) {
                    return "Rendement x2 des cultures proches\nMaximum 2 sur la carte";
                }
                return "Rendement x2 des cultures proches\n2e compost disponible\nau jour 10";
            default:
                return "";
        }
    }

    /**
     * On renvoie le petit badge affiché sur une carte quand l'article est déjà dans le panier.
     */
    private String getCatalogBadgeText(Product product) {
        int cartQuantity = shop.getShoppingCardQuantity(product);
        return cartQuantity > 0 ? "Panier " + cartQuantity : "";
    }

    /**
     * On renvoie le texte de pied de carte, surtout utilisé pour les stocks limités.
     */
    private String getProductFooterLabel(Product product) {
        if (product == null || !shop.usesManagedStock(product)) {
            return "";
        }

        return "Stock : " + shop.getRemainingStock(product);
    }

    /**
     * On dit si le produit sélectionné peut encore être demandé en quantité positive.
     */
    private boolean hasAvailableQuantity(Product product) {
        return product != null && (!shop.usesManagedStock(product) || shop.getRemainingStock(product) > 0);
    }

    /**
     * On choisit la quantité de départ la plus simple à proposer pour un produit.
     */
    private int getInitialDesiredQuantity(Product product) {
        return hasAvailableQuantity(product) ? 1 : 0;
    }

    /**
     * On dit si la quantité désirée peut encore être augmentée d'un cran.
     */
    private boolean canIncreaseDesiredQuantity(Product product, int currentQuantity) {
        if (product == null || currentQuantity <= 0) {
            return false;
        }

        if (!shop.usesManagedStock(product)) {
            return currentQuantity < Integer.MAX_VALUE;
        }

        return currentQuantity < shop.getRemainingStock(product);
    }

    /**
     * On incrémente une quantité sans jamais dépasser la borne entière maximale.
     */
    private int incrementQuantity(int quantity) {
        return quantity == Integer.MAX_VALUE ? Integer.MAX_VALUE : quantity + 1;
    }

    /**
     * On force un composant à rester bien ancré à gauche dans un BoxLayout.
     */
    private JComponent createLeftAlignedRow(JComponent component) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.add(component);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, component.getPreferredSize().height));
        return row;
    }

    /**
     * On crée le bouton standard du stepper principal de quantité.
     */
    private ShopPixelButton createControlButton(String label) {
        // Version standard pour le stepper principal.
        ShopPixelButton button = new ShopPixelButton(label, labelFont, new Color(87, 63, 44), new Color(109, 79, 55), new Color(53, 36, 24), TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(40, 36));
        button.setMinimumSize(new Dimension(40, 36));
        button.setMaximumSize(new Dimension(40, 36));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    /**
     * On crée la version compacte des boutons utilisée dans les lignes du panier.
     */
    private ShopPixelButton createCompactControlButton(String label) {
        // Version plus petite reservee au panier.
        ShopPixelButton button = new ShopPixelButton(label, labelFont, new Color(87, 63, 44), new Color(109, 79, 55), new Color(53, 36, 24), TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(30, 30));
        button.setMinimumSize(new Dimension(30, 30));
        button.setMaximumSize(new Dimension(30, 30));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    /**
     * On crée le petit bouton de suppression complète d'une ligne de panier.
     */
    private ShopPixelButton createCompactDangerButton() {
        // Meme gabarit que les petits boutons du panier,
        // mais avec une teinte plus chaude pour signaler la suppression.
        ShopPixelButton button = new ShopPixelButton("X", labelFont, new Color(122, 67, 54), new Color(146, 79, 63), new Color(83, 40, 31), TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(30, 30));
        button.setMinimumSize(new Dimension(30, 30));
        button.setMaximumSize(new Dimension(30, 30));
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    /**
     * On crée un label au style principal de la boutique.
     */
    private JLabel createPrimaryLabel(Font font) {
        JLabel label = new JLabel();
        label.setForeground(TEXT_PRIMARY);
        label.setFont(font);
        return label;
    }

    /**
     * On crée un label au style secondaire de la boutique.
     */
    private JLabel createSecondaryLabel(Font font) {
        JLabel label = new JLabel();
        label.setForeground(TEXT_SECONDARY);
        label.setFont(font);
        return label;
    }

}
