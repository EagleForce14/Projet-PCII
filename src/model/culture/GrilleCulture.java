package model.culture;

import model.management.Inventaire;
import model.objective.GestionnaireObjectifs;
import model.shop.FacilityType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Classe représentant la grille de culture */
public class GrilleCulture {
    public static final int FENCE_HIT_POINTS = 4;
    private static final long FENCE_BAR_VISIBLE_MS = 1400L;

    /**
     * La grille couvre désormais toute la zone de jeu visible.
     *
     * Ces dimensions ont été choisies pour remplir confortablement la fenêtre.
     */
    public static final int LARGEUR_GRILLE = 22;
    
    /** Constante représentant la hauteur de la grille */
    public static final int HAUTEUR_GRILLE = 16;

    /** Constante représentant le prix d'achat de chaque culture */
    public static final Map<Type, Integer> PRIX_ACHAT_CULTURES = Map.of(
        Type.TULIPE, 10,
        Type.ROSE, 10,
        Type.MARGUERITE, 10,
        Type.ORCHIDEE, 10,
        Type.NENUPHAR, 10,
        Type.IRIS_DES_MARAIS, 10,
        Type.CAROTTE, 20,
        Type.RADIS, 20,
        Type.CHOUFLEUR, 20
    );

    /** Constante représentant le prix de vente de chaque culture */
    public static final Map<Type, Integer> PRIX_VENTE_CULTURES = Map.of(
        Type.TULIPE, 15,
        Type.ROSE, 20,
        Type.MARGUERITE, 12,
        Type.ORCHIDEE, 25,
        Type.NENUPHAR, 15,
        Type.IRIS_DES_MARAIS, 15,
        Type.CAROTTE, 25,
        Type.RADIS, 30,
        Type.CHOUFLEUR, 35
    );

    /** SERA UTILISE PROCHAINEMENT. Constante représentant le delai de croissance de chaque culture */
    public static final Map<Type, Integer> DELAI_CROISSANCE_CULTURES = Map.of(
        Type.TULIPE, 5,
        Type.ROSE, 5,
        Type.MARGUERITE, 5,
        Type.ORCHIDEE, 5,
        Type.NENUPHAR, 5,
        Type.IRIS_DES_MARAIS, 5,
        Type.CAROTTE, 10,
        Type.RADIS, 10,
        Type.CHOUFLEUR, 10
    );

    /** Attribut représentant la grille de culture */
    ZoneCulture[][] grille;

    // Chaque bit représente un segment de clôture posé sur un des 4 bords d'une case.
    private final int[][] fenceMasks;
    // Chaque segment garde son propre nombre de coups encaissés.
    private final int[][][] fenceHitCounts;
    // La barre de vie n'est pas toujours affichée : elle apparaît après un impact.
    // C'est un tableau 3D car on doit stocker la position de la case mais aussi le côté concerné de cette case.
    // Même remarque pour les autres attrbuts avec des tableaux 3D de cette classe.
    private final long[][][] fenceBarVisibleUntilMs;
    // Les destructions gardent un effet visuel très court pour rendre l'ouverture du passage lisible.
    private final List<FenceDestructionEffect> fenceDestructionEffects;

    /*
     * Les composts sont peu nombreux par design :
     * on en autorise au maximum deux sur la map.
     *
     * Une petite liste de positions reste donc plus simple et plus lisible
     * qu'une matrice dédiée supplémentaire.
     */
    private final List<Point> compostCells;
    private final List<Point> bridgeAnchorCells;

    /*
     * Les chemins occupent la surface complete d'une case.
     * On les stocke donc à part, comme un simple drapeau par case.
     *
     * Pourquoi ne pas les mettre dans ZoneCulture ?
     * Parce qu'un chemin n'est pas une culture.
     * C'est un décor de sol, au même niveau que l'herbe ou la terre.
     */
    private final boolean[][] pathCells;

    /*
     * La rivière est décorative.
     * On stocke donc directement ses cases ici :
     * elles servent à la fois au rendu et à toutes les règles de blocage.
     */
    private final boolean[][] decorativeRiverCells;

