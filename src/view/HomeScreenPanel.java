package view;

import view.shop.ShopPixelButton;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Ecran d'accueil affiche avant de lancer la partie.
 */
public class HomeScreenPanel extends JPanel {
    // Police pixel utilisée sur l'écran d'accueil.
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";
    // Image de fond dédiée à l'écran d'accueil.
    private static final String HOME_BACKGROUND_PATH = "/assets/fond_accueil.png";

    // Couleur du titre principal.
    private static final Color TITLE_COLOR = new Color(255, 239, 205);
    // Couleur du sous-titre descriptif.
    private static final Color SUBTITLE_COLOR = new Color(232, 214, 181);
    // Couleur du texte du bouton de lancement.
    private static final Color START_LABEL_COLOR = new Color(255, 239, 205);
    // Couleur du texte du bouton de sortie.
    private static final Color QUIT_LABEL_COLOR = new Color(232, 214, 181);
    // Couleur normale du bouton de lancement.
    private static final Color START_BUTTON_BASE = new Color(140, 96, 56, 255);
    // Couleur de survol du bouton de lancement.
    private static final Color START_BUTTON_HOVER = new Color(170, 118, 69, 255);
    // Couleur de contour du bouton de lancement.
    private static final Color START_BUTTON_BORDER = new Color(70, 42, 23);
    // Couleur normale du bouton de sortie.
    private static final Color QUIT_BUTTON_BASE = new Color(94, 66, 46, 240);
    // Couleur de survol du bouton de sortie.
    private static final Color QUIT_BUTTON_HOVER = new Color(114, 82, 57, 240);
    // Couleur de contour du bouton de sortie.
    private static final Color QUIT_BUTTON_BORDER = new Color(58, 36, 22);
    // Couleur de la séparation visuelle de la sidebar.
    private static final Color SIDEBAR_SEPARATOR = new Color(52, 31, 18, 215);
    // Feuilles decoratives animees pour donner de la vie a l'ecran d'accueil.
    private static final int LEAF_COUNT = 24;
    // Palette melangeant teintes naturelles et roses pour varier l'effet visuel.
    private static final Color[] LEAF_PALETTE = {
            new Color(96, 152, 86, 220),
            new Color(122, 177, 94, 220),
            new Color(74, 128, 62, 220),
            new Color(148, 196, 116, 220),
            new Color(230, 150, 186, 220),
            new Color(216, 124, 170, 220),
            new Color(244, 179, 206, 220)
    };

    // Fond fixe affiché derrière la sidebar et les feuilles.
    private final Image homeBackground;
    // Liste complète des feuilles actuellement animées.
    private final List<FallingLeaf> fallingLeaves = new ArrayList<>();
    // Generateur pseudo-aleatoire utilise pour les tailles, vitesses et positions.
    private final Random random = new Random();
    // Timer Swing qui fait avancer l'animation des feuilles.
    private final Timer leavesTimer;
    // Permet d'attendre la premiere taille reelle du panneau avant de repartir les feuilles.
    private boolean leavesNeedInitialDistribution = true;

    // Constructeur pratique pour creer l'accueil sans decor figé fourni.
    public HomeScreenPanel(Runnable onStartGame, Runnable onQuitGame) {
        this(onStartGame, onQuitGame, null);
    }

