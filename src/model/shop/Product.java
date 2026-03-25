package model.shop;

public abstract class Product {
    /**
     * Classe abstraite représentant un produit du magasin.
     * Les produits peuvent être des graines, des clotures, de la main d'oeuvre ou de l'engrais.
     * Chaque produit a un nom, un prix et une quantité.
     * **/
    
    // attributs
    protected String name;
    protected int price;
    protected int quantity;

    // constructeur
    public Product(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    // getter et setter

    // récupératuion du nom du produit
    public String getName() {
        return name;
    }
    // récupération du prix du produit
    public int getPrice() {
        return price;
    }

    //récupération de la quantité du produit
    public int getQuantity() {
        return quantity;
    }

    //modification de la quantité du produit
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    //modification du prix du produit
    public void setPrice(int price) {
        this.price = price;
    }   

    
}