package model.enemy;

import model.culture.GrilleCulture;
import model.culture.Culture;
import model.culture.Stade;
import model.environment.TreeObstacleMap;
import model.movement.Barn;
import model.movement.Unit;
import model.objective.GestionnaireObjectifs;

import java.awt.Rectangle;
import java.util.Random;

/**
 * Classe représentant un lapin autonome.
 * Son cycle de vie est toujours le même :
 * apparition hors écran, entrée dans le champ, promenade, éventuelle recherche d'une culture,
 * attente de 5 secondes sur la culture avant de manger, ou retour/fuite vers l'extérieur.
 */
public class EnemyUnit {
    // La taille de la zone de "sécurité" autour de la grange (on ne peut pas traverser cette zone i.e.
    // on ne peut pas traverser la grange)
    private static final int HITBOX_SIZE = 20;
    // Le rayon de détection d'une culture par un lapin
    private static final int CULTURE_DETECTION_RADIUS = 120;
    // Le délai de 5s avant de manger la culture une fois que l'on est positionné dessus.
    private static final long DELAI_AVANT_MANGER_MS = 5000L;
    // Le délai de retour au terrier si le lapin n'a rien trouvé. Correspond à 30s
    private static final long DELAI_RETOUR_TERRIER_MS = 30000L;
    // Ces listes décrivent les rotations possibles autour de la direction voulue afin de contourner la grange.
    // On teste d'abord de petits écarts, puis des virages plus forts, puis seulement un demi-tour.
    // RIGHT_HAND_OFFSETS privilégie un contournement "main droite", LEFT_HAND_OFFSETS l'inverse.
    // Le lapin garde ainsi un côté préféré le long de la grange, ce qui limite les hésitations.
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
    private double lastMoveY = 0;
    // Côté de contournement actuellement privilégié pour longer proprement la grange.
    private int preferredTurnSign = 1;
    // Case de culture actuellement visée par le lapin.
    private int targetCultureGridX = -1;
    private int targetCultureGridY = -1;
    // Instant où le lapin a atteint la case et commence son attente de 5 secondes avant de manger.
    private long cultureWaitStartTime = -1L;
    // Début de la période pendant laquelle le lapin ne trouve aucune culture à viser.
    private long noCultureFoundStartTime = -1L;

    // Temporisateur avant de choisir une nouvelle cible de promenade.
    private int wanderTimer = 0;
    // Générateur aléatoire pour rendre les comportements moins mécaniques.
    private final Random random = new Random();
    
    // Largeur courante de la fenêtre de jeu.
    private int viewportWidth;
    // Hauteur courante de la fenêtre de jeu.
    private int viewportHeight;
    // Largeur utile du champ cultivable.
    private int fieldWidth;
    // Hauteur utile du champ cultivable.
    private int fieldHeight;

    // Gestionnaire d'objectifs pour mettre à jour les objectifs liés à la fuite.
    private GestionnaireObjectifs gestionnaireObjectifs;
    private final TreeObstacleMap treeObstacleMap;

