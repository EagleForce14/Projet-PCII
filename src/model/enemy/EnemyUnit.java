package model.enemy;

import model.Combat.CombatUnit;
import model.culture.GrilleCulture;
import model.culture.CellSide;
import model.culture.Culture;
import model.environment.FenceCollision;
import model.culture.Stade;
import model.environment.FieldObstacleMap;
import model.movement.Barn;
import model.movement.MovementCollisionMap;
import model.movement.Unit;
import model.objective.GestionnaireObjectifs;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Représente une unité ennemie pilotée par un contexte de monde unique.
 * Le monde FARM active la logique de lapin autour des cultures et des clôtures.
 * Le monde CAVE active la logique de monstre autour de la patrouille et du combat.
 */
public class EnemyUnit {
    public enum DisplaySprite {
        FRONT,
        BACK,
        LEFT,
        RIGHT,
        EATING
    }

    private enum CaveBehavior {
        PAUSE,
        PATROL,
        ALERT
    }

    // Identifiant du bord haut pour les apparitions hors écran.
    private static final int SPAWN_SIDE_TOP = 0;
    // Identifiant du bord droit pour les apparitions hors écran.
    private static final int SPAWN_SIDE_RIGHT = 1;
    // Identifiant du bord bas pour les apparitions hors écran.
    private static final int SPAWN_SIDE_BOTTOM = 2;
    // Identifiant du bord gauche pour les apparitions hors écran.
    private static final int SPAWN_SIDE_LEFT = 3;
    // Seuil minimal pour considérer qu'une direction d'affichage (orientation) n'est pas nulle (pour savoir quelle image du joueur afficher :
    // de dos, de face, à gauche ou à droite)
    private static final double DISPLAY_DIRECTION_EPSILON = 0.01;
    // Vitesse normale d'un monstre en patrouille dans la grotte.
    private static final double CAVE_PATROL_SPEED = 1.4;
    // Vitesse plus élevée utilisée quand un monstre est en alerte.
    private static final double CAVE_ALERT_SPEED = 2.0;
    // Nombre minimal de frames avant de recalculer une cible de patrouille.
    private static final int CAVE_PATROL_RETARGET_MIN_FRAMES = 28;
    // Variation aléatoire ajoutée avant de recalculer une cible de patrouille.
    private static final int CAVE_PATROL_RETARGET_RANDOM_FRAMES = 42;
    // Durée minimale d'une pause volontaire en patrouille.
    private static final int CAVE_PAUSE_MIN_FRAMES = 6;
    // Variation aléatoire de la durée des pauses de patrouille.
    private static final int CAVE_PAUSE_RANDOM_FRAMES = 22;
    // Probabilité de marquer une pause au lieu de repartir tout de suite.
    private static final double CAVE_PAUSE_PROBABILITY = 0.12;
    // Probabilité de choisir une cible de patrouille dans toute la grotte.
    private static final double CAVE_GLOBAL_PATROL_PROBABILITY = 0.32;
    // Nombre de ticks avant d'alterner la frame de marche du monstre.
    private static final int CAVE_WALK_FRAME_TOGGLE_TICKS = 8;
    // Rayon dans lequel un monstre peut repérer le joueur.
    private static final int CAVE_INFLUENCE_RADIUS = 120;
    // Portée maximale de tir d'un monstre de grotte.
    private static final int CAVE_SHOOT_RANGE = 185;
    // Temps pendant lequel l'alerte reste mémorisée après perte de vue.
    private static final long CAVE_ALERT_MEMORY_MS = 1_250L;
    // Délai minimal avant de changer de sens de strafe.
    private static final long CAVE_STRAFE_SWAP_DELAY_MS = 820L;
    // Cooldown entre deux tirs de monstre.
    private static final long CAVE_SHOT_COOLDOWN_MS = 860L;
    // Durée du flash visuel quand le monstre reçoit un coup.
    private static final long CAVE_HIT_FLASH_MS = 150L;
    // Distance minimale que le monstre essaie de garder avec le joueur.
    private static final double CAVE_COMBAT_MIN_DISTANCE = 82.0;
    // Distance maximale avant que le monstre ne se rapproche de nouveau.
    private static final double CAVE_COMBAT_MAX_DISTANCE = 138.0;
    // Distance latérale visée pendant le strafe autour du joueur.
    private static final double CAVE_COMBAT_STRAFE_DISTANCE = 30.0;
    // Vie maximale d'un monstre de grotte.
    private static final int CAVE_MAX_HEALTH = 60;
    // Dégâts de base infligés par un monstre de grotte.
    private static final int CAVE_ATTACK_POWER = 12;
    // La taille de la zone de "sécurité" autour de la boutique principale (à droite)
    // (on ne peut pas traverser cette zone i.e. on ne peut pas traverser la boutique principale).
    private static final int HITBOX_SIZE = 20;
    // Le rayon de détection d'une culture par un lapin
    private static final int CULTURE_DETECTION_RADIUS = 120;
    // Le délai de 5s avant de manger la culture une fois que l'on est positionné dessus.
    private static final long DELAI_AVANT_MANGER_MS = 5000L;
    // Le délai entre deux coups donnés à une clôture.
    private static final long DELAI_COUP_CLOTURE_MS = 900L;
    // Mouvement "bélier" : le lapin recule un peu puis revient plus vite sur la clôture.
    private static final double FENCE_ATTACK_APPROACH_SPEED = 1.5;
    // Vitesse du petit recul avant la charge suivante contre la clôture.
    private static final double FENCE_ATTACK_BACKSTEP_SPEED = 1.15;
    // Vitesse de la charge rapide du lapin contre la clôture ciblée.
    private static final double FENCE_ATTACK_CHARGE_SPEED = 3.1;
    // Distance de recul prise avant de relancer un impact de clôture.
    private static final double FENCE_ATTACK_BACKSTEP_DISTANCE = 18.0;
    // Marge d'arrivée utilisée pour considérer le point de contact atteint.
    private static final double FENCE_ATTACK_CONTACT_TOLERANCE = 0.75;
    // Marge d'arrivée un peu plus large pour la phase de recul.
    private static final double FENCE_ATTACK_BACKSTEP_TOLERANCE = 1.0;
    // Le délai de retour au terrier si le lapin n'a rien trouvé. Correspond à 30s
    private static final long DELAI_RETOUR_TERRIER_MS = 30000L;
    // Ces listes décrivent les rotations possibles autour de la direction voulue afin de contourner
    // la boutique principale (à droite).
    // On teste d'abord de petits écarts, puis des virages plus forts, puis seulement un demi-tour.
    // RIGHT_HAND_OFFSETS privilégie un contournement "main droite", LEFT_HAND_OFFSETS l'inverse.
    // Le lapin garde ainsi un côté préféré le long de la boutique principale (à droite),
    // ce qui limite les hésitations.
    private static final double[] RIGHT_HAND_OFFSETS = {
        Math.PI / 8, Math.PI / 4, (3 * Math.PI) / 8, Math.PI / 2,
        (5 * Math.PI) / 8, (3 * Math.PI) / 4, (7 * Math.PI) / 8,
        -Math.PI / 8, -Math.PI / 4, -(3 * Math.PI) / 8, -Math.PI / 2,
        -(5 * Math.PI) / 8, -(3 * Math.PI) / 4, -(7 * Math.PI) / 8,
        Math.PI
    };
    private static final double[] LEFT_HAND_OFFSETS = {
        -Math.PI / 8, -Math.PI / 4, -(3 * Math.PI) / 8, -Math.PI / 2,
        -(5 * Math.PI) / 8, -(3 * Math.PI) / 4, -(7 * Math.PI) / 8,
        Math.PI / 8, Math.PI / 4, (3 * Math.PI) / 8, Math.PI / 2,
        (5 * Math.PI) / 8, (3 * Math.PI) / 4, (7 * Math.PI) / 8,
        Math.PI
    };

    // Position courante du lapin sur l'axe horizontal.
    private volatile double x;
    // Position courante du lapin sur l'axe vertical.
    private volatile double y;
    // Cible courante vers laquelle le lapin se dirige sur l'axe horizontal.
    private volatile double targetX;
    // Cible courante vers laquelle le lapin se dirige sur l'axe vertical.
    private volatile double targetY;
    // Indique si le lapin a déjà atteint la zone du champ.
    private volatile boolean isInsideMap = false;
    // Indique si le lapin est en train de fuir un jardinier.
    private volatile boolean isFleeing = false;
    // Indique si le lapin repart vers le terrier après une recherche infructueuse.
    private volatile boolean isReturningToBurrow = false;
    // Indique si le lapin a complètement quitté l'écran après une sortie de carte.
    private volatile boolean hasFled = false;
    // Dernière position connue sur X, que l'on utilise pour détecter un blocage.
    private double lastX;
    // Dernière position connue sur Y, que l'on utilise pour détecter un blocage.
    private double lastY;
    // Compteur du nombre d'updates pendant lesquels le lapin a presque cessé de bouger.
    private int stagnantFrames = 0;
    // Dernier déplacement réellement appliqué, utile pour éviter un demi-tour immédiat.
    private double lastMoveX = 0;
    // Même idée que lastMoveX, mais pour l'axe vertical.
    private double lastMoveY = 0;
    // Côté de contournement actuellement privilégié pour longer proprement
    // la boutique principale (à droite).
    private int preferredTurnSign = 1;
    // Case de culture actuellement visée par le lapin.
    private int targetCultureGridX = -1;
    // Ligne de culture actuellement visée par le lapin.
    private int targetCultureGridY = -1;
    // Si une clôture protège la culture ciblée, le lapin garde ce segment comme cible jusqu'à sa destruction.
    private CellSide targetFenceSide = null;
    // Indique si le lapin recule avant sa prochaine charge contre la clôture.
    private boolean isFenceAttackBackingOff = false;
    // Indique si le lapin est dans sa phase de charge rapide contre la clôture.
    private boolean isFenceAttackCharging = false;
    // Instant du dernier impact réellement porté à la clôture ciblée.
    private long lastFenceHitTime = -1L;
    // Instant où le lapin a atteint la case et commence son attente de 5 secondes avant de manger.
    private long cultureWaitStartTime = -1L;
    // Début de la période pendant laquelle le lapin ne trouve aucune culture à viser.
    private long noCultureFoundStartTime = -1L;

