package model.enemy;

import model.culture.GrilleCulture;
import model.environment.FieldObstacleMap;
import model.grotte.GrotteMap;
import model.movement.MovementCollisionMap;
import model.movement.Unit;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Modèle de gestion des ennemis (IA ennemie).
 */
public class EnemyModel {
    public enum WorldType {
        FARM,
        CAVE
    }

    // La liste des unités ennemies présentes.
    private final List<EnemyUnit> enemies;
    // Associe chaque case de culture à l'unique lapin autorisé à la viser.
    private final Map<Point, EnemyUnit> reservedCultures;
    private final WorldType worldType;
    private GrilleCulture grilleCulture;
    private GrotteMap grotteMap;
    private volatile int viewportWidth = 1280;
    private volatile int viewportHeight = 720;
    private volatile int fieldWidth = 900;
    private volatile int fieldHeight = 540;
    
    // Constante pour limiter le nombre maximum d'ennemis sur la carte
    private static final int MAX_ENEMIES = 12;
    private static final int CAVE_CORNER_POST_SIZE = 4;
    
    private int spawnTimer = 0;
    private final Random random = new Random();
    
    // Référence au joueur pour la fuite
    private Unit player;
    private FieldObstacleMap fieldObstacleMap;
    private MovementCollisionMap movementCollisionMap;
    private volatile boolean caveActive = false;
    private volatile boolean cavePopulationInitialized = false;

    public EnemyModel() {
        this(WorldType.FARM, null);
    }

    public static EnemyModel createCaveModel(GrotteMap grotteMap) {
        return new EnemyModel(WorldType.CAVE, grotteMap);
    }

    private EnemyModel(WorldType worldType, GrotteMap grotteMap) {
        // Remarque : On utilise CopyOnWriteArrayList pour éviter les ConcurrentModificationException
        // entre le thread de rendu (qui lit la liste) et le thread physique (qui ajoute des ennemis)
        this.enemies = new CopyOnWriteArrayList<>();
        this.reservedCultures = new HashMap<>();
        this.worldType = worldType;
        this.grotteMap = grotteMap;
    }
    
    public void setPlayer(Unit player) {
        this.player = player;
    }

    public void setGrilleCulture(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
    }

    public void setFieldObstacleMap(FieldObstacleMap fieldObstacleMap) {
        this.fieldObstacleMap = fieldObstacleMap;
        this.movementCollisionMap = fieldObstacleMap;
    }

