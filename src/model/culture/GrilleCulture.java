package model.culture;

import model.management.Inventaire;
import model.objective.GestionnaireObjectifs;
import model.shop.FacilityType;

import java.util.Map;

/** Classe représentant la grille de culture */
public class GrilleCulture {
    /**
     * La grille n'est plus un petit carré central :
     * elle couvre désormais toute la zone de jeu visible.
     *
     * Ces dimensions ont été choisies pour remplir confortablement la fenêtre
     * avec des cases carrées proches de la taille visuelle de l'ancien champ.
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
        Type.CAROTTE, 20,
        Type.TOMATE, 20,
        Type.POIVRON, 20,
        Type.COURGETTE, 20
    );

    /** Constante représentant le prix de vente de chaque culture */
    public static final Map<Type, Integer> PRIX_VENTE_CULTURES = Map.of(
        Type.TULIPE, 15,
        Type.ROSE, 20,
        Type.MARGUERITE, 12,
        Type.ORCHIDEE, 25,
        Type.CAROTTE, 25,
        Type.TOMATE, 30,
        Type.POIVRON, 35,
        Type.COURGETTE, 28
    );

    /** SERA UTILISE PROCHAINEMENT. Constante représentant le delai de croissance de chaque culture */
    public static final Map<Type, Integer> DELAI_CROISSANCE_CULTURES = Map.of(
        Type.TULIPE, 5,
        Type.ROSE, 5,
        Type.MARGUERITE, 5,
        Type.ORCHIDEE, 5,
        Type.CAROTTE, 10,
        Type.TOMATE, 10,
        Type.POIVRON, 10,
        Type.COURGETTE, 10
    );

    /** Attribut représentant la grille de culture */
    ZoneCulture[][] grille;

    // Chaque bit représente un segment de clôture posé sur un des 4 bords d'une case.
    private final int[][] fenceMasks;

    /*
     * Les chemins occupent la surface complete d'une case.
     * On les stocke donc à part, comme un simple drapeau par case.
     *
     * Pourquoi ne pas les mettre dans ZoneCulture ?
     * Parce qu'un chemin n'est pas une culture.
     * C'est un décor de sol, au même niveau que l'herbe ou la terre.
     */
    private final boolean[][] pathCells;

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
        this.pathCells = new boolean[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        // Toute la map démarre en herbe :
        // chaque case existe déjà, mais aucune n'est labourée au lancement.
        for (int i = 0; i < LARGEUR_GRILLE; i++) {
            for (int j = 0; j < HAUTEUR_GRILLE; j++) {
                grille[i][j] = new ZoneCulture(false);
                pathCells[i][j] = false;
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
            return false;
        }

        int neighborX = x + side.getDeltaX();
        int neighborY = y + side.getDeltaY();
        if (!estDansGrille(neighborX, neighborY)) {
            return true;
        }

        CellSide oppositeSide = side.opposite();
        if (oppositeSide == null || hasFence(neighborX, neighborY, oppositeSide)) {
            return false;
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
            return true;
        }

        return !isLabouree(neighborX, neighborY);
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
        if (!canPlaceFence(x, y, side)) {
            throw new IllegalStateException("Cette cloture doit etre posee sur le bord libre d'une case de terre.");
        }

        fenceMasks[x][y] = fenceMasks[x][y] | side.getMask(); // Attention OU binaire
        inventaire.UseInstallation(FacilityType.CLOTURE);
    }

    /**
     * Rend une case cultivable.
     * On garde cette méthode dans la grille pour que le contrôleur n'ait jamais
     * besoin de manipuler les objets ZoneCulture directement.
     */
    public void labourerCase(int x, int y) {
        if (!estDansGrille(x, y)) {
            throw new IllegalStateException("Coordonnees hors de la grille.");
        }
        if (hasPath(x, y)) {
            throw new IllegalStateException("Impossible de labourer une case recouverte par un chemin.");
        }

        grille[x][y].labourer();
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

        if (grille[x][y].planterCulture(type)) {
            // Met à jour les objectifs liés à la plantation de cultures
            gestionnaireObjectifs.mettreAJourObjectifsPlanter(type);
        }

        // retirer la graine de l'inventaire
        inventaire.UseGraineFleure(type);
    }

    /** Méthode qui recolte une culture de la grille */
    public int recolterCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            Type type = getCulture(x, y).getType(); // Récupère le type de la culture avant de la récolter pour mettre à jour les objectifs
            int prixVente = grille[x][y].recolterCulture(); // Récupère le prix de vente de la culture récoltée
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
}
