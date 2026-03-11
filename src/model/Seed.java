package model; 


public class Seed extends Product {

    public enum SeedType {
        FLOWER, VEGETABLE
    }
    /**
     * Classe représentant une graine du magasin.
     * Chaque graine a un nom, un prix et une quantité.
     * **/
    // atttributs propres à une graine 
    private int prixDeVente; // prix de vente de la culture une fois récoltée
    private SeedType type; // type de la graine (fleur ou légume)
    
    // constructeur
    public Seed(String name, int price, int quantity, int PrVente, SeedType type) {
        super(name, price, quantity);   
        this.prixDeVente = PrVente;
        this.type = type;
    }

    // getter et setter
    /** getPrixDeVente : méthode pour récupérer le prix de vente de la culture une fois récoltée
     * @return int : le prix de vente de la culture une fois récoltée 
     **/
    public int getPrixDeVente() {
        return prixDeVente;
    }

    
     /** getType : méthode pour récupérer le type de la graine (fleur ou légume)
      * @return SeedType : le type de la graine (fleur ou légume)
      **/
     public SeedType getType() {
        return type;
    }

    /** setPrixDeVente : méthode pour modifier le prix de vente de la culture une fois récoltée
    * @param prixDeVente : le nouveau prix de vente de la culture une fois récoltée
    **/
    public void setPrixDeVente(int prixDeVente) {
        this.prixDeVente = prixDeVente;
    }
}
