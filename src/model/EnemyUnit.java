package model;

import java.util.Random;

/**
 * Représente un ennemi (IA ennemie).
 */
public class EnemyUnit {
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
    // Indique si le lapin a complètement quitté l'écran après une fuite.
    private volatile boolean hasFled = false;
    // Dernière position connue sur X, que l'on utilise pour détecter un blocage.
    private double lastX;
    // Dernière position connue sur Y, que l'on utilise pour détecter un blocage.
    private double lastY;
    // Compteur du nombre d'updates pendant lesquels le lapin a presque cessé de bouger.
    private int stagnantFrames = 0;

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
    
    // On construit ici une unité ennemie avec les dimensions connues au moment de son apparition.
    public EnemyUnit(int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
        // On fait apparaître le lapin hors écran.
        spawnOutside();
        // On initialise la dernière position connue sur X.
        lastX = x;
        // On initialise la dernière position connue sur Y.
        lastY = y;
    }
    
    /**
     * On fait apparaître l'unité ennemie aléatoirement en dehors des limites de la carte.
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
     * On met à jour la position et le comportement du ennemi.
     * @param player Le joueur (pour vérifier la zone d'influence)
     */
    public synchronized void update(Unit player, int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight) {
        refreshDimensions(viewportWidth, viewportHeight, fieldWidth, fieldHeight);
        handleFleeTrigger(player);
        ensureValidTarget(player);
        updateNavigationState();
        moveTowardTarget(player);
        updateStagnationAndRecover(player);
        updateFledStatus();
    }

    /**
     * Met à jour les dimensions de référence utilisées par l'IA sur cette frame.
     */
    private void refreshDimensions(int viewportWidth, int viewportHeight, int fieldWidth, int fieldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.fieldWidth = fieldWidth;
        this.fieldHeight = fieldHeight;
    }

    /**
     * Passe le lapin en état de fuite si le joueur entre dans sa zone d'influence.
     */
    private void handleFleeTrigger(Unit player) {
        if (player == null || isFleeing) {
            return;
        }

        if (player.isInInfluenceZone((int) x, (int) y)) {
            isFleeing = true;
            updateFleeTarget(player);
        }
    }

    /**
     * Garantit que la cible courante est valide; sinon, recalcule une cible adaptée à l'état.
     */
    private void ensureValidTarget(Unit player) {
        if (!Double.isNaN(targetX) && !Double.isNaN(targetY)
            && !Double.isInfinite(targetX) && !Double.isInfinite(targetY)) {
            return;
        }

        if (isFleeing && player != null) {
            updateFleeTarget(player);
        } else {
            pickNewTarget();
        }
    }

    /**
     * Gère l'état de navigation: entrée dans le champ, puis promenade aléatoire hors fuite.
     */
    private void updateNavigationState() {
        int halfFieldWidth = fieldWidth / 2;
        int halfFieldHeight = fieldHeight / 2;

        if (!isInsideMap) {
            if (x >= -halfFieldWidth && x <= halfFieldWidth && y >= -halfFieldHeight && y <= halfFieldHeight) {
                isInsideMap = true;
            } else if (!isFleeing && random.nextInt(90) == 0) {
                pickFieldEntryTarget();
            }
            return;
        }

        if (!isFleeing) {
            wanderTimer--;
            if (wanderTimer <= 0) {
                pickNewTarget();
                wanderTimer = 60 + random.nextInt(120);
            }
        }
    }

    /**
     * Calcule le pas de déplacement vers la cible et applique les collisions avec la grange.
     */
    private void moveTowardTarget(Unit player) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double currentSpeed = isFleeing ? 3.0 : 1.5;

        if (distance > 0.0001) {
            double step = Math.min(currentSpeed, distance);
            double stepX = (dx / distance) * step;
            double stepY = (dy / distance) * step;
            moveWithBarnCollision(stepX, stepY, currentSpeed, player);
            return;
        }

        if (isInsideMap && !isFleeing) {
            pickNewTarget();
        }
    }

    /**
     * Détecte un blocage (l'unité ennemie est quasi immobile) et force donc une relance de cible pour débloquer l'ennemi.
     */
    private void updateStagnationAndRecover(Unit player) {
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
        } else {
            pickNewTarget();
        }
        stagnantFrames = 0;
    }

    /**
     * Marque l'ennemi comme "fui" lorsqu'il a quitté la zone de jeu pendant une fuite.
     */
    private void updateFledStatus() {
        if (!isFleeing) {
            return;
        }

        int halfWidth = viewportWidth / 2;
        int halfHeight = viewportHeight / 2;
        if (x < -halfWidth - 100 || x > halfWidth + 100 || y < -halfHeight - 100 || y > halfHeight + 100) {
            hasFled = true;
        }
    }
    
    /**
     * Surcharge pour la compatibilité si on n'a pas de joueur. A VOIR SI ON SUPPRIME (PROBABLEMENT)
     */
    public synchronized void update() {
        // On réutilise la méthode principale avec les dimensions déjà connues.
        update(null, viewportWidth, viewportHeight, fieldWidth, fieldHeight);
    }
    
    /**
     * Permet de choisir un point d'entree aleatoire dans le champ.
     */
    private void pickFieldEntryTarget() {
        // Moitié de la largeur du champ.
        int halfFieldWidth = fieldWidth / 2;
        // Moitié de la hauteur du champ.
        int halfFieldHeight = fieldHeight / 2;

        // Cible horizontale choisie à l'intérieur du champ avec une petite marge.
        targetX = random.nextInt(Math.max(1, fieldWidth - 80)) - (halfFieldWidth - 40);
        // Cible verticale choisie à l'intérieur du champ avec une petite marge.
        targetY = random.nextInt(Math.max(1, fieldHeight - 80)) - (halfFieldHeight - 40);
    }

    /**
     * Recalcule une direction de fuite loin du joueur.
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
     * Permet de choisir une nouvelle destination proche dans le champ pour garder un déplacement naturel.
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

    private void moveWithBarnCollision(double stepX, double stepY, double speed, Unit player) {
        // Collision grange désactivée: les lapins traversent librement.
        x += stepX;
        y += stepY;
    }
    
    // Retourne la position entière actuelle sur X pour l'affichage.
    public synchronized int getX() { return (int) x; }
    // Retourne la position entière actuelle sur Y pour l'affichage.
    public synchronized int getY() { return (int) y; }
    // Indique si l'unité ennemie est sorti de la fenêtre près sa fuite.
    public synchronized boolean hasFled() { return hasFled; }
}