    public void setMovementCollisionMap(MovementCollisionMap movementCollisionMap) {
        this.movementCollisionMap = movementCollisionMap;
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
        if (worldType == WorldType.CAVE) {
            updateCaveEnemies();
            return;
        }

        int currentViewportWidth = viewportWidth;
        int currentViewportHeight = viewportHeight;
        int currentFieldWidth = fieldWidth;
        int currentFieldHeight = fieldHeight;

        // Apparition aléatoire de nouveaux ennemis
        spawnTimer--;
        if (spawnTimer <= 0) {
            if (enemies.size() < MAX_ENEMIES) { // On limite le nombre d'ennemis sur la carte
                enemies.add(new EnemyUnit(
                        currentViewportWidth,
                        currentViewportHeight,
                        currentFieldWidth,
                        currentFieldHeight,
                        grilleCulture,
                        grilleCulture.getGestionnaireObjectifs(),
                        fieldObstacleMap
                ));
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

    /**
     * Active la population de monstres de la grotte.
     * On regénère la répartition à chaque entrée pour rester simple et déterministe.
     */
    public synchronized void enterCave() {
        if (worldType != WorldType.CAVE) {
            return;
        }

        caveActive = true;
        cavePopulationInitialized = false;
        enemies.clear();
    }

    public synchronized void exitCave() {
        if (worldType != WorldType.CAVE) {
            return;
        }

        caveActive = false;
        cavePopulationInitialized = false;
        enemies.clear();
    }
    
    public List<EnemyUnit> getEnemyUnits() {
        return enemies;
    }

    private void updateCaveEnemies() {
        if (!caveActive) {
            return;
        }

        if (!cavePopulationInitialized) {
            populateCaveEnemies();
        }

        int currentViewportWidth = viewportWidth;
        int currentViewportHeight = viewportHeight;
        int currentFieldWidth = fieldWidth;
        int currentFieldHeight = fieldHeight;

        for (EnemyUnit enemy : enemies) {
            enemy.update(
                    this,
                    player,
                    null,
                    currentViewportWidth,
                    currentViewportHeight,
                    currentFieldWidth,
                    currentFieldHeight
            );
        }
    }

    private void populateCaveEnemies() {
        if (grotteMap == null || fieldWidth <= 0 || fieldHeight <= 0) {
            return;
        }

        enemies.clear();
        Rectangle caveRoamingBounds = buildGlobalLogicalRoamingBounds();

        for (CaveGuardPost post : buildCaveGuardPosts()) {
            Rectangle spawnBounds = buildLogicalSpawnBounds(post.gridBounds);
            if (spawnBounds == null || caveRoamingBounds == null) {
                continue;
            }

            enemies.add(new EnemyUnit(spawnBounds, caveRoamingBounds, post.roomIndex, movementCollisionMap));
        }

        cavePopulationInitialized = true;
    }

    /**
     * On peuple les coins des salles avec plus de densité qu'avant,
     * puis on ajoute quelques postes supplémentaires autour des zones importantes.
     */
    private List<CaveGuardPost> buildCaveGuardPosts() {
        List<CaveGuardPost> posts = new ArrayList<>();
        addSelectedCornerPosts(posts, 0, grotteMap.getWaterRoomBounds(), true, true, true, true);
        addSelectedCornerPosts(posts, 1, grotteMap.getShrineRoomBounds(), true, true, false, false);
        addSelectedCornerPosts(posts, 2, grotteMap.getLavaRoomBounds(), true, true, true, true);
        addSelectedCornerPosts(posts, 3, grotteMap.getWorkshopRoomBounds(), true, false, true, true);
        addSelectedCornerPosts(posts, 4, grotteMap.getStorageRoomBounds(), false, true, true, true);

        addLavaChestPosts(posts, 2, grotteMap.getLavaRoomBounds());
        return posts;
    }

    /**
     * On choisit explicitement quels coins garder pour éviter de surpeupler
     * les bords proches de l'axe central.
     */
    private void addSelectedCornerPosts(
            List<CaveGuardPost> posts,
            int roomIndex,
            Rectangle roomBounds,
            boolean includeTopLeft,
            boolean includeTopRight,
            boolean includeBottomLeft,
            boolean includeBottomRight
    ) {
        if (roomBounds == null) {
            return;
        }

        int postWidth = Math.max(2, Math.min(CAVE_CORNER_POST_SIZE, roomBounds.width - 2));
        int postHeight = Math.max(2, Math.min(CAVE_CORNER_POST_SIZE, roomBounds.height - 2));
        int left = roomBounds.x + 1;
        int top = roomBounds.y + 1;
        int right = roomBounds.x + roomBounds.width - postWidth - 1;
        int bottom = roomBounds.y + roomBounds.height - postHeight - 1;

        if (includeTopLeft) {
            posts.add(new CaveGuardPost(roomIndex, new Rectangle(left, top, postWidth, postHeight)));
        }
        if (includeTopRight) {
            posts.add(new CaveGuardPost(roomIndex, new Rectangle(right, top, postWidth, postHeight)));
        }
        if (includeBottomLeft) {
            posts.add(new CaveGuardPost(roomIndex, new Rectangle(left, bottom, postWidth, postHeight)));
        }
        if (includeBottomRight) {
            posts.add(new CaveGuardPost(roomIndex, new Rectangle(right, bottom, postWidth, postHeight)));
        }
    }

    /**
     * Le coffre actuel est dans la salle de lave, vers le coin haut droit.
     * On ajoute donc deux zones de garde serrées autour de cette zone.
     */
    private void addLavaChestPosts(List<CaveGuardPost> posts, int roomIndex, Rectangle lavaRoomBounds) {
        if (lavaRoomBounds == null) {
            return;
        }

        int postWidth = Math.max(2, Math.min(2, lavaRoomBounds.width - 2));
        int postHeight = Math.max(2, Math.min(2, lavaRoomBounds.height - 2));
        int nearChestX = lavaRoomBounds.x + lavaRoomBounds.width - postWidth - 2;
        int topGuardY = lavaRoomBounds.y + 1;
        int innerGuardY = lavaRoomBounds.y + Math.max(2, (lavaRoomBounds.height / 2) - 1);

        posts.add(new CaveGuardPost(roomIndex, new Rectangle(nearChestX, topGuardY, postWidth, postHeight)));
        posts.add(new CaveGuardPost(roomIndex, new Rectangle(nearChestX - 2, innerGuardY, postWidth, postHeight)));
    }

    /**
     * Petite zone locale utilisée uniquement pour faire apparaître les monstres
     * près de leurs postes de garde.
     */
    private Rectangle buildLogicalSpawnBounds(Rectangle gridBounds) {
        if (gridBounds == null || grotteMap == null) {
            return null;
        }

        double tileWidth = fieldWidth / (double) grotteMap.getWidth();
        double tileHeight = fieldHeight / (double) grotteMap.getHeight();
        Rectangle logicalBounds = new Rectangle(
                (int) Math.round((-fieldWidth / 2.0) + (gridBounds.x * tileWidth)),
                (int) Math.round((-fieldHeight / 2.0) + (gridBounds.y * tileHeight)),
                Math.max(1, (int) Math.round(gridBounds.width * tileWidth)),
                Math.max(1, (int) Math.round(gridBounds.height * tileHeight))
        );

        return new Rectangle(
                logicalBounds.x,
                logicalBounds.y,
                logicalBounds.width,
                logicalBounds.height
        );
    }

    /**
     * Tous les monstres peuvent ensuite patrouiller dans l'ensemble de la grotte.
     * Le rectangle retourné couvre toute la zone marchable globale ;
     * les murs et objets centraux restent gérés par la collision réelle.
     */
    private Rectangle buildGlobalLogicalRoamingBounds() {
        if (grotteMap == null) {
            return null;
        }

        int minColumn = Integer.MAX_VALUE;
        int minRow = Integer.MAX_VALUE;
        int maxColumn = Integer.MIN_VALUE;
        int maxRow = Integer.MIN_VALUE;

        for (int row = 0; row < grotteMap.getHeight(); row++) {
            for (int column = 0; column < grotteMap.getWidth(); column++) {
                if (!grotteMap.isWalkableCell(column, row)) {
                    continue;
                }
                minColumn = Math.min(minColumn, column);
                minRow = Math.min(minRow, row);
                maxColumn = Math.max(maxColumn, column);
                maxRow = Math.max(maxRow, row);
            }
        }

        if (minColumn == Integer.MAX_VALUE) {
            return null;
        }

        return buildLogicalSpawnBounds(new Rectangle(
                minColumn,
                minRow,
                (maxColumn - minColumn) + 1,
                (maxRow - minRow) + 1
        ));
    }

    private static final class CaveGuardPost {
        private final int roomIndex;
        private final Rectangle gridBounds;

        private CaveGuardPost(int roomIndex, Rectangle gridBounds) {
            this.roomIndex = roomIndex;
            this.gridBounds = gridBounds == null ? null : new Rectangle(gridBounds);
        }
    }
}
