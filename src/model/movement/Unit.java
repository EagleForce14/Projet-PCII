package model.movement;

import model.Combat.CombatUnit;

/**
 * Représente une entité déplaçable.
 */
public class Unit {
    public static final int SIZE = 30;
    public static final int NORMAL_SPEED = 3;
    public static final int PATH_SPEED = 5;

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
    private CombatUnit combatUnit;

    // Attribut qui indique si l'unité est dans la grotte ou non
    private volatile boolean inCave = false;

    // Le constructeur de la classe.
    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
        combatUnit = new CombatUnit();
    }

    // On met à jour la position selon le flag activé
    public synchronized void updatePosition() {
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

    // Force l'unité à rester dans les bornes visibles de la fenêtre.
    public synchronized void clampPosition(int minX, int maxX, int minY, int maxY) {
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }
    
    public void setMoveUp(boolean moveUp) { this.moveUp = moveUp; }
    public void setMoveDown(boolean moveDown) { this.moveDown = moveDown; }
    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; }
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; }
    public void setCurrentSpeed(int currentSpeed) { this.currentSpeed = currentSpeed; }
    public void setFieldObstacleMap(MovementCollisionMap fieldObstacleMap) { this.fieldObstacleMap = fieldObstacleMap; }

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
        if (inCave) {
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
        return inCave;
    }

    /** 
     * Méthode qui permet de faire rentrer l'unité dans la grotte 
     */
    public void enterCave() {
        inCave = true;
    }

    /** 
     * Méthode qui permet de faire sortir l'unité de la grotte 
     */
    public void exitCave() {
        inCave = false;
    }
}
