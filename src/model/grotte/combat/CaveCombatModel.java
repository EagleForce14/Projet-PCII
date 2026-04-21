package model.grotte.combat;

import model.culture.Type;
import model.enemy.EnemyModel;
import model.enemy.EnemyUnit;
import model.grotte.GrotteMap;
import model.grotte.drop.CaveDrop;
import model.grotte.drop.CaveDropDefinition;
import model.management.Inventaire;
import model.movement.BuildingGeometry;
import model.movement.FacingDirection;
import model.movement.MovementCollisionMap;
import model.movement.Unit;
import model.runtime.DefeatCause;
import model.runtime.Jour;
import model.shop.FacilityType;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Boucle de combat dédiée à la grotte.
 *
 * Le but de cette classe est de concentrer au même endroit :
 * - les tirs du joueur,
 * - les tirs des monstres,
 * - les collisions projectiles / murs / entités,
 * - la défaite quand la vie du joueur tombe à zéro.
 *
 * Remarque : les déplacements du joueur et l'IA de patrouille sont implémentés dans une autre classe.
 */
public final class CaveCombatModel {
    private static final int PLAYER_PROJECTILE_WIDTH = 12;
    private static final int PLAYER_PROJECTILE_HEIGHT = 6;
    private static final int ENEMY_PROJECTILE_WIDTH = 10;
    private static final int ENEMY_PROJECTILE_HEIGHT = 10;
    private static final double PLAYER_PROJECTILE_SPEED = 9.5;
    private static final double ENEMY_PROJECTILE_SPEED = 4.6;
    private static final double PLAYER_PROJECTILE_MAX_DISTANCE = 340.0;
    private static final double ENEMY_PROJECTILE_MAX_DISTANCE = 280.0;
    private static final long PLAYER_FIRE_COOLDOWN_MS = 150L;
    private static final long PLAYER_HIT_FLASH_MS = 180L;
    private static final double PLAYER_AUTO_AIM_RANGE = 230.0;
    private static final double PLAYER_AUTO_AIM_MIN_DOT = 0.76;
    private static final double DROP_SPAWN_PROBABILITY = 0.45;
    private static final long DROP_PICKUP_DELAY_MS = 220L;
    private static final int DEFAULT_DROP_QUANTITY = 1;
    private static final double SEED_DROP_WEIGHT = 1.0;
    private static final double WOOD_DROP_WEIGHT = 0.62;
    private static final double DEFAULT_FACILITY_DROP_WEIGHT = 0.48;
    private static final double FENCE_DROP_WEIGHT = 0.86;
    private static final double PATH_DROP_WEIGHT = 0.60;
    private static final double COMPOST_DROP_WEIGHT = 0.22;
    private static final double BRIDGE_DROP_WEIGHT = 0.12;
    private static final int MAX_DROP_SEARCH_RADIUS = 3;

    private final Unit playerUnit;
    private final EnemyModel enemyModel;
    private final MovementCollisionMap collisionMap;
    private final Jour jour;
    private final Inventaire inventaire;
    private final GrotteMap grotteMap;
    private final List<CaveProjectile> projectiles;
    private final List<CaveDrop> drops;
    private final List<CaveInventoryRewardEffect> inventoryRewardEffects;
    private final Random random;

    private volatile boolean active;
    private volatile boolean playerFiring;
    private volatile long lastPlayerShotTime;
    private volatile long playerHitFlashUntilMs;
    private volatile long lastInventoryPickupTimeMs;
    private volatile boolean playerWasInsideHealingZone;

    public CaveCombatModel(
            Unit playerUnit,
            EnemyModel enemyModel,
            MovementCollisionMap collisionMap,
            Jour jour,
            Inventaire inventaire,
            GrotteMap grotteMap
    ) {
        this.playerUnit = playerUnit;
        this.enemyModel = enemyModel;
        this.collisionMap = collisionMap;
        this.jour = jour;
        this.inventaire = inventaire;
        this.grotteMap = grotteMap;
        this.projectiles = new CopyOnWriteArrayList<>();
        this.drops = new CopyOnWriteArrayList<>();
        this.inventoryRewardEffects = new CopyOnWriteArrayList<>();
        this.random = new Random();
        this.active = false;
        this.playerFiring = false;
        this.lastPlayerShotTime = Long.MIN_VALUE;
        this.playerHitFlashUntilMs = 0L;
        this.lastInventoryPickupTimeMs = Long.MIN_VALUE;
        this.playerWasInsideHealingZone = false;
    }