    // Construit l'accueil avec le fond, la sidebar et l'animation des feuilles.
    public HomeScreenPanel(Runnable onStartGame, Runnable onQuitGame, JComponent frozenGameBackground) {
        setLayout(new BorderLayout());
        setFocusable(true);
        setOpaque(true);

        // Le parametre frozenGameBackground est conserve pour compatibilite API,
        // mais l'accueil utilise desormais un fond dedie.
        this.homeBackground = ImageLoader.load(HOME_BACKGROUND_PATH);

        // Les feuilles sont preparees avant le lancement du timer pour eviter un premier rendu vide.
        initializeLeaves();
        leavesTimer = new Timer(33, event -> {
            updateLeaves();
            repaint();
        });

        Font titleFont = CustomFontLoader.loadFont(FONT_PATH, 30.0f);
        Font subtitleFont = CustomFontLoader.loadFont(FONT_PATH, 11.0f);
        Font buttonFont = CustomFontLoader.loadFont(FONT_PATH, 12.0f);

        ShopPixelButton startButton = new ShopPixelButton(
                "Lancer la partie",
                buttonFont,
                START_BUTTON_BASE,
                START_BUTTON_HOVER,
                START_BUTTON_BORDER,
                START_LABEL_COLOR
        );
        startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startButton.addActionListener(event -> {
            startButton.setEnabled(false);
            if (onStartGame != null) {
                onStartGame.run();
            }
        });

        ShopPixelButton quitButton = new ShopPixelButton(
                "Quitter",
                buttonFont,
                QUIT_BUTTON_BASE,
                QUIT_BUTTON_HOVER,
                QUIT_BUTTON_BORDER,
                QUIT_LABEL_COLOR
        );
        quitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        quitButton.addActionListener(event -> {
            if (onQuitGame != null) {
                onQuitGame.run();
                return;
            }

            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }
        });

        int buttonWidth = 320;
        startButton.setPreferredSize(new Dimension(buttonWidth, 48));
        startButton.setMaximumSize(new Dimension(buttonWidth, 48));

        quitButton.setPreferredSize(new Dimension(buttonWidth, 44));
        quitButton.setMaximumSize(new Dimension(buttonWidth, 44));

        add(buildSidebarPanel(titleFont, subtitleFont, startButton, quitButton), BorderLayout.EAST);

