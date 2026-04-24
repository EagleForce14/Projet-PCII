package view;

import model.movement.FacingDirection;
import model.movement.MovementModel;
import model.movement.Unit;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.List;

/**
 * On réalise une interface graphique très simple afin de mieux tester la fonctionnalité
 */
public class MovementView extends JPanel {
    private static final int FARM_INTERACTION_FOOTPRINT_HORIZONTAL_INSET = 5;
    private static final int FARM_INTERACTION_FOOTPRINT_BOTTOM_INSET = 2;
    private static final int FARM_INTERACTION_FOOTPRINT_HEIGHT = 12;
    private static final double GARDENER_BASE_RENDER_SCALE = 1.25;
    private static final double GARDENER_WALK_RENDER_SCALE = 1.32;
    private static final double GARDENER_ACTION_RENDER_SCALE = 1.45;

    // Le modèle contenant la liste des unités à afficher
    private final MovementModel model;
    private final PlayableMapPanel mapPanel;
    private final Map<Unit.SpriteAnimation, Image[]> gardenerSprites;
    private final Map<Unit.SpriteAnimation, Image[]> caveExplorerSprites;

    public MovementView(MovementModel model, PlayableMapPanel mapPanel) {
        this.model = model;
        this.mapPanel = mapPanel;
        this.gardenerSprites = loadGardenerSprites();
        this.caveExplorerSprites = loadCaveExplorerSprites();
        this.setOpaque(false);
        this.setDoubleBuffered(true); // Petite optimisation pour éviter les clignotements
        this.setFocusable(true); // Pour recevoir les événements clavier
    }

    // On boucle sur toutes les unités du modèle pour les dessiner.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Toujours appeler super pour nettoyer le fond

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Unit> units = model.getUnits();
        Unit playerUnit = model.getPlayerUnit();
        Point highlightedCell = null;

        // On convertit les bornes du champ dans le repère de cette vue pour dessiner l'unité au centre du champ
        Rectangle fieldBounds = SwingUtilities.convertRectangle(mapPanel.getMapComponent(), mapPanel.getFieldBounds(), this);
        int centerX = fieldBounds.x + (fieldBounds.width / 2);
        int centerY = fieldBounds.y + (fieldBounds.height / 2);
        // On calcule ici les positions minimales et maximales autorisées pour que
        // l'unité reste entièrement visible dans la fenêtre.
        int minX = -centerX + (Unit.SIZE / 2);
        int maxX = getWidth() - centerX - (Unit.SIZE / 2);
        int minY = -centerY + (Unit.SIZE / 2);
        int maxY = getHeight() - centerY - (Unit.SIZE / 2);

        // On dessine chaque unité présente dans le modèle
        for (Unit u : units) {
            // On borne la position avant le dessin pour empêcher l'unité de sortir de l'écran.
            u.clampPosition(minX, maxX, minY, maxY);

            // La zone d'influence n'existe pas en grotte :
            // ni visuellement, ni côté logique.
            if (!u.isInCave()) {
                g2d.setColor(new Color(0, 0, 255, 50)); // Bleu semi-transparent
                int radius = Unit.INFLUENCE_RADIUS;
                int circleX = centerX + u.getX() - radius;
                int circleY = centerY + u.getY() - radius;
                g2d.fillOval(circleX, circleY, radius * 2, radius * 2);
                g2d.setColor(Color.BLUE);
                g2d.drawOval(circleX, circleY, radius * 2, radius * 2);
            }
            
            // Position relative au centre de la fenêtre
            // Rappel : La position (0,0) de l'unité correspond au centre de l'écran
            int drawX = centerX - (Unit.SIZE / 2) + u.getX(); 
            int drawY = centerY - (Unit.SIZE / 2) + u.getY();

            // La case active est calculée à partir du rectangle réel du joueur,
            // mais en ferme on utilise surtout la "zone des pieds" du jardinier.
            // Cela rend la sélection plus tolérante : il n'est plus nécessaire
            // de faire rentrer tout le sprite dans une seule case pour agir.
            if (u == playerUnit) {
                Rectangle playerBounds = buildInteractionBounds(drawX, drawY, u.isInCave());
                Rectangle playerBoundsInField = SwingUtilities.convertRectangle(this, playerBounds, mapPanel.getMapComponent());
                highlightedCell = mapPanel.getFullyOccupiedCell(playerBoundsInField);

                // Une case recouverte par la boutique principale (à droite) ou par un arbre
                // ne doit jamais devenir la case active du gameplay.
                if (!mapPanel.isFarmableCell(highlightedCell)) {
                    highlightedCell = null;
                }

                /*
                 * Le bonus de vitesse du chemin ne doit pas dependre
                 * de la surbrillance de gameplay.
                 *
                 * Pourquoi ?
                 * La case active impose que tout le rectangle du joueur
                 * soit contenu dans une seule case.
                 * C'est tres bien pour labourer / planter,
                 * mais trop strict pour une sensation de deplacement fluide.
                 *
                 * Pour la vitesse, on lit donc simplement la case situee
                 * sous le centre du joueur.
                 * Des que son centre entre sur un chemin, il accelere.
                 */
                Point playerCenterInField = SwingUtilities.convertPoint(
                        this,
                        drawX + (Unit.SIZE / 2),
                        drawY + (Unit.SIZE / 2),
                        mapPanel.getMapComponent()
                );
                Point movementCell = mapPanel.getGridPositionAt(playerCenterInField.x, playerCenterInField.y);
                u.setCurrentSpeed(mapPanel.resolveMovementSpeed(movementCell));
            }

            drawUnit(g2d, u, drawX, drawY);
        }

