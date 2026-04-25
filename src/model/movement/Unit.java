package model.movement;

import model.Combat.CombatUnit;

/**
 * Représente une entité déplaçable.
 */
public class Unit {
    public static final int SIZE = 30;
    public static final int NORMAL_SPEED = 3;
    public static final int PATH_SPEED = 5;

    /**
     * Etat visuel du jardinier pour la vue principale.
     */
    public enum SpriteAnimation {
        IDLE(1, false),
        WALK_DOWN(4, false),
        WALK_RIGHT(4, false),
        WALK_LEFT(4, false),
        WALK_UP(5, false),
        LABOURER(5, true),
        PLANTER(3, true),
        RECOLTER(3, true);

        private final int frameCount;
        private final boolean oneShot;

        SpriteAnimation(int frameCount, boolean oneShot) {
            this.frameCount = frameCount;
            this.oneShot = oneShot;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public boolean isOneShot() {
            return oneShot;
        }
    }

    private static final int WALK_FRAME_DELAY = 4;
    private static final int ACTION_FRAME_DELAY = 4;

    // On rappelle que volatile assure que les modifications sont immédiatement visibles par tous les threads
    private volatile int x;
    private volatile int y;
    
    // Flags de mouvement propre à cette unité
    private volatile boolean moveUp = false;
    private volatile boolean moveDown = false;
    private volatile boolean moveLeft = false;
    private volatile boolean moveRight = false;

    // Rayon de la zone d'influence
    public static final int INFLUENCE_RADIUS = 50;

    /*
     * La vitesse peut maintenant varier selon le sol sous le joueur.
     * On garde tout de meme une seule valeur simple par frame
     * pour ne pas compliquer inutilement le déplacement.
     */
    private volatile int currentSpeed = NORMAL_SPEED;
    private MovementCollisionMap fieldObstacleMap;

    // Attribut pour la gestion des combats
    private final CombatUnit combatUnit;

    // La scene courante dit sur quelle carte la meme unite evolue.
    // Cela evite de dupliquer l'etat du joueur entre ferme et grotte.
    private volatile PlayerScene scene = PlayerScene.FARM;
    private volatile FacingDirection facingDirection = FacingDirection.DOWN;
    private volatile SpriteAnimation spriteAnimation = SpriteAnimation.IDLE;
    private volatile int animationFrameIndex = 0;
    private volatile int animationTick = 0;

    // Le constructeur de la classe.
    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
        combatUnit = new CombatUnit();
    }

    // On met à jour la position selon le flag activé
    public synchronized void updatePosition() {
        if (spriteAnimation.isOneShot()) {
            updateOneShotAnimation();
            return;
        }

        int stepX = 0;
        int stepY = 0;
        if (moveUp) {
            stepY -= currentSpeed;
        } else if (moveDown) {
            stepY += currentSpeed;
        } else if (moveLeft) {
            stepX -= currentSpeed;
        } else if (moveRight) {
            stepX += currentSpeed;
        }

        int nextX = x;
        int nextY = y;

        /* La collision avec la boutique principale (à droite) s'applique uniquement au corps du joueur.
         * Ainsi, le cercle de zone d'influence n'est pas pris en compte pour bloquer le déplacement, il est par ailleurs
         * purement visuel.
         */
        if (stepX != 0 && canOccupy(x + stepX, y)) {
            nextX = x + stepX;
        }
        if (stepY != 0 && canOccupy(nextX, y + stepY)) {
            nextY = y + stepY;
        }

        x = nextX;
        y = nextY;

        if (stepX != 0 || stepY != 0) {
            updateWalkingAnimation();
        } else {
            setIdleAnimation();
        }
    }

    // Getters et Setters
    public synchronized int getX() { return x; }
    public synchronized int getY() { return y; }

    /**
     * Permet de replacer proprement l'unité sur une position sûre,
     * par exemple au démarrage de la partie après le placement du décor.
     */
    public synchronized void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Coupe immédiatement tout déplacement en cours.
     * On évite ainsi de recopier les quatre mêmes affectations
     * partout où le jeu doit figer le joueur.
     */
    public void stopMovement() {
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
    }

    /**
     * Lance une animation d'action en une seule fois.
     * A la fin de la sequence, l'unite revient automatiquement à l'immobile.
     */
    public synchronized void playActionAnimation(SpriteAnimation animation) {
        if (animation == null || !animation.isOneShot()) {
            return;
        }

        stopMovement();
        spriteAnimation = animation;
        animationFrameIndex = 0;
        animationTick = 0;
    }

    // Force l'unité à rester dans les bornes visibles de la fenêtre.
    public synchronized void clampPosition(int minX, int maxX, int minY, int maxY) {
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }
    
    public void setMoveUp(boolean moveUp) { setMoveUp(moveUp, true); }
    public void setMoveDown(boolean moveDown) { setMoveDown(moveDown, true); }
    public void setMoveLeft(boolean moveLeft) { setMoveLeft(moveLeft, true); }
    public void setMoveRight(boolean moveRight) { setMoveRight(moveRight, true); }
    public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
    public void setFieldObstacleMap(MovementCollisionMap fieldObstacleMap) { this.fieldObstacleMap = fieldObstacleMap; }

