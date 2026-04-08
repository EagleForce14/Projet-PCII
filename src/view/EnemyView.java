package view;

import model.enemy.EnemyModel;
import model.enemy.EnemyUnit;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Couche d'affichage dédiée aux lapins.
 * Cette vue ne pilote jamais l'IA elle-même: elle lit l'état du modèle,
 * dessine chaque lapin à la bonne position et affiche un overlay pour le lapin sélectionné.
 *
 * La gestion des interactions souris est volontairement déportée dans le contrôleur.
 * Ici, on expose seulement de petits helpers que le contrôleur peut appeler.
 */
public class EnemyView extends JPanel {
    // Rayon de tolérance pour rendre le clic plus confortable qu'un simple clic précis "pile sur le pixel".
    private static final int ENEMY_CLICK_RADIUS = 27;
    private static final int ENEMY_FRAME_WIDTH = 54;
    private static final int ENEMY_FRAME_HEIGHT = 54;

    // Modèle contenant les lapins actifs.
    private final EnemyModel model;
    // Le champ sert de repère visuel pour convertir les coordonnées logiques en pixels.
    private final FieldPanel fieldPanel;
    // Renderer dédié à la carte d'information du lapin sélectionné.
    private final EnemyStatusOverlay statusOverlay;
    // Lapin actuellement sélectionné par un clic utilisateur.
    private EnemyUnit selectedEnemy;
    private final BufferedImage enemyFrontSprite;
    private final BufferedImage enemyBackSprite;
    private final BufferedImage enemyLeftSprite;
    private final BufferedImage enemyRightSprite;
    private final BufferedImage enemyEatingSprite;

    /**
     * Prépare uniquement l'état graphique de la vue.
     * Le branchement des listeners souris est fait dans le contrôleur.
     */
    public EnemyView(EnemyModel model, FieldPanel fieldPanel) {
        this.model = model;
        this.fieldPanel = fieldPanel;
        this.statusOverlay = new EnemyStatusOverlay();
        BufferedImage[] displaySprites = loadDisplayEnemySprites();
        this.enemyFrontSprite = displaySprites[0];
        this.enemyBackSprite = displaySprites[1];
        this.enemyLeftSprite = displaySprites[2];
        this.enemyRightSprite = displaySprites[3];
        this.enemyEatingSprite = displaySprites[4];
        this.setOpaque(false);
        this.setDoubleBuffered(true); // Évite les clignotements
    }

    private BufferedImage[] loadDisplayEnemySprites() {
        BufferedImage frontSprite = loadSprite("/assets/ennemi_ferme_front.png");
        BufferedImage backSprite = loadSprite("/assets/ennemi_ferme_back.png");
        BufferedImage leftSprite = loadSprite("/assets/ennemi_ferme_left.png");
        BufferedImage rightSprite = loadSprite("/assets/ennemi_ferme_right.png");
        BufferedImage eatingSprite = loadSprite("/assets/ennemi_ferme_eating.png");

        int maxWidth = 1;
        int maxHeight = 1;
        BufferedImage[] sprites = {frontSprite, backSprite, leftSprite, rightSprite, eatingSprite};
        for (BufferedImage sprite : sprites) {
            maxWidth = Math.max(maxWidth, sprite.getWidth());
            maxHeight = Math.max(maxHeight, sprite.getHeight());
        }

        return new BufferedImage[] {
                prepareDisplaySprite(frontSprite, maxWidth, maxHeight),
                prepareDisplaySprite(backSprite, maxWidth, maxHeight),
                prepareDisplaySprite(leftSprite, maxWidth, maxHeight),
                prepareDisplaySprite(rightSprite, maxWidth, maxHeight),
                prepareDisplaySprite(eatingSprite, maxWidth, maxHeight)
        };
    }

    private BufferedImage loadSprite(String path) {
        Image image = ImageLoader.load(path);
        if (image == null) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        int width = Math.max(1, image.getWidth(null));
        int height = Math.max(1, image.getHeight(null));
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return makeWhiteBackgroundTransparent(bufferedImage);
    }