        // La condition d'activation des actions est liée à la présence
        // de l'unité déplaçable sur une case valide highlightée du champ.
        model.setActiveFieldCell(highlightedCell);
        mapPanel.setHighlightedCell(highlightedCell);
        g2d.dispose();
    }

    private Rectangle buildInteractionBounds(int drawX, int drawY, boolean inCave) {
        if (inCave) {
            return new Rectangle(drawX, drawY, Unit.SIZE, Unit.SIZE);
        }

        int footprintX = drawX + FARM_INTERACTION_FOOTPRINT_HORIZONTAL_INSET;
        int footprintWidth = Math.max(
                1,
                Unit.SIZE - (FARM_INTERACTION_FOOTPRINT_HORIZONTAL_INSET * 2)
        );
        int footprintHeight = Math.min(Unit.SIZE, FARM_INTERACTION_FOOTPRINT_HEIGHT);
        int footprintY = drawY + Unit.SIZE - footprintHeight - FARM_INTERACTION_FOOTPRINT_BOTTOM_INSET;
        return new Rectangle(footprintX, footprintY, footprintWidth, footprintHeight);
    }

    private void drawUnit(Graphics2D g2d, Unit unit, int drawX, int drawY) {
        if (unit.isInCave()) {
            drawCaveExplorer(g2d, unit, drawX, drawY);
            return;
        }

        drawFarmGardener(g2d, unit, drawX, drawY);
    }

    private void drawFarmGardener(Graphics2D g2d, Unit unit, int drawX, int drawY) {
        int renderSize = resolveGardenerRenderSize(unit.getSpriteAnimation());
        int renderX = drawX - ((renderSize - Unit.SIZE) / 2);
        int renderY = drawY - (renderSize - Unit.SIZE);

        g2d.setColor(new Color(0, 0, 0, 55));
        g2d.fillOval(drawX + 4, drawY + Unit.SIZE - 7, Unit.SIZE - 8, 8);

        Image sprite = resolveGardenerSprite(unit);
        if (sprite != null) {
            g2d.drawImage(sprite, renderX, renderY, renderSize, renderSize, null);
            return;
        }

        g2d.setColor(new Color(83, 122, 64));
        g2d.fillRoundRect(renderX + 5, renderY + 5, renderSize - 10, renderSize - 8, 9, 9);
        g2d.setColor(new Color(229, 209, 171));
        g2d.fillRoundRect(renderX + 8, renderY + 7, renderSize - 16, 10, 6, 6);
        g2d.setColor(new Color(82, 52, 34));
        g2d.fillRoundRect(renderX + 6, renderY + 2, renderSize - 12, 6, 6, 6);
        g2d.setColor(new Color(38, 29, 21));
        g2d.drawRoundRect(renderX + 5, renderY + 5, renderSize - 11, renderSize - 9, 9, 9);
    }

    private int resolveGardenerRenderSize(Unit.SpriteAnimation animation) {
        if (animation == Unit.SpriteAnimation.LABOURER
                || animation == Unit.SpriteAnimation.PLANTER
                || animation == Unit.SpriteAnimation.RECOLTER) {
            return Math.max(Unit.SIZE, (int) Math.round(Unit.SIZE * GARDENER_ACTION_RENDER_SCALE));
        }

        if (animation == Unit.SpriteAnimation.WALK_DOWN
                || animation == Unit.SpriteAnimation.WALK_RIGHT
                || animation == Unit.SpriteAnimation.WALK_LEFT
                || animation == Unit.SpriteAnimation.WALK_UP) {
            return Math.max(Unit.SIZE, (int) Math.round(Unit.SIZE * GARDENER_WALK_RENDER_SCALE));
        }

        return Math.max(Unit.SIZE, (int) Math.round(Unit.SIZE * GARDENER_BASE_RENDER_SCALE));
    }

    private Map<Unit.SpriteAnimation, Image[]> loadGardenerSprites() {
        Map<Unit.SpriteAnimation, Image[]> sprites = new EnumMap<>(Unit.SpriteAnimation.class);
        sprites.put(Unit.SpriteAnimation.IDLE, new Image[] {
                loadSprite("/assets/Immobile/JardinierImmobile.png", "/assets/Immobile/FermierImmobile.png", "/assets/Immobile/fermierImmobile.png")
        });
        sprites.put(Unit.SpriteAnimation.WALK_DOWN, loadSequence("MarcheBas", "JardinierDesc", "FermierDesc", 4));
        sprites.put(Unit.SpriteAnimation.WALK_RIGHT, loadSequence("MarcheDroite", "JardinierDroite", "FermierDroite", 4));
        sprites.put(Unit.SpriteAnimation.WALK_LEFT, loadSequence("MarcheGauche", "JardinierGauche", "FermierGauche", 4));
        sprites.put(Unit.SpriteAnimation.WALK_UP, loadSequence("MarcheHaut", "JardinierMonte", "FermierMonte", 5));
        sprites.put(Unit.SpriteAnimation.LABOURER, loadSequence("Labourer", "JardinierLabourer", "FermierLabourer", 5));
        sprites.put(Unit.SpriteAnimation.PLANTER, loadSequence("Planter", "JardinierPlanter", "FermierPlanter", 3));
        sprites.put(Unit.SpriteAnimation.RECOLTER, loadSequence("Recolter", "JardinierRecolter", "FermierRecolter", 3));
        return sprites;
    }

    private Map<Unit.SpriteAnimation, Image[]> loadCaveExplorerSprites() {
        Map<Unit.SpriteAnimation, Image[]> sprites = new EnumMap<>(Unit.SpriteAnimation.class);
        sprites.put(Unit.SpriteAnimation.IDLE, new Image[] {
                loadSprite("/assets/Immobile/JardinierImmobileGun.png")
        });
        sprites.put(Unit.SpriteAnimation.WALK_DOWN, loadSequence("MarcheBas", "JardinierBasGun", "JardinierBasGun", 4));
        sprites.put(Unit.SpriteAnimation.WALK_RIGHT, loadSequence("MarcheDroite", "JardinierDroiteGun", "JardinierDroiteGun", 4));
        sprites.put(Unit.SpriteAnimation.WALK_LEFT, loadSequence("MarcheGauche", "JardinierGaucheGun", "JardinierGaucheGun", 4));
        sprites.put(Unit.SpriteAnimation.WALK_UP, loadSequence("MarcheHaut", "JardinierHautGun", "JardinierHautGun", 4));
        return sprites;
    }

    private Image[] loadSequence(String folderName, String jardinierPrefix, String fermierPrefix, int frameCount) {
        Image[] frames = new Image[frameCount];
        for (int index = 1; index <= frameCount; index++) {
            frames[index - 1] = loadSprite(
                    "/assets/" + folderName + "/" + jardinierPrefix + index + ".png",
                    "/assets/" + folderName + "/" + fermierPrefix + index + ".png"
            );
        }
        return frames;
    }

    private Image loadSprite(String... candidatePaths) {
        for (String candidatePath : candidatePaths) {
            Image sprite = ImageLoader.loadOptional(candidatePath);
            if (sprite != null) {
                return sprite;
            }
        }

        return null;
    }

    private Image resolveGardenerSprite(Unit unit) {
        Image[] frames = gardenerSprites.get(unit.getSpriteAnimation());
        if (frames == null || frames.length == 0) {
            return null;
        }

        int frameIndex = Math.max(0, Math.min(unit.getAnimationFrameIndex(), frames.length - 1));
        Image sprite = frames[frameIndex];
        if (sprite != null) {
            return sprite;
        }

        for (Image frame : frames) {
            if (frame != null) {
                return frame;
            }
        }

        return null;
    }

    private Image resolveCaveExplorerSprite(Unit unit) {
        Unit.SpriteAnimation animation = unit.getSpriteAnimation();
        Image[] frames = caveExplorerSprites.get(animation);

        if (frames == null || frames.length == 0) {
            frames = caveExplorerSprites.get(resolveWalkingAnimationForFacing(unit.getFacingDirection()));
        }
        if (frames == null || frames.length == 0) {
            frames = caveExplorerSprites.get(Unit.SpriteAnimation.IDLE);
        }
        if (frames == null || frames.length == 0) {
            return null;
        }

        int frameIndex = Math.max(0, Math.min(unit.getAnimationFrameIndex(), frames.length - 1));
        Image sprite = frames[frameIndex];
        if (sprite != null) {
            return sprite;
        }

        for (Image frame : frames) {
            if (frame != null) {
                return frame;
            }
        }

        return null;
    }

    private Unit.SpriteAnimation resolveWalkingAnimationForFacing(FacingDirection facingDirection) {
        if (facingDirection == FacingDirection.UP) {
            return Unit.SpriteAnimation.WALK_UP;
        }
        if (facingDirection == FacingDirection.LEFT) {
            return Unit.SpriteAnimation.WALK_LEFT;
        }
        if (facingDirection == FacingDirection.RIGHT) {
            return Unit.SpriteAnimation.WALK_RIGHT;
        }
        return Unit.SpriteAnimation.WALK_DOWN;
    }

    /**
     * Le joueur de la grotte garde volontairement un look très lisible :
     * corps sombre, visière claire et petit canon orienté dans la direction courante.
     */
    private void drawCaveExplorer(Graphics2D g2d, Unit unit, int drawX, int drawY) {
        int renderSize = resolveGardenerRenderSize(unit.getSpriteAnimation());
        int renderX = drawX - ((renderSize - Unit.SIZE) / 2);
        int renderY = drawY - (renderSize - Unit.SIZE);

        Image caveSprite = resolveCaveExplorerSprite(unit);
        if (caveSprite != null) {
            g2d.setColor(new Color(0, 0, 0, 75));
            g2d.fillOval(drawX + 4, drawY + Unit.SIZE - 7, Unit.SIZE - 8, 8);
            g2d.drawImage(caveSprite, renderX, renderY, renderSize, renderSize, null);
            return;
        }

        g2d.setColor(new Color(0, 0, 0, 75));
        g2d.fillOval(drawX + 4, drawY + Unit.SIZE - 7, Unit.SIZE - 8, 8);

        int bodyInset = Math.max(4, renderSize / 8);
        int visorInset = Math.max(8, renderSize / 6);
        int visorHeight = Math.max(8, renderSize / 6);

        g2d.setColor(new Color(29, 37, 53));
        g2d.fillRoundRect(renderX + bodyInset, renderY + bodyInset, renderSize - (bodyInset * 2), renderSize - (bodyInset * 2), 10, 10);
        g2d.setColor(new Color(84, 215, 221));
        g2d.fillRoundRect(renderX + visorInset, renderY + visorInset, renderSize - (visorInset * 2), visorHeight, 6, 6);

        FacingDirection facingDirection = unit.getFacingDirection();
        int weaponWidth = Math.max(8, renderSize / 6);
        int weaponHeight = Math.max(4, renderSize / 11);
        int weaponX = renderX + ((renderSize - weaponWidth) / 2);
        int weaponY = renderY + ((renderSize - weaponHeight) / 2);
        if (facingDirection == FacingDirection.UP) {
            weaponY = renderY + Math.max(2, renderSize / 16);
            weaponHeight = Math.max(8, renderSize / 6);
            weaponWidth = Math.max(4, renderSize / 11);
            weaponX = renderX + ((renderSize - weaponWidth) / 2);
        } else if (facingDirection == FacingDirection.DOWN) {
            weaponY = renderY + renderSize - Math.max(10, renderSize / 8);
            weaponHeight = Math.max(8, renderSize / 6);
            weaponWidth = Math.max(4, renderSize / 11);
            weaponX = renderX + ((renderSize - weaponWidth) / 2);
        } else if (facingDirection == FacingDirection.LEFT) {
            weaponX = renderX + Math.max(2, renderSize / 16);
        } else if (facingDirection == FacingDirection.RIGHT) {
            weaponX = renderX + renderSize - Math.max(10, renderSize / 8);
        }

        g2d.setColor(new Color(208, 174, 90));
        g2d.fillRoundRect(weaponX, weaponY, weaponWidth, weaponHeight, 4, 4);
        g2d.setColor(new Color(30, 24, 20));
        g2d.drawRoundRect(renderX + bodyInset, renderY + bodyInset, renderSize - (bodyInset * 2) - 1, renderSize - (bodyInset * 2) - 1, 10, 10);
    }
}
