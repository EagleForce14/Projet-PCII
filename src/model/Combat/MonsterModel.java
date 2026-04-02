//package model.Combat;
//
//import model.Combat.GrilleGrotte;
//import model.environment.FieldObstacleMap;
//import model.movement.Unit;
//import model.Combat.MonsterUnit;
//
//
//import java.awt.Point;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.CopyOnWriteArrayList;
//
///**
// * Modèle de gestion des ennemis (IA ennemie).
// */
//public class MonsterModel {
//    // La liste des unités ennemies présentes.
//    private final List<MonsterUnit> monsters;
//
//    // peut-être un attribut pour chercher les coordonnées du joueur et les monstre l'attaque)
//
//
//
//    // pas grille culture mais grille grotte
//
//    // TODO : Faire La classe GrilleGrotte pour gérer les cases de la grotte
//    private GrilleGrotte  GrilleGrotte;
//    private volatile int viewportWidth = 1280;
//    private volatile int viewportHeight = 720;
//    private volatile int fieldWidth = 900;
//    private volatile int fieldHeight = 540;
//
//    // Constante pour limiter le nombre maximum d'ennemis sur la carte
//    private static final int MAX_ENEMIES = 12;
//
//    private int spawnTimer = 0;
//    private final Random random = new Random();
//
//    // Référence au joueur pour la fuite
//    private Unit player;
//    private FieldObstacleMap fieldObstacleMap;
//
//    public MonsterModel() {
//        // Remarque : On utilise CopyOnWriteArrayList pour éviter les ConcurrentModificationException
//        // entre le thread de rendu (qui lit la liste) et le thread physique (qui ajoute des ennemis)
//        monsters = new CopyOnWriteArrayList<>();
//
//    }
//
//    public void setPlayer(Unit player) {
//        this.player = player;
//    }
//
//    public void setGrilleGrotte(GrilleGrotte grilleGrotte) {
//        this.grilleGrotte = grilleGrotte;
//    }
//
//    public void setFieldObstacleMap(FieldObstacleMap fieldObstacleMap) {
//        this.fieldObstacleMap = fieldObstacleMap;
//    }
//
//    public void setViewportSize(int viewportWidth, int viewportHeight) {
//        if (viewportWidth > 0) {
//            this.viewportWidth = viewportWidth;
//        }
//        if (viewportHeight > 0) {
//            this.viewportHeight = viewportHeight;
//        }
//    }
//
//    public void setFieldSize(int fieldWidth, int fieldHeight) {
//        if (fieldWidth > 0) {
//            this.fieldWidth = fieldWidth;
//        }
//        if (fieldHeight > 0) {
//            this.fieldHeight = fieldHeight;
//        }
//    }
//
//    /**
//     * Met à jour l'état du modèle (apparition et déplacement des ennemis).
//     */
//    public void update() {
//        int currentViewportWidth = viewportWidth;
//        int currentViewportHeight = viewportHeight;
//        int currentFieldWidth = fieldWidth;
//        int currentFieldHeight = fieldHeight;
//
//        // Apparition aléatoire de nouveaux ennemis
//        spawnTimer--;
//        if (spawnTimer <= 0) {
//            if (monsters.size() < MAX_ENEMIES) { // On limite le nombre d'ennemis sur la carte
//                monsters.add(new MonsterUnit(
//                        currentViewportWidth,
//                        currentViewportHeight,
//                        currentFieldWidth,
//                        currentFieldHeight,
//                        grilleGrotte.getGestionnaireObjectifs(),
//                        fieldObstacleMap
//                ));
//            }
//            // Un nouveau ennemi apparaît toutes les 2 à 5 secondes (à 60 FPS)
//            spawnTimer = 120 + random.nextInt(180);
//        }
//
//        // Mise à jour de tous les ennemis
//        for (MonsterUnit monster : monsters) {
//            monster.update(this, player, grilleGrotte, currentViewportWidth, currentViewportHeight, currentFieldWidth, currentFieldHeight);
//
//            // Si le ennemi a fui en dehors de la carte ou a mangé une culture, on le supprime.
//            if (monster.hasFled()) {
//                monsters.remove(monster);
//            }
//        }
//    }
//
//
//
//
//
//    public List<MonsterUnit> getEnemyUnits() {
//        return monsters;
//    }
//}
