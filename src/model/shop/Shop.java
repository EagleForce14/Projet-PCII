package model.shop;

import model.culture.Type;
import model.management.Inventaire;
import model.management.Money;

import java.util.ArrayList;


public class Shop {
    /**
     * Classe représentant le magasin du jeu.
     * Achat des graines, de clotures et de main d'oeuvre.
     *
     **/
    private final ArrayList<Seed> seeds; // liste des graines disponibles dans le magasin
    private final ArrayList<Facility> facilities; // liste des installations disponibles dans le magasin
    // panier avec des item différents du stock pour éviter de modifier les quantités du stock avant l'achat
    private ArrayList<CartItem> shoppingCard; // liste des produits que le joueur souhaite acheter

    // référence vers l'inventaire du joueur


    //Constructeur
    public Shop() {

        seeds = new ArrayList<>();
        facilities = new ArrayList<>();
        shoppingCard = new ArrayList<>();

        // Toutes les graines partagees avec l'inventaire sont visibles en boutique.
        addSeed("Tulipe", 8, Type.TULIPE);
        addSeed("Rose", 8, Type.ROSE);
        addSeed("Marguerite", 8, Type.MARGUERITE);
        addSeed("Orchidee", 8, Type.ORCHIDEE);
        addSeed("Carotte", 5, Type.CAROTTE);
        addSeed("Tomate", 5, Type.TOMATE);
        addSeed("Poivron", 5, Type.POIVRON);
        addSeed("Courgette", 5, Type.COURGETTE);
        facilities.add(new Facility("Cloture", 50, 20, FacilityType.CLOTURE));
        // Le chemin est volontairement bon marché et disponible en plus grand stock,
        // car le joueur peut vouloir en acheter beaucoup.
        facilities.add(new Facility("Chemin", 12, 200, FacilityType.CHEMIN));
        facilities.add(new Facility("Engrais", 30, 50, FacilityType.ENGRAIS));
        facilities.add(new Facility("Jardinier", 100, 10, FacilityType.JARDINIER));
    }

    // getter et setter

    /**
     * une méthode pour récupérer la liste des graines disponibles dans le magasin
     *
     * @return ArrayList<Seed> : la liste des graines disponibles dans le magasin
     *
     **/
    public ArrayList<Seed> getSeeds() {
        return seeds;
    }


    /**
     * une méthode pour récupérer la liste des installations disponibles dans le magasin
     *
     * @return ArrayList<Facility> : la liste des installations disponibles dans le magasin
     *
     **/
    public ArrayList<Facility> getFacilities() {
        return facilities;
    }

    /**
     * une méthode pour récupérer la liste des produits que le joueur souhaite acheter
     *
     * @return ArrayList<CartItem> : la liste des produits que le joueur souhaite acheter
     *
     **/
    public synchronized ArrayList<CartItem> getShoppingCard() {
        if (shoppingCard == null) {
            shoppingCard = new ArrayList<>();
        }
        return new ArrayList<>(shoppingCard);
    }

    public synchronized int getShoppingCardTotalPrice() {
        int totalPrice = 0;
        for (CartItem item : shoppingCard) {
            totalPrice += item.totalPrice();
        }
        return totalPrice;
    }


    /**
     * une méthode pour ajouter un produit à la liste des produits que le joueur souhaite acheter
     *
     * @param product : le produit que le joueur souhaite acheter
     *
     **/
    public synchronized Boolean addToShoppingCard(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return false;
        }

