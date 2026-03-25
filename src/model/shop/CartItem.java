package model.shop;

/** Classe représentant un élément du panier d'achat du magasin.
 * Un élément du panier est composé d'un produit et d'une quantité.
 * **/
public  class CartItem {
    Product product;
    int quantity;
    // constructeur
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public int totalPrice() {
        return product.getPrice() * quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}