    // On construit ici une unité ennemie avec les dimensions connues au moment de son apparition.
    public EnemyUnit(int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight,
                     GestionnaireObjectifs gestionnaireObjectifs, TreeObstacleMap treeObstacleMap) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        this.gestionnaireObjectifs = gestionnaireObjectifs;
        this.treeObstacleMap = treeObstacleMap;
        // On fait apparaître le lapin hors écran.
        spawnOutside();
        // On initialise la dernière position connue sur X.
        lastX = x;
        // On initialise la dernière position connue sur Y.
        lastY = y;
    }
    
    /**
     * Choisit un point de spawn hors écran et une première direction plausible vers le champ.
     */
    private void spawnOutside() {
        // On le place au milieu de la fenêtre
        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;

        // 0: Haut, 1: Droite, 2: Bas, 3: Gauche
        // Choix aléatoire du bord d'apparition.
        int side = random.nextInt(4);
        // On ajoute une petite distance pour garantir une apparition réellement hors écran.
        int offset = 50;
        
        // On place le lapin sur l'un des quatre côtés externes de la fenêtre.
        switch (side) {
            case 0:
                // Apparition horizontale aléatoire au-dessus de l'écran.
                x = random.nextInt(viewportWidth) - halfWidth;
                // Position verticale au-dessus de la zone visible.
                y = -halfHeight - offset;
                break;
            case 1:
                // Position horizontale à droite de la zone visible.
                x = halfWidth + offset;
                // Apparition verticale aléatoire sur le bord droit.
                y = random.nextInt(viewportHeight) - halfHeight;
                break;
            case 2:
                // Apparition horizontale aléatoire sous l'écran.
                x = random.nextInt(viewportWidth) - halfWidth;
                // Position verticale sous la zone visible.
                y = halfHeight + offset;
                break;
            case 3:
                // Position horizontale à gauche de la zone visible.
                x = -halfWidth - offset;
                // Apparition verticale aléatoire sur le bord gauche.
                y = random.nextInt(viewportHeight) - halfHeight;
                break;
        }
        
        // Dès l'apparition, on choisit un premier point d'entrée dans le champ.
        pickFieldEntryTarget();
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
        moveTowardTarget();
        updateCultureWaiting(enemyModel, grilleCulture);
        updateStagnationAndRecover(player);
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
    private void moveTowardTarget() {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double currentSpeed = isActivelyLeavingMap() ? 3.0 : 1.5;

        if (distance > 0.0001) {
            double step = Math.min(currentSpeed, distance);
            double stepX = (dx / distance) * step;
            double stepY = (dy / distance) * step;
            moveWithBarnCollision(stepX, stepY, currentSpeed);
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
     * Surveille les situations où le lapin semble coincé.
     * Si son mouvement devient quasi nul pendant plusieurs frames, on relance une cible adaptée pour le débloquer.
     */
    private void updateStagnationAndRecover(Unit player) {
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
    }
    
    /**
     * Choisit un premier point d'entrée crédible dans le champ, en évitant la grange.
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

            if (!isInsideBarnAvoidanceZone(candidateX, candidateY, barnBounds, barnMargin)) {
                targetX = candidateX;
                targetY = candidateY;
                return;
            }
        }

        // Fallback: on vise franchement un des côtés libres de la grange pour contourner l'obstacle.
        boolean goLeft = x <= (barnBounds.x + (barnBounds.width / 2.0));
        double fallbackX = goLeft ? barnBounds.x - barnMargin : barnBounds.x + barnBounds.width + barnMargin;
        double fallbackY = barnBounds.y + barnBounds.height + 35;
        targetX = Math.max(-halfFieldWidth + 40, Math.min(halfFieldWidth - 40, fallbackX));
        targetY = Math.max(-halfFieldHeight + 40, Math.min(halfFieldHeight - 40, fallbackY));
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

        // On publie les nouvelles cibles une fois les calculs terminés.
        targetX = nextTargetX;
        targetY = nextTargetY;
    }

    /**
     * Tente d'appliquer le pas voulu. Si un obstacle bloque, on délègue à la logique de contournement.
     */
    private void moveWithBarnCollision(double stepX, double stepY, double speed) {
        if (tryMove(stepX, stepY)) {
            return;
        }

        redirectAroundBarn(stepX, stepY, speed);
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
     * Cherche une petite déviation libre quand la trajectoire directe tape la grange.
     * On garde ainsi un contournement doux au lieu d'un arrêt brutal.
     */
    private void redirectAroundBarn(double desiredStepX, double desiredStepY, double speed) {
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
     * Encapsule le test de collision avec la grange et les arbres
     * pour garder le reste du code lisible.
     */
    private boolean canOccupy(double centerX, double centerY) {
        return Barn.canOccupyCenteredBox(centerX, centerY, HITBOX_SIZE, HITBOX_SIZE)
                && (treeObstacleMap == null || treeObstacleMap.canOccupyCenteredBox(centerX, centerY, HITBOX_SIZE, HITBOX_SIZE));
    }

    /**
     * Détecte un demi-tour immédiat par rapport au pas précédent.
     * Cela évite l'effet "ping-pong" contre la grange quand plusieurs directions sont techniquement possibles.
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
     * Prolonge légèrement le détour choisi autour de la grange.
     * Sans ça, le lapin risquerait de repointer tout de suite vers l'obstacle à la frame suivante.
     */
    private void setDetourTarget(double angle) {
        double detourDistance = 180 + random.nextDouble() * 60;
        double nextTargetX = x + Math.cos(angle) * detourDistance;
        double nextTargetY = y + Math.sin(angle) * detourDistance;
        int halfViewportWidth = viewportWidth / 2;
        int halfViewportHeight = viewportHeight / 2;

        targetX = Math.max(-halfViewportWidth + 20, Math.min(halfViewportWidth - 20, nextTargetX));
        targetY = Math.max(-halfViewportHeight + 20, Math.min(halfViewportHeight - 20, nextTargetY));
    }

    /**
     * Petit helper géométrique: il définit la "zone à éviter" autour de la grange au moment
     * de choisir des cibles approximatives dans le champ.
     */
    private boolean isInsideBarnAvoidanceZone(double candidateX, double candidateY, Rectangle barnBounds, int margin) {
        return candidateX >= barnBounds.x - margin
            && candidateX <= barnBounds.x + barnBounds.width + margin
            && candidateY >= barnBounds.y - margin
            && candidateY <= barnBounds.y + barnBounds.height + margin;
    }
    
    /**
     * Expose la position X arrondie pour l'affichage de la vue.
     */
    public synchronized int getX() { return (int) x; }

    public static int getCollisionSize() {
        return HITBOX_SIZE;
    }

    /**
     * Expose la position Y arrondie pour l'affichage de la vue.
     */
    public synchronized int getY() { return (int) y; }

    /**
     * Indique au modèle si ce lapin peut être retiré de la liste active.
     */
    public synchronized boolean hasFled() { return hasFled; }

    /**
     * Indique à l'overlay si le chrono pertinent est actuellement celui de la consommation.
     */
    public synchronized boolean isEatingCultureCountdownActive() {
        return hasCultureTarget() && isOnTargetCultureCell();
    }

    /**
     * Donne la durée totale du chrono affiché.
     * L'overlay peut ainsi calculer sa progression sans connaître les détails métier.
     */
    public synchronized long getOverlayCountdownMaxMs() {
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
