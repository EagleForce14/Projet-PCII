package model;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modèle de gestion des ennemis (IA ennemie).
 */
public class EnemyModel {
    // La liste des unités ennemies présentes.
    private final List<EnemyUnit> enemies;
    // Associe chaque case de culture à l'unique lapin autorisé à la viser.
    private final Map<Point, EnemyUnit> reservedCultures;
    private GrilleCulture grilleCulture;
    private volatile int viewportWidth = 1280;
    private volatile int viewportHeight = 720;
    private volatile int fieldWidth = 900;
    private volatile int fieldHeight = 540;
    
    // Constante pour limiter le nombre maximum d'ennemis sur la carte
    private static final int MAX_ENEMIES = 12;
    
    private int spawnTimer = 0;
    private final Random random = new Random();
    
    // Référence au joueur pour la fuite
    private Unit player;

    public EnemyModel() {
        // Remarque : On utilise CopyOnWriteArrayList pour éviter les ConcurrentModificationException
        // entre le thread de rendu (qui lit la liste) et le thread physique (qui ajoute des ennemis)
        enemies = new CopyOnWriteArrayList<>();
        reservedCultures = new HashMap<>();
    }
    
    public void setPlayer(Unit player) {
        this.player = player;
    }

    public void setGrilleCulture(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
    }

    public void setViewportSize(int viewportWidth, int viewportHeight) {
        if (viewportWidth > 0) {
            this.viewportWidth = viewportWidth;
        }
        if (viewportHeight > 0) {
            this.viewportHeight = viewportHeight;
        }
    }

    public void setFieldSize(int fieldWidth, int fieldHeight) {
        if (fieldWidth > 0) {
            this.fieldWidth = fieldWidth;
        }
        if (fieldHeight > 0) {
            this.fieldHeight = fieldHeight;
        }
    }
    
    /**
     * Met à jour l'état du modèle (apparition et déplacement des ennemis).
     */
    public void update() {
        int currentViewportWidth = viewportWidth;
        int currentViewportHeight = viewportHeight;
        int currentFieldWidth = fieldWidth;
        int currentFieldHeight = fieldHeight;

        // Apparition aléatoire de nouveaux ennemis
        spawnTimer--;
        if (spawnTimer <= 0) {
            if (enemies.size() < MAX_ENEMIES) { // On limite le nombre d'ennemis sur la carte
                enemies.add(new EnemyUnit(currentViewportWidth, currentViewportHeight, currentFieldWidth, currentFieldHeight));
            }
            // Un nouveau ennemi apparaît toutes les 2 à 5 secondes (à 60 FPS)
            spawnTimer = 120 + random.nextInt(180); 
        }
        
        // Mise à jour de tous les ennemis
        for (EnemyUnit enemy : enemies) {
            enemy.update(this, player, grilleCulture, currentViewportWidth, currentViewportHeight, currentFieldWidth, currentFieldHeight);
            
            // Si le ennemi a fui en dehors de la carte ou a mangé une culture, on le supprime.
            if (enemy.hasFled()) {
                enemies.remove(enemy);
            }
        }
    }

    /**
     * Réserve une culture pour un lapin donné.
     * Renvoie false si la case est déjà réservée par un autre lapin.
     */
    public synchronized boolean reserveCulture(int gridX, int gridY, EnemyUnit enemy) {
        // La clé est la position logique de la culture dans la grille.
        Point cell = new Point(gridX, gridY);
        EnemyUnit reserver = reservedCultures.get(cell);
        // Si un autre lapin a déjà réservé cette culture, on la refuse.
        if (reserver != null && reserver != enemy) {
            return false;
        }

        // Sinon on mémorise que cette case appartient à ce lapin.
        reservedCultures.put(cell, enemy);
        return true;
    }

    /**
     * Libère la réservation d'une culture si elle appartient encore à ce lapin.
     */
    public synchronized void releaseCultureReservation(int gridX, int gridY, EnemyUnit enemy) {
        Point cell = new Point(gridX, gridY);
        // On libère uniquement si c'est bien ce lapin qui détenait la réservation.
        // Cela évite qu'un autre lapin efface la réservation en cours par erreur.
        if (reservedCultures.get(cell) == enemy) {
            reservedCultures.remove(cell);
        }
    }
    
    public List<EnemyUnit> getEnemyUnits() {
        return enemies;
    }
}
