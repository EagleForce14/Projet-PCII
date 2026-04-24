package view;

import model.enemy.EnemyModel;
import model.enemy.EnemyUnit;
import model.enemy.SpriteKey;

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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Couche d'affichage partagée entre les lapins du champ et les monstres de la grotte.
 * La vue reste volontairement simple :
 * elle ne pilote jamais l'IA, elle se contente de lire le modèle et d'afficher le bon sprite.
 */
public class EnemyView extends JPanel {
    private static final int ENEMY_CLICK_RADIUS = 27;
    private static final int RABBIT_FRAME_WIDTH = 54;
    private static final int RABBIT_FRAME_HEIGHT = 54;
    private static final int MONSTER_FRAME_WIDTH = 48;
    private static final int MONSTER_FRAME_HEIGHT = 48;
    private static final Color CAVE_HEALTH_BACKGROUND = new Color(28, 14, 16, 196);
    private static final Color CAVE_HEALTH_FILL = new Color(212, 74, 74, 235);
    private static final Color CAVE_HEALTH_BORDER = new Color(245, 208, 184, 214);
    private static final Color CAVE_HIT_FLASH = new Color(255, 255, 255, 68);
    private static Map<SpriteKey, BufferedImage> sharedSpritesByKey;
    private static BufferedImage sharedMissingSprite;

    private final EnemyModel model;
    private final PlayableMapPanel mapPanel;
    private final EnemyStatusOverlay statusOverlay;
    private final Map<SpriteKey, BufferedImage> spritesByKey;
    private final BufferedImage missingSprite;

    private EnemyUnit selectedEnemy;

    /**
     * Prépare la vue chargée d'afficher tous les ennemis de la scène courante.
     */
    public EnemyView(EnemyModel model, PlayableMapPanel mapPanel) {
        this.model = model;
        this.mapPanel = mapPanel;
        this.statusOverlay = new EnemyStatusOverlay();
        this.spritesByKey = getSharedSpritesByKey();
        this.missingSprite = getSharedMissingSprite();
        this.setOpaque(false);
        this.setDoubleBuffered(true);
    }

    /**
     * Force le chargement des sprites d'ennemis avant qu'ils soient dessinés pour la première fois.
     */
    public static void warmupSharedSprites() {
        getSharedSpritesByKey();
        getSharedMissingSprite();
    }

    /**
     * Renvoie la table partagée qui associe chaque état d'ennemi à son sprite d'affichage.
     */
    private static synchronized Map<SpriteKey, BufferedImage> getSharedSpritesByKey() {
        if (sharedSpritesByKey == null) {
            sharedSpritesByKey = loadDisplaySprites();
        }
        return sharedSpritesByKey;
    }

