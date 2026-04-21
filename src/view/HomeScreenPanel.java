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
import javax.swing.OverlayLayout;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;

/**
 * Ecran d'accueil affiche avant de lancer la partie.
 */
public class HomeScreenPanel extends JPanel {
    private static final String FONT_PATH = "src/assets/fonts/Minecraftia.ttf";

    private static final Color TITLE_COLOR = new Color(255, 239, 205);
    private static final Color SUBTITLE_COLOR = new Color(232, 214, 181);
    private static final Color START_BUTTON_BASE = new Color(140, 96, 56, 255);
    private static final Color START_BUTTON_HOVER = new Color(170, 118, 69, 255);
    private static final Color START_BUTTON_BORDER = new Color(70, 42, 23);
    private static final Color QUIT_BUTTON_BASE = new Color(94, 66, 46, 240);
    private static final Color QUIT_BUTTON_HOVER = new Color(114, 82, 57, 240);
    private static final Color QUIT_BUTTON_BORDER = new Color(58, 36, 22);
    private static final Color SIDEBAR_SEPARATOR = new Color(52, 31, 18, 215);

    public HomeScreenPanel(Runnable onStartGame, Runnable onQuitGame) {
        this(onStartGame, onQuitGame, null);
    }

    public HomeScreenPanel(Runnable onStartGame, Runnable onQuitGame, JComponent frozenGameBackground) {
        setLayout(new BorderLayout());
        setFocusable(true);

        Font titleFont = CustomFontLoader.loadFont(FONT_PATH, 30.0f);
        Font subtitleFont = CustomFontLoader.loadFont(FONT_PATH, 11.0f);
        Font buttonFont = CustomFontLoader.loadFont(FONT_PATH, 12.0f);

        ShopPixelButton startButton = new ShopPixelButton(
                "Lancer la partie",
                buttonFont,
                START_BUTTON_BASE,
                START_BUTTON_HOVER,
                START_BUTTON_BORDER,
                TITLE_COLOR
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
                SUBTITLE_COLOR
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

        add(buildFrozenGameArea(frozenGameBackground), BorderLayout.CENTER);
        add(buildSidebarPanel(titleFont, subtitleFont, startButton, quitButton), BorderLayout.EAST);

        bindEnterKey(startButton);
    }

    private JPanel buildFrozenGameArea(JComponent frozenGameBackground) {
        JPanel frozenGameArea = new JPanel();
        frozenGameArea.setLayout(new OverlayLayout(frozenGameArea));
        frozenGameArea.setOpaque(false);
        configureOverlayAlignment(frozenGameArea);

        JComponent backgroundLayer = buildBackgroundLayer(frozenGameBackground);
        frozenGameArea.add(backgroundLayer);
        frozenGameArea.setComponentZOrder(backgroundLayer, 0);
        return frozenGameArea;
    }

    private JComponent buildBackgroundLayer(JComponent frozenGameBackground) {
        if (frozenGameBackground != null) {
            configureOverlayAlignment(frozenGameBackground);
            return frozenGameBackground;
        }

        final Image fallbackBackground = ImageLoader.load("/assets/Main_Background.png");
        JPanel fallbackPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (fallbackBackground == null) {
                    return;
                }

                int imageWidth = fallbackBackground.getWidth(this);
                int imageHeight = fallbackBackground.getHeight(this);
                if (imageWidth <= 0 || imageHeight <= 0) {
                    return;
                }

                double ratio = Math.max((double) getWidth() / imageWidth, (double) getHeight() / imageHeight);
                int drawWidth = (int) Math.round(imageWidth * ratio);
                int drawHeight = (int) Math.round(imageHeight * ratio);
                int drawX = (getWidth() - drawWidth) / 2;
                int drawY = (getHeight() - drawHeight) / 2;
                graphics.drawImage(fallbackBackground, drawX, drawY, drawWidth, drawHeight, this);
            }
        };
        fallbackPanel.setOpaque(true);
        configureOverlayAlignment(fallbackPanel);
        return fallbackPanel;
    }

    private JPanel buildSidebarPanel(
            Font titleFont,
            Font subtitleFont,
            ShopPixelButton startButton,
            ShopPixelButton quitButton
    ) {
        final Image woodBackground = ImageLoader.load("/assets/bois.png");

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
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(28, 22, 28, 22));

        JLabel titleLabel = createSidebarLabel("PROJET PCII", titleFont, TITLE_COLOR);
        JLabel subtitleLabel = createSidebarLabel(
                "<html>Gérez votre ferme<br/>et survivez à la grotte</html>",
                subtitleFont,
                SUBTITLE_COLOR
        );

        int buttonWidth = SidebarPanel.SIDEBAR_WIDTH - 44;
        startButton.setPreferredSize(new Dimension(buttonWidth, 46));
        startButton.setMaximumSize(new Dimension(buttonWidth, 46));

        quitButton.setPreferredSize(new Dimension(buttonWidth, 42));
        quitButton.setMaximumSize(new Dimension(buttonWidth, 42));

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

    private JLabel createSidebarLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private void bindEnterKey(ShopPixelButton startButton) {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

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

    private void configureOverlayAlignment(Component component) {
        if (component == null) {
            return;
        }

        if (component instanceof JComponent) {
            ((JComponent) component).setAlignmentX(0.5f);
            ((JComponent) component).setAlignmentY(0.5f);
        }
    }
}