    /**
     * Toutes les poses sont posées dans le même canevas transparent.
     * On les centre horizontalement et on aligne le bas pour éviter tout saut visuel.
     */
    private BufferedImage normalizeSprite(BufferedImage sprite, int canvasWidth, int canvasHeight) {
        BufferedImage normalizedSprite = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = normalizedSprite.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int drawX = (canvasWidth - sprite.getWidth()) / 2;
        int drawY = canvasHeight - sprite.getHeight();
        g2d.drawImage(sprite, drawX, drawY, null);
        g2d.dispose();
        return normalizedSprite;
    }

    private BufferedImage prepareDisplaySprite(BufferedImage sprite, int canvasWidth, int canvasHeight) {
        BufferedImage normalizedSprite = normalizeSprite(sprite, canvasWidth, canvasHeight);
        return scaleSprite(normalizedSprite, ENEMY_FRAME_WIDTH, ENEMY_FRAME_HEIGHT);
    }

    private BufferedImage scaleSprite(BufferedImage sprite, int width, int height) {
        BufferedImage scaledSprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledSprite.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(sprite, 0, 0, width, height, null);
        g2d.dispose();
        return scaledSprite;
    }

    /**
     * Les fichiers utilisent un fond blanc uni, qu'on transforme ici en alpha.
     */
    private BufferedImage makeWhiteBackgroundTransparent(BufferedImage sprite) {
        BufferedImage transparentSprite = new BufferedImage(sprite.getWidth(), sprite.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < sprite.getHeight(); y++) {
            for (int x = 0; x < sprite.getWidth(); x++) {
                int pixel = sprite.getRGB(x, y);
                if ((pixel & 0x00FFFFFF) == 0x00FFFFFF) {
                    transparentSprite.setRGB(x, y, 0x00000000);
                } else {
                    transparentSprite.setRGB(x, y, pixel);
                }
            }
        }
        return transparentSprite;
    }

    /**
     * Convertit les bornes utiles du champ dans le repère de cette vue.
     * On a besoin de cette conversion car les lapins se déplacent dans un repère centré sur le champ.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(fieldPanel, fieldPanel.getFieldBounds(), this);
    }

    /**
     * Cherche quel lapin est visé par la souris.
     * Le point reçu est la position de la souris en pixels dans EnemyView.
     *
     * On ne demande pas au joueur de cliquer exactement au centre du lapin:
     * si le pointeur tombe dans un petit rayon autour d'un lapin, on considère
     * que ce lapin est bien ciblé.
     *
     * Si plusieurs lapins sont proches, on renvoie simplement le plus proche du curseur.
     * Si aucun lapin n'est assez proche, on renvoie null.
     */
    private EnemyUnit findEnemyAt(Point point) {
        Rectangle fieldBounds = getFieldBoundsInView();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        // Pour savoir si la souris "vise" un lapin, on travaille avec une distance au carré.
        // Cela évite une racine carrée inutile et garde un calcul simple.
        double clickRadiusSquared = (double) ENEMY_CLICK_RADIUS * ENEMY_CLICK_RADIUS;

        EnemyUnit nearestEnemy = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (EnemyUnit enemy : model.getEnemyUnits()) {
            // Les positions des lapins sont stockées dans un repère centré sur le champ.
            // On les convertit donc ici en vraie position écran.
            double rabbitCenterX = centerX + enemy.getPreciseX();
            double rabbitCenterY = centerY + enemy.getPreciseY();

            // Vecteur entre la souris et le centre du lapin.
            double dx = point.x - rabbitCenterX;
            double dy = point.y - rabbitCenterY;
            double distanceSquared = (dx * dx) + (dy * dy);

            // Si le lapin est trop loin du curseur, ou moins bon qu'un lapin déjà trouvé,
            // on l'ignore et on continue.
            if (distanceSquared > clickRadiusSquared || distanceSquared >= nearestDistanceSquared) {
                continue;
            }

            // À ce stade, c'est le meilleur candidat rencontré jusque-là.
            nearestEnemy = enemy;
            nearestDistanceSquared = distanceSquared;
        }

        return nearestEnemy;
    }