    // Temporisateur avant de choisir une nouvelle cible de promenade.
    private int wanderTimer = 0;
    // Générateur aléatoire pour rendre les comportements moins mécaniques.
    private final Random random = new Random();
    // Le contexte de monde suffit ici à choisir toute la famille de comportements de l'unité.
    private final WorldType worldType;
    // Petite zone locale autour du point de garde initial du monstre.
    private Rectangle caveGuardBounds;
    // Zone globale dans laquelle le monstre de grotte peut patrouiller.
    private Rectangle cavePatrolBounds;
    // Position de référence en X du poste de garde du monstre.
    private double caveHomeX;
    // Position de référence en Y du poste de garde du monstre.
    private double caveHomeY;
    // Salle logique d'affectation du monstre pour l'overlay et le debug.
    private int caveRoomIndex = -1;
    // État courant du monstre entre pause, patrouille et alerte.
    private CaveBehavior caveBehavior = CaveBehavior.PATROL;
    // Compte à rebours avant de recalculer une cible de patrouille.
    private int cavePatrolRetargetTimer = 0;
    // Temps restant pendant lequel le monstre reste volontairement immobile.
    private int cavePauseTimer = 0;
    // Indique si le monstre de grotte est effectivement en train de marcher.
    private boolean caveMoving = false;
    // Direction affichée en X pour choisir le bon sprite du monstre.
    private double caveDisplayVectorX = 0.0;
    // Direction affichée en Y pour choisir le bon sprite du monstre.
    private double caveDisplayVectorY = 1.0;
    // Index courant de la frame de marche affichée.
    private int caveWalkFrameIndex = 0;
    // Compteur qui pilote l'alternance des frames de marche.
    private int caveWalkTickCounter = 0;
    
    // Largeur courante de la fenêtre de jeu.
    private int viewportWidth;
    // Hauteur courante de la fenêtre de jeu.
    private int viewportHeight;
    // Largeur utile du champ cultivable.
    private int fieldWidth;
    // Hauteur utile du champ cultivable.
    private int fieldHeight;

    // Gestionnaire d'objectifs pour mettre à jour les objectifs liés à la fuite.
    private final GestionnaireObjectifs gestionnaireObjectifs;
    // Obstacles propres à la ferme, consultés surtout par les lapins.
    private final FieldObstacleMap farmObstacleMap;
    // Carte de collision effectivement utilisée dans le monde courant.
    private final MovementCollisionMap movementCollisionMap;
    // Colonne de rivière décorative utilisée pour éviter certains choix de spawn.
    private final int decorativeRiverColumn;
    // Composant de combat partagé pour la vie et l'attaque de l'unité.
    private final CombatUnit combatUnit;

    // Indique si le monstre de grotte est actuellement en mode alerte.
    private boolean caveAggroed = false;
    // Dernière position X connue du joueur quand le monstre l'a vu.
    private double caveLastSeenPlayerX = Double.NaN;
    // Dernière position Y connue du joueur quand le monstre l'a vu.
    private double caveLastSeenPlayerY = Double.NaN;
    // Instant de la dernière vision fiable du joueur.
    private long caveLastSeenPlayerTime = -1L;
    // Instant du dernier changement de sens de strafe.
    private long caveLastStrafeSwapTime = 0L;
    // Instant du dernier tir effectué par le monstre.
    private long caveLastShotTime = Long.MIN_VALUE;
    // Instant jusqu'auquel le flash de dégât doit rester visible.
    private long caveHitFlashUntilMs = 0L;
    // Sens de strafe actuellement privilégié autour du joueur.
    private boolean caveStrafeClockwise = true;

    // On construit ici une unité ennemie avec les dimensions connues au moment de son apparition.
    public EnemyUnit(int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight, GrilleCulture grilleCulture,
                     GestionnaireObjectifs gestionnaireObjectifs, FieldObstacleMap fieldObstacleMap) {
        this.worldType = WorldType.FARM;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.gestionnaireObjectifs = gestionnaireObjectifs;
        this.farmObstacleMap = fieldObstacleMap;
        this.movementCollisionMap = fieldObstacleMap;
        this.decorativeRiverColumn = resolveDecorativeRiverColumn(grilleCulture);
        this.combatUnit = new CombatUnit();
        // On fait apparaître le lapin hors écran.
        spawnOutside();
        // On initialise la dernière position connue sur X.
        lastX = x;
        // On initialise la dernière position connue sur Y.
        lastY = y;
    }

    /**
     * Variante dédiée à la grotte.
     * Le monstre apparaît près de son poste de garde,
     * puis peut patrouiller dans toute la grotte.
     */
    public EnemyUnit(Rectangle caveSpawnBounds, Rectangle cavePatrolBounds, int roomIndex, MovementCollisionMap movementCollisionMap) {
        this.worldType = WorldType.CAVE;
        this.viewportWidth = 1280;
        this.viewportHeight = 720;
        this.fieldWidth = 900;
        this.fieldHeight = 540;
        this.gestionnaireObjectifs = null;
        this.farmObstacleMap = null;
        this.movementCollisionMap = movementCollisionMap;
        this.decorativeRiverColumn = -1;
        this.combatUnit = new CombatUnit(CAVE_MAX_HEALTH, CAVE_ATTACK_POWER);
        this.caveGuardBounds = buildCaveGuardBounds(caveSpawnBounds);
        this.cavePatrolBounds = cavePatrolBounds == null ? new Rectangle(-20, -20, 40, 40) : new Rectangle(cavePatrolBounds);
        this.caveRoomIndex = roomIndex;
        this.caveBehavior = CaveBehavior.PATROL;
        this.preferredTurnSign = random.nextBoolean() ? 1 : -1;
        this.caveStrafeClockwise = random.nextBoolean();
        this.caveLastShotTime = System.currentTimeMillis() - random.nextInt((int) CAVE_SHOT_COOLDOWN_MS);

        Point2D.Double spawnPoint = findValidCavePoint(caveSpawnBounds, 40);
        if (spawnPoint == null) {
            spawnPoint = findValidCavePoint(this.cavePatrolBounds, 80);
        }
        if (spawnPoint == null) {
            spawnPoint = new Point2D.Double(0.0, 0.0);
        }

        x = spawnPoint.x;
        y = spawnPoint.y;
        targetX = x;
        targetY = y;
        caveHomeX = x;
        caveHomeY = y;
        lastX = x;
        lastY = y;
        pickNewCavePatrolTarget();
    }

    /**
     * On tire une coordonnée flottante dans les bornes données.
     */
    private double randomCoordinateInBounds(int origin, int size) {
        if (size <= 0) {
            return origin;
        }
        return origin + (random.nextDouble() * size);
    }

    /**
     * On élargit le poste de garde pour donner au monstre une petite zone locale autour de son origine.
     */
    private Rectangle buildCaveGuardBounds(Rectangle caveSpawnBounds) {
        if (caveSpawnBounds == null) {
            return null;
        }

        int paddingX = Math.max(70, caveSpawnBounds.width);
        int paddingY = Math.max(70, caveSpawnBounds.height);
        return new Rectangle(
                caveSpawnBounds.x - paddingX,
                caveSpawnBounds.y - paddingY,
                caveSpawnBounds.width + (paddingX * 2),
                caveSpawnBounds.height + (paddingY * 2)
        );
    }

    /**
     * On cherche un point libre dans la zone donnée avant d'y faire apparaître ou patrouiller le monstre.
     */
    private Point2D.Double findValidCavePoint(Rectangle bounds, int attempts) {
        if (bounds == null) {
            return null;
        }

        for (int attempt = 0; attempt < attempts; attempt++) {
            double candidateX = randomCoordinateInBounds(bounds.x, bounds.width);
            double candidateY = randomCoordinateInBounds(bounds.y, bounds.height);
            if (!canOccupy(candidateX, candidateY)) {
                continue;
            }
            return new Point2D.Double(candidateX, candidateY);
        }

        return null;
    }
    
    /**
     * Choisit un point de spawn hors écran et une première direction plausible vers le champ.
     */
    private void spawnOutside() {
        // On le place au milieu de la fenêtre
        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;

        // Choix aléatoire du bord d'apparition.
        int side = random.nextInt(4);
        // On ajoute une petite distance pour garantir une apparition réellement hors écran.
        int offset = 50;

        placeOutsideForSide(side, halfWidth, halfHeight, offset);
        
        // Dès l'apparition, on choisit un premier point d'entrée dans le champ.
        pickFieldEntryTarget();

        if (side == SPAWN_SIDE_TOP && isRightOfDecorativeRiver(targetX)) {
            int redirectedSide = SPAWN_SIDE_RIGHT + random.nextInt(3);
            placeOutsideForSide(redirectedSide, halfWidth, halfHeight, offset);
        }
    }

    /**
     * On place l'ennemi juste hors écran sur le bord choisi.
     */
    private void placeOutsideForSide(int side, int halfWidth, int halfHeight, int offset) {
        switch (side) {
            case SPAWN_SIDE_TOP:
                x = random.nextInt(viewportWidth) - halfWidth;
                y = -halfHeight - offset;
                break;
            case SPAWN_SIDE_RIGHT:
                x = halfWidth + offset;
                y = random.nextInt(viewportHeight) - halfHeight;
                break;
            case SPAWN_SIDE_BOTTOM:
                x = random.nextInt(viewportWidth) - halfWidth;
                y = halfHeight + offset;
                break;
            case SPAWN_SIDE_LEFT:
            default:
                x = -halfWidth - offset;
                y = random.nextInt(viewportHeight) - halfHeight;
                break;
        }
    }

