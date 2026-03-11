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

    /** Méthode qui plante une culture dans la grille */
    public void planterCulture(int x, int y, Type type) {
        if (x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE) {
            grille[x][y].planterCulture(type);
        }
    }

    /** Getter qui renvoie la culture à une position donnée */
    public Culture getCulture(int x, int y) {
        if (x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE) {
            return grille[x][y].getCulture();
        }
        return null;
    }

    /** Méthode qui mange la culture à une position donnée */
    public void mangerCulture(int x, int y) {
        if (x >= 0 && x < LARGEUR_GRILLE && y >= 0 && y < HAUTEUR_GRILLE) {
            grille[x][y].mangerCulture();
        }
    }
}
