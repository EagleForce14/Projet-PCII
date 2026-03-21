package model;

public class Facility extends Product {
    /**
     * Classe représentant une installation du jeu.
     * Les installations peuvent Des clôtures, des graine ou des jardiniers.
     * Chaque installation a un nom, un prix et une quantité et un type (cloture, engrais ou jardinier).
     *
     **/


    // atttributs propres à une installation 
    private final FacilityType type; // type  (jardinier, cloture, engrais)

    // constructeur
    public Facility(String name, int price, int quantity, FacilityType type) {
        super(name, price, quantity);
        this.type = type;
    }

    // getter et setter

    /**
     * getType : méthode pour récupérer le type de l'installation (cloture, engrais ou jardinier)
     *
     * @return FacilityType : le type de l'installation (cloture, engrais ou jardinier)
     **/
    public FacilityType getType() {
        return type;
    }


    // pas de setter pour type car inchangeable
}