    /*
     * Portée choisie pour le compost.
     * On utilise ici une distance de Manhattan :
     * le bonus forme un "diamant" lisible autour de chaque compost.
     */
    private static final int COMPOST_RANGE = 2;

    /*
     * au total, la ferme ne peut jamais accueillir plus de deux composts posés en même temps.
     */
    private static final int MAX_COMPOST_COUNT = 2;

    /*
     * Pour la rivière, on choisit un voisinage très proche :
     * la case elle-même n'est jamais cultivable,
     * mais toutes les cases adjacentes (y compris en diagonale)
     * profitent d'un petit microclimat plus humide.
     */
    private static final int RIVER_BOOST_RADIUS = 1;

    /** Attribut représentant le gestionnaire d'objectifs */
    private final GestionnaireObjectifs gestionnaireObjectifs;

    // Centralise la validation des coordonnées pour que vue et logique manipulent la même grille.
    private boolean estDansGrille(int x, int y) {
        return x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE;
    }

    /** Constructeur de la grille de culture */
    public GrilleCulture(GestionnaireObjectifs gestionnaireObjectifs) {
        this.gestionnaireObjectifs = gestionnaireObjectifs;
        this.grille = new ZoneCulture[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        this.fenceMasks = new int[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        this.fenceHitCounts = new int[LARGEUR_GRILLE][HAUTEUR_GRILLE][CellSide.values().length];
        this.fenceBarVisibleUntilMs = new long[LARGEUR_GRILLE][HAUTEUR_GRILLE][CellSide.values().length];
        this.fenceDestructionEffects = new ArrayList<>();
        this.compostCells = new ArrayList<>();
        this.bridgeAnchorCells = new ArrayList<>();
        this.pathCells = new boolean[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        this.decorativeRiverCells = new boolean[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        // Toute la map démarre en herbe :
        // chaque case existe déjà, mais aucune n'est labourée au lancement.
        for (int i = 0; i < LARGEUR_GRILLE; i++) {
            for (int j = 0; j < HAUTEUR_GRILLE; j++) {
                grille[i][j] = new ZoneCulture(false, decorativeRiverCells, i, j);
                pathCells[i][j] = false;
                decorativeRiverCells[i][j] = false;
            }
        }
    }

    /** Fonction qui renvoie le prix de vente d'une culture */
    public static int getPrixVente(Type type) {
        return PRIX_VENTE_CULTURES.get(type);
    }

    /** Getter qui renvoie la largeur logique de la grille. */
    public int getLargeur() {
        return LARGEUR_GRILLE;
    }

    /** Getter qui renvoie la hauteur logique de la grille. */
    public int getHauteur() {
        return HAUTEUR_GRILLE;
    }

    public boolean hasFence(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y)) {
            return false;
        }

        return (fenceMasks[x][y] & side.getMask()) != 0;
    }

    public int getFenceRemainingHitPoints(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y) || !hasFence(x, y, side)) {
            return 0;
        }

        return Math.max(0, FENCE_HIT_POINTS - fenceHitCounts[x][y][side.ordinal()]);
    }

    public double getFenceIntegrityRatio(int x, int y, CellSide side) {
        int remainingHitPoints = getFenceRemainingHitPoints(x, y, side);
        if (remainingHitPoints <= 0) {
            return 0.0;
        }

        return remainingHitPoints / (double) FENCE_HIT_POINTS;
    }