    /**
     * On vérifie si une coordonnée tombe du côté droit de la rivière décorative.
     */
    private boolean isRightOfDecorativeRiver(double logicalX) {
        if (decorativeRiverColumn < 0) {
            return false;
        }

        double tileWidth = (double) fieldWidth / GrilleCulture.LARGEUR_GRILLE;
        double riverRightEdgeX = (-fieldWidth / 2.0) + ((decorativeRiverColumn + 1) * tileWidth);
        return logicalX >= riverRightEdgeX;
    }

    /**
     * On repère la colonne de rivière pour éviter certains spawns incohérents.
     */
    private int resolveDecorativeRiverColumn(GrilleCulture grilleCulture) {
        if (grilleCulture == null) {
            return -1;
        }

        for (int column = 0; column < grilleCulture.getLargeur(); column++) {
            if (grilleCulture.hasRiver(column, 0)) {
                return column;
            }
        }

        return -1;
    }
    
    /**
     * La méthode centrale
     * Elle orchestre toute la frame dans un ordre précis pour éviter des états incohérents
     * entre fuite, recherche de culture, retour au terrier et consommation.
     *
     * @param player joueur utilisé pour déclencher la fuite si le lapin entre dans sa zone d'influence
     */
    public synchronized void update(EnemyModel enemyModel, Unit player, GrilleCulture grilleCulture, int viewportWidth, int viewportHeight,
                                    int fieldWidth, int fieldHeight) {
        if (worldType == WorldType.CAVE) {
            updateCaveMonster(enemyModel, player, viewportWidth, viewportHeight, fieldWidth, fieldHeight);
            return;
        }

        refreshDimensions(viewportWidth, viewportHeight, fieldWidth, fieldHeight);
        // L'ordre compte:
        // 1) la fuite peut annuler une cible,
        // 2) la recherche peut réserver une culture libre,
        // 3) au bout de 30 s sans rien trouver, le lapin repart,
        // 4) le déplacement rapproche ensuite le lapin de sa cible,
        // 5) puis seulement on déclenche l'attente avant consommation.
        handleFleeTrigger(enemyModel, player);
        updateCultureTarget(enemyModel, grilleCulture);
        updateBurrowReturnState(enemyModel);
        ensureValidTarget(player);
        updateNavigationState();
        moveTowardTarget(grilleCulture);
        updateCultureWaiting(enemyModel, grilleCulture);
        updateStagnationAndRecover(player, grilleCulture);
        updateFledStatus();
    }

    /**
     * Synchronise les dimensions de travail de l'IA des lapins avec la fenêtre réellement visible.
     * Le lapin s'appuie ensuite sur ces valeurs pour choisir ses cibles et savoir quand il a quitté l'écran.
     */
    private void refreshDimensions(int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
    }

    /**
     * Version grotte :
     * certains monstres patrouillent comme éclaireurs,
     * les autres restent immobiles jusqu'à l'alerte.
     */
    private void updateCaveMonster(
            EnemyModel enemyModel,
            Unit player,
            int viewportWidth,
            int viewportHeight,
            int fieldWidth,
            int fieldHeight
    ) {
        refreshDimensions(viewportWidth, viewportHeight, fieldWidth, fieldHeight);
        if (!combatUnit.isAlive()) {
            caveMoving = false;
            return;
        }

        updateCaveAggroState(player);
        if (caveAggroed) {
            updateCaveCombatMovement(player);
        } else {
            updateCavePatrolState();
        }
        moveDirectlyTowardTarget(resolveCaveSpeed());
    }

    /**
     * La grotte ne doit pas devenir un simple couloir de patrouille.
     * Dès que le joueur entre dans la zone d'influence avec une ligne de vue dégagée,
     * le monstre bascule en état d'alerte et garde ce souvenir quelques instants.
     */
    private void updateCaveAggroState(Unit player) {
        if (player == null || !player.isInCave()) {
            caveAggroed = false;
            return;
        }

        long now = System.currentTimeMillis();
        double deltaX = player.getX() - x;
        double deltaY = player.getY() - y;
        double distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
        boolean playerInInfluenceZone = distanceSquared <= ((double) CAVE_INFLUENCE_RADIUS * CAVE_INFLUENCE_RADIUS);
        boolean lineOfSight = playerInInfluenceZone && hasLineOfSight(player.getX(), player.getY());

        if (lineOfSight) {
            caveAggroed = true;
            caveLastSeenPlayerX = player.getX();
            caveLastSeenPlayerY = player.getY();
            caveLastSeenPlayerTime = now;
            return;
        }

        if (!caveAggroed) {
            return;
        }

        double alertLeashDistance = CAVE_SHOOT_RANGE * 1.45;
        if ((now - caveLastSeenPlayerTime) > CAVE_ALERT_MEMORY_MS
                || distanceSquared > (alertLeashDistance * alertLeashDistance)) {
            caveAggroed = false;
            caveBehavior = CaveBehavior.PATROL;
        }
    }

    /**
     * En état d'alerte, le monstre essaie de rester dans une "bonne" distance de tir.
     * Trop proche : il recule. Trop loin : il revient. Entre les deux : il strafe.
     */
    private void updateCaveCombatMovement(Unit player) {
        caveBehavior = CaveBehavior.ALERT;

        double focusX = Double.isFinite(caveLastSeenPlayerX) ? caveLastSeenPlayerX : caveHomeX;
        double focusY = Double.isFinite(caveLastSeenPlayerY) ? caveLastSeenPlayerY : caveHomeY;
        if (player != null && player.isInCave() && hasLineOfSight(player.getX(), player.getY())) {
            focusX = player.getX();
            focusY = player.getY();
        }

        double deltaX = focusX - x;
        double deltaY = focusY - y;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        if (distance < 0.0001) {
            targetX = x;
            targetY = y;
            return;
        }

        double normalizedX = deltaX / distance;
        double normalizedY = deltaY / distance;
        if (distance < CAVE_COMBAT_MIN_DISTANCE) {
            targetX = x - (normalizedX * CAVE_COMBAT_STRAFE_DISTANCE);
            targetY = y - (normalizedY * CAVE_COMBAT_STRAFE_DISTANCE);
            return;
        }

        if (distance > CAVE_COMBAT_MAX_DISTANCE) {
            targetX = focusX - (normalizedX * CAVE_COMBAT_MIN_DISTANCE);
            targetY = focusY - (normalizedY * CAVE_COMBAT_MIN_DISTANCE);
            return;
        }

        long now = System.currentTimeMillis();
        if ((now - caveLastStrafeSwapTime) > CAVE_STRAFE_SWAP_DELAY_MS) {
            caveLastStrafeSwapTime = now;
            if (random.nextDouble() < 0.35) {
                caveStrafeClockwise = !caveStrafeClockwise;
            }
        }

        double perpendicularX = caveStrafeClockwise ? -normalizedY : normalizedY;
        double perpendicularY = caveStrafeClockwise ? normalizedX : -normalizedX;
        targetX = x + (perpendicularX * CAVE_COMBAT_STRAFE_DISTANCE);
        targetY = y + (perpendicularY * CAVE_COMBAT_STRAFE_DISTANCE);
    }

    /**
     * Tous les monstres de grotte suivent désormais le même schéma simple :
     * ils patrouillent près de leur poste, puis marquent parfois une pause.
     */
    private void updateCavePatrolState() {
        if (cavePauseTimer > 0) {
            cavePauseTimer--;
            caveBehavior = CaveBehavior.PAUSE;
            targetX = x;
            targetY = y;
            return;
        }

        caveBehavior = CaveBehavior.PATROL;
        cavePatrolRetargetTimer--;
        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
        if (cavePatrolRetargetTimer > 0 && distanceSquared > 36.0) {
            return;
        }

        if (random.nextDouble() < CAVE_PAUSE_PROBABILITY) {
            cavePauseTimer = CAVE_PAUSE_MIN_FRAMES + random.nextInt(CAVE_PAUSE_RANDOM_FRAMES);
            caveBehavior = CaveBehavior.PAUSE;
            targetX = x;
            targetY = y;
            return;
        }

        pickNewCavePatrolTarget();
        cavePatrolRetargetTimer = CAVE_PATROL_RETARGET_MIN_FRAMES + random.nextInt(CAVE_PATROL_RETARGET_RANDOM_FRAMES);
    }

    /**
     * On choisit la vitesse du monstre selon son état courant.
     */
    private double resolveCaveSpeed() {
        if (caveBehavior == CaveBehavior.PAUSE) {
            return 0.0;
        }

        return caveBehavior == CaveBehavior.ALERT ? CAVE_ALERT_SPEED : CAVE_PATROL_SPEED;
    }

    /**
     * Ici on garde volontairement un déplacement direct et minimal :
     * pas de collisions monstres/joueur pour l'instant,
     * seulement une cible logique et une animation de marche propre.
     */
    private void moveDirectlyTowardTarget(double speed) {
        if (speed <= 0.0) {
            caveMoving = false;
            return;
        }

        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        if (distance <= 0.0001) {
            caveMoving = false;
            return;
        }

        double step = Math.min(speed, distance);
        double stepX = (deltaX / distance) * step;
        double stepY = (deltaY / distance) * step;

        if (!tryMove(stepX, stepY)) {
            redirectCaveAroundObstacle(stepX, stepY, speed);
        } else {
            updateCaveAnimation(stepX, stepY);
        }

        clampInsidePatrolBounds();
    }

    /**
     * On garde ici la direction affichée et l'alternance des frames de marche.
     */
    private void updateCaveAnimation(double stepX, double stepY) {
        caveMoving = Math.abs(stepX) >= 0.001 || Math.abs(stepY) >= 0.001;
        if (!caveMoving) {
            return;
        }

        caveDisplayVectorX = stepX;
        caveDisplayVectorY = stepY;
        caveWalkTickCounter++;
        if (caveWalkTickCounter >= CAVE_WALK_FRAME_TOGGLE_TICKS) {
            caveWalkTickCounter = 0;
            caveWalkFrameIndex = 1 - caveWalkFrameIndex;
        }
    }

