package model.movement;

import model.Combat.CombatUnit;

/**
 * Représente une entité déplaçable.
 */
public class Unit {
    // Taille logique de la hitbox principale de l'unité.
    public static final int SIZE = 30;
    // Vitesse normale utilisée hors chemins accélérés.
    public static final int NORMAL_SPEED = 3;
    // Vitesse utilisée quand l'unité profite d'un chemin.
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

        // Nombre total d'images dans cette animation.
        private final int frameCount;
        // Indique si l'animation doit se jouer une seule fois.
        private final boolean oneShot;

        /**
         * On décrit ici le format de lecture de chaque animation.
         */
        SpriteAnimation(int frameCount, boolean oneShot) {
            this.frameCount = frameCount;
            this.oneShot = oneShot;
        }

        /**
         * On renvoie le nombre d'images à parcourir pour cette animation.
         */
        public int getFrameCount() {
            return frameCount;
        }

        /**
         * On dit si l'animation doit se terminer seule après une lecture complète.
         */
        public boolean isOneShot() {
            return oneShot;
        }
    }

    // Nombre de ticks à attendre avant de passer à l'image suivante en marche.
    private static final int WALK_FRAME_DELAY = 4;
    // Nombre de ticks à attendre avant de passer à l'image suivante pour une action.
    private static final int ACTION_FRAME_DELAY = 4;

    // Position horizontale actuelle de l'unité dans le repère logique.
    private volatile int x;
    // Position verticale actuelle de l'unité dans le repère logique.
    private volatile int y;
    
    // Indique si l'unité essaie actuellement de monter.
    private volatile boolean moveUp = false;
    // Indique si l'unité essaie actuellement de descendre.
    private volatile boolean moveDown = false;
    // Indique si l'unité essaie actuellement d'aller à gauche.
    private volatile boolean moveLeft = false;
    // Indique si l'unité essaie actuellement d'aller à droite.
    private volatile boolean moveRight = false;

    // Rayon logique de la zone d'influence du joueur dans la ferme.
    public static final int INFLUENCE_RADIUS = 50;

    /*
     * La vitesse peut maintenant varier selon le sol sous le joueur.
     * On garde tout de meme une seule valeur simple par frame
     * pour ne pas compliquer inutilement le déplacement.
     */
    private volatile int currentSpeed = NORMAL_SPEED;
    // Carte de collision actuellement utilisée par l'unité.
    private MovementCollisionMap fieldObstacleMap;

    // État de combat associé à cette unité.
    private final CombatUnit combatUnit;

    // La scene courante dit sur quelle carte la meme unite evolue.
    // Cela evite de dupliquer l'etat du joueur entre ferme et grotte.
    private volatile PlayerScene scene = PlayerScene.FARM;
    // Dernière direction visuelle portée par l'unité.
    private volatile FacingDirection facingDirection = FacingDirection.DOWN;
    // Animation actuellement affichée pour l'unité.
    private volatile SpriteAnimation spriteAnimation = SpriteAnimation.IDLE;
    // Index de l'image actuellement affichée dans l'animation.
    private volatile int animationFrameIndex = 0;
    // Compteur interne utilisé pour ralentir l'avancement des animations.
    private volatile int animationTick = 0;

    /**
     * On crée une unité mobile à la position donnée avec tout son état de base.
     */
    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
        combatUnit = new CombatUnit();
    }

    /**
     * On fait avancer l'unité selon les directions actives, les collisions et l'animation courante.
     */
    public synchronized void updatePosition() {
        // Une animation d'action bloque le déplacement tant qu'elle n'est pas terminée.
        if (spriteAnimation.isOneShot()) {
            updateOneShotAnimation();
            return;
        }

        int stepX = 0;
        int stepY = 0;
        // On ne garde qu'une direction de déplacement à la fois selon l'ordre de priorité existant.
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

        // Si l'unité a bougé, on fait vivre l'animation de marche,
        // sinon on la remet sur son état immobile.
        if (stepX != 0 || stepY != 0) {
            updateWalkingAnimation();
        } else {
            setIdleAnimation();
        }
    }

    /**
     * On renvoie l'abscisse courante de l'unité.
     */
    public synchronized int getX() { return x; }
    /**
     * On renvoie l'ordonnée courante de l'unité.
     */
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

    /**
     * On force l'unité à rester dans les bornes autorisées.
     */
    public synchronized void clampPosition(int minX, int maxX, int minY, int maxY) {
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }
    
    /**
     * On active ou non le déplacement vers le haut, en mettant aussi à jour l'orientation si besoin.
     */
    public void setMoveUp(boolean moveUp) { setMoveUp(moveUp, true); }
    /**
     * On active ou non le déplacement vers le bas, en mettant aussi à jour l'orientation si besoin.
     */
    public void setMoveDown(boolean moveDown) { setMoveDown(moveDown, true); }
    /**
     * On active ou non le déplacement vers la gauche, en mettant aussi à jour l'orientation si besoin.
     */
    public void setMoveLeft(boolean moveLeft) { setMoveLeft(moveLeft, true); }
    /**
     * On active ou non le déplacement vers la droite, en mettant aussi à jour l'orientation si besoin.
     */
    public void setMoveRight(boolean moveRight) { setMoveRight(moveRight, true); }
    /**
     * On remplace la vitesse courante utilisée pour les prochains déplacements.
     */
    public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
    /**
     * On branche la carte de collision qui servira à valider les déplacements.
     */
    public void setFieldObstacleMap(MovementCollisionMap fieldObstacleMap) { this.fieldObstacleMap = fieldObstacleMap; }

    /**
     * On renvoie l'état de combat associé à cette unité.
     */
    public CombatUnit getCombatUnit() {
        return combatUnit;
    }

    /**
     * On renvoie l'orientation actuellement portée par l'unité.
     */
    public FacingDirection getFacingDirection() {
        return facingDirection;
    }

    /**
     * On renvoie l'animation actuellement jouée.
     */
    public synchronized SpriteAnimation getSpriteAnimation() {
        return spriteAnimation;
    }

    /**
     * On renvoie l'index de l'image courante dans l'animation active.
     */
    public synchronized int getAnimationFrameIndex() {
        return animationFrameIndex;
    }

    /**
     * On remplace l'orientation actuelle si une direction valide est fournie.
     */
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

    /**
     * On renvoie la scène courante dans laquelle évolue l'unité.
     */
    public PlayerScene getScene() {
        return scene;
    }

    /**
     * On met à jour le flag de déplacement haut, avec option de mise à jour de l'orientation.
     */
    public void setMoveUp(boolean moveUp, boolean updateFacing) {
        this.moveUp = moveUp;
        if (moveUp && updateFacing) {
            setFacingDirection(FacingDirection.UP);
        }
    }

    /**
     * On met à jour le flag de déplacement bas, avec option de mise à jour de l'orientation.
     */
    public void setMoveDown(boolean moveDown, boolean updateFacing) {
        this.moveDown = moveDown;
        if (moveDown && updateFacing) {
            setFacingDirection(FacingDirection.DOWN);
        }
    }

    /**
     * On met à jour le flag de déplacement gauche, avec option de mise à jour de l'orientation.
     */
    public void setMoveLeft(boolean moveLeft, boolean updateFacing) {
        this.moveLeft = moveLeft;
        if (moveLeft && updateFacing) {
            setFacingDirection(FacingDirection.LEFT);
        }
    }

    /**
     * On met à jour le flag de déplacement droite, avec option de mise à jour de l'orientation.
     */
    public void setMoveRight(boolean moveRight, boolean updateFacing) {
        this.moveRight = moveRight;
        if (moveRight && updateFacing) {
            setFacingDirection(FacingDirection.RIGHT);
        }
    }

    /**
     * On fait avancer l'animation de marche en gardant la bonne direction affichée.
     */
    private void updateWalkingAnimation() {
        SpriteAnimation desiredAnimation = resolveWalkingAnimation();
        // Si l'on change de direction visuelle, on réinitialise proprement l'animation correspondante.
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

    /**
     * On fait progresser une animation d'action jusqu'à son retour automatique à l'immobile.
     */
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

    /**
     * On replace l'unité sur son animation immobile si besoin.
     */
    private void setIdleAnimation() {
        if (spriteAnimation != SpriteAnimation.IDLE) {
            spriteAnimation = SpriteAnimation.IDLE;
            animationTick = 0;
        }
    }

    /**
     * On choisit l'animation de marche qui correspond au flag de déplacement actif.
     */
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