        int desiredQuantity = getShoppingCardQuantity(product) + quantity;
        return setShoppingCardQuantity(product, desiredQuantity);
    }

    /**
     * une méthode pour retirer un produit de la liste des produits que le joueur souhaite acheter
     *
     * @param product : le produit que le joueur souhaite retirer
     * @param quantite : la quantité du produit que le joueur souhaite retirer
     **/

    public synchronized void removeFromShoppingCard(Product product,int quantite) {
        if (product == null || quantite <= 0) {
            return;
        }

        int nextQuantity = Math.max(0, getShoppingCardQuantity(product) - quantite);
        setShoppingCardQuantity(product, nextQuantity);
    }

    /**
     * une méthode pour vider la liste des produits que le joueur souhaite acheter
     *
     **/
    public synchronized void clearShoppingCard() {
        shoppingCard.clear();
    }

    public synchronized int getShoppingCardQuantity(Product product) {
        CartItem item = findCartItem(product);
        return item == null ? 0 : item.getQuantity();
    }

    public synchronized int getRemainingStock(Product product) {
        if (product == null) {
            return 0;
        }
        return Math.max(0, product.getQuantity() - getShoppingCardQuantity(product));
    }

    /**
     * Fixe la quantité exacte présente dans le panier.
     * Une quantité à 0 retire naturellement l'article.
     */
    public synchronized boolean setShoppingCardQuantity(Product product, int quantity) {
        if (product == null || quantity < 0 || quantity > product.getQuantity()) {
            return false;
        }

        CartItem existingItem = findCartItem(product);
        if (quantity == 0) {
            if (existingItem != null) {
                shoppingCard.remove(existingItem);
            }
            return true;
        }

        if (existingItem != null) {
            existingItem.quantity = quantity;
            return true;
        }

        shoppingCard.add(new CartItem(product, quantity));
        return true;
    }

    /**
     * une méthode pour acheter les produits du panier
     *
     * @param playerMoney : l'argent du joueur
     * @return int : l'argent du joueur après l'achat des produits du panier
     *
     **/

    public synchronized boolean buyProducts(Money playerMoney,Inventaire inventaire) {
        if (shoppingCard.isEmpty()) {
            return false;
        }

        int totalPrice = 0;
        int playerAmount = playerMoney.getAmount();
        boolean achatReussi = false;
        // calcul du prix total des produits dans le panier
        for (CartItem item : shoppingCard) {
            totalPrice += item.totalPrice();
        }

        // cas d'achat réussi : le joueur a assez d'argent pour acheter les produits du panier
        if (playerAmount >= totalPrice) {
            // mise à jour de l'argent du joueur et des quantités des produits dans le magasin
            playerMoney.subtract(new Money(totalPrice));
            ;


            // mise à jour des quantités des produits dans le magasin
            // en parcourant tout le panier et en mettant à jour les quantités des produits correspondants 
            for (CartItem item : shoppingCard) {

                // cas des graines 
                if (item.product instanceof Seed) {
                    for (Product seed : seeds) {
                        if (seed.getName().equals(item.product.getName())) {
                            seed.setQuantity(seed.getQuantity() - item.quantity);
                            break;
                        }
                    }
                    // ajout dans l'inventaire
                    inventaire.ajoutGraine((Seed) item.product, item.quantity);

                    // cas des installations
                } else if (item.product instanceof Facility) {
                    for (Product facility : facilities) {
                        if (facility.getName().equals(item.product.getName())) {
                            facility.setQuantity(facility.getQuantity() - item.quantity);
                            break;
                        }
                    }
                    // ajout dans l'inventaire
                    inventaire.ajoutInstallation((Facility) item.product, item.quantity);
                }
            }
            achatReussi = true;


            // vider le panier après l'achat
            clearShoppingCard();
            // affichage de l'argent restant du joueur après l'achat

            return achatReussi;
        }
        // cas d'achat échoué : le joueur n'a pas assez d'argent pour acheter les produits du panier
        else {

            return achatReussi;
        }


    }

    private CartItem findCartItem(Product product) {
        if (product == null) {
            return null;
        }

        for (CartItem item : shoppingCard) {
            if (item.getProduct().getName().equals(product.getName())) {
                return item;
            }
        }
        return null;
    }

    private void addSeed(String name, int price, Type type) {
        seeds.add(new Seed(name, price, 100, type));
    }


}