    /**
     * Renvoie une petite image vide utilisée si un sprite d'ennemi est manquant.
     */
    private static synchronized BufferedImage getSharedMissingSprite() {
        if (sharedMissingSprite == null) {
            sharedMissingSprite = new BufferedImage(RABBIT_FRAME_WIDTH, RABBIT_FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }
        return sharedMissingSprite;
    }

    /**
     * Charge tous les sprites nécessaires à l'affichage des lapins et des monstres.
     */
    private static Map<SpriteKey, BufferedImage> loadDisplaySprites() {
        Map<SpriteKey, BufferedImage> sprites = new EnumMap<>(SpriteKey.class);
        loadRabbitSprites(sprites);
        loadMonsterSprites(sprites);
        return sprites;
    }

    /**
     * Charge et range tous les sprites utilisés pour les lapins de la ferme.
     */
    private static void loadRabbitSprites(Map<SpriteKey, BufferedImage> sprites) {
        BufferedImage frontSprite = loadSprite("/assets/ennemi_ferme_front.png");
        BufferedImage backSprite = loadSprite("/assets/ennemi_ferme_back.png");
        BufferedImage leftSprite = loadSprite("/assets/ennemi_ferme_left.png");
        BufferedImage rightSprite = loadSprite("/assets/ennemi_ferme_right.png");
        BufferedImage eatingSprite = loadSprite("/assets/ennemi_ferme_eating.png");

        BufferedImage[] normalizedSprites = normalizeFamily(
                RABBIT_FRAME_WIDTH,
                RABBIT_FRAME_HEIGHT,
                frontSprite,
                backSprite,
                leftSprite,
                rightSprite,
                eatingSprite
        );
        sprites.put(SpriteKey.RABBIT_FRONT, normalizedSprites[0]);
        sprites.put(SpriteKey.RABBIT_BACK, normalizedSprites[1]);
        sprites.put(SpriteKey.RABBIT_LEFT, normalizedSprites[2]);
        sprites.put(SpriteKey.RABBIT_RIGHT, normalizedSprites[3]);
        sprites.put(SpriteKey.RABBIT_EATING, normalizedSprites[4]);
    }

    /**
     * Charge et range tous les sprites utilisés pour les monstres de la grotte.
     */
    private static void loadMonsterSprites(Map<SpriteKey, BufferedImage> sprites) {
        BufferedImage upWalk1Sprite = loadSprite("/assets/monsterTop.png");
        BufferedImage upWalk2Sprite = loadSprite("/assets/monsterTop2.png");
        BufferedImage downIdleSprite = loadSprite("/assets/monsterBasArret.png");
        BufferedImage downWalk1Sprite = loadSprite("/assets/monsterBasAvance.png");
        BufferedImage downWalk2Sprite = loadSprite("/assets/monsterBasAvance2.png");
        BufferedImage leftIdleSprite = loadSprite("/assets/monsterGaucheArret.png");
        BufferedImage leftWalk1Sprite = loadSprite("/assets/monsterGaucheAvance1.png");
        BufferedImage leftWalk2Sprite = loadSprite("/assets/monsterGaucheAvance2.png");
        BufferedImage rightIdleSprite = loadSprite("/assets/monsterDroitArret.png");
        BufferedImage rightWalk1Sprite = loadSprite("/assets/monsterDroitAvance1.png");
        BufferedImage rightWalk2Sprite = loadSprite("/assets/monsterDroitAvance2.png");

        BufferedImage[] normalizedSprites = normalizeFamily(
                MONSTER_FRAME_WIDTH,
                MONSTER_FRAME_HEIGHT,
                upWalk1Sprite,
                upWalk2Sprite,
                downIdleSprite,
                downWalk1Sprite,
                downWalk2Sprite,
                leftIdleSprite,
                leftWalk1Sprite,
                leftWalk2Sprite,
                rightIdleSprite,
                rightWalk1Sprite,
                rightWalk2Sprite
        );

        sprites.put(SpriteKey.MONSTER_UP_WALK_1, normalizedSprites[0]);
        sprites.put(SpriteKey.MONSTER_UP_WALK_2, normalizedSprites[1]);
        sprites.put(SpriteKey.MONSTER_DOWN_IDLE, normalizedSprites[2]);
        sprites.put(SpriteKey.MONSTER_DOWN_WALK_1, normalizedSprites[3]);
        sprites.put(SpriteKey.MONSTER_DOWN_WALK_2, normalizedSprites[4]);
        sprites.put(SpriteKey.MONSTER_LEFT_IDLE, normalizedSprites[5]);
        sprites.put(SpriteKey.MONSTER_LEFT_WALK_1, normalizedSprites[6]);
        sprites.put(SpriteKey.MONSTER_LEFT_WALK_2, normalizedSprites[7]);
        sprites.put(SpriteKey.MONSTER_RIGHT_IDLE, normalizedSprites[8]);
        sprites.put(SpriteKey.MONSTER_RIGHT_WALK_1, normalizedSprites[9]);
        sprites.put(SpriteKey.MONSTER_RIGHT_WALK_2, normalizedSprites[10]);
    }

    /**
     * Met toute une famille de sprites au même format d'affichage
     * pour éviter les sauts visuels quand l'animation change d'image.
     */
    private static BufferedImage[] normalizeFamily(int targetWidth, int targetHeight, BufferedImage... sourceSprites) {
        int maxWidth = 1;
        int maxHeight = 1;
        for (BufferedImage sprite : sourceSprites) {
            maxWidth = Math.max(maxWidth, sprite.getWidth());
            maxHeight = Math.max(maxHeight, sprite.getHeight());
        }

        BufferedImage[] normalizedSprites = new BufferedImage[sourceSprites.length];
        for (int index = 0; index < sourceSprites.length; index++) {
            normalizedSprites[index] = prepareDisplaySprite(sourceSprites[index], maxWidth, maxHeight, targetWidth, targetHeight);
        }
        return normalizedSprites;
    }

    /**
     * Charge un sprite depuis le disque puis le convertit en image transparente prête à être affichée.
     */
    private static BufferedImage loadSprite(String path) {
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
     * Replace un sprite dans un canevas commun pour que toutes les poses restent bien alignées.
     */
    private static BufferedImage normalizeSprite(BufferedImage sprite, int canvasWidth, int canvasHeight) {
        BufferedImage normalizedSprite = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = normalizedSprite.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        int drawX = (canvasWidth - sprite.getWidth()) / 2;
        int drawY = canvasHeight - sprite.getHeight();
        g2d.drawImage(sprite, drawX, drawY, null);
        g2d.dispose();
        return normalizedSprite;
    }

    /**
     * Prépare un sprite pour l'écran :
     * d'abord on l'aligne dans un canevas commun, puis on le redimensionne.
     */
    private static BufferedImage prepareDisplaySprite(
            BufferedImage sprite,
            int canvasWidth,
            int canvasHeight,
            int targetWidth,
            int targetHeight
    ) {
        BufferedImage normalizedSprite = normalizeSprite(sprite, canvasWidth, canvasHeight);
        return scaleSprite(normalizedSprite, targetWidth, targetHeight);
    }

    /**
     * Redimensionne un sprite à la taille voulue en conservant un rendu net.
     */
    private static BufferedImage scaleSprite(BufferedImage sprite, int width, int height) {
        BufferedImage scaledSprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaledSprite.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(sprite, 0, 0, width, height, null);
        g2d.dispose();
        return scaledSprite;
    }

    /**
     * Rend transparents les pixels blancs d'un sprite
     * pour éviter d'afficher un fond blanc autour de l'ennemi.
     */
    private static BufferedImage makeWhiteBackgroundTransparent(BufferedImage sprite) {
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
     * Convertit les limites de la carte dans le repère de cette vue.
     * Cela permet de dessiner les ennemis au bon endroit à l'écran.
     */
    private Rectangle getFieldBoundsInView() {
        return SwingUtilities.convertRectangle(mapPanel.getMapComponent(), mapPanel.getFieldBounds(), this);
    }

    /**
     * Cherche l'ennemi le plus proche d'un clic, à condition qu'il soit assez près du pointeur.
     */
    private EnemyUnit findEnemyAt(Point point) {
        Rectangle fieldBounds = getFieldBoundsInView();
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        double clickRadiusSquared = (double) ENEMY_CLICK_RADIUS * ENEMY_CLICK_RADIUS;

        EnemyUnit nearestEnemy = null;
        double nearestDistanceSquared = Double.MAX_VALUE;
        for (EnemyUnit enemy : model.getEnemyUnits()) {
            double enemyCenterX = centerX + enemy.getPreciseX();
            double enemyCenterY = centerY + enemy.getPreciseY();
            double dx = point.x - enemyCenterX;
            double dy = point.y - enemyCenterY;
            double distanceSquared = (dx * dx) + (dy * dy);
            if (distanceSquared > clickRadiusSquared || distanceSquared >= nearestDistanceSquared) {
                continue;
            }
            nearestEnemy = enemy;
            nearestDistanceSquared = distanceSquared;
        }

        return nearestEnemy;
    }

    /**
     * Sélectionne un ennemi à partir d'un point cliqué puis redessine la vue.
     */
    public void selectEnemyAt(Point point) {
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Traite un clic sur le monde exactement comme une sélection d'ennemi.
     */
    public void handleWorldClick(Point point) {
        selectedEnemy = findEnemyAt(point);
        repaint();
    }

    /**
     * Supprime la sélection si l'ennemi choisi n'existe plus ou a quitté la scène.
     */
    private void syncSelectedEnemyState() {
        if (selectedEnemy == null) {
            return;
        }

        if (!model.getEnemyUnits().contains(selectedEnemy) || selectedEnemy.hasFled()) {
            selectedEnemy = null;
        }
    }

    /**
     * Renvoie le sprite à utiliser pour un ennemi donné.
     * Si le sprite demandé manque, la méthode renvoie une image de secours.
     */
    private BufferedImage resolveSprite(EnemyUnit enemy) {
        BufferedImage sprite = spritesByKey.get(enemy.getSpriteKey());
        return sprite == null ? missingSprite : sprite;
    }

    /**
     * Donne la largeur d'affichage d'un ennemi selon son type.
     */
    private int getFrameWidth(EnemyUnit enemy) {
        return enemy != null && enemy.isCaveMonster() ? MONSTER_FRAME_WIDTH : RABBIT_FRAME_WIDTH;
    }

    /**
     * Donne la hauteur d'affichage d'un ennemi selon son type.
     */
    private int getFrameHeight(EnemyUnit enemy) {
        return enemy != null && enemy.isCaveMonster() ? MONSTER_FRAME_HEIGHT : RABBIT_FRAME_HEIGHT;
    }

    /**
     * Dessine le sprite de l'ennemi, son halo de sélection,
     * son flash d'impact et sa barre de vie si nécessaire.
     */
    private void drawEnemySprite(
            Graphics2D g2d,
            EnemyUnit enemy,
            double enemyCenterX,
            double enemyCenterY,
            boolean isSelected
    ) {
        int frameWidth = getFrameWidth(enemy);
        int frameHeight = getFrameHeight(enemy);
        if (isSelected) {
            g2d.setColor(new Color(255, 218, 107, 70));
            g2d.fillOval(
                    (int) Math.round(enemyCenterX - (frameWidth / 2.0)),
                    (int) Math.round(enemyCenterY - (frameHeight / 2.0)),
                    frameWidth,
                    frameHeight
            );
        }

        BufferedImage sprite = resolveSprite(enemy);
        double frameX = enemyCenterX - (frameWidth / 2.0);
        double frameY = enemyCenterY - (frameHeight / 2.0);
        g2d.drawImage(sprite, AffineTransform.getTranslateInstance(frameX, frameY), this);
        if (enemy != null && enemy.isHitFlashVisible()) {
            g2d.setColor(CAVE_HIT_FLASH);
            g2d.fillOval(
                    (int) Math.round(enemyCenterX - (frameWidth / 2.0)),
                    (int) Math.round(enemyCenterY - (frameHeight / 2.0)),
                    frameWidth,
                    frameHeight
            );
        }
        if (enemy != null && enemy.isCaveMonster()) {
            drawCaveHealthBar(g2d, enemy, enemyCenterX, enemyCenterY, isSelected);
        }
    }

    /**
     * Dessine la barre de vie d'un monstre de grotte au-dessus de sa tête.
     */
    private void drawCaveHealthBar(
            Graphics2D g2d,
            EnemyUnit enemy,
            double enemyCenterX,
            double enemyCenterY,
            boolean isSelected
    ) {
        boolean shouldDisplay = isSelected || enemy.isCaveAggroed() || enemy.getHealthRatio() < 1.0;
        if (!shouldDisplay) {
            return;
        }

        int barWidth = 36;
        int barHeight = 6;
        int drawX = (int) Math.round(enemyCenterX - (barWidth / 2.0));
        int drawY = (int) Math.round(enemyCenterY - 34);
        int fillWidth = Math.max(0, Math.min(barWidth - 2, (int) Math.round((barWidth - 2) * enemy.getHealthRatio())));

        g2d.setColor(CAVE_HEALTH_BACKGROUND);
        g2d.fillRoundRect(drawX, drawY, barWidth, barHeight, 6, 6);
        g2d.setColor(CAVE_HEALTH_FILL);
        g2d.fillRoundRect(drawX + 1, drawY + 1, fillWidth, barHeight - 2, 6, 6);
        g2d.setColor(CAVE_HEALTH_BORDER);
        g2d.drawRoundRect(drawX, drawY, barWidth, barHeight, 6, 6);
    }

    /**
     * Redessine tous les ennemis visibles, met à jour la taille utile du terrain
     * et affiche aussi l'overlay d'informations sur l'ennemi sélectionné.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getWidth() > 0 && getHeight() > 0) {
            model.setViewportSize(getWidth(), getHeight());
        }

        syncSelectedEnemyState();

        List<EnemyUnit> enemies = model.getEnemyUnits();
        Rectangle fieldBounds = getFieldBoundsInView();
        model.setFieldSize(fieldBounds.width, fieldBounds.height);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        for (EnemyUnit enemy : enemies) {
            double enemyCenterX = centerX + enemy.getPreciseX();
            double enemyCenterY = centerY + enemy.getPreciseY();
            drawEnemySprite(g2d, enemy, enemyCenterX, enemyCenterY, enemy == selectedEnemy);
        }

        statusOverlay.paint(g2d, selectedEnemy, getWidth());
    }
}
