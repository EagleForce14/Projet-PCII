package model.shop;

import model.culture.Type;

public class Seed extends Product {

    /**
     * Classe représentant une graine du magasin.
     * Chaque graine a un nom, un prix et une quantité.
     * **/
    // atttributs propres à une graine 
    //private int prixDeVente; // prix de vente de la culture une fois récoltée
    private final Type type; // type concret de la graine
    
    // constructeur
    public Seed(String name, int price, int quantity, Type type) {
        super(name, price, quantity);   
        //this.prixDeVente = PrVente;
        this.type = type;
    }

     /** getType : méthode pour récupérer le type concret de la graine
      * @return Type : le type de la graine
      **/
     public Type getType() {
        return type;
    }

    public boolean isFleur() {
        return type.isFleur();
    }

    public boolean isLegume() {
        return type.isLegume();
    }
}