    public CombatUnit getCombatUnit() {
        return combatUnit;
    }

    public FacingDirection getFacingDirection() {
        return facingDirection;
    }

    public synchronized SpriteAnimation getSpriteAnimation() {
        return spriteAnimation;
    }

    public synchronized int getAnimationFrameIndex() {
        return animationFrameIndex;
    }

    public void setFacingDirection(FacingDirection facingDirection) {
        if (facingDirection != null) {
            this.facingDirection = facingDirection;
        }
    }

    /**
     * Restaure la vie du joueur sans toucher a son positionnement ni a ses déplacements.
     * Cela permet de relancer une tentative dans la grotte sur une base saine.
     */
    public void restoreFullHealth() {
        combatUnit.healToFull();
    }

    /**
     * Le joueur partage le même filtre d'occupation que les autres entités mobiles
     * pour les obstacles de terrain classiques.
     * Les clôtures restent volontairement exclues ici : elles ne bloquent que les lapins.
     */
    private boolean canOccupy(double centerX, double centerY) {
        return Barn.canOccupyCenteredBox(centerX, centerY, SIZE, SIZE)
                && (fieldObstacleMap == null || fieldObstacleMap.canOccupyCenteredBox(centerX, centerY, SIZE, SIZE));
    }

    /**
     * Vérifie si une position donnée (x, y) est dans la zone d'influence de l'unité.
     * @param targetX Coordonnée X de la cible
     * @param targetY Coordonnée Y de la cible
     * @return true si la cible est dans le rayon d'influence, false sinon.
     */
    public boolean isInInfluenceZone(int targetX, int targetY) {
        if (isInCave()) {
            return false;
        }

        // Remarque : on calcule la distance au carré pour éviter d'utiliser la racine carrée coûteuse
        int dx = this.x - targetX;
        int dy = this.y - targetY;
        return (dx * dx + dy * dy) <= (INFLUENCE_RADIUS * INFLUENCE_RADIUS);
    }

    /**
     * Méthode qui vérifie si l'unité est dans la grotte ou non.
     * @return true si l'unité est dans la grotte, false sinon.
     */
    public boolean isInCave() {
        return scene == PlayerScene.CAVE;
    }

    /** 
     * La meme unite reste vivante, on change seulement sa scene courante.
     */
    public void enterCave() {
        scene = PlayerScene.CAVE;
    }

    /** 
     * Retour a la scene de ferme sans recréer un nouveau joueur.
     */
    public void exitCave() {
        scene = PlayerScene.FARM;
    }

    public PlayerScene getScene() {
        return scene;
    }

    public void setMoveUp(boolean moveUp, boolean updateFacing) {
        this.moveUp = moveUp;
        if (moveUp && updateFacing) {
            setFacingDirection(FacingDirection.UP);
        }
    }

    public void setMoveDown(boolean moveDown, boolean updateFacing) {
        this.moveDown = moveDown;
        if (moveDown && updateFacing) {
            setFacingDirection(FacingDirection.DOWN);
        }
    }

    public void setMoveLeft(boolean moveLeft, boolean updateFacing) {
        this.moveLeft = moveLeft;
        if (moveLeft && updateFacing) {
            setFacingDirection(FacingDirection.LEFT);
        }
    }

    public void setMoveRight(boolean moveRight, boolean updateFacing) {
        this.moveRight = moveRight;
        if (moveRight && updateFacing) {
            setFacingDirection(FacingDirection.RIGHT);
        }
    }

    private void updateWalkingAnimation() {
        SpriteAnimation desiredAnimation = resolveWalkingAnimation();
        if (spriteAnimation != desiredAnimation) {
            spriteAnimation = desiredAnimation;
            if (!isInCave() || animationFrameIndex >= desiredAnimation.getFrameCount()) {
                animationFrameIndex = 0;
            }
            animationTick = 0;
        }

        animationTick++;
        if (animationTick < WALK_FRAME_DELAY) {
            return;
        }

        animationTick = 0;
        animationFrameIndex = (animationFrameIndex + 1) % spriteAnimation.getFrameCount();
    }

    private void updateOneShotAnimation() {
        animationTick++;
        if (animationTick < ACTION_FRAME_DELAY) {
            return;
        }

        animationTick = 0;
        if (animationFrameIndex < spriteAnimation.getFrameCount() - 1) {
            animationFrameIndex++;
            return;
        }

        spriteAnimation = SpriteAnimation.IDLE;
        animationFrameIndex = 0;
    }

    private void setIdleAnimation() {
        if (spriteAnimation != SpriteAnimation.IDLE) {
            spriteAnimation = SpriteAnimation.IDLE;
            animationTick = 0;
        }
    }

    private SpriteAnimation resolveWalkingAnimation() {
        if (moveUp) {
            return SpriteAnimation.WALK_UP;
        }
        if (moveDown) {
            return SpriteAnimation.WALK_DOWN;
        }
        if (moveLeft) {
            return SpriteAnimation.WALK_LEFT;
        }
        if (moveRight) {
            return SpriteAnimation.WALK_RIGHT;
        }

        return SpriteAnimation.IDLE;
    }
}
