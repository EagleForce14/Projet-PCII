package model.shop;

import model.culture.Type;

public class Seed extends Product {

    /**
     * Classe représentant une graine du magasin.
     * Chaque graine a un nom, un prix et une quantité.
     * **/
    // atttributs propres à une graine 
    //private int prixDeVente; // prix de vente de la culture une fois récoltée
    private Type type; // type concret de la graine
    
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

    
     /** getType : méthode pour récupérer le type concret de la graine
      * @return Type : le type de la graine
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

    public boolean isFleur() {
        return type.isFleur();
    }

    public boolean isLegume() {
        return type.isLegume();
    }
}
