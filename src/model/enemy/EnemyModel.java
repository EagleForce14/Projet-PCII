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
    // La liste des unités ennemies présentes.
    private final List<EnemyUnit> enemies;
    // Dans la ferme, une culture ne peut être visée que par un seul lapin à la fois.
    private final Map<Point, EnemyUnit> reservedCultures;
    // Le contexte global détermine si ce modèle orchestre la ferme ou la grotte.
    private final WorldType worldType;
    // La grille de culture n'est utile que pour la logique de ferme.
    private GrilleCulture grilleCulture;
    // La carte de grotte sert à placer et organiser les monstres souterrains.
    private final GrotteMap grotteMap;
    // Largeur visible courante utilisée par les spawns et les sorties d'écran.
    private volatile int viewportWidth = 1280;
    // Hauteur visible courante utilisée par les spawns et les sorties d'écran.
    private volatile int viewportHeight = 720;
    // Largeur logique du terrain utilisée pour les conversions de coordonnées.
    private volatile int fieldWidth = 900;
    // Hauteur logique du terrain utilisée pour les conversions de coordonnées.
    private volatile int fieldHeight = 540;
    
    // Nombre maximal de lapins de ferme présents en même temps.
    private static final int MAX_ENEMIES = 12;
    // Taille logique d'un petit poste de garde placé dans un coin de salle.
    private static final int CAVE_CORNER_POST_SIZE = 4;
    
    // Compte à rebours avant la prochaine tentative d'apparition d'un ennemi.
    private int spawnTimer = 0;
    // Source d'aléatoire partagée pour les spawns et les petites variations d'IA.
    private final Random random = new Random();
    
    // Référence au joueur pour la fuite
    private Unit player;
    // Obstacles propres à la ferme, utilisés notamment par les lapins.
    private FieldObstacleMap fieldObstacleMap;
    // Carte de collision réellement consultée par l'ennemi dans son monde courant.
    private MovementCollisionMap movementCollisionMap;
    // Indique si la population de grotte doit actuellement être simulée.
    private volatile boolean caveActive = false;
    // Indique si les monstres de grotte ont déjà été recréés pour l'entrée en cours.
    private volatile boolean cavePopulationInitialized = false;

    /**
     * On construit ici le modèle ennemi standard utilisé pour la ferme.
     */
    public EnemyModel() {
        this(WorldType.FARM, null);
    }

    /**
     * On fabrique ici la variante dédiée à la grotte avec sa carte propre.
     */
    public static EnemyModel createCaveModel(GrotteMap grotteMap) {
        return new EnemyModel(WorldType.CAVE, grotteMap);
    }

    /**
     * On initialise la structure commune du modèle avant toute apparition d'ennemis.
     */
    private EnemyModel(WorldType worldType, GrotteMap grotteMap) {
        // La liste reste sûre à lire pendant que la logique de jeu ajoute ou retire des unités.
        this.enemies = new CopyOnWriteArrayList<>();
        this.reservedCultures = new HashMap<>();
        this.worldType = worldType;
        this.grotteMap = grotteMap;
    }
    
    /**
     * On mémorise le joueur pour que les ennemis puissent réagir à sa présence.
     */
    public void setPlayer(Unit player) {
        this.player = player;
    }

    /**
     * On branche la grille de culture que les lapins vont surveiller et viser.
     */
    public void setGrilleCulture(GrilleCulture grilleCulture) {
        this.grilleCulture = grilleCulture;
    }

    /**
     * On fournit ici les obstacles de ferme, qui servent aussi de collision de déplacement.
     */
    public void setFieldObstacleMap(FieldObstacleMap fieldObstacleMap) {
        this.fieldObstacleMap = fieldObstacleMap;
        this.movementCollisionMap = fieldObstacleMap;
    }

    /**
     * On remplace explicitement la carte de collision utilisée par les ennemis.
     */
    public void setMovementCollisionMap(MovementCollisionMap movementCollisionMap) {
        this.movementCollisionMap = movementCollisionMap;
    }

    /**
     * On met à jour la taille visible pour les apparitions et les sorties d'écran.
     */
    public void setViewportSize(int viewportWidth, int viewportHeight) {
        if (viewportWidth > 0) {
            this.viewportWidth = viewportWidth;
        }
        if (viewportHeight > 0) {
            this.viewportHeight = viewportHeight;
        }
    }

    /**
     * On met à jour la taille logique du terrain utilisée par l'IA.
     */
    public void setFieldSize(int fieldWidth, int fieldHeight) {
        if (fieldWidth > 0) {
            this.fieldWidth = fieldWidth;
        }
        if (fieldHeight > 0) {
            this.fieldHeight = fieldHeight;
        }
    }

    /**
     * Le combat de la grotte a besoin de convertir une position logique
     * en coordonnées de case.
     * On expose donc uniquement les dimensions déjà calculées par la vue,
     * sans lui donner accès à d'autres détails d'implémentation.
     */
    public int getFieldWidth() {
        return fieldWidth;
    }

    /**
     * On expose aussi la hauteur logique déjà calculée pour les conversions de la vue.
     */
    public int getFieldHeight() {
        return fieldHeight;
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
            // Un nouvel ennemi apparaît toutes les 2 à 5 secondes (à 60 FPS)
            spawnTimer = 120 + random.nextInt(180); 
        }
        
        // Mise à jour de tous les ennemis
        for (EnemyUnit enemy : enemies) {
            enemy.update(this, player, grilleCulture, currentViewportWidth, currentViewportHeight, currentFieldWidth, currentFieldHeight);
            
            // Si l'ennemi a fui en dehors de la carte ou a mangé une culture, on le supprime.
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

        // Sinon, on mémorise que cette case appartient à ce lapin.
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
     * On régénère la répartition à chaque entrée pour rester simple et déterministe.
     */
    public synchronized void enterCave() {
        if (worldType != WorldType.CAVE) {
            return;
        }

        caveActive = true;
        cavePopulationInitialized = false;
        enemies.clear();
    }

    /**
     * On coupe ici l'activité de la grotte et on vide sa population courante.
     */
    public synchronized void exitCave() {
        if (worldType != WorldType.CAVE) {
            return;
        }

        caveActive = false;
        cavePopulationInitialized = false;
        enemies.clear();
    }
    
    /**
     * On expose la liste vivante pour le rendu et les autres lectures d'état.
     */
    public List<EnemyUnit> getEnemyUnits() {
        return enemies;
    }

    /**
     * Suppression explicite utilisée par le combat de la grotte.
     * On garde cette opération centralisée dans le modèle des ennemis
     * pour éviter qu'une autre couche manipule directement la collection interne.
     */
    public void removeEnemy(EnemyUnit enemy) {
        if (enemy != null) {
            enemies.remove(enemy);
        }
    }

    /**
     * On met à jour uniquement les monstres actifs de la grotte et on enlève ceux qui sont morts.
     */
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
            if (!enemy.isAlive()) {
                enemies.remove(enemy);
                continue;
            }
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

    /**
     * On recrée ici toute la population de grotte à partir des postes de garde définis.
     */
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
        addSelectedCornerPosts(posts, 0, grotteMap.getUpperLeftRoomBounds(), true, true, true, true);
        addSelectedCornerPosts(posts, 1, grotteMap.getShrineRoomBounds(), true, true, false, false);
        addSelectedCornerPosts(posts, 2, grotteMap.getUpperRightRoomBounds(), true, true, true, true);
        addSelectedCornerPosts(posts, 3, grotteMap.getLowerLeftRoomBounds(), true, false, true, true);
        addSelectedCornerPosts(posts, 4, grotteMap.getLowerRightRoomBounds(), false, true, true, true);
        return posts;
    }

    /**
     * On choisit explicitement quels coins sont à surveiller pour éviter de surpeupler
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
     * Le rectangle retourné couvre toute la zone "marchable" globale ;
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

}