    /**
     * Une nouvelle tentative dans la grotte redonne toute la vie au joueur
     * car le reste du jeu ne possède pas encore de système de soins.
     */
    public void enterCave() {
        projectiles.clear();
        drops.clear();
        inventoryRewardEffects.clear();
        playerFiring = false;
        active = true;
        playerHitFlashUntilMs = 0L;
        lastInventoryPickupTimeMs = Long.MIN_VALUE;
        playerWasInsideHealingZone = false;
        lastPlayerShotTime = System.currentTimeMillis() - PLAYER_FIRE_COOLDOWN_MS;
        if (playerUnit != null) {
            playerUnit.restoreFullHealth();
        }
    }

    public void exitCave() {
        active = false;
        playerFiring = false;
        playerHitFlashUntilMs = 0L;
        lastInventoryPickupTimeMs = Long.MIN_VALUE;
        playerWasInsideHealingZone = false;
        projectiles.clear();
        drops.clear();
        inventoryRewardEffects.clear();
    }

    public void setPlayerFiring(boolean playerFiring) {
        this.playerFiring = playerFiring;
    }

    public boolean isActive() {
        return active && playerUnit != null && playerUnit.isInCave();
    }

    public List<CaveProjectile> getProjectiles() {
        return projectiles;
    }

    public List<CaveDrop> getDrops() {
        return drops;
    }

    /**
     * Horodatage du dernier ramassage en grotte.
     * L'overlay d'inventaire l'utilise pour déclencher son animation temporaire.
     */
    public long getLastInventoryPickupTimeMs() {
        return lastInventoryPickupTimeMs;
    }

    /**
     * Les effets sont purgés au moment de la lecture
     * pour que la vue puisse simplement les dessiner sans logique supplémentaire.
     */
    public List<CaveInventoryRewardEffect> getActiveInventoryRewardEffects() {
        long now = System.currentTimeMillis();
        inventoryRewardEffects.removeIf(effect -> effect == null || effect.isExpired(now));
        return new ArrayList<>(inventoryRewardEffects);
    }

    public int getPlayerHealth() {
        return playerUnit == null ? 0 : playerUnit.getCombatUnit().getHealth();
    }

    public int getPlayerMaxHealth() {
        return playerUnit == null ? 1 : playerUnit.getCombatUnit().getMaxHealth();
    }

    public double getPlayerHealthRatio() {
        return playerUnit == null ? 0.0 : playerUnit.getCombatUnit().getHealthRatio();
    }

    public double getPlayerFireReadiness() {
        long elapsedMs = System.currentTimeMillis() - lastPlayerShotTime;
        return clamp01(elapsedMs / (double) PLAYER_FIRE_COOLDOWN_MS);
    }

    public double getPlayerHitFlashRatio() {
        long remainingMs = playerHitFlashUntilMs - System.currentTimeMillis();
        if (remainingMs <= 0L) {
            return 0.0;
        }
        return clamp01(remainingMs / (double) PLAYER_HIT_FLASH_MS);
    }

    public int getPlayerX() {
        return playerUnit == null ? 0 : playerUnit.getX();
    }

    public int getPlayerY() {
        return playerUnit == null ? 0 : playerUnit.getY();
    }

    /**
     * La barre au-dessus du joueur n'apparaît que lorsqu'elle apporte
     * une vraie information utile : dégâts récents ou vie déjà entamée.
     */
    public boolean shouldDisplayPlayerHealthBar() {
        return playerUnit != null
                && playerUnit.isInCave()
                && (getPlayerHitFlashRatio() > 0.0 || getPlayerHealth() < getPlayerMaxHealth());
    }