    /**
     * On recadre le monstre dans sa zone de patrouille pour éviter une dérive progressive.
     */
    private void clampInsidePatrolBounds() {
        if (cavePatrolBounds == null) {
            return;
        }

        x = Math.max(cavePatrolBounds.x, Math.min(x, cavePatrolBounds.x + cavePatrolBounds.width));
        y = Math.max(cavePatrolBounds.y, Math.min(y, cavePatrolBounds.y + cavePatrolBounds.height));
    }

    /**
     * On choisit une nouvelle destination de patrouille cohérente avec la zone du monstre.
     */
    private void pickNewCavePatrolTarget() {
        if (cavePatrolBounds == null && caveGuardBounds == null) {
            targetX = caveHomeX;
            targetY = caveHomeY;
            return;
        }

        Rectangle preferredBounds = random.nextDouble() < CAVE_GLOBAL_PATROL_PROBABILITY
                ? cavePatrolBounds
                : caveGuardBounds;
        Rectangle fallbackBounds = preferredBounds == cavePatrolBounds ? caveGuardBounds : cavePatrolBounds;

        Point2D.Double patrolPoint = findValidCavePoint(preferredBounds, 80);
        if (patrolPoint == null) {
            patrolPoint = findValidCavePoint(fallbackBounds, 80);
        }
        if (patrolPoint == null) {
            targetX = caveHomeX;
            targetY = caveHomeY;
            return;
        }

        targetX = patrolPoint.x;
        targetY = patrolPoint.y;
    }

