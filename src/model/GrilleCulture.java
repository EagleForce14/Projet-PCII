package model;

import java.util.Map;

/** Classe représentant la grille de culture */
public class GrilleCulture {
    /** Constante représentant la largeur de la grille */
    public static final int LARGEUR_GRILLE = 10;
    
    /** Constante représentant la hauteur de la grille */
    public static final int HAUTEUR_GRILLE = 10;

    /** Constante représentant le prix d'achat de chaque culture */
    public static final Map<Type, Integer> PRIX_ACHAT_CULTURES = Map.of(
        Type.TYPE1, 10,
        Type.TYPE2, 20,
        Type.TYPE3, 30
    );

    /** Constante représentant le prix de vente de chaque culture */
    public static final Map<Type, Integer> PRIX_VENTE_CULTURES = Map.of(
        Type.TYPE1, 15,
        Type.TYPE2, 25,
        Type.TYPE3, 35
    );

    /** Constante représentant le delai de croissance de chaque culture */
    public static final Map<Type, Integer> DELAI_CROISSANCE_CULTURES = Map.of(
        Type.TYPE1, 5,
        Type.TYPE2, 10,
        Type.TYPE3, 15
    );

    /** Attribut représentant la grille de culture */
    ZoneCulture[][] grille;

    // Centralise la validation des coordonnées pour que vue et logique manipulent la même grille.
    private boolean estDansGrille(int x, int y) {
        return x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE;
    }

    /** Constructeur de la grille de culture */
    public GrilleCulture() {
        this.grille = new ZoneCulture[LARGEUR_GRILLE][HAUTEUR_GRILLE];
        // Initialiser chaque zone de culture de la grille
        for (int i = 0; i < LARGEUR_GRILLE; i++) {
            for (int j = 0; j < HAUTEUR_GRILLE; j++) {
                grille[i][j] = new ZoneCulture();
            }
        }
    }

    /** Fonction qui renvoie le prix d'achat d'une culture */
    public static int getPrixAchat(Type type) {
        return PRIX_ACHAT_CULTURES.get(type);
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

    /** Getter qui renvoie la zone de culture a une position donnee. */
    public ZoneCulture getZoneCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            return grille[x][y];
        }
        return null;
    }

    /** Méthode qui plante une culture dans la grille */
    public void planterCulture(int x, int y, Type type) {
        if (estDansGrille(x, y)) {
            grille[x][y].planterCulture(type);
        }
    }

    /** Méthode qui recolte une culture de la grille */
    public int recolterCulture(int x, int y) {
        if (estDansGrille(x, y)) {
            return grille[x][y].recolterCulture();
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
}
