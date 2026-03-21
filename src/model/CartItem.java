package model;

// Élément de panier : référence produit + quantité demandée
// classe interne pour représenter un élément du panier
public  class CartItem {
    /** Classe représentant un élément du panier d'achat du magasin.
     * Un élément du panier est composé d'un produit et d'une quantité.
     * **/

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