    /**
     * Appelé par le contrôleur lors d'un clic.
     * Le clic ne change pas le comportement du lapin: il choisit seulement quel lapin afficher dans l'overlay.
     */
    public void selectEnemyAt(Point point) {
        // Un clic sur le vide désélectionne naturellement le lapin courant
        // puisque findEnemyAt renverra null.
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Point d'entree unique pour les clics "dans le monde".
     *
     * Le controleur reste tres simple:
     * il transmet juste la position du clic, puis cette vue decide quel lapin
     * est vise dans son propre repere.
     */
    public void handleWorldClick(Point point) {
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Nettoie la sélection si le lapin a quitté l'écran ou a été supprimé du modèle.
     * Cela évite qu'un overlay reste affiché pour un lapin qui n'existe plus.
     */
    private void syncSelectedEnemyState() {
        if (selectedEnemy == null) {
            return;
        }

        if (!model.getEnemyUnits().contains(selectedEnemy) || selectedEnemy.hasFled()) {
            selectedEnemy = null;
        }
    }

    private BufferedImage resolveSprite(EnemyUnit enemy) {
        switch (enemy.getDisplaySprite()) {
            case BACK:
                return enemyBackSprite;
            case LEFT:
                return enemyLeftSprite;
            case RIGHT:
                return enemyRightSprite;
            case EATING:
                return enemyEatingSprite;
            case FRONT:
            default:
                return enemyFrontSprite;
        }
    }

    /**
     * Dessine l'ennemi à l'intérieur d'un cadre fixe.
     * Les PNG ayant été normalisés au chargement, chaque pose garde exactement le même encombrement visuel.
     */
    private void drawEnemySprite(
            Graphics2D g2d,
            EnemyUnit enemy,
            double enemyCenterX,
            double enemyCenterY,
            boolean isSelected
    ) {
        if (isSelected) {
            g2d.setColor(new Color(255, 218, 107, 70));
            g2d.fillOval(
                    (int) Math.round(enemyCenterX - 27),
                    (int) Math.round(enemyCenterY - 27),
                    54,
                    54
            );
        }

        BufferedImage sprite = resolveSprite(enemy);
        double frameX = enemyCenterX - (ENEMY_FRAME_WIDTH / 2.0);
        double frameY = enemyCenterY - (ENEMY_FRAME_HEIGHT / 2.0);
        g2d.drawImage(sprite, AffineTransform.getTranslateInstance(frameX, frameY), this);
    }

    /**
     * À chaque frame:
     * 1) on synchronise la taille visible de la zone de jeu (pour que les lapins
     * sachent où sont les bords visibles de l'écran.
     * 2) on valide la sélection courante,
     * 3) on dessine tous les lapins,
     * 4) puis on dessine l'overlay du lapin sélectionné.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // EnemyModel a besoin de la vraie taille visible pour calculer correctement les bords de carte.
        if (getWidth() > 0 && getHeight() > 0) {
            model.setViewportSize(getWidth(), getHeight());
        }

        syncSelectedEnemyState();

        List<EnemyUnit> enemies = model.getEnemyUnits();
        // Les lapins partagent le même repère logique que le centre du champ.
        // On reconstitue donc ici ce point d'ancrage avant de transformer leurs coordonnées en pixels.
        Rectangle fieldBounds = getFieldBoundsInView();
        model.setFieldSize(fieldBounds.width, fieldBounds.height);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        // Toute la vue est rendue en 2D avec antialiasing léger
        // pour garder des formes propres malgré les petites tailles de dessin.
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (EnemyUnit enemy : enemies) {
            // Le lapin n'est pas stocké en coordonnées écran.
            // Sa position est un décalage par rapport au centre du champ.
            // Pour le dessiner dans la fenêtre, on ajoute ce décalage à centerX/centerY.
            double rabbitCenterX = centerX + enemy.getPreciseX();
            double rabbitCenterY = centerY + enemy.getPreciseY();
            drawEnemySprite(g2d, enemy, rabbitCenterX, rabbitCenterY, enemy == selectedEnemy);
        }

        // L'overlay reste fixe à l'écran pour être lisible,
        // même quand le lapin continue à marcher ou change d'état.
        statusOverlay.paint(g2d, selectedEnemy, getWidth());
    }
}
