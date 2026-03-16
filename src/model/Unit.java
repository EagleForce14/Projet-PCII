package model;

/**
 * Représente une entité déplaçable.
 */
public class Unit {
    public static final int SIZE = 30;

    // On rappelle que volatile assure que les modifications sont immédiatement visibles par tous les threads
    private volatile int x;
    private volatile int y;
    
    // Flags de mouvement propre à cette unité
    private volatile boolean moveUp = false;
    private volatile boolean moveDown = false;
    private volatile boolean moveLeft = false;
    private volatile boolean moveRight = false;

    // Rayon de la zone d'influence
    public static final int INFLUENCE_RADIUS = 100;

    public Unit(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // On met à jour la position selon le flag activé
    public synchronized void updatePosition() {
        int nextX = x;
        int nextY = y;

        // La vitesse de déplacement
        int SPEED = 3;
        if (moveUp) {
            nextY -= SPEED;
        } else if (moveDown) {
            nextY += SPEED;
        } else if (moveLeft) {
            nextX -= SPEED;
        } else if (moveRight) {
            nextX += SPEED;
        }

        // Collision grange désactivée: l'unité traverse librement.
        x = nextX;
        y = nextY;
    }

    // Getters et Setters
    public synchronized int getX() { return x; }
    public synchronized int getY() { return y; }

    // Force l'unité à rester dans les bornes visibles de la fenêtre.
    public synchronized void clampPosition(int minX, int maxX, int minY, int maxY) {
        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
    }
    
    public void setMoveUp(boolean moveUp) { this.moveUp = moveUp; }
    public void setMoveDown(boolean moveDown) { this.moveDown = moveDown; }
    public void setMoveLeft(boolean moveLeft) { this.moveLeft = moveLeft; }
    public void setMoveRight(boolean moveRight) { this.moveRight = moveRight; }
    
    public boolean isMoving() {
        return moveUp || moveDown || moveLeft || moveRight;
    }

    /**
     * Vérifie si une position donnée (x, y) est dans la zone d'influence de l'unité.
     * @param targetX Coordonnée X de la cible
     * @param targetY Coordonnée Y de la cible
     * @return true si la cible est dans le rayon d'influence, false sinon.
     */
    public boolean isInInfluenceZone(int targetX, int targetY) {
        // Remarque : on calcule la distance au carré pour éviter d'utiliser la racine carrée coûteuse
        int dx = this.x - targetX;
        int dy = this.y - targetY;
        return (dx * dx + dy * dy) <= (INFLUENCE_RADIUS * INFLUENCE_RADIUS);
    }
}
