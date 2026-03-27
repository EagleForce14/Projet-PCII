package model.culture;

import model.management.Inventaire;
import model.objective.GestionnaireObjectifs;
import model.shop.FacilityType;

import java.util.Map;

/** Classe représentant la grille de culture */
public class GrilleCulture {

    /**
     * Une clôture n'occupe pas toute la case:
     * on mémorise simplement sur quel bord extérieur elle a été posée.
     */
    public enum CellSide {
        TOP(0, -1, 1),
        RIGHT(1, 0, 1 << 1),
        BOTTOM(0, 1, 1 << 2),
        LEFT(-1, 0, 1 << 3);

        private final int deltaX;
        private final int deltaY;
        private final int mask;

        CellSide(int deltaX, int deltaY, int mask) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.mask = mask;
        }
    }

    /** Constante représentant la largeur de la grille */
    public static final int LARGEUR_GRILLE = 10;
    
    /** Constante représentant la hauteur de la grille */
    public static final int HAUTEUR_GRILLE = 10;

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

    /** Attribut représentant le gestionnaire d'objectifs */
    private GestionnaireObjectifs gestionnaireObjectifs;

    // Centralise la validation des coordonnées pour que vue et logique manipulent la même grille.
    private boolean estDansGrille(int x, int y) {
        return x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE;
    }

    /** Constructeur de la grille de culture */
    public GrilleCulture(GestionnaireObjectifs gestionnaireObjectifs) {
        this.gestionnaireObjectifs = gestionnaireObjectifs;
        this.grille = new ZoneCulture[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        this.fenceMasks = new int[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        // Initialiser chaque zone de culture de la grille
        for (int i = 0; i < LARGEUR_GRILLE; i++) {
            for (int j = 0; j < HAUTEUR_GRILLE; j++) {
                grille[i][j] = new ZoneCulture();
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

    /**
     * Un bord est "extérieur" si, en avançant dans sa direction,
     * on tombe hors de la grille.
     */
    public boolean isOuterSide(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y)) {
            return false;
        }

        return !estDansGrille(x + side.deltaX, y + side.deltaY);
    }

    public boolean hasFence(int x, int y, CellSide side) {
        if (side == null || !estDansGrille(x, y)) {
            return false;
        }

        return (fenceMasks[x][y] & side.mask) != 0;
    }

    /**
     * Une clôture ne peut être posée que sur un bord extérieur encore libre.
     */
    public boolean canPlaceFence(int x, int y, CellSide side) {
        return isOuterSide(x, y, side) && !hasFence(x, y, side);
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
            throw new IllegalStateException("Cette cloture doit etre posee sur un bord exterieur libre.");
        }

        fenceMasks[x][y] |= side.mask;
        inventaire.UseInstallation(FacilityType.CLOTURE);
    }

    /** Méthode qui plante une culture dans la grille */
    public void planterCulture(int x, int y, Type type, Inventaire inventaire) {
        if (!inventaire.possedeGraine(type)) {
            throw new IllegalStateException("L'inventaire est vide. Vous ne pouvez pas planter une culture. veuillez acheter des graines dans la boutique.");
        }
        if (estDansGrille(x, y) && grille[x][y].planterCulture(type)) {
            // Met à jour les objectifs liés à la plantation de cultures
            gestionnaireObjectifs.mettreAJourObjectifsPlanter(type);
        } else {
            throw new IllegalStateException("Coordonnées hors de la grille ou la zone est déjà occupée.");
        }

        // retirer la graine de l'inventaire
        inventaire.UseGraineFleure(type);
    }

    /** Méthode qui recolte une culture de la grille */
    public int recolterCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            return grille[x][y].recolterCulture();
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
        if (estDansGrille(x, y)) {
            grille[x][y].mangerCulture();
        }
    }

    /** Le nettoyage ne supprime que les cultures flétries. */
    public void nettoyerCultureFletrie(int x, int y) {
        if (estDansGrille(x, y)) {
            grille[x][y].nettoyerCultureFletrie();
        }
    }
}