        bindEnterKey(startButton);
    }

    @Override
    // Demarre l'animation quand le panneau entre dans l'arbre Swing.
    public void addNotify() {
        super.addNotify();
        // Le timer ne tourne que lorsque le panneau est visible.
        leavesNeedInitialDistribution = true;
        if (!leavesTimer.isRunning()) {
            leavesTimer.start();
        }
    }

    // Arrete l'animation quand le panneau n'est plus affiche.
    @Override
    public void removeNotify() {
        // On coupe l'animation quand l'accueil quitte l'arbre de composants.
        leavesTimer.stop();
        super.removeNotify();
    }

    // Construit la sidebar droite avec le titre, l'accroche et les boutons.
    private JPanel buildSidebarPanel(
            Font titleFont,
            Font subtitleFont,
            ShopPixelButton startButton,
            ShopPixelButton quitButton
    ) {
        final Image woodBackground = ImageLoader.load("/assets/bois.png");

        // Sidebar visuelle a droite avec le fond bois et le liseret vertical.
        JPanel sidebarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2d = (Graphics2D) graphics.create();

                int width = getWidth();
                int height = getHeight();
                if (woodBackground != null) {
                    g2d.drawImage(woodBackground, 0, 0, width, height, this);
                } else {
                    g2d.setColor(new Color(102, 71, 45));
                    g2d.fillRect(0, 0, width, height);
                }

                g2d.setPaint(new GradientPaint(
                        0,
                        0,
                        new Color(0, 0, 0, 42),
                        0,
                        height,
                        new Color(0, 0, 0, 86)
                ));
                g2d.fillRect(0, 0, width, height);
                g2d.setColor(SIDEBAR_SEPARATOR);
                g2d.fillRect(0, 0, 3, height);
                g2d.dispose();
            }
        };

        sidebarPanel.setOpaque(false);
        sidebarPanel.setPreferredSize(new Dimension(SidebarPanel.SIDEBAR_WIDTH, 0));
        sidebarPanel.setMinimumSize(new Dimension(SidebarPanel.SIDEBAR_WIDTH, 0));
        // Empilement vertical pour garder le titre, l'accroche et les boutons dans un seul bloc.
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(28, 22, 28, 22));

        JLabel titleLabel = createSidebarLabel("PROJET PCII", titleFont, TITLE_COLOR);
        JLabel subtitleLabel = createSidebarLabel(
                "<html>Gérez votre ferme<br/>et survivez à la grotte</html>",
                subtitleFont,
                SUBTITLE_COLOR
        );

        // Les composants sont ajoutés dans l'ordre d'affichage, avec des espaces verticaux pour aérer la mise en page.
        sidebarPanel.add(titleLabel);
        sidebarPanel.add(Box.createVerticalStrut(10));
        sidebarPanel.add(subtitleLabel);
        sidebarPanel.add(Box.createVerticalStrut(34));
        sidebarPanel.add(startButton);
        sidebarPanel.add(Box.createVerticalStrut(12));
        sidebarPanel.add(quitButton);
        sidebarPanel.add(Box.createVerticalGlue());
        return sidebarPanel;
    }

    // Cree un label de sidebar avec la typographie et la couleur voulues.
    private JLabel createSidebarLabel(String text, Font font, Color color) {
        // Petite fabrique pour garder le titre et l'accroche coherents visuellement.
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    // Dessine le fond de l'accueil et la zone animee reservee aux feuilles.
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (homeBackground == null) {
            return;
        }
        // On recupere les dimensions de l'image pour la dessiner en "cover" dans la zone a gauche de la sidebar.
        int imageWidth = homeBackground.getWidth(this);
        int imageHeight = homeBackground.getHeight(this);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        // On reserve la largeur de la sidebar pour que l'image reste bien alignee a gauche.
        int drawableWidth = Math.max(1, getWidth() - SidebarPanel.SIDEBAR_WIDTH);
        // Le ratio de type "cover" evite les bandes vides dans la zone image.
        double ratio = Math.max((double) drawableWidth / imageWidth, (double) getHeight() / imageHeight);
        int drawWidth = (int) Math.round(imageWidth * ratio);
        int drawHeight = (int) Math.round(imageHeight * ratio);
        int drawX = (drawableWidth - drawWidth) / 2;
        int drawY = (getHeight() - drawHeight) / 2;

        Graphics2D g2d = (Graphics2D) graphics.create();
        // Tout le decor de gauche est dessine dans la zone image uniquement.
        g2d.setClip(0, 0, drawableWidth, getHeight());
        g2d.drawImage(homeBackground, drawX, drawY, drawWidth, drawHeight, this);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintFallingLeaves(g2d, drawableWidth, getHeight());
        g2d.dispose();
    }

    // Prepare toutes les feuilles avant le lancement de l'animation.
    private void initializeLeaves() {
        // Initialisation hors ecran: les feuilles entreront par le haut ensuite.
        fallingLeaves.clear();
        leavesNeedInitialDistribution = true;
        for (int index = 0; index < LEAF_COUNT; index++) {
            FallingLeaf leaf = new FallingLeaf();
            // Demarrage sans feuille visible: toutes apparaissent ensuite depuis le haut.
            randomizeLeaf(leaf);
            fallingLeaves.add(leaf);
        }
    }

    // Met a jour la position et la rotation de chaque feuille a chaque tick.
    private void updateLeaves() {
        int drawableWidth = Math.max(1, getWidth() - SidebarPanel.SIDEBAR_WIDTH);
        int drawableHeight = Math.max(1, getHeight());
        if (drawableWidth == 1 || drawableHeight == 1) {
            return;
        }

        // Premiere frame avec dimensions valides : repartit les feuilles sur toute la largeur.
        if (leavesNeedInitialDistribution) {
            for (FallingLeaf leaf : fallingLeaves) {
                randomizeLeaf(leaf, drawableWidth, drawableHeight);
            }
            leavesNeedInitialDistribution = false; // Evite de repositionner les feuilles a chaque redimensionnement ensuite.
        }

        // Chaque feuille avance avec une legere derive et est recyclee quand elle sort du cadre.
        for (FallingLeaf leaf : fallingLeaves) {
            leaf.y += leaf.fallSpeed;
            leaf.x += leaf.horizontalDrift;
            leaf.swayPhase += leaf.swaySpeed;
            leaf.angle += leaf.spinSpeed;

            double visualX = leaf.x + Math.sin(leaf.swayPhase) * leaf.swayAmplitude;
            boolean outOfBottom = leaf.y - leaf.size > drawableHeight;
            boolean outOfHorizontalBounds = visualX < -40 || visualX > drawableWidth + 40;
            if (outOfBottom || outOfHorizontalBounds) {
                randomizeLeaf(leaf, drawableWidth, drawableHeight);
            }
        }
    }

    // Dessine les feuilles visibles avec une forme simple et legere.
    private void paintFallingLeaves(Graphics2D graphics, int drawableWidth, int drawableHeight) {
        if (drawableWidth <= 0 || drawableHeight <= 0) {
            return;
        }

        // Rendu simple en forme de petale allongee pour simuler des feuilles.
        for (FallingLeaf leaf : fallingLeaves) {
            double drawX = leaf.x + Math.sin(leaf.swayPhase) * leaf.swayAmplitude;
            double drawY = leaf.y;
            if (drawY < -30 || drawY > drawableHeight + 30) {
                continue;
            }

            // La taille de la feuille est proportionnelle a sa "size" pour varier les formes et les profondeurs.
            int leafWidth = (int) Math.round(leaf.size * 1.6);
            int leafHeight = (int) Math.round(leaf.size);

            // On utilise une transformation pour dessiner la feuille avec sa rotation et sa position, 
            // puis on remet la transformation precedente pour ne pas affecter les autres feuilles.
            AffineTransform previousTransform = graphics.getTransform();
            graphics.translate(drawX, drawY);
            graphics.rotate(leaf.angle);
            graphics.setColor(leaf.color);
            graphics.fillOval(-leafWidth / 2, -leafHeight / 2, leafWidth, leafHeight);
            graphics.setColor(new Color(78, 47, 27, 130));
            graphics.drawLine(-leafWidth / 4, 0, leafWidth / 4, 0);
            graphics.setTransform(previousTransform);
        }
    }

    // Variante pratique qui prend les dimensions courantes du panneau.
    private void randomizeLeaf(FallingLeaf leaf) {
        int drawableWidth = Math.max(1, getWidth() - SidebarPanel.SIDEBAR_WIDTH);
        int drawableHeight = Math.max(1, getHeight());
        randomizeLeaf(leaf, drawableWidth, drawableHeight);
    }

    // Repositionne une feuille avec de nouvelles valeurs de chute et de derives.
    private void randomizeLeaf(FallingLeaf leaf, int drawableWidth, int drawableHeight) {
        // Les feuilles respawnent soit dans toute la zone pour le premier affichage,
        // soit au-dessus de l'ecran pour retomber naturellement.
        leaf.size = 7.0 + random.nextDouble() * 7.0;
        leaf.x = random.nextDouble() * drawableWidth;
        leaf.y = -10.0 - random.nextDouble() * Math.max(120.0, drawableHeight * 0.35);
        leaf.fallSpeed = 0.9 + random.nextDouble() * 1.8;
        leaf.horizontalDrift = -0.35 + random.nextDouble() * 0.7;
        leaf.swayAmplitude = 3.0 + random.nextDouble() * 9.0;
        leaf.swaySpeed = 0.05 + random.nextDouble() * 0.08;
        leaf.swayPhase = random.nextDouble() * Math.PI * 2.0;
        leaf.angle = random.nextDouble() * Math.PI * 2.0;
        leaf.spinSpeed = -0.03 + random.nextDouble() * 0.06;
        leaf.color = LEAF_PALETTE[random.nextInt(LEAF_PALETTE.length)];
    }

    private static final class FallingLeaf {
        // Position courante de la feuille dans la zone d'affichage.
        private double x;
        private double y;
        // Parametres de deplacement utilises pour animer la chute.
        private double fallSpeed;
        private double horizontalDrift;
        private double swayAmplitude;
        private double swaySpeed;
        private double swayPhase;
        // Apparence et rotation de la feuille.
        private double size;
        private double angle;
        private double spinSpeed;
        private Color color;
    }

    // Associe la touche Entree au bouton de lancement.
    private void bindEnterKey(ShopPixelButton startButton) {
        // La touche Entree lance la partie comme le bouton principal.
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // On associe la touche "ENTER" a une action nommee "launch-game".
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), "launch-game");
        actionMap.put("launch-game", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (startButton.isEnabled()) {
                    startButton.doClick();
                }
            }
        });
    }

}
