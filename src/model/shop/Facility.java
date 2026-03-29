package model.shop;

/**
 * Classe représentant une installation du jeu.
 * Les installations couvrent ici tous les objets "non graine" vendus en boutique:
 * clôture, chemin, compost, jardinier, etc.
 *
 * Chaque installation garde seulement
 * - un nom lisible,
 * - un prix,
 * - un stock en boutique,
 * - un type métier.
 *
 **/
public class Facility extends Product {
    // Attribut propre à une installation : son type métier.
    private final FacilityType type;

    // constructeur
    public Facility(String name, int price, int quantity, FacilityType type) {
        super(name, price, quantity);
        this.type = type;
    }

    // getter et setter

    /** Renvoie le type métier de l'installation. */
    public FacilityType getType() {
        return type;
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    // pas de setter pour type car inchangeable
}
