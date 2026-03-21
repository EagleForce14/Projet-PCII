package model;

import java.util.ArrayList;


public class Shop {
    /**
     * Classe représentant le magasin du jeu.
     * Achat des graines, de clotures et de main d'oeuvre.
     *
     **/
    private ArrayList<Seed> seeds; // liste des graines disponibles dans le magasin
    private ArrayList<Facility> facilities; // liste des installations disponibles dans le magasin 
    // panier avec des item différents du stock pour éviter de modifier les quantités du stock avant l'achat
    private ArrayList<CartItem> shoppingCard; // liste des produits que le joueur souhaite acheter

    // référence vers l'inventaire du joueur


    //Constructeur
    public Shop() {

        seeds = new ArrayList<>();
        facilities = new ArrayList<>();
        shoppingCard = new ArrayList<>();

        // initialisation des produits disponibles dans le magasin
        /** pour l'instant, les autres types sont commenté 

         */
        seeds.add(new Seed("Tulipe", 8, 100, Type.TULIPE));
        seeds.add(new Seed("Carrote", 5, 100, Type.CAROTTE));
        facilities.add(new Facility("Cloture", 50, 20, FacilityType.CLOTURE));
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
    public ArrayList<CartItem> getShoppingCard() {
        // si le panier est null, on initialise une nouvelle liste de produits à acheter
        if (shoppingCard == null) {
            shoppingCard = new ArrayList<>();
        } else if (shoppingCard.size() == 0) {
            System.out.println("Le panier est vide");
        }
        return shoppingCard;
    }

    public int getShoppingCardTotalPrice() {
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
    public Boolean addToShoppingCard(Product product, int quantity) {
        // on vérifie que la quantité demandée est disponible dans le magasin
        if (product.getQuantity() < quantity) {
            System.out.println("La quantité demandée n'est pas disponible dans le magasin. Veuillez choisir une quantité inférieure ou égale à " + product.getQuantity());
            return false;
        }


        // on ajoute au panier sans modifier l'objet de stock

        // on vérifie le nom du produit pour éviter les doublons dans le panier
        for (CartItem item : shoppingCard) {
            if (item.getProduct().getName().equals(product.getName())) {
                // si le produit est déjà dedans on change juste la quantité
                item.quantity += quantity;
                return true;
            }
        }

        // sinon on ajoute le nouvel item
        shoppingCard.add(new CartItem(product, quantity));
        return true;
    }

    /**
     * une méthode pour retirer un produit de la liste des produits que le joueur souhaite acheter
     *
     * @param product : le produit que le joueur souhaite retirer
     * @param quantité : la quantité du produit que le joueur souhaite retirer
     **/

    public void removeFromShoppingCard(Product product,int quantité) {
        for (CartItem item : shoppingCard) {
            if (item.getProduct().getName().equals(product.getName())) {
                if (item.getQuantity() > quantité) {
                    item.quantity -= quantité;
                } else {
                    shoppingCard.remove(item);
                }
                break;
            }
        }

    }

    /**
     * une méthode pour vider la liste des produits que le joueur souhaite acheter
     *
     **/
    public void clearShoppingCard() {
        shoppingCard.clear();
    }

    /**
     * une méthode pour acheter les produits du panier
     *
     * @param playerMoney : l'argent du joueur
     * @return int : l'argent du joueur après l'achat des produits du panier
     *
     **/

    public boolean buyProducts(Money playerMoney,Inventaire inventaire) {

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


}
