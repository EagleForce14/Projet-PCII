package model; 

public class Seed extends Product {

    /**
     * Classe représentant une graine du magasin.
     * Chaque graine a un nom, un prix et une quantité.
     * **/
    // atttributs propres à une graine 
    //private int prixDeVente; // prix de vente de la culture une fois récoltée
    private Type type; // type de la graine (fleur ou légume)
    
    // constructeur
    public Seed(String name, int price, int quantity, Type type) {
        super(name, price, quantity);   
        //this.prixDeVente = PrVente;
        this.type = type;
    }

    // getter et setter
    /** getPrixDeVente : méthode pour récupérer le prix de vente de la culture une fois récoltée
     * @return int : le prix de vente de la culture une fois récoltée 
     **/
    /* 
    public int getPrixDeVente() {
        return prixDeVente;
    }*/

    
     /** getType : méthode pour récupérer le type de la graine (fleur ou légume)
      * @return SeedType : le type de la graine (fleur ou légume)
      **/
     public Type getType() {
        return type;
    }

    /** setPrixDeVente : méthode pour modifier le prix de vente de la culture une fois récoltée
    * @param prixDeVente : le nouveau prix de vente de la culture une fois récoltée
    **/

    /**
    public void setPrixDeVente(int prixDeVente) {
        this.prixDeVente = prixDeVente;
    }**/

    // récupération du type de la graine , si fleure 

    public boolean isFleur() {
        return type == Type.FLEURS;
    }

    /**
     * Récupère le type de la graine si elle est une fleur.
     * @return FleurType : le type de la graine
     * @throws IllegalStateException : si la graine n'est pas une fleur
     **/
    public FleurType getFleurType() {
        if (isFleur()) {
            return FleurType.valueOf(name.toUpperCase());
        } else {
            throw new IllegalStateException("La graine n'est pas une fleur.");
        }
    }

    // si légume
    public boolean isLegume() {
        return type == Type.LEGUMES;
    }

    /**
    * Récupère le type de la graine si elle est un légume.
    * @return LegumeType : le type de la graine
    * @throws IllegalStateException : si la graine n'est pas un légume
    **/
    public LegumeType getLegumeType() {
        if (isLegume()) {
            return LegumeType.valueOf(name.toUpperCase());
        } else {
            throw new IllegalStateException("La graine n'est pas un légume.");
        }
    }
}