    /**
     * Dit si un segment de clôture touche ce bord précis de la case.
     * On regarde donc à la fois le segment stocké sur la case elle-même
     * et celui éventuellement stocké sur la voisine d'en face.
     */
    public boolean hasFenceTouchingCellSide(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y)) {
            return false;
        }

        if (hasFence(x, y, side)) {
            return true;
        }

        int neighborX = x + side.getDeltaX();
        int neighborY = y + side.getDeltaY();
        CellSide oppositeSide = side.opposite();
        return oppositeSide != null
                && estDansGrille(neighborX, neighborY)
                && hasFence(neighborX, neighborY, oppositeSide);
    }

    /** Dit si la case touche une clôture sur un de ses 4 côtés. */
    public boolean isLabourBlockedByAdjacentFence(int x, int y) {
        if (!estDansGrille(x, y)) {
            return false;
        }

        for (CellSide side : CellSide.values()) {
            if (hasFenceTouchingCellSide(x, y, side)) {
                return true;
            }
        }
        return false;
    }

    /** Centralise la règle de labourage pour le contrôleur et la sidebar. */
    public boolean canLabourCell(int x, int y) {
        return estDansGrille(x, y)
                && !isLabouree(x, y)
                && !hasPath(x, y)
                && !hasRiver(x, y)
                && !hasBridgeAnchorAt(x, y)
                && !hasCompostAt(x, y)
                && !isLabourBlockedByAdjacentFence(x, y);
    }

    /**
     * La barre de vie ne doit pas encombrer le champ en permanence.
     * On la montre donc juste après un coup, sauf en état critique :
     * une clôture rouge reste visible tant qu'elle tient encore debout.
     */
    public boolean shouldShowFenceHealthBar(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y) || !hasFence(x, y, side)) {
            return false;
        }

        int sideIndex = side.ordinal();
        int remainingHitPoints = getFenceRemainingHitPoints(x, y, side);
        if (remainingHitPoints <= 1) {
            return true;
        }

        return System.currentTimeMillis() <= fenceBarVisibleUntilMs[x][y][sideIndex];
    }

    /**
     * Applique un coup de lapin sur un segment.
     * Le segment saute seulement au quatrième impact frontal.
     */
    public synchronized boolean damageFence(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y) || !hasFence(x, y, side)) {
            return false;
        }

        int sideIndex = side.ordinal();
        long now = System.currentTimeMillis();
        fenceHitCounts[x][y][sideIndex] = Math.min(FENCE_HIT_POINTS, fenceHitCounts[x][y][sideIndex] + 1);
        fenceBarVisibleUntilMs[x][y][sideIndex] = now + FENCE_BAR_VISIBLE_MS;
        if (fenceHitCounts[x][y][sideIndex] < FENCE_HIT_POINTS) {
            return false;
        }

        destroyFence(x, y, side, now);
        return true;
    }

    /**
     * Une clôture sert ici à fermer le bord d'une parcelle de terre.
     *
     * Les règles :
     * - la case de depart doit etre une case de terre,
     * - on ne pose pas de clôture entre deux cases de terre voisines,
     * - un chemin reste un bord valide,
     * - on refuse toujours les doublons sur un segment déjà occupé.
     *
     * Autrement dit, la clôture se pose sur le contour de la parcelle,
     * pas au milieu d'un bloc de terre continu.
     */
    public boolean canPlaceFence(int x, int y, CellSide side) {
        if (side == null
                || !estDansGrille(x, y)
                || !isLabouree(x, y)
                || hasPath(x, y)
                || hasFence(x, y, side)) {
            return true;
        }

        int neighborX = x + side.getDeltaX();
        int neighborY = y + side.getDeltaY();
        if (!estDansGrille(neighborX, neighborY)) {
            return false;
        }

        CellSide oppositeSide = side.opposite();
        if (oppositeSide == null || hasFence(neighborX, neighborY, oppositeSide)) {
            return true;
        }

        /*
         * Si le voisin est lui aussi une case de terre "normale",
         * alors ce bord est interieur a la parcelle:
         * on interdit donc la clôture a cet endroit.
         *
         * Le chemin est explicitement autorisé comme voisin:
         * il reste visuellement logique d'avoir une clôture entre terre et chemin.
         */
        if (hasPath(neighborX, neighborY)) {
            return false;
        }

        return isLabouree(neighborX, neighborY);
    }

    /**
     * La pose consomme une clôture de l'inventaire uniquement après validation complète.
     */
    public void placeFence(int x, int y, CellSide side, Inventaire inventaire) {
        if (side == null) {
            throw new IllegalStateException("Aucun cote de cloture n'a ete selectionne.");
        }
        if (inventaire == null || inventaire.possedeInstallation(FacilityType.CLOTURE)) {
            throw new IllegalStateException("Aucune cloture n'est disponible dans l'inventaire.");
        }
        if (canPlaceFence(x, y, side)) {
            throw new IllegalStateException("Cette cloture doit etre posee sur le bord libre d'une case de terre.");
        }

        fenceMasks[x][y] = fenceMasks[x][y] | side.getMask(); // Attention OU binaire
        resetFenceState(x, y, side);
        inventaire.UseInstallation(FacilityType.CLOTURE);
    }

    public synchronized List<FenceDestructionEffect> getActiveFenceDestructionEffects() {
        long now = System.currentTimeMillis();
        fenceDestructionEffects.removeIf(effect -> effect.isExpired(now));
        return new ArrayList<>(fenceDestructionEffects);
    }

    private void destroyFence(int x, int y, CellSide side, long destroyedAtMs) {
        fenceMasks[x][y] = fenceMasks[x][y] & ~side.getMask();
        resetFenceState(x, y, side);
        fenceDestructionEffects.add(new FenceDestructionEffect(x, y, side, destroyedAtMs));
    }

    private void resetFenceState(int x, int y, CellSide side) {
        int sideIndex = side.ordinal();
        fenceHitCounts[x][y][sideIndex] = 0;
        fenceBarVisibleUntilMs[x][y][sideIndex] = 0L;
    }

    /**
     * Rend une case cultivable.
     * On garde cette méthode dans la grille pour que le contrôleur n'ait jamais
     * besoin de manipuler les objets ZoneCulture directement.
     */
    public void labourerCase(int x, int y) {
        labourerCaseInterne(x, y, true);
    }

    /**
     * Variante utilisée pendant l'initialisation du terrain.
     * Les cases pré-labourées de départ ne doivent pas compter pour les objectifs joueurs.
     */
    public void labourerCaseSansObjectif(int x, int y) {
        labourerCaseInterne(x, y, false);
    }

    private void labourerCaseInterne(int x, int y, boolean compterObjectif) {
        if (!estDansGrille(x, y)) {
            throw new IllegalStateException("Coordonnees hors de la grille.");
        }
        if (hasPath(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case recouverte par un chemin.");
        }
        if (hasRiver(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case occupée par une rivière.");
        }
        if (hasBridgeAnchorAt(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case occupée par un pont.");
        }
        if (hasCompostAt(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case recouverte par un compost.");
        }
        if (isLabourBlockedByAdjacentFence(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case adjacente à une clôture.");
        }

        boolean etaitLabouree = grille[x][y].isLabouree();
        grille[x][y].labourer();
        if (compterObjectif && !etaitLabouree && gestionnaireObjectifs != null) {
            gestionnaireObjectifs.mettreAJourObjectifsLabourer();
        }
    }

    /**
     * Petit helper de lecture utilisé par la vue et la sidebar.
     * Le but est d'éviter de répéter partout "grille[x][y].isLabouree()"
     * avec une validation manuelle des coordonnées.
     */
    public boolean isLabouree(int x, int y) {
        return estDansGrille(x, y) && grille[x][y].isLabouree();
    }

    /**
     * Lecture simple de l'etat décoratif d'une case.
     * La vue s'en sert pour choisir la bonne tuile de sol,
     * et le gameplay pour savoir si le joueur marche sur un chemin.
     */
    public boolean hasPath(int x, int y) {
        return estDansGrille(x, y) && pathCells[x][y];
    }

    /**
     * Lecture simple de l'état "rivière" d'une case.
     * La rivière est seulement décorative,
     * mais elle porte aussi toutes les règles métier de blocage.
     */
    public boolean hasRiver(int x, int y) {
        return estDansGrille(x, y) && decorativeRiverCells[x][y];
    }

    /**
     * Dit si au moins un compost est déjà posé quelque part sur la map.
     */
    public boolean hasCompost() {
        return !compostCells.isEmpty();
    }

    /**
     * Le pont est mémorisé par sa case d'ancrage côté droit de la rivière.
     * Cette case reste marchable, mais elle n'est plus libre pour d'autres placements.
     */
    public boolean hasBridgeAnchorAt(int x, int y) {
        return findBridgeAnchorIndexAt(x, y) >= 0;
    }

    /**
     * Expose les ponts posés sous forme de copies pour que les vues puissent
     * les dessiner sans toucher à l'état interne de la grille.
     */
    public List<Point> getBridgeAnchorCells() {
        return new ArrayList<>(bridgeAnchorCells);
    }

    /**
     * Petit helper de lecture pratique pour les règles d'interface
     * qui veulent afficher explicitement combien de composts sont déjà posés.
     */
    public int getCompostCount() {
        return compostCells.size();
    }

    /**
     * Helper de lecture très simple utilisé par la vue et le contrôleur
     * quand ils veulent savoir si la case cliquée correspond au compost.
     */
    public boolean hasCompostAt(int x, int y) {
        return findCompostIndexAt(x, y) >= 0;
    }

    /**
     * Un chemin peut etre posé seulement sur une case libre :
     * qui n'est pas déjà recouverte par un chemin,
     * qui n'est pas occupée par une culture.
     *
     * Ici la règle voulue est simple :
     * un chemin se pose sur de l'herbe,
     * pas sur une case deja transformée en terre.
     */
    public boolean canPlacePath(int x, int y) {
        return estDansGrille(x, y)
                && !hasPath(x, y)
                && !hasRiver(x, y)
                && !hasBridgeAnchorAt(x, y)
                && !hasCompostAt(x, y)
                && !isLabouree(x, y)
                && getCulture(x, y) == null;
    }

    /**
     * Pose un chemin sur une case et consomme une unite dans l'inventaire.
     */
    public void placePath(int x, int y, Inventaire inventaire) {
        if (inventaire == null || inventaire.possedeInstallation(FacilityType.CHEMIN)) {
            throw new IllegalStateException("Aucun chemin n'est disponible dans l'inventaire.");
        }
        if (!canPlacePath(x, y)) {
            throw new IllegalStateException("Ce chemin doit etre pose sur une case libre.");
        }

        pathCells[x][y] = true;
        inventaire.UseInstallation(FacilityType.CHEMIN);
    }

    /**
     * Une rivière se pose sur une case d'herbe totalement libre.
     *
     * Important :
     * on la traite comme un vrai obstacle de terrain.
     * Donc pas de labour, pas de culture, pas de chemin, pas de compost sur la même case.
     */
    public boolean canPlaceRiver(int x, int y) {
        return estDansGrille(x, y)
                && !hasRiver(x, y)
                && !hasPath(x, y)
                && !hasCompostAt(x, y)
                && !isLabouree(x, y)
                && getCulture(x, y) == null;
    }

    /**
     * Pose une rivière fixe au chargement de la partie.
     * On n'utilise pas l'inventaire ici car il s'agit d'un element de decor.
     */
    public void placeDecorativeRiver(int x, int y) {
        if (!canPlaceRiver(x, y)) {
            throw new IllegalStateException("La rivière decorative doit être posée sur une case d'herbe libre.");
        }

        decorativeRiverCells[x][y] = true;
    }

    /**
     * Un pont se pose uniquement sur la berge droite de la rivière décorative.
     *
     * La case ciblée reste une case de berge normale côté sol,
     * mais elle devient l'ancre métier et visuelle du pont posé.
     */
    public boolean canPlaceBridge(int x, int y) {
        return estDansGrille(x, y)
                && estDansGrille(x - 1, y)
                && hasRiver(x - 1, y)
                && !hasRiver(x, y)
                && !hasPath(x, y)
                && !hasBridgeAnchorAt(x, y)
                && !hasCompostAt(x, y)
                && !isLabouree(x, y)
                && getCulture(x, y) == null;
    }

    /**
     * Pose le pont sur sa case d'ancrage côté droit puis consomme un exemplaire
     * dans l'inventaire. Le rendu couvrira ensuite la rivière et la berge opposée,
     * mais l'état minimal à stocker ici reste seulement cette case d'ancrage.
     */
    public void placeBridge(int x, int y, Inventaire inventaire) {
        if (inventaire == null || inventaire.possedeInstallation(FacilityType.PONT)) {
            throw new IllegalStateException("Aucun pont n'est disponible dans l'inventaire.");
        }
        if (!canPlaceBridge(x, y)) {
            throw new IllegalStateException("Le pont doit être posé sur une case libre collée à droite de la rivière.");
        }

        bridgeAnchorCells.add(new Point(x, y));
        inventaire.UseInstallation(FacilityType.PONT);
    }

    /**
     * Le compost se pose seulement sur de l'herbe libre.
     *
     * Les règles :
     * - pas plus de deux composts posés en même temps,
     * - pas sur une case deja labourée,
     * - pas sur un chemin,
     * - pas sur une case deja occupee.
     */
    public boolean canPlaceCompost(int x, int y) {
        return estDansGrille(x, y)
                && getCompostCount() < MAX_COMPOST_COUNT
                && !hasCompostAt(x, y)
                && !hasPath(x, y)
                && !hasRiver(x, y)
                && !hasBridgeAnchorAt(x, y)
                && !isLabouree(x, y)
                && getCulture(x, y) == null;
    }

    /**
     * Pose un compost puis consomme l'objet de l'inventaire.
     * On garde volontairement la validation ici, pour que le contrôleur et l'interface
     * profitent exactement des mêmes règles que le coeur du gameplay.
     */
    public void placeCompost(int x, int y, Inventaire inventaire) {
        if (inventaire == null || inventaire.possedeInstallation(FacilityType.COMPOST)) {
            throw new IllegalStateException("Aucun compost n'est disponible dans l'inventaire.");
        }
        if (!canPlaceCompost(x, y)) {
            throw new IllegalStateException("Le compost doit etre pose sur une case d'herbe libre.");
        }

        compostCells.add(new Point(x, y));
        inventaire.UseInstallation(FacilityType.COMPOST);
    }

    /**
     * Permet de reprendre le compost pour le remettre dans l'inventaire.
     */
    public void storeCompost(int x, int y, Inventaire inventaire) {
        if (inventaire == null) {
            throw new IllegalStateException("Impossible de remiser le compost sans inventaire.");
        }
        int compostIndex = findCompostIndexAt(x, y);
        if (compostIndex < 0) {
            throw new IllegalStateException("Aucun compost n'est posé sur cette case.");
        }

        compostCells.remove(compostIndex);
        inventaire.ajoutInstallation(FacilityType.COMPOST, 1);
    }

    /**
     * Indique si une case de terre profite du compost.
     *
     * Important :
     * on ne booste que les cases labourées.
     * Le compost peut être proche d'une zone d'herbe,
     * mais cette herbe ne gagne rien tant qu'elle n'est pas transformée en terre.
     */
    public boolean isCellBoostedByCompost(int x, int y) {
        if (!estDansGrille(x, y) || !isLabouree(x, y) || compostCells.isEmpty()) {
            return false;
        }

        for (Point compostCell : compostCells) {
            int distance = Math.abs(x - compostCell.x) + Math.abs(y - compostCell.y);
            if (distance <= COMPOST_RANGE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renvoie les cases de terre actuellement couvertes par le compost.
     * La vue s'en sert pour afficher la surbrillance quand le joueur clique dessus.
     */
    public List<Point> getCompostAffectedSoilCells() {
        List<Point> affectedCells = new ArrayList<>();
        if (compostCells.isEmpty()) {
            return affectedCells;
        }

        for (int x = 0; x < LARGEUR_GRILLE; x++) {
            for (int y = 0; y < HAUTEUR_GRILLE; y++) {
                if (isCellBoostedByCompost(x, y)) {
                    affectedCells.add(new Point(x, y));
                }
            }
        }

        return affectedCells;
    }

    /**
     * Comme on ne gère que deux composts au maximum,
     * une petite boucle est plus claire qu'une structure plus lourde.
     */
    private int findCompostIndexAt(int x, int y) {
        for (int index = 0; index < compostCells.size(); index++) {
            Point compostCell = compostCells.get(index);
            if (compostCell.x == x && compostCell.y == y) {
                return index;
            }
        }
        return -1;
    }

    private int findBridgeAnchorIndexAt(int x, int y) {
        for (int index = 0; index < bridgeAnchorCells.size(); index++) {
            Point bridgeAnchorCell = bridgeAnchorCells.get(index);
            if (bridgeAnchorCell.x == x && bridgeAnchorCell.y == y) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Dit si une case de terre est assez proche d'une rivière pour recevoir son bonus.
     *
     * On inclut volontairement les diagonales :
     * visuellement, cela donne une zone d'humidité compacte et facile à comprendre.
     */
    public boolean isCellBoostedByRiver(int x, int y) {
        return estDansGrille(x, y)
                && isLabouree(x, y)
                && isCellBoostedByRiver(decorativeRiverCells, x, y);
    }

    /**
     * Helper partagé par la grille et les cultures.
     * On centralise ici la lecture du voisinage rivière pour éviter de dupliquer
     * la même boucle sur les cases adjacentes.
     */
    static boolean isCellBoostedByRiver(boolean[][] decorativeRiverCells, int x, int y) {
        if (decorativeRiverCells == null
                || decorativeRiverCells.length == 0
                || x < 0
                || x >= decorativeRiverCells.length
                || decorativeRiverCells[x] == null
                || y < 0
                || y >= decorativeRiverCells[x].length) {
            return false;
        }

        for (int deltaX = -RIVER_BOOST_RADIUS; deltaX <= RIVER_BOOST_RADIUS; deltaX++) {
            for (int deltaY = -RIVER_BOOST_RADIUS; deltaY <= RIVER_BOOST_RADIUS; deltaY++) {
                if (deltaX == 0 && deltaY == 0) {
                    continue;
                }

                int candidateX = x + deltaX;
                int candidateY = y + deltaY;
                if (hasRiverAt(decorativeRiverCells, candidateX, candidateY)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hasRiverAt(boolean[][] decorativeRiverCells, int x, int y) {
        return decorativeRiverCells != null
                && x >= 0
                && x < decorativeRiverCells.length
                && decorativeRiverCells[x] != null
                && y >= 0
                && y < decorativeRiverCells[x].length
                && decorativeRiverCells[x][y];
    }

    /**
     * La rivière décorative découpe la ferme en deux zones de plantation.
     * Toute graine connaît sa zone autorisée, et la grille expose ici
     * la zone d'une case pour éviter de refaire ce calcul côté vue.
     */
    public FieldZone getFieldZoneAt(int x, int y) {
        if (!estDansGrille(x, y)) {
            return null;
        }

        int riverColumn = findDecorativeRiverColumn();
        if (riverColumn < 0) {
            return FieldZone.RIGHT_OF_RIVER;
        }

        return x < riverColumn ? FieldZone.LEFT_OF_RIVER : FieldZone.RIGHT_OF_RIVER;
    }

    /**
     * Point d'entrée unique pour la règle "quelle graine peut être plantée où ?".
     * Le contrôleur et la sidebar l'utilisent directement pour rester alignés
     * avec la validation finale de planterCulture.
     */
    public boolean canPlantCulture(int x, int y, Type type, Inventaire inventaire) {
        return type != null
                && inventaire != null
                && inventaire.possedeGraine(type)
                && estDansGrille(x, y)
                && !hasPath(x, y)
                && !hasRiver(x, y)
                && !hasBridgeAnchorAt(x, y)
                && isLabouree(x, y)
                && getCulture(x, y) == null
                && type.canBePlantedIn(getFieldZoneAt(x, y));
    }

    /** Méthode qui plante une culture dans la grille */
    public void planterCulture(int x, int y, Type type, Inventaire inventaire) {
        if (!inventaire.possedeGraine(type)) {
            throw new IllegalStateException("L'inventaire est vide. Vous ne pouvez pas planter une culture. veuillez acheter des graines dans la boutique.");
        }

        if (!estDansGrille(x, y)) {
            throw new IllegalStateException("Coordonnees hors de la grille.");
        }
        if (hasPath(x, y)) {
            throw new IllegalStateException("Impossible de planter sur une case recouverte par un chemin.");
        }
        if (hasRiver(x, y)) {
            throw new IllegalStateException("Impossible de planter sur une case occupée par une rivière.");
        }
        if (hasBridgeAnchorAt(x, y)) {
            throw new IllegalStateException("Impossible de planter sur une case occupée par un pont.");
        }
        if (!type.canBePlantedIn(getFieldZoneAt(x, y))) {
            throw new IllegalStateException("Cette graine ne peut pas être plantée dans cette zone du champ.");
        }

        grille[x][y].planterCulture(type);
        // Met à jour les objectifs liés à la plantation de cultures
        gestionnaireObjectifs.mettreAJourObjectifsPlanter(type);

        // retirer la graine de l'inventaire
        inventaire.UseGraineFleure(type);
    }

    /** Méthode qui recolte une culture de la grille */
    public int recolterCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            Type type = getCulture(x, y).getType(); // Récupère le type de la culture avant de la récolter pour mettre à jour les objectifs
            int prixVente = grille[x][y].recolterCulture(); // Récupère le prix de vente de la culture récoltée
            if (isCellBoostedByCompost(x, y)) {
                prixVente = prixVente * 2;
            }
            // Met à jour les objectifs liés à la récolte de cultures
            gestionnaireObjectifs.mettreAJourObjectifsRecolter(type);
            return prixVente;
        } else {
            throw new IllegalStateException("Coordonnées hors de la grille.");
        }
    }

    /** Méthode qui arrose une culture de la grille */
    public void arroserCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            grille[x][y].arroserCulture();
            gestionnaireObjectifs.mettreAJourObjectifsArroser();
        } else {
            throw new IllegalStateException("Coordonnées hors de la grille.");
        }
    }

    /** Getter qui renvoie la culture à une position donnée */
    public Culture getCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            return grille[x][y].getCulture();
        }
        return null;
    }

    private int findDecorativeRiverColumn() {
        for (int x = 0; x < LARGEUR_GRILLE; x++) {
            for (int y = 0; y < HAUTEUR_GRILLE; y++) {
                if (decorativeRiverCells[x][y]) {
                    return x;
                }
            }
        }

        return -1;
    }

    /** Méthode qui mange la culture à une position donnée */
    public void mangerCulture(int x, int y) {
        if (estDansGrille(x, y) && grille[x][y].mangerCulture()) {
            // Met à jour les objectifs liés à la consommation de cultures par les lapins
            gestionnaireObjectifs.mettreAJourObjectifsManger();
        }
    }

    /** Le nettoyage ne supprime que les cultures flétries. */
    public void nettoyerCultureFletrie(int x, int y) {
        if (estDansGrille(x, y)) {
            grille[x][y].nettoyerCultureFletrie();
        }
    }

    /** Getter pour le gestionnaire d'objectifs */
    public GestionnaireObjectifs getGestionnaireObjectifs() {
        return gestionnaireObjectifs;
    }

    /**
     * Quand le joueur relance une partie, on coupe toutes les croissances encore actives
     * pour que l'ancienne grille ne garde aucun thread vivant derrière le rideau.
     */
    public void arreterToutesLesCultures() {
        for (int x = 0; x < LARGEUR_GRILLE; x++) {
            for (int y = 0; y < HAUTEUR_GRILLE; y++) {
                Culture culture = getCulture(x, y);
                if (culture != null) {
                    culture.arreterCroissance();
                }
            }
        }
    }
}
