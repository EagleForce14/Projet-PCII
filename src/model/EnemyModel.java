package model;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modèle de gestion des ennemis (IA ennemie).
 */
public class EnemyModel {
    // La liste des unités ennemies présentes.
    private final List<EnemyUnit> enemies;
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
    }
    
    public void setPlayer(Unit player) {
        this.player = player;
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
            enemy.update(player, currentViewportWidth, currentViewportHeight, currentFieldWidth, currentFieldHeight);
            
            // Si le ennemi a fui en dehors de la carte, on le supprime
            if (enemy.hasFled()) {
                enemies.remove(enemy);
            }
        }
    }
    
    public List<EnemyUnit> getEnemyUnits() {
        return enemies;
    }
    
    public int getViewportWidth() { return viewportWidth; }
    public int getViewportHeight() { return viewportHeight; }
    public int getFieldWidth() { return fieldWidth; }
    public int getFieldHeight() { return fieldHeight; }
}