    /**
     * Une frame de combat :
     * 1. le joueur peut tirer,
     * 2. les monstres agressifs peuvent riposter,
     * 3. les projectiles avancent puis appliquent leurs collisions.
     */
    public void update() {
        if (!isActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        updatePlayerFire(now);
        updateEnemyFire(now);
        updateProjectiles(now);
        collectDropsUnderPlayer(now);
        updateHealingZone();
    }

    private void updatePlayerFire(long now) {
        if (!playerFiring || playerUnit == null) {
            return;
        }

        if ((now - lastPlayerShotTime) < PLAYER_FIRE_COOLDOWN_MS) {
            return;
        }

        CaveDirectionVector shotDirection = resolvePlayerShotDirection();
        double spawnDistance = (Unit.SIZE / 2.0) + 8.0;
        double spawnX = playerUnit.getX() + (shotDirection.getX() * spawnDistance);
        double spawnY = playerUnit.getY() + (shotDirection.getY() * spawnDistance);
        projectiles.add(new CaveProjectile(
                CaveProjectileOwner.PLAYER,
                spawnX,
                spawnY,
                shotDirection.getX() * PLAYER_PROJECTILE_SPEED,
                shotDirection.getY() * PLAYER_PROJECTILE_SPEED,
                PLAYER_PROJECTILE_WIDTH,
                PLAYER_PROJECTILE_HEIGHT,
                playerUnit.getCombatUnit().getAttackPower(),
                PLAYER_PROJECTILE_MAX_DISTANCE
        ));
        lastPlayerShotTime = now;
    }

    private CaveDirectionVector resolvePlayerShotDirection() {
        FacingDirection facingDirection = playerUnit == null ? FacingDirection.DOWN : playerUnit.getFacingDirection();
        double baseX = facingDirection.getDeltaX();
        double baseY = facingDirection.getDeltaY();

        EnemyUnit autoAimTarget = findAutoAimTarget(baseX, baseY);
        if (autoAimTarget == null) {
            return normalizeVector(baseX, baseY, 0.0, 1.0);
        }

        return normalizeVector(
                autoAimTarget.getPreciseX() - playerUnit.getX(),
                autoAimTarget.getPreciseY() - playerUnit.getY(),
                baseX,
                baseY
        );
    }

    /**
     * L'auto-aim reste volontairement léger :
     * on aide seulement quand un monstre est déjà bien aligné avec la direction de tir.
     */
    private EnemyUnit findAutoAimTarget(double baseDirectionX, double baseDirectionY) {
        if (enemyModel == null || playerUnit == null) {
            return null;
        }

        CaveDirectionVector normalizedBaseDirection = normalizeVector(baseDirectionX, baseDirectionY, 0.0, 1.0);
        EnemyUnit bestTarget = null;
        double bestDistanceSquared = Double.MAX_VALUE;

        for (EnemyUnit enemy : enemyModel.getEnemyUnits()) {
            if (enemy == null || !enemy.isCaveMonster() || !enemy.isAlive()) {
                continue;
            }

            double deltaX = enemy.getPreciseX() - playerUnit.getX();
            double deltaY = enemy.getPreciseY() - playerUnit.getY();
            double distanceSquared = (deltaX * deltaX) + (deltaY * deltaY);
            if (distanceSquared > (PLAYER_AUTO_AIM_RANGE * PLAYER_AUTO_AIM_RANGE)) {
                continue;
            }

            CaveDirectionVector enemyDirection = normalizeVector(deltaX, deltaY, 0.0, 1.0);
            double alignment = (normalizedBaseDirection.getX() * enemyDirection.getX())
                    + (normalizedBaseDirection.getY() * enemyDirection.getY());
            if (alignment < PLAYER_AUTO_AIM_MIN_DOT || distanceSquared >= bestDistanceSquared) {
                continue;
            }

            bestTarget = enemy;
            bestDistanceSquared = distanceSquared;
        }

        return bestTarget;
    }

    private void updateEnemyFire(long now) {
        if (enemyModel == null || playerUnit == null) {
            return;
        }

        for (EnemyUnit enemy : enemyModel.getEnemyUnits()) {
            if (enemy == null || !enemy.canFireAtPlayer(playerUnit, now)) {
                continue;
            }

            CaveDirectionVector shotDirection = normalizeVector(
                    playerUnit.getX() - enemy.getPreciseX(),
                    playerUnit.getY() - enemy.getPreciseY(),
                    0.0,
                    1.0
            );
            double spawnDistance = (EnemyUnit.getCollisionSize() / 2.0) + 8.0;
            double spawnX = enemy.getPreciseX() + (shotDirection.getX() * spawnDistance);
            double spawnY = enemy.getPreciseY() + (shotDirection.getY() * spawnDistance);
            projectiles.add(new CaveProjectile(
                    CaveProjectileOwner.ENEMY,
                    spawnX,
                    spawnY,
                    shotDirection.getX() * ENEMY_PROJECTILE_SPEED,
                    shotDirection.getY() * ENEMY_PROJECTILE_SPEED,
                    ENEMY_PROJECTILE_WIDTH,
                    ENEMY_PROJECTILE_HEIGHT,
                    enemy.getAttackPower(),
                    ENEMY_PROJECTILE_MAX_DISTANCE
            ));
            enemy.markShotFiredAt(playerUnit.getX(), playerUnit.getY(), now);
        }
    }

    private void updateProjectiles(long now) {
        for (CaveProjectile projectile : projectiles) {
            boolean stillActive = projectile.advance();
            if (!stillActive || hitsSolidObstacle(projectile)) {
                projectiles.remove(projectile);
                continue;
            }

            boolean hitTarget = projectile.getOwner() == CaveProjectileOwner.PLAYER
                    ? resolvePlayerProjectileHit(projectile)
                    : resolveEnemyProjectileHit(projectile, now);
            if (hitTarget) {
                projectiles.remove(projectile);
            }
        }
    }

    private boolean hitsSolidObstacle(CaveProjectile projectile) {
        return collisionMap != null
                && !collisionMap.canOccupyCenteredBox(
                        projectile.getX(),
                        projectile.getY(),
                        projectile.getWidth(),
                        projectile.getHeight()
                );
    }

    private boolean resolvePlayerProjectileHit(CaveProjectile projectile) {
        if (enemyModel == null || playerUnit == null) {
            return false;
        }

        Rectangle projectileBounds = BuildingGeometry.buildCenteredBounds(
                projectile.getX(),
                projectile.getY(),
                projectile.getWidth(),
                projectile.getHeight()
        );

        for (EnemyUnit enemy : enemyModel.getEnemyUnits()) {
            if (enemy == null || !enemy.isCaveMonster() || !enemy.isAlive()) {
                continue;
            }

            Rectangle enemyBounds = BuildingGeometry.buildCenteredBounds(
                    enemy.getPreciseX(),
                    enemy.getPreciseY(),
                    EnemyUnit.getCollisionSize(),
                    EnemyUnit.getCollisionSize()
            );
            if (!enemyBounds.intersects(projectileBounds)) {
                continue;
            }

            boolean enemyKilled = enemy.receiveProjectileDamage(
                    projectile.getDamage(),
                    playerUnit.getX(),
                    playerUnit.getY()
            );
            if (enemyKilled) {
                maybeSpawnDrop(enemy);
                enemyModel.removeEnemy(enemy);
            }
            return true;
        }

        return false;
    }

    private boolean resolveEnemyProjectileHit(CaveProjectile projectile, long now) {
        if (playerUnit == null) {
            return false;
        }

        Rectangle projectileBounds = BuildingGeometry.buildCenteredBounds(
                projectile.getX(),
                projectile.getY(),
                projectile.getWidth(),
                projectile.getHeight()
        );
        Rectangle playerBounds = BuildingGeometry.buildCenteredBounds(
                playerUnit.getX(),
                playerUnit.getY(),
                Unit.SIZE,
                Unit.SIZE
        );
        if (!playerBounds.intersects(projectileBounds)) {
            return false;
        }

        playerUnit.getCombatUnit().receiveDamage(projectile.getDamage());
        playerHitFlashUntilMs = now + PLAYER_HIT_FLASH_MS;
        if (!playerUnit.getCombatUnit().isAlive()) {
            playerUnit.stopMovement();
            playerFiring = false;
            active = false;
            projectiles.clear();
            drops.clear();
            inventoryRewardEffects.clear();
            lastInventoryPickupTimeMs = Long.MIN_VALUE;
            if (jour != null) {
                jour.terminerPartie(DefeatCause.CAVE_ENEMY);
            }
        }
        return true;
    }

    /**
     * Le drop n'est pas garanti :
     * on veut un petit moment de récompense, pas une pluie d'objets à chaque kill.
     *
     * Le pool est recalculé à chaque mort à partir de l'inventaire actuel.
     * Ainsi, si le contenu de l'inventaire change plus tard dans le code,
     * les drops suivent automatiquement sans liste codée en dur ici.
     */
    private void maybeSpawnDrop(EnemyUnit enemy) {
        if (enemy == null || inventaire == null || grotteMap == null) {
            return;
        }
        if (random.nextDouble() > DROP_SPAWN_PROBABILITY) {
            return;
        }

        Point deathCell = resolveGridCellAt(enemy.getPreciseX(), enemy.getPreciseY());
        Point spawnCell = resolveCollectibleDropCell(deathCell);
        if (spawnCell == null) {
            return;
        }

        CaveDropDefinition selectedDefinition = selectDropDefinition(buildDropPoolSnapshot());
        if (selectedDefinition == null) {
            return;
        }

        drops.add(new CaveDrop(selectedDefinition, spawnCell.x, spawnCell.y, System.currentTimeMillis()));
    }

    /**
     * Le joueur n'a rien à faire d'autre que se placer sur la case.
     * On lit donc simplement la case occupée par son centre logique,
     * puis on absorbe tous les drops présents dessus.
     */
    private void collectDropsUnderPlayer(long now) {
        if (playerUnit == null || inventaire == null || drops.isEmpty()) {
            return;
        }

        Point playerCell = resolveGridCellAt(playerUnit.getX(), playerUnit.getY());
        if (playerCell == null) {
            return;
        }

        for (CaveDrop drop : drops) {
            if (drop.getGridX() != playerCell.x || drop.getGridY() != playerCell.y) {
                continue;
            }
            if ((now - drop.getSpawnedAtMs()) < DROP_PICKUP_DELAY_MS) {
                continue;
            }

            inventoryRewardEffects.add(new CaveInventoryRewardEffect(
                    drop.getDefinition(),
                    drop.getGridX(),
                    drop.getGridY(),
                    now
            ));
            lastInventoryPickupTimeMs = now;
            grantDropToInventory(drop.getDefinition());
            drops.remove(drop);
        }
    }

    private void grantDropToInventory(CaveDropDefinition definition) {
        if (definition == null || inventaire == null) {
            return;
        }

        if (definition.isSeed()) {
            inventaire.ajoutGraine(definition.getSeedType(), definition.getQuantity());
            return;
        }
        if (definition.isFacility()) {
            inventaire.ajoutInstallation(definition.getFacilityType(), definition.getQuantity());
            return;
        }
        if (definition.isWood()) {
            inventaire.ajoutBois(definition.getQuantity());
        }
    }

    /**
     * Ce pool est reconstruit à partir de la définition réelle de l'inventaire.
     *
     * On évite ainsi le bug où une partie neuve ne pouvait jamais afficher de drop
     * simplement parce que les quantités de départ étaient encore à zéro.
     */
    private List<CaveDropDefinition> buildDropPoolSnapshot() {
        List<CaveDropDefinition> dropPool = new ArrayList<>();

        for (Type seedType : inventaire.getDropCandidateSeedTypes()) {
            dropPool.add(new CaveDropDefinition(seedType, DEFAULT_DROP_QUANTITY, SEED_DROP_WEIGHT));
        }

        for (FacilityType facilityType : inventaire.getDropCandidateFacilityTypes()) {
            dropPool.add(new CaveDropDefinition(
                    facilityType,
                    DEFAULT_DROP_QUANTITY,
                    resolveFacilityDropWeight(facilityType)
            ));
        }

        if (inventaire.supportsWoodDrops()) {
            dropPool.add(new CaveDropDefinition(DEFAULT_DROP_QUANTITY, WOOD_DROP_WEIGHT));
        }

        return dropPool;
    }

    /**
     * Les ponts et le compost doivent rester nettement plus rares.
     * Les autres installations gardent un poids intermédiaire,
     * et tout futur type inconnu tombe sur une valeur par défaut raisonnable.
     */
    private double resolveFacilityDropWeight(FacilityType facilityType) {
        if (facilityType == null) {
            return 0.0;
        }
        if (facilityType == FacilityType.CLOTURE) {
            return FENCE_DROP_WEIGHT;
        }
        if (facilityType == FacilityType.CHEMIN) {
            return PATH_DROP_WEIGHT;
        }
        if (facilityType == FacilityType.COMPOST) {
            return COMPOST_DROP_WEIGHT;
        }
        if (facilityType == FacilityType.PONT) {
            return BRIDGE_DROP_WEIGHT;
        }
        return DEFAULT_FACILITY_DROP_WEIGHT;
    }

    private CaveDropDefinition selectDropDefinition(List<CaveDropDefinition> dropPool) {
        if (dropPool == null || dropPool.isEmpty()) {
            return null;
        }

        double totalWeight = 0.0;
        for (CaveDropDefinition definition : dropPool) {
            if (definition != null && definition.getSelectionWeight() > 0.0) {
                totalWeight += definition.getSelectionWeight();
            }
        }
        if (totalWeight <= 0.0) {
            return null;
        }

        double draw = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;
        CaveDropDefinition lastValidDefinition = null;

        for (CaveDropDefinition definition : dropPool) {
            if (definition == null || definition.getSelectionWeight() <= 0.0) {
                continue;
            }

            cumulativeWeight += definition.getSelectionWeight();
            lastValidDefinition = definition;
            if (draw <= cumulativeWeight) {
                return definition;
            }
        }

        return lastValidDefinition;
    }

    /**
     * La grotte utilise des coordonnées logiques centrées sur le milieu du terrain.
     * On reconstruit donc ici la case correspondante à partir des dimensions
     * déjà synchronisées par la vue des ennemis.
     */
    private Point resolveGridCellAt(double logicalX, double logicalY) {
        if (grotteMap == null) {
            return null;
        }

        int tileSize = resolveLogicalTileSize();
        if (tileSize <= 0) {
            return null;
        }

        int logicalFieldWidth = grotteMap.getWidth() * tileSize;
        int logicalFieldHeight = grotteMap.getHeight() * tileSize;
        double logicalStartX = -(logicalFieldWidth / 2.0);
        double logicalStartY = -(logicalFieldHeight / 2.0);

        int gridX = (int) Math.floor((logicalX - logicalStartX) / tileSize);
        int gridY = (int) Math.floor((logicalY - logicalStartY) / tileSize);
        return grotteMap.isInside(gridX, gridY) ? new Point(gridX, gridY) : null;
    }

    /**
     * Garantit un spawn récupérable :
     * - priorité à la case de mort,
     * - sinon recherche courte autour pour éviter les façades de murs
     *   qui bloquent parfois l'accès malgré une case "walkable".
     */
    private Point resolveCollectibleDropCell(Point preferredCell) {
        if (preferredCell == null) {
            return null;
        }
        if (isCollectibleDropCell(preferredCell.x, preferredCell.y)) {
            return preferredCell;
        }

        for (int radius = 1; radius <= MAX_DROP_SEARCH_RADIUS; radius++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) {
                for (int xOffset = -radius; xOffset <= radius; xOffset++) {
                    // On parcourt uniquement l'anneau courant pour privilégier la case la plus proche.
                    if (Math.abs(xOffset) != radius && Math.abs(yOffset) != radius) {
                        continue;
                    }

                    int candidateX = preferredCell.x + xOffset;
                    int candidateY = preferredCell.y + yOffset;
                    if (isCollectibleDropCell(candidateX, candidateY)) {
                        return new Point(candidateX, candidateY);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Une case de drop doit être vraiment atteignable pour le joueur :
     * on combine la logique "walkable" de la map avec la collision réelle.
     */
    private boolean isCollectibleDropCell(int gridX, int gridY) {
        if (grotteMap == null || !grotteMap.isInside(gridX, gridY) || !grotteMap.isWalkableCell(gridX, gridY)) {
            return false;
        }

        if (collisionMap == null) {
            return true;
        }

        Point.Double logicalCellCenter = resolveLogicalCellCenter(gridX, gridY);
        if (logicalCellCenter == null) {
            return false;
        }

        return collisionMap.canOccupyCenteredBox(
                logicalCellCenter.x,
                logicalCellCenter.y,
                Unit.SIZE,
                Unit.SIZE
        );
    }

    private Point.Double resolveLogicalCellCenter(int gridX, int gridY) {
        int tileSize = resolveLogicalTileSize();
        if (grotteMap == null || tileSize <= 0) {
            return null;
        }

        double logicalFieldWidth = grotteMap.getWidth() * tileSize;
        double logicalFieldHeight = grotteMap.getHeight() * tileSize;
        double logicalStartX = -(logicalFieldWidth / 2.0);
        double logicalStartY = -(logicalFieldHeight / 2.0);

        return new Point.Double(
                logicalStartX + ((gridX + 0.5) * tileSize),
                logicalStartY + ((gridY + 0.5) * tileSize)
        );
    }

    private int resolveLogicalTileSize() {
        if (grotteMap == null || enemyModel == null) {
            return 0;
        }

        int widthBasedTileSize = Math.max(1, (int) Math.round(enemyModel.getFieldWidth() / (double) grotteMap.getWidth()));
        int heightBasedTileSize = Math.max(1, (int) Math.round(enemyModel.getFieldHeight() / (double) grotteMap.getHeight()));
        return Math.max(widthBasedTileSize, heightBasedTileSize);
    }

    private CaveDirectionVector normalizeVector(double x, double y, double fallbackX, double fallbackY) {
        double magnitude = Math.sqrt((x * x) + (y * y));
        if (magnitude <= 0.0001) {
            double fallbackMagnitude = Math.sqrt((fallbackX * fallbackX) + (fallbackY * fallbackY));
            if (fallbackMagnitude <= 0.0001) {
                return new CaveDirectionVector(0.0, 1.0);
            }
            return new CaveDirectionVector(fallbackX / fallbackMagnitude, fallbackY / fallbackMagnitude);
        }

        return new CaveDirectionVector(x / magnitude, y / magnitude);
    }

    private double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /**
     * La régénération ne se déclenche qu'à l'entrée de la zone.
     * Le joueur peut donc rester dessus sans spam de traitements inutiles.
     */
    private void updateHealingZone() {
        if (playerUnit == null || grotteMap == null) {
            playerWasInsideHealingZone = false;
            return;
        }

        boolean isInsideHealingZone = isPlayerInsideHealingZoneArea();

        if (isInsideHealingZone && !playerWasInsideHealingZone) {
            playerUnit.restoreFullHealth();
        }

        playerWasInsideHealingZone = isInsideHealingZone;
    }

    /**
     * La zone de soin est une surface (4 cases), pas un point.
     * On valide donc avec la hitbox réelle du joueur.
     */
    private boolean isPlayerInsideHealingZoneArea() {
        Rectangle playerBounds = BuildingGeometry.buildCenteredBounds(
                playerUnit.getX(),
                playerUnit.getY(),
                Unit.SIZE,
                Unit.SIZE
        );

        for (Point healingCell : grotteMap.getHealingCells()) {
            Rectangle healingCellBounds = resolveLogicalCellBounds(healingCell.x, healingCell.y);
            if (healingCellBounds != null && healingCellBounds.intersects(playerBounds)) {
                return true;
            }
        }

        return false;
    }

    private Rectangle resolveLogicalCellBounds(int gridX, int gridY) {
        int tileSize = resolveLogicalTileSize();
        if (grotteMap == null || tileSize <= 0 || !grotteMap.isInside(gridX, gridY)) {
            return null;
        }

        double logicalFieldWidth = grotteMap.getWidth() * tileSize;
        double logicalFieldHeight = grotteMap.getHeight() * tileSize;
        int logicalStartX = (int) Math.round(-(logicalFieldWidth / 2.0));
        int logicalStartY = (int) Math.round(-(logicalFieldHeight / 2.0));

        return new Rectangle(
                logicalStartX + (gridX * tileSize),
                logicalStartY + (gridY * tileSize),
                tileSize,
                tileSize
        );
    }
}