    /**
     * Ligne de vue simplifiée :
     * on échantillonne plusieurs points entre le monstre et sa cible.
     * Si un de ces points tombe dans un mur, le tir ou l'aggro directe sont refusés.
     */
    private boolean hasLineOfSight(double targetX, double targetY) {
        if (movementCollisionMap == null) {
            return true;
        }

        double deltaX = targetX - x;
        double deltaY = targetY - y;
        double distance = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
        if (distance <= 0.0001) {
            return true;
        }

        int sampleCount = Math.max(2, (int) Math.ceil(distance / 12.0));
        int probeSize = Math.max(8, HITBOX_SIZE / 2);
        for (int sampleIndex = 1; sampleIndex < sampleCount; sampleIndex++) {
            double ratio = sampleIndex / (double) sampleCount;
            double sampleX = x + (deltaX * ratio);
            double sampleY = y + (deltaY * ratio);
            if (!movementCollisionMap.canOccupyCenteredBox(sampleX, sampleY, probeSize, probeSize)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Petit contournement pour la grotte :
     * on garde la même idée que pour les lapins,
     * mais sans logique de détour hors carte.
     */
    private void redirectCaveAroundObstacle(double desiredStepX, double desiredStepY, double speed) {
        double desiredAngle = Math.atan2(desiredStepY, desiredStepX);
        double[] offsets = preferredTurnSign >= 0 ? RIGHT_HAND_OFFSETS : LEFT_HAND_OFFSETS;

        for (double offset : offsets) {
            double angle = desiredAngle + offset;
            double candidateStepX = Math.cos(angle) * speed;
            double candidateStepY = Math.sin(angle) * speed;
            if (!tryMove(candidateStepX, candidateStepY)) {
                continue;
            }

            if (offset > 0.0001) {
                preferredTurnSign = 1;
            } else if (offset < -0.0001) {
                preferredTurnSign = -1;
            }

            updateCaveAnimation(candidateStepX, candidateStepY);
            return;
        }

        caveMoving = false;
    }

    /**
     * Déclenche la fuite si le lapin entre dans la zone d'influence du joueur.
     * Cette méthode a priorité sur le reste : un lapin en fuite abandonne immédiatement sa culture et son déplacement normal.
     */
    private void handleFleeTrigger(EnemyModel enemyModel, Unit player) {
        if (player == null || isFleeing) {
            return;
        }

        if (player.isInInfluenceZone((int) x, (int) y)) {
            isFleeing = true;
            isReturningToBurrow = false;
            noCultureFoundStartTime = -1L;
            clearFenceAttackTarget();
            // Si le joueur fait fuir le lapin, la culture redevient immédiatement disponible
            // pour un autre lapin et le délai avant consommation est perdu.
            clearCultureTarget(enemyModel);
            updateFleeTarget(player);
            gestionnaireObjectifs.mettreAJourObjectifsFuite();
        }
    }

    /**
     * Vérifie que le lapin a toujours une destination exploitable.
     * Si la cible est invalide, on en reconstruit une cohérente avec son état actuel
     * (fuite, retour au terrier ou simple promenade).
     */
    private void ensureValidTarget(Unit player) {
        if (hasCultureTarget()) {
            return;
        }

        if (!Double.isNaN(targetX) && !Double.isNaN(targetY)
            && !Double.isInfinite(targetX) && !Double.isInfinite(targetY)) {
            return;
        }

        if (isReturningToBurrow) {
            updateBurrowReturnTarget();
        } else if (isFleeing && player != null) {
            updateFleeTarget(player);
        } else if (isFleeing) {
            // Si le joueur n'est plus disponible, on garde au moins une sortie de secours hors carte.
            updateBurrowReturnTarget();
        } else {
            pickNewTarget();
        }
    }

    /**
     * Gère la phase de navigation "normale".
     * Tant que le lapin n'est pas occupé à fuir ou à sortir, on le fait entrer dans le champ
     * puis on lui donne de petites destinations de promenade pour éviter un mouvement robotique.
     */
    private void updateNavigationState() {
        int halfFieldWidth = fieldWidth / 2;
        int halfFieldHeight = fieldHeight / 2;

        if (!isInsideMap) {
            if (x >= -halfFieldWidth && x <= halfFieldWidth && y >= -halfFieldHeight && y <= halfFieldHeight) {
                isInsideMap = true;
            } else if (isLeavingMap() && random.nextInt(90) == 0) {
                pickFieldEntryTarget();
            }
            return;
        }

        if (isLeavingMap()) {
            if (hasCultureTarget()) {
                return;
            }
            wanderTimer--;
            if (wanderTimer <= 0) {
                pickNewTarget();
                // Correspond au nombre de mises à jour à attendre avant de choisir une nouvelle destination aléatoire
                wanderTimer = 60 + random.nextInt(120);
            }
        }
    }

    /**
     * Transforme la cible actuelle en un petit pas de déplacement concret.
     * C'est ici qu'on choisit aussi la vitesse normale ou la vitesse plus rapide de sortie.
     */
    private void moveTowardTarget(GrilleCulture grilleCulture) {
        if (advanceFenceAttack(grilleCulture)) {
            return;
        }

        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double currentSpeed = isActivelyLeavingMap() ? 3.0 : 1.5;

        if (distance > 0.0001) {
            double step = Math.min(currentSpeed, distance);
            double stepX = (dx / distance) * step;
            double stepY = (dy / distance) * step;
            moveWithObstacleCollision(stepX, stepY, currentSpeed, grilleCulture);
            return;
        }

        if (hasCultureTarget() || isActivelyLeavingMap()) {
            return;
        }

        if (isInsideMap) {
            pickNewTarget();
        }
    }

    /**
     * Tant qu'un segment précis bloque la culture ciblée, le lapin reste dessus
     * au lieu de contourner l'obstacle.
     */
    private boolean advanceFenceAttack(GrilleCulture grilleCulture) {
        if (!hasFenceAttackTarget(grilleCulture) || farmObstacleMap == null) {
            clearFenceAttackTarget();
            return false;
        }

        Rectangle fenceBounds = farmObstacleMap.getFenceLogicalBounds(targetCultureGridX, targetCultureGridY, targetFenceSide);
        if (fenceBounds == null) {
            clearFenceAttackTarget();
            return false;
        }

        double attackTargetX = getFenceContactTargetX(fenceBounds, targetFenceSide);
        double attackTargetY = getFenceContactTargetY(fenceBounds, targetFenceSide);
        if (isFenceAttackBackingOff) {
            double backstepTargetX = getFenceBackstepTargetX(attackTargetX, targetFenceSide);
            double backstepTargetY = getFenceBackstepTargetY(attackTargetY, targetFenceSide);
            if (advanceTowardFenceAttackPoint(
                    backstepTargetX,
                    backstepTargetY,
                    FENCE_ATTACK_BACKSTEP_SPEED,
                    FENCE_ATTACK_BACKSTEP_TOLERANCE
            )) {
                return true;
            }

            isFenceAttackBackingOff = false;
            isFenceAttackCharging = true;
            return true;
        }

        double approachSpeed = isFenceAttackCharging ? FENCE_ATTACK_CHARGE_SPEED : FENCE_ATTACK_APPROACH_SPEED;
        if (advanceTowardFenceAttackPoint(
                attackTargetX,
                attackTargetY,
                approachSpeed,
                FENCE_ATTACK_CONTACT_TOLERANCE
        )) {
            return true;
        }

        long now = System.currentTimeMillis();
        if (lastFenceHitTime >= 0L && (now - lastFenceHitTime) < DELAI_COUP_CLOTURE_MS) {
            return true;
        }

        lastFenceHitTime = now;
        if (grilleCulture.damageFence(targetCultureGridX, targetCultureGridY, targetFenceSide)) {
            clearFenceAttackTarget();
            return true;
        }

        isFenceAttackBackingOff = true;
        isFenceAttackCharging = false;
        return true;
    }

    /**
     * Même pendant l'attaque de clôture, on garde un seul helper de déplacement
     * pour éviter de dupliquer la logique de pas, de collision et de petit détour.
     */
    private boolean advanceTowardFenceAttackPoint(
            double destinationX,
            double destinationY,
            double speed,
            double arrivalTolerance
    ) {
        double dx = destinationX - x;
        double dy = destinationY - y;
        double distance = Math.sqrt((dx * dx) + (dy * dy));
        if (distance <= arrivalTolerance) {
            return false;
        }

        double step = Math.min(speed, distance);
        double stepX = (dx / distance) * step;
        double stepY = (dy / distance) * step;
        if (tryMove(stepX, stepY)) {
            return true;
        }

        if (!isTargetFenceCollision(farmObstacleMap.findBlockingFenceCollision(x + stepX, y + stepY, HITBOX_SIZE, HITBOX_SIZE))) {
            redirectAroundObstacle(stepX, stepY, speed);
            return true;
        }

        return false;
    }

    /**
     * On calcule le point de contact à viser pour frapper la clôture sur le bon côté.
     */
    private double getFenceContactTargetX(Rectangle fenceBounds, CellSide side) {
        switch (side) {
            case LEFT:
                return fenceBounds.x - (HITBOX_SIZE / 2.0) - 1;
            case RIGHT:
                return fenceBounds.x + fenceBounds.width + (HITBOX_SIZE / 2.0) + 1;
            case TOP:
            case BOTTOM:
                return Math.max(
                        fenceBounds.x + (HITBOX_SIZE / 2.0),
                        Math.min(x, fenceBounds.x + fenceBounds.width - (HITBOX_SIZE / 2.0))
                );
            default:
                return x;
        }
    }

    /**
     * On calcule le point de contact vertical à viser pour frapper la clôture sur le bon côté.
     */
    private double getFenceContactTargetY(Rectangle fenceBounds, CellSide side) {
        switch (side) {
            case TOP:
                return fenceBounds.y - (HITBOX_SIZE / 2.0) - 1;
            case BOTTOM:
                return fenceBounds.y + fenceBounds.height + (HITBOX_SIZE / 2.0) + 1;
            case LEFT:
            case RIGHT:
                return Math.max(
                        fenceBounds.y + (HITBOX_SIZE / 2.0),
                        Math.min(y, fenceBounds.y + fenceBounds.height - (HITBOX_SIZE / 2.0))
                );
            default:
                return y;
        }
    }

    /**
     * On calcule le petit recul horizontal avant la charge suivante contre la clôture.
     */
    private double getFenceBackstepTargetX(double contactTargetX, CellSide side) {
        switch (side) {
            case LEFT:
                return contactTargetX - FENCE_ATTACK_BACKSTEP_DISTANCE;
            case RIGHT:
                return contactTargetX + FENCE_ATTACK_BACKSTEP_DISTANCE;
            case TOP:
            case BOTTOM:
                return contactTargetX;
            default:
                return x;
        }
    }

    /**
     * On calcule le petit recul vertical avant la charge suivante contre la clôture.
     */
    private double getFenceBackstepTargetY(double contactTargetY, CellSide side) {
        switch (side) {
            case TOP:
                return contactTargetY - FENCE_ATTACK_BACKSTEP_DISTANCE;
            case BOTTOM:
                return contactTargetY + FENCE_ATTACK_BACKSTEP_DISTANCE;
            case LEFT:
            case RIGHT:
                return contactTargetY;
            default:
                return y;
        }
    }

    /**
     * Surveille les situations où le lapin semble coincé.
     * Si son mouvement devient quasi nul pendant plusieurs frames, on relance une cible adaptée pour le débloquer.
     */
    private void updateStagnationAndRecover(Unit player, GrilleCulture grilleCulture) {
        if (hasFenceAttackTarget(grilleCulture)) {
            stagnantFrames = 0;
            lastX = x;
            lastY = y;
            return;
        }

        if (hasCultureTarget() && cultureWaitStartTime >= 0L) {
            stagnantFrames = 0;
            lastX = x;
            lastY = y;
            return;
        }

        double movedDistance = Math.abs(x - lastX) + Math.abs(y - lastY);
        if (movedDistance < 0.05) {
            stagnantFrames++;
        } else {
            stagnantFrames = 0;
        }

        lastX = x;
        lastY = y;

        if (stagnantFrames <= 20) {
            return;
        }

        if (isFleeing && player != null) {
            updateFleeTarget(player);
        } else if (hasCultureTarget()) {
            targetX = getCultureCenterX(targetCultureGridX);
            targetY = getCultureCenterY(targetCultureGridY);
        } else if (isReturningToBurrow) {
            updateBurrowReturnTarget();
        } else {
            pickNewTarget();
        }
        stagnantFrames = 0;
    }

    /**
     * Marque définitivement le lapin comme sorti quand il a réellement quitté l'écran.
     * Le modèle pourra alors le retirer de la liste active.
     */
    private void updateFledStatus() {
        if (!isActivelyLeavingMap()) {
            return;
        }

        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;
        if (x < -halfWidth - 100 || x > halfWidth + 100 || y < -halfHeight - 100 || y > halfHeight + 100) {
            hasFled = true;
        }
    }

    /**
     * Cherche une culture mature intéressante à proximité.
     * Si une bonne candidate existe, le lapin la réserve et la transforme en vraie cible de navigation.
     */
    private void updateCultureTarget(EnemyModel enemyModel, GrilleCulture grilleCulture) {
        if (grilleCulture == null || isFleeing) {
            clearCultureTarget(enemyModel);
            return;
        }

        if (hasCultureTarget()) {
            if (isCultureAvailable(grilleCulture, targetCultureGridX, targetCultureGridY)) {
                clearCultureTarget(enemyModel);
                return;
            }

            // Tant que la culture existe encore, on continue simplement vers cette case.
            targetX = getCultureCenterX(targetCultureGridX);
            targetY = getCultureCenterY(targetCultureGridY);
            return;
        }

        int bestGridX = -1;
        int bestGridY = -1;
        double bestDistanceSquared = (double) CULTURE_DETECTION_RADIUS * CULTURE_DETECTION_RADIUS;

        for (int gridX = 0; gridX < grilleCulture.getLargeur(); gridX++) {
            for (int gridY = 0; gridY < grilleCulture.getHauteur(); gridY++) {
                if (isCultureAvailable(grilleCulture, gridX, gridY)) {
                    continue;
                }
                // Une culture mature déjà réservée par un autre lapin est ignorée.
                if (enemyModel != null && !enemyModel.reserveCulture(gridX, gridY, this)) {
                    continue;
                }

                double cultureCenterX = getCultureCenterX(gridX);
                double cultureCenterY = getCultureCenterY(gridY);
                double dx = cultureCenterX - x;
                double dy = cultureCenterY - y;
                double distanceSquared = (dx * dx) + (dy * dy);
                if (distanceSquared > bestDistanceSquared) {
                    // Cette culture n'est pas la meilleure candidate pour ce lapin:
                    // on annule donc tout de suite la réservation temporaire.
                    if (enemyModel != null) {
                        enemyModel.releaseCultureReservation(gridX, gridY, this);
                    }
                    continue;
                }

                // Si on vient de trouver une culture plus proche, on relâche l'ancienne
                // réservation pour ne pas bloquer inutilement cette case.
                if (bestGridX >= 0 && enemyModel != null) {
                    enemyModel.releaseCultureReservation(bestGridX, bestGridY, this);
                }

                bestDistanceSquared = distanceSquared;
                bestGridX = gridX;
                bestGridY = gridY;
            }
        }

        if (bestGridX >= 0) {
            targetCultureGridX = bestGridX;
            targetCultureGridY = bestGridY;
            targetX = getCultureCenterX(bestGridX);
            targetY = getCultureCenterY(bestGridY);
            cultureWaitStartTime = -1L;
            noCultureFoundStartTime = -1L;
        }
    }

    /**
     * Gère le chrono des 30 secondes si le lapin n'est pas sur une culture.
     * Tant qu'aucune cible n'est trouvée, cette méthode prépare puis déclenche le retour vers l'extérieur.
     */
    private void updateBurrowReturnState(EnemyModel enemyModel) {
        if (isFleeing || isReturningToBurrow) {
            noCultureFoundStartTime = -1L;
            return;
        }

        if (hasCultureTarget()) {
            noCultureFoundStartTime = -1L;
            return;
        }

        long now = System.currentTimeMillis();
        if (noCultureFoundStartTime < 0L) {
            noCultureFoundStartTime = now;
            return;
        }

        if ((now - noCultureFoundStartTime) < DELAI_RETOUR_TERRIER_MS) {
            return;
        }

        startBurrowReturn(enemyModel);
    }

    /**
     * Gère l'attente de 5 secondes avant la consommation d'une culture.
     * C'est aussi cette méthode qui maintient le chrono affiché dans l'overlay (quand on clique sur le lapin) quand le lapin est posé sur la case.
     */
    private void updateCultureWaiting(EnemyModel enemyModel, GrilleCulture grilleCulture) {
        if (!hasCultureTarget() || grilleCulture == null) {
            return;
        }

        if (isCultureAvailable(grilleCulture, targetCultureGridX, targetCultureGridY)) {
            clearCultureTarget(enemyModel);
            return;
        }

        if (!isOnTargetCultureCell()) {
            // Tant que le lapin n'est pas exactement sur la case, le chrono ne démarre pas.
            cultureWaitStartTime = -1L;
            return;
        }

        if (cultureWaitStartTime < 0L) {
            // Premier frame où le lapin est vraiment arrivé sur la culture.
            cultureWaitStartTime = System.currentTimeMillis();
            return;
        }

        if ((System.currentTimeMillis() - cultureWaitStartTime) < DELAI_AVANT_MANGER_MS) {
            return;
        }

        boolean cultureMangee = false;
        try {
            grilleCulture.mangerCulture(targetCultureGridX, targetCultureGridY);
            cultureMangee = true;
        } catch (IllegalStateException ignored) {
            // La culture a pu changer d'état ou disparaître avant la fin de l'attente.
        }

        // Après consommation (ou échec), on libère la réservation pour que la case redevienne neutre.
        clearCultureTarget(enemyModel);
        if (cultureMangee) {
            // Le lapin disparaît dès qu'il a effectivement mangé la culture.
            hasFled = true;
            return;
        }
        if (isReturningToBurrow) {
            updateBurrowReturnTarget();
        } else if (!isFleeing) {
            pickNewTarget();
        }
    }

    /**
     * Vérifie rapidement si une case contient encore une culture mature exploitable.
     */
    private boolean isCultureAvailable(GrilleCulture grilleCulture, int gridX, int gridY) {
        Culture culture = grilleCulture.getCulture(gridX, gridY);
        return culture == null || culture.getStadeCroissance() != Stade.MATURE;
    }

    /**
     * On transforme la clôture qui bloque la culture ciblée en vraie cible d'attaque.
     */
    private boolean acquireTargetFence(double stepX, double stepY, GrilleCulture grilleCulture) {
        if (!hasCultureTarget() || grilleCulture == null || farmObstacleMap == null) {
            return false;
        }

        FenceCollision collision =
                farmObstacleMap.findBlockingFenceCollision(x + stepX, y + stepY, HITBOX_SIZE, HITBOX_SIZE);
        if (collision == null
                || collision.getGridX() != targetCultureGridX
                || collision.getGridY() != targetCultureGridY
                || !grilleCulture.hasFence(targetCultureGridX, targetCultureGridY, collision.getSide())) {
            return false;
        }

        targetFenceSide = collision.getSide();
        isFenceAttackBackingOff = false;
        isFenceAttackCharging = false;
        lastFenceHitTime = -1L;
        return true;
    }

    /**
     * On dit simplement si une clôture valide reste verrouillée comme cible.
     */
    private boolean hasFenceAttackTarget(GrilleCulture grilleCulture) {
        return targetFenceSide != null
                && hasCultureTarget()
                && grilleCulture != null
                && grilleCulture.hasFence(targetCultureGridX, targetCultureGridY, targetFenceSide);
    }

    /**
     * On vérifie si la collision rencontrée correspond bien à la clôture actuellement attaquée.
     */
    private boolean isTargetFenceCollision(FenceCollision collision) {
        return collision != null
                && targetFenceSide != null
                && collision.getGridX() == targetCultureGridX
                && collision.getGridY() == targetCultureGridY
                && collision.getSide() == targetFenceSide;
    }

    /**
     * On remet à zéro tout l'état temporaire lié à l'attaque de clôture.
     */
    private void clearFenceAttackTarget() {
        targetFenceSide = null;
        isFenceAttackBackingOff = false;
        isFenceAttackCharging = false;
        lastFenceHitTime = -1L;
    }

    /**
     * Indique si le lapin est vraiment posé sur le centre de sa culture cible.
     * L'attente de 5 secondes ne doit démarrer qu'à ce moment précis.
     */
    private boolean isOnTargetCultureCell() {
        if (!hasCultureTarget()) {
            return false;
        }

        double dx = targetX - x;
        double dy = targetY - y;
        return Math.abs(dx) < 0.001 && Math.abs(dy) < 0.001;
    }

    /**
     * Transforme une colonne de grille en coordonnée X dans le repère interne du lapin.
     */
    private double getCultureCenterX(int gridX) {
        double tileWidth = (double) fieldWidth / GrilleCulture.LARGEUR_GRILLE;
        return (-fieldWidth / 2.0) + ((gridX + 0.5) * tileWidth);
    }

    /**
     * Même idée que getCultureCenterX, mais pour l'axe vertical.
     * On vise toujours le centre visuel de la case et non son coin.
     */
    private double getCultureCenterY(int gridY) {
        double tileHeight = (double) fieldHeight / GrilleCulture.HAUTEUR_GRILLE;
        return (-fieldHeight / 2.0) + ((gridY + 0.5) * tileHeight);
    }

    /**
     * Sert de test rapide pour savoir si l'IA a déjà une culture réservée en ligne de mire.
     */
    private boolean hasCultureTarget() {
        return targetCultureGridX >= 0 && targetCultureGridY >= 0;
    }

    /**
     * Annule proprement la cible culture en cours.
     * Toute sortie de ce "mini état" passe par ici afin de ne jamais oublier de libérer la réservation.
     */
    private void clearCultureTarget(EnemyModel enemyModel) {
        // La libération se fait ici pour centraliser tous les cas de sortie:
        // fuite, culture disparue, culture mangée ou abandon de cible.
        if (enemyModel != null && hasCultureTarget()) {
            enemyModel.releaseCultureReservation(targetCultureGridX, targetCultureGridY, this);
        }
        targetCultureGridX = -1;
        targetCultureGridY = -1;
        cultureWaitStartTime = -1L;
        clearFenceAttackTarget();
    }
    
    /**
     * Choisit un premier point d'entrée crédible dans le champ,
     * en évitant la boutique principale (à droite).
     * Le but n'est pas d'être optimal, mais d'obtenir une entrée naturelle et sans collision.
     */
    private void pickFieldEntryTarget() {
        // Moitié de la largeur du champ.
        int halfFieldWidth = fieldWidth / 2;
        // Moitié de la hauteur du champ.
        int halfFieldHeight = fieldHeight / 2;
        Rectangle barnBounds = Barn.getCollisionBounds();
        int barnMargin = 90;

        for (int attempt = 0; attempt < 12; attempt++) {
            double candidateX = random.nextInt(Math.max(1, fieldWidth - 80)) - (halfFieldWidth - 40);
            double candidateY = random.nextInt(Math.max(1, fieldHeight - 80)) - (halfFieldHeight - 40);

            if (isInsideBarnAvoidanceZone(candidateX, candidateY, barnBounds, barnMargin)) {
                continue;
            }
            if (trySetNavigableGroundTarget(candidateX, candidateY)) {
                return;
            }
        }

        // Fallback: on vise franchement un des côtés libres de la boutique principale (à droite)
        // pour contourner l'obstacle.
        boolean goLeft = x <= (barnBounds.x + (barnBounds.width / 2.0));
        double fallbackX = goLeft ? barnBounds.x - barnMargin : barnBounds.x + barnBounds.width + barnMargin;
        double fallbackY = barnBounds.y + barnBounds.height + 35;
        double clampedFallbackX = Math.max(-halfFieldWidth + 40, Math.min(halfFieldWidth - 40, fallbackX));
        double clampedFallbackY = Math.max(-halfFieldHeight + 40, Math.min(halfFieldHeight - 40, fallbackY));
        if (trySetNavigableGroundTarget(clampedFallbackX, clampedFallbackY)) {
            return;
        }

        targetX = clampedFallbackX;
        targetY = clampedFallbackY;
    }

    /**
     * Recalcule une cible de fuite très lointaine opposée au joueur.
     * En visant loin, on simplifie la logique: le lapin continue juste tout droit jusqu'à sortir.
     */
    private void updateFleeTarget(Unit player) {
        // Vecteur horizontal qui va du joueur vers l'unité ennemie.
        double dx = x - player.getX();
        // Vecteur vertical qui va du joueur vers l'unité ennemie.
        double dy = y - player.getY();

        // Si l'unité ennemie est exactement sur le joueur, on lui donne quand même une direction.
        if (dx == 0 && dy == 0) {
            // On force une petite composante aléatoire sur X.
            dx = random.nextDouble() - 0.5;
            // On force une petite composante aléatoire sur Y.
            dy = random.nextDouble() - 0.5;
        }

        // Angle de fuite dans la direction opposée au joueur.
        double angle = Math.atan2(dy, dx);
        // Cible très lointaine sur X pour s'assurer que l'on sort de l'écran.
        targetX = x + Math.cos(angle) * 1000;
        // Cible très lointaine sur Y pour s'assurer que l'on sort de l'écran.
        targetY = y + Math.sin(angle) * 1000;
    }

    /**
     * Bascule l'IA dans son mode "retour au terrier".
     * Dans le jeu, ce terrier est abstrait: on simule simplement une sortie vers un bord de l'écran.
     */
    private void startBurrowReturn(EnemyModel enemyModel) {
        isReturningToBurrow = true;
        clearCultureTarget(enemyModel);
        updateBurrowReturnTarget();
    }

    /**
     * Choisit le bord de sortie le plus proche pour matérialiser le retour au terrier.
     */
    private void updateBurrowReturnTarget() {
        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;
        int exitOffset = 140;

        double distanceToLeft = Math.abs(x + halfWidth);
        double distanceToRight = Math.abs(halfWidth - x);
        double distanceToTop = Math.abs(y + halfHeight);
        double distanceToBottom = Math.abs(halfHeight - y);

        if (distanceToLeft <= distanceToRight
            && distanceToLeft <= distanceToTop
            && distanceToLeft <= distanceToBottom) {
            targetX = -halfWidth - exitOffset;
            targetY = y;
            return;
        }

        if (distanceToRight <= distanceToTop && distanceToRight <= distanceToBottom) {
            targetX = halfWidth + exitOffset;
            targetY = y;
            return;
        }

        if (distanceToTop <= distanceToBottom) {
            targetX = x;
            targetY = -halfHeight - exitOffset;
            return;
        }

        targetX = x;
        targetY = halfHeight + exitOffset;
    }

    /**
     * Donne au lapin une petite destination aléatoire de promenade.
     * Le mélange entre cible locale et biais global nous permet de garder un mouvement vivant sans sortir complètement du champ.
     */
    private void pickNewTarget() {
        // Moitié de la largeur de la fenêtre.
        int halfViewportWidth = viewportWidth / 2;
        // Moitié de la hauteur de la fenêtre.
        int halfViewportHeight = viewportHeight / 2;
        for (int attempt = 0; attempt < 12; attempt++) {
            // Angle aléatoire de déplacement.
            double angle = random.nextDouble() * 2 * Math.PI;
            // Distance aléatoire pour éviter une promenade trop régulière.
            double distance = 40 + random.nextDouble() * 120;
            // Cible locale sur X selon l'angle et la distance.
            double localTargetX = x + Math.cos(angle) * distance;
            // Cible locale sur Y selon l'angle et la distance.
            double localTargetY = y + Math.sin(angle) * distance;

            // On garde un leger biais vers le coeur du champ sans faire foncer l'ennemi dessus.
            // Biais horizontal vers une zone aléatoire interne du champ.
            double fieldBiasX = (random.nextDouble() - 0.5) * fieldWidth * 0.55;
            // Biais vertical vers une zone aléatoire interne du champ.
            double fieldBiasY = (random.nextDouble() - 0.5) * fieldHeight * 0.55;

            // Mélange entre la cible locale et le biais global sur X.
            double nextTargetX = (localTargetX * 0.75) + (fieldBiasX * 0.25);
            // Mélange entre la cible locale et le biais global sur Y.
            double nextTargetY = (localTargetY * 0.75) + (fieldBiasY * 0.25);

            // Le champ reste leur zone priviliégiée, mais ils peuvent aussi en sortir.
            nextTargetX = Math.max(-halfViewportWidth + 20, Math.min(halfViewportWidth - 20, nextTargetX));
            // Meme logique sur l'axe vertical.
            nextTargetY = Math.max(-halfViewportHeight + 20, Math.min(halfViewportHeight - 20, nextTargetY));

            if (trySetNavigableGroundTarget(nextTargetX, nextTargetY)) {
                return;
            }
        }

        pickFieldEntryTarget();
    }

    /**
     * Tente d'appliquer le pas voulu.
     * Si l'obstacle bloquant est précisément la clôture qui protège la culture ciblée,
     * on verrouille ce segment comme cible d'attaque au lieu de contourner.
     */
    private void moveWithObstacleCollision(double stepX, double stepY, double speed, GrilleCulture grilleCulture) {
        if (tryMove(stepX, stepY)) {
            return;
        }

        if (acquireTargetFence(stepX, stepY, grilleCulture)) {
            return;
        }

        redirectAroundObstacle(stepX, stepY, speed);
    }

    /**
     * Applique un déplacement seulement s'il laisse le lapin dans une zone autorisée.
     * C'est le point central qui filtre toutes les collisions avec les obstacles fixes.
     */
    private boolean tryMove(double stepX, double stepY) {
        double nextX = x + stepX;
        double nextY = y + stepY;

        if (!canOccupy(nextX, nextY)) {
            return false;
        }

        x = nextX;
        y = nextY;
        lastMoveX = stepX;
        lastMoveY = stepY;
        return true;
    }

    /**
     * Cherche une petite déviation libre quand la trajectoire directe tape un obstacle fixe.
     * À l'origine cette logique servait surtout à la boutique principale (à droite),
     * mais on la réutilise maintenant aussi pour la rivière afin d'éviter
     * un arrêt sec dès qu'un lapin rencontre un obstacle de terrain.
     */
    private void redirectAroundObstacle(double desiredStepX, double desiredStepY, double speed) {
        double desiredAngle = Math.atan2(desiredStepY, desiredStepX);
        double[] offsets = preferredTurnSign >= 0 ? RIGHT_HAND_OFFSETS : LEFT_HAND_OFFSETS;

        for (int pass = 0; pass < 2; pass++) {
            for (double offset : offsets) {
                double angle = desiredAngle + offset;
                double candidateStepX = Math.cos(angle) * speed;
                double candidateStepY = Math.sin(angle) * speed;

                // On évite le demi-tour immédiat si une autre porte de sortie existe.
                if (pass == 0 && isImmediateReverse(candidateStepX, candidateStepY)) {
                    continue;
                }

                if (!tryMove(candidateStepX, candidateStepY)) {
                    continue;
                }

                if (offset > 0.0001) {
                    preferredTurnSign = 1;
                } else if (offset < -0.0001) {
                    preferredTurnSign = -1;
                }

                if (isLeavingMap() && !hasCultureTarget()) {
                    setDetourTarget(angle);
                }
                return;
            }
        }
    }

    /**
     * Répond simplement à la question: "le lapin est-il dans une logique de sortie ?".
     */
    private boolean isLeavingMap() {
        return !isFleeing && !isReturningToBurrow;
    }

    /**
     * Variante plus stricte de l'état de sortie: ici on parle d'un départ effectivement en cours.
     * Pendant un retour au terrier, une culture peut encore annuler la sortie tant qu'elle n'est pas perdue.
     */
    private boolean isActivelyLeavingMap() {
        return isFleeing || (isReturningToBurrow && !hasCultureTarget());
    }

    /**
     * Encapsule le test de collision avec tous les obstacles fixes
     * pour garder le reste du code lisible :
     * boutique principale (à droite), arbres, rivière, et clôtures côté lapins.
     */
    private boolean canOccupy(double centerX, double centerY) {
        if (worldType == WorldType.CAVE) {
            return movementCollisionMap == null
                    || movementCollisionMap.canOccupyCenteredBox(centerX, centerY, HITBOX_SIZE, HITBOX_SIZE);
        }

        return Barn.canOccupyCenteredBox(centerX, centerY, HITBOX_SIZE, HITBOX_SIZE)
                && (farmObstacleMap == null || farmObstacleMap.canOccupyCenteredBox(centerX, centerY, HITBOX_SIZE, HITBOX_SIZE, true));
    }

    /**
     * Détecte un demi-tour immédiat par rapport au pas précédent.
     * Cela évite l'effet "ping-pong" contre la boutique principale (à droite)
     * quand plusieurs directions sont techniquement possibles.
     */
    private boolean isImmediateReverse(double stepX, double stepY) {
        double lastMagnitude = Math.sqrt((lastMoveX * lastMoveX) + (lastMoveY * lastMoveY));
        double nextMagnitude = Math.sqrt((stepX * stepX) + (stepY * stepY));
        if (lastMagnitude < 0.0001 || nextMagnitude < 0.0001) {
            return false;
        }

        double dot = (lastMoveX * stepX) + (lastMoveY * stepY);
        return dot < -(lastMagnitude * nextMagnitude * 0.35);
    }

    /**
     * Prolonge légèrement le détour choisi autour de la boutique principale (à droite).
     * Sans ça, le lapin risquerait de repointer tout de suite vers l'obstacle à la frame suivante.
     */
    private void setDetourTarget(double angle) {
        double detourDistance = 180 + random.nextDouble() * 60;
        double nextTargetX = x + Math.cos(angle) * detourDistance;
        double nextTargetY = y + Math.sin(angle) * detourDistance;
        int halfViewportWidth = viewportWidth / 2;
        int halfViewportHeight = viewportHeight / 2;

        double clampedTargetX = Math.max(-halfViewportWidth + 20, Math.min(halfViewportWidth - 20, nextTargetX));
        double clampedTargetY = Math.max(-halfViewportHeight + 20, Math.min(halfViewportHeight - 20, nextTargetY));
        if (trySetNavigableGroundTarget(clampedTargetX, clampedTargetY)) {
            return;
        }

        pickFieldEntryTarget();
    }

    /**
     * Petit helper géométrique: il définit la "zone à éviter" autour de la
     * boutique principale (à droite) au moment de choisir des cibles approximatives dans le champ.
     */
    private boolean isInsideBarnAvoidanceZone(double candidateX, double candidateY, Rectangle barnBounds, int margin) {
        return candidateX >= barnBounds.x - margin
            && candidateX <= barnBounds.x + barnBounds.width + margin
            && candidateY >= barnBounds.y - margin
            && candidateY <= barnBounds.y + barnBounds.height + margin;
    }

    /**
     * On n'accepte la cible proposée que si le lapin peut vraiment l'occuper.
     */
    private boolean trySetNavigableGroundTarget(double candidateX, double candidateY) {
        if (!canOccupy(candidateX, candidateY)) {
            return false;
        }

        targetX = candidateX;
        targetY = candidateY;
        return true;
    }
    
    /**
     * Expose la position X arrondie pour l'affichage de la vue.
     */
    public synchronized int getX() { return (int) x; }

    /**
     * Expose la position X exacte pour le rendu des sprites.
     * On évite ainsi les à-coups liés à une troncature trop tôt en entier.
     */
    public synchronized double getPreciseX() {
        return x;
    }

    /**
     * On expose la taille de hitbox commune utilisée pour les collisions.
     */
    public static int getCollisionSize() {
        return HITBOX_SIZE;
    }

    /**
     * Expose la position Y arrondie pour l'affichage de la vue.
     */
    public synchronized int getY() { return (int) y; }

    /**
     * Même principe que getPreciseX, mais sur l'axe vertical.
     * La vue peut alors placer le sprite avec une précision sub-pixel.
     */
    public synchronized double getPreciseY() {
        return y;
    }

    /**
     * Indique au modèle si ce lapin peut être retiré de la liste active.
     */
    public synchronized boolean hasFled() { return hasFled; }

    /**
     * On indique si cette unité relève de la logique grotte plutôt que lapin de ferme.
     */
    public synchronized boolean isCaveMonster() {
        return worldType == WorldType.CAVE;
    }

    /**
     * On expose la salle d'origine du monstre pour les aides visuelles et le debug.
     */
    public synchronized int getAssignedRoomIndex() {
        return caveRoomIndex;
    }

    /**
     * On indique si le monstre est actuellement en état d'alerte.
     */
    public synchronized boolean isCaveAggroed() {
        return worldType == WorldType.CAVE && caveAggroed;
    }

    /**
     * On expose le rayon d'aggro de la grotte pour l'overlay et le debug.
     */
    public synchronized int getCaveInfluenceRadius() {
        return worldType == WorldType.CAVE ? CAVE_INFLUENCE_RADIUS : 0;
    }

    /**
     * On fournit la vie normalisée pour les barres de santé.
     */
    public synchronized double getHealthRatio() {
        return combatUnit.getHealthRatio();
    }

    /**
     * On expose la vie actuelle brute de l'unité.
     */
    public synchronized int getHealth() {
        return combatUnit.getHealth();
    }

    /**
     * On expose la vie maximale de l'unité.
     */
    public synchronized int getMaxHealth() {
        return combatUnit.getMaxHealth();
    }

    /**
     * On prépare un libellé de vie prêt à afficher dans l'interface.
     */
    public synchronized String getHealthLabel() {
        return combatUnit.getHealth() + " / " + combatUnit.getMaxHealth();
    }

    /**
     * On indique si l'unité peut encore agir.
     */
    public synchronized boolean isAlive() {
        return combatUnit.isAlive();
    }

    /**
     * On dit si le flash de coup doit encore être affiché.
     */
    public synchronized boolean isHitFlashVisible() {
        return worldType == WorldType.CAVE && System.currentTimeMillis() < caveHitFlashUntilMs;
    }

    /**
     * Le modèle de combat demande explicitement au monstre
     * s'il a le droit de tirer à cet instant.
     */
    public synchronized boolean canFireAtPlayer(Unit player, long now) {
        if (worldType != WorldType.CAVE
                || player == null
                || !player.isInCave()
                || !caveAggroed
                || !combatUnit.isAlive()) {
            return false;
        }

        double deltaX = player.getX() - x;
        double deltaY = player.getY() - y;
        double distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
        return distanceSquared <= ((double) CAVE_SHOOT_RANGE * CAVE_SHOOT_RANGE)
                && hasLineOfSight(player.getX(), player.getY())
                && (now - caveLastShotTime) >= CAVE_SHOT_COOLDOWN_MS;
    }

    /**
     * On expose la puissance d'attaque brute du monstre.
     */
    public synchronized int getAttackPower() {
        return combatUnit.getAttackPower();
    }

    /**
     * On mémorise le tir pour le cooldown et pour orienter le sprite vers la cible.
     */
    public synchronized void markShotFiredAt(double targetX, double targetY, long now) {
        caveLastShotTime = now;
        caveDisplayVectorX = targetX - x;
        caveDisplayVectorY = targetY - y;
        caveLastSeenPlayerX = targetX;
        caveLastSeenPlayerY = targetY;
        caveLastSeenPlayerTime = now;
    }

    /**
     * Les tirs du joueur réveillent immédiatement le monstre touché.
     * Le booléen renvoyé permet au modèle de combat de le retirer sans re-tester sa vie.
     */
    public synchronized boolean receiveProjectileDamage(int damage, double attackerX, double attackerY) {
        if (worldType != WorldType.CAVE || !combatUnit.isAlive()) {
            return false;
        }

        combatUnit.receiveDamage(damage);
        caveAggroed = true;
        caveBehavior = CaveBehavior.ALERT;
        caveHitFlashUntilMs = System.currentTimeMillis() + CAVE_HIT_FLASH_MS;
        caveLastSeenPlayerX = attackerX;
        caveLastSeenPlayerY = attackerY;
        caveLastSeenPlayerTime = System.currentTimeMillis();
        caveDisplayVectorX = x - attackerX;
        caveDisplayVectorY = y - attackerY;
        return !combatUnit.isAlive();
    }

    /**
     * Expose à la vue quel sprite afficher sans lui faire relire la logique métier.
     * On privilégie le vrai dernier pas appliqué, puis la cible courante si le lapin
     * n'a pas encore réellement bougé.
     */
    private DisplaySprite resolveDisplaySprite() {
        if (isEatingCultureCountdownActive()) {
            return DisplaySprite.EATING;
        }

        double displayVectorX = lastMoveX;
        double displayVectorY = lastMoveY;
        double directionMagnitudeSquared = (displayVectorX * displayVectorX) + (displayVectorY * displayVectorY);
        if (directionMagnitudeSquared < (DISPLAY_DIRECTION_EPSILON * DISPLAY_DIRECTION_EPSILON)) {
            displayVectorX = targetX - x;
            displayVectorY = targetY - y;
        }

        if (!Double.isFinite(displayVectorX) || !Double.isFinite(displayVectorY)) {
            return DisplaySprite.FRONT;
        }

        if (Math.abs(displayVectorX) < DISPLAY_DIRECTION_EPSILON
                && Math.abs(displayVectorY) < DISPLAY_DIRECTION_EPSILON) {
            return DisplaySprite.FRONT;
        }

        if (Math.abs(displayVectorX) >= Math.abs(displayVectorY)) {
            return displayVectorX >= 0 ? DisplaySprite.RIGHT : DisplaySprite.LEFT;
        }

        return displayVectorY >= 0 ? DisplaySprite.FRONT : DisplaySprite.BACK;
    }

    /**
     * On expose le sprite lapin déjà résolu pour ne pas refaire la logique dans la vue.
     */
    public synchronized DisplaySprite getDisplaySprite() {
        return resolveDisplaySprite();
    }

    /**
     * On traduit l'état visuel du monstre de grotte en clé de sprite concrète.
     */
    private SpriteKey resolveCaveSpriteKey() {
        double displayVectorX = caveDisplayVectorX;
        double displayVectorY = caveDisplayVectorY;
        if (!Double.isFinite(displayVectorX) || !Double.isFinite(displayVectorY)) {
            return SpriteKey.MONSTER_DOWN_IDLE;
        }

        if (Math.abs(displayVectorX) >= Math.abs(displayVectorY)) {
            if (displayVectorX >= 0.0) {
                if (!caveMoving) {
                    return SpriteKey.MONSTER_RIGHT_IDLE;
                }
                return caveWalkFrameIndex == 0 ? SpriteKey.MONSTER_RIGHT_WALK_1 : SpriteKey.MONSTER_RIGHT_WALK_2;
            }

            if (!caveMoving) {
                return SpriteKey.MONSTER_LEFT_IDLE;
            }
            return caveWalkFrameIndex == 0 ? SpriteKey.MONSTER_LEFT_WALK_1 : SpriteKey.MONSTER_LEFT_WALK_2;
        }

        if (displayVectorY < 0.0) {
            return caveWalkFrameIndex == 0 ? SpriteKey.MONSTER_UP_WALK_1 : SpriteKey.MONSTER_UP_WALK_2;
        }

        if (!caveMoving) {
            return SpriteKey.MONSTER_DOWN_IDLE;
        }
        return caveWalkFrameIndex == 0 ? SpriteKey.MONSTER_DOWN_WALK_1 : SpriteKey.MONSTER_DOWN_WALK_2;
    }

    /**
     * On fournit la clé de sprite finale quel que soit le type d'ennemi.
     */
    public synchronized SpriteKey getSpriteKey() {
        if (worldType == WorldType.CAVE) {
            return resolveCaveSpriteKey();
        }

        return switch (resolveDisplaySprite()) {
            case BACK -> SpriteKey.RABBIT_BACK;
            case LEFT -> SpriteKey.RABBIT_LEFT;
            case RIGHT -> SpriteKey.RABBIT_RIGHT;
            case EATING -> SpriteKey.RABBIT_EATING;
            case FRONT -> SpriteKey.RABBIT_FRONT;
        };
    }

    /**
     * Indique à l'overlay si le chrono pertinent est actuellement celui de la consommation.
     */
    public synchronized boolean isEatingCultureCountdownActive() {
        if (worldType == WorldType.CAVE) {
            return false;
        }
        return hasCultureTarget() && isOnTargetCultureCell();
    }

    /**
     * Donne la durée totale du chrono affiché.
     * L'overlay peut ainsi calculer sa progression sans connaître les détails métier.
     */
    public synchronized long getOverlayCountdownMaxMs() {
        if (worldType == WorldType.CAVE) {
            return -1L;
        }

        if (isEatingCultureCountdownActive()) {
            return DELAI_AVANT_MANGER_MS;
        }

        if (!isFleeing && !isReturningToBurrow && !hasCultureTarget()) {
            return DELAI_RETOUR_TERRIER_MS;
        }

        return -1L;
    }

    /**
     * Renvoie le temps restant du chrono que l'interface doit montrer maintenant.
     * La valeur dépend donc du vrai comportement courant du lapin, et non d'un état UI artificiel.
     */
    public synchronized long getOverlayCountdownMs() {
        if (worldType == WorldType.CAVE) {
            return -1L;
        }

        if (isEatingCultureCountdownActive()) {
            if (cultureWaitStartTime < 0L) {
                return DELAI_AVANT_MANGER_MS;
            }

            long elapsedMs = System.currentTimeMillis() - cultureWaitStartTime;
            return Math.max(0L, DELAI_AVANT_MANGER_MS - elapsedMs);
        }

        if (isFleeing || isReturningToBurrow || hasCultureTarget()) {
            return -1L;
        }

        if (noCultureFoundStartTime < 0L) {
            return DELAI_RETOUR_TERRIER_MS;
        }

        long elapsedMs = System.currentTimeMillis() - noCultureFoundStartTime;
        return Math.max(0L, DELAI_RETOUR_TERRIER_MS - elapsedMs);
    }

    /**
     * Fournit un libellé très simple pour résumer le comportement courant dans l'overlay.
     */
    public synchronized String getOverlayStatus() {
        if (worldType == WorldType.CAVE) {
            if (!combatUnit.isAlive()) {
                return "Neutralise";
            }
            if (caveAggroed) {
                return "Alerte";
            }
            if (caveBehavior == CaveBehavior.PAUSE) {
                return "En garde";
            }
            return "Patrouille";
        }

        if (hasFled) {
            return "Disparu";
        }
        if (isFleeing) {
            return "En fuite";
        }
        if (isReturningToBurrow) {
            return "Retour en cours";
        }
        if (isEatingCultureCountdownActive()) {
            return "Mange la culture";
        }
        if (hasCultureTarget()) {
            return "Culture trouvee";
        }
        return "Recherche";
    }
}
