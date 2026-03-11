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
        // On met à jour la largeur de la fenêtre avec la valeur la plus récente.
        this.viewportWidth = viewportWidth;
        // On met à jour la hauteur de la fenêtre avec la valeur la plus récente.
        this.viewportHeight = viewportHeight;
        // On met à jour la largeur du champ avec la valeur la plus récente.
        this.fieldWidth = fieldWidth;
        // On met à jour la hauteur du champ avec la valeur la plus récente.
        this.fieldHeight = fieldHeight;

        // Moitié de la largeur de la fenêtre.
        int halfWidth = viewportWidth / 2;
        // Moitié de la hauteur de la fenêtre.
        int halfHeight = viewportHeight / 2;
        // Moitié de la largeur du champ.
        int halfFieldWidth = fieldWidth / 2;
        // Moitié de la hauteur du champ.
        int halfFieldHeight = fieldHeight / 2;

        // Vérifie si l'ennemi doit fuir
        // On n'entre dans ce bloc que si un joueur existe et que le lapin n'est pas déjà en fuite.
        if (player != null && !isFleeing) {
            // Si l'ennemi entre dans la zone d'influence de l'unité, l'unité ennemie fuit
            if (player.isInInfluenceZone((int) x, (int) y)) {
                // Changement d'état: l'unité ennemie fuit désormais.
                isFleeing = true;
                // On calcule une cible de fuite.
                updateFleeTarget(player);
            }
        }

        // Si une cible devient invalide (i.e. une valeur incohérente dans targetX oiu targetY), on la recalcule.
        // C'est une sécurité pour éviter qu'une unité ennemie se bloque.
        if (Double.isNaN(targetX) || Double.isNaN(targetY) || Double.isInfinite(targetX) || Double.isInfinite(targetY)) {
            // Si l'unité ennemie est en fuite, on recalcule une fuite.
            if (isFleeing && player != null) {
                updateFleeTarget(player);
            } else {
                // S'il n'est pas en fuite, on le relance simplement sur une nouvelle cible.
                pickNewTarget();
            }
        }

        // Tant qu'il n'a pas atteint le champ, il vise une zone aleatoire du champ.
        // On gère ici l'approche initiale du lapin depuis l'extérieur.
        if (!isInsideMap) {
            // Si sa position est maintenant dans les bornes du champ, on considère qu'il est arrivé.
            if (x >= -halfFieldWidth && x <= halfFieldWidth && y >= -halfFieldHeight && y <= halfFieldHeight) {
                isInsideMap = true;
            // Sinon, de temps en temps, on ajuste son point d'entrée pour éviter une trajectoire trop rigide.
            // Remarque = on ne le fait surtout pas pendant une fuite, sinon on écraserait la cible de fuite.
            } else if (!isFleeing && random.nextInt(90) == 0) {
                pickFieldEntryTarget();
            }
        // Une fois dans le champ, s'il ne fuit pas, il se promène.
        } else if (!isFleeing) {
            // Comportement de balade (wander) une fois à l'intérieur, s'il ne fuit pas
            // On décrémente le temps avant le prochain changement de cible.
            wanderTimer--;
            // Quand le timer arrive à zéro, on choisit une nouvelle destination.
            if (wanderTimer <= 0) {
                pickNewTarget();
                // Change de direction toutes les 1 à 3 secondes.
                wanderTimer = 60 + random.nextInt(120); 
            }
        }
        
        // Déplacement vers la cible
        // Distance horizontale à la cible.
        double dx = targetX - x;
        // Distance verticale à la cible.
        double dy = targetY - y;
        // On calcule la distance euclidienne jusqu'à la cible.
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // La vitesse de l'unité dépend de l'état courant du lapin.
        // Vitesse plus élevée utilisée quand le lapin fuit le joueur.
        double fleeSpeed = 3.0;
        // Vitesse normale de déplacement quand le lapin se promène.
        double speed = 1.5;
        double currentSpeed = isFleeing ? fleeSpeed : speed;
        
        // Si la cible est encore assez loin, on avance dans sa direction.
        if (distance > currentSpeed) {
            // Déplacement normalisé sur X.
            x += (dx / distance) * currentSpeed;
            // Déplacement normalisé sur Y.
            y += (dy / distance) * currentSpeed;
        } else {
            // La cible atteinte, on en choisit une nouvelle si on est dans la carte et qu'on ne fuit pas
            // Remarque : en fuite, on ne remplace pas la cible ici, car il faut sortir de l'écran.
            if (isInsideMap && !isFleeing) {
                pickNewTarget();
            }
        }

        // Certaines unités ennemies peuvent tomber sur une cible trop proche ou un etat incoherent.
        // On detecte ce cas et on force une nouvelle cible pour eviter qu'ils restent bloqués.
        // On applique donc une mesure simple de déplacement depuis la dernière frame logique.
        double movedDistance = Math.abs(x - lastX) + Math.abs(y - lastY);
        // Si le déplacement est presque nul, on considère qu'il est possiblement bloqué.
        if (movedDistance < 0.05) {
            stagnantFrames++;
        } else {
            stagnantFrames = 0;
        }
        // On mémorise la nouvelle position X pour l'update suivante.
        lastX = x;
        // On mémorise la nouvelle position Y pour l'update suivante.
        lastY = y;

        // Si le lapin reste trop longtemps quasi immobile, on le débloque.
        if (stagnantFrames > 20) {
            // En fuite, on recalcule une cible de fuite.
            if (isFleeing && player != null) {
                updateFleeTarget(player);
            } else {
                // Hors fuite, on repart sur une autre direction normale.
                pickNewTarget();
            }
            // Le compteur repart à zéro après correction.
            stagnantFrames = 0;
        }
        
        // Si le ennemi fuit et qu'il est sorti de la carte, on le marque comme ayant fui
        // Cette condition permet au modèle de le retirer proprement de la liste.
        if (isFleeing && (x < -halfWidth - 100 || x > halfWidth + 100 || y < -halfHeight - 100 || y > halfHeight + 100)) {
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
    
    // Retourne la position entière actuelle sur X pour l'affichage.
    public synchronized int getX() { return (int) x; }
    // Retourne la position entière actuelle sur Y pour l'affichage.
    public synchronized int getY() { return (int) y; }
    // Indique si l'unité ennemie est sorti de la fenêtre près sa fuite.
    public synchronized boolean hasFled() { return hasFled; }
}
