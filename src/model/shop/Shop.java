package model.shop;

import model.culture.Type;
import model.management.Inventaire;
import model.management.Money;
import model.runtime.DayChangeListener;

import java.util.ArrayList;
import java.util.Random;



public class Shop implements DayChangeListener {
    private static final int COMPOST_RESTOCK_DAY = 10;
    private static final int INITIAL_COMPOST_STOCK = 1;
    final Random rand = new Random();


    /**
     * Classe représentant le magasin du jeu.
     * Achat des graines, de clotures et de main d'oeuvre.
     *
     **/
    private final ArrayList<Seed> seeds; // liste des graines disponibles dans le magasin
    private final ArrayList<Facility> facilities; // liste des installations disponibles dans le magasin
    // panier avec des item différents du stock pour éviter de modifier les quantités du stock avant l'achat
    private ArrayList<CartItem> shoppingCard; // liste des produits que le joueur souhaite acheter
    private int currentDay;
    private boolean compostRestockUnlocked;

    // référence vers l'inventaire du joueur


    //Constructeur
    public Shop() {

        seeds = new ArrayList<>();
        facilities = new ArrayList<>();
        shoppingCard = new ArrayList<>();
        currentDay = 1;
        compostRestockUnlocked = false;


        // Toutes les graines partagees avec l'inventaire sont visibles en boutique.
        addSeed("Tulipe", 8, Type.TULIPE);
        addSeed("Rose", 8, Type.ROSE);
        addSeed("Marguerite", 8, Type.MARGUERITE);
        addSeed("Orchidee", 8, Type.ORCHIDEE);
        addSeed("Carotte", 5, Type.CAROTTE);
        addSeed("Tomate", 5, Type.TOMATE);
        addSeed("Choufleur", 5, Type.CHOUFLEUR);
        addSeed("Courgette", 5, Type.COURGETTE);
        facilities.add(new Facility("Cloture", 50, 20, FacilityType.CLOTURE));
        facilities.add(new Facility("Chemin", 12, 200, FacilityType.CHEMIN));
        facilities.add(new Facility("Compost", 80, INITIAL_COMPOST_STOCK, FacilityType.COMPOST));
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

    /** Une methode pour modifier le prix des produits dans la boutique
     * @param product : le produit dont on veut modifier le prix
     * @param newPrice : le nouveau prix du produit
     * **/
    public synchronized boolean updateProductPrice(Product product, int newPrice) {

        // Validation des paramètres d'entrée
        if (product == null || newPrice < 0) {
            return false;
        }

        // Recherche du produit dans les graines
        for (Seed seed : seeds) {
            if (seed.getName().equals(product.getName())) {
                seed.setPrice(newPrice);
                return true;
            }
        }

        // Recherche du produit dans les installations
        for (Facility facility : facilities) {
            if (facility.getName().equals(product.getName())) {
                facility.setPrice(newPrice);
                return true;
            }
        }

        return false; // Produit non trouvé
    }

    /**
     * une méthode pour ajouter un produit à la liste des produits que le joueur souhaite acheter
     *
     * @param product : le produit que le joueur souhaite acheter
     *
     **/
    public synchronized Boolean addToShoppingCard(Product product, int quantity) {
        if (product == null || quantity <= 0 || quantity > product.getQuantity()) {
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
                    inventaire.ajoutInstallation(((Facility) item.product).getType(), item.quantity);
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

    /**
     * Petit helper local pour retrouver rapidement une installation par son type.
     * Le magasin a peu d'objets, donc une simple boucle reste largement suffisante.
     */
    private Facility getFacilityByType(FacilityType type) {
        for (Facility facility : facilities) {
            if (facility.getType() == type) {
                return facility;
            }
        }
        return null;
    }

    /**
     * Expose le jour courant à l'interface pour que la boutique puisse
     * expliquer clairement quand le second compost devient disponible.
     */
    public synchronized int getCurrentDay() {
        return currentDay;
    }

    /**
     * Indique si le palier du jour 10 a déjà été atteint.
     */
    public synchronized boolean isCompostRestockUnlocked() {
        return compostRestockUnlocked;
    }


    /**increasePricesByPercent
     * @param percent : pourcentage en augmentation**/
    public synchronized void increasePricesByPercent(double percent) {
        increasePricesByPercent(percent, 0.0, Integer.MAX_VALUE, 0.0);
    }


    /**
     * increasePricesByPercent
     * Augmente tous les prix du magasin d'un certain pourcentage (arrondis à l'entier le plus proche).
     * Si percent est négatif, les prix baissent.
     * l'augmentation se fait aléatoirement sur les produit pour avoir de la variété et augmenter ceux qui se vendent plus cher
     * @param percent le pourcentage d'augmentation (ex: 10.0 pour +10%, -5.0 pour -5%)
      * @param volatilityPercent la volatilité de l'augmentation, exprimée en pourcentage (ex: 20.0 pour une variation de +/-20% autour du pourcentage de base)
     * @param expens le seuil de prix à partir duquel les produits sont considérés comme chers et ont une augmentation supplémentaire
     * (ex: 50 pour considérer les produits à plus de 50 comme chers)
     *  @param expensExtra le pourcentage d'augmentation supplémentaire pour les produits chers (ex: 10.0 pour +10% en plus pour les produits chers)
     *
     */
    public synchronized void increasePricesByPercent(double percent,
                                                     double volatilityPercent,
                                                     int expens,
                                                     double expensExtra) {

        // toujours positive pour faciliter les calculs
        if (volatilityPercent < 0) {
            volatilityPercent = Math.abs(volatilityPercent);
        }

        // on fixe la volatilité pour éviter les problèmes de concurrence avec les lambda
        double finalVolatilityPercent = volatilityPercent;
        // lambda pour calculer la variation aléatoire en fonction de la volatilité
        java.util.function.Supplier<Double> nextPercent = () -> {
            double rand = (new Random().nextDouble() * 2.0 - 1.0) * finalVolatilityPercent; // entre -volatilityPercent et +volatilityPercent
            return percent * rand;
        };

        // on applique la variation aléatoire à chaque produit, avec un bonus pour les produits chers
        for (Seed seed : seeds) {
            double randVariation = (rand.nextDouble() * 2.0 - 1.0) * volatilityPercent; // entre -volatilityPercent et +volatilityPercent
            double finalPercent = percent + randVariation;

            if (seed.getPrice() > expens) {
                finalPercent += rand.nextDouble() * expensExtra; // ajouter un bonus aléatoire pour les produits chers
            }
            // on fixe une augmentation minimale de 90% pour éviter que les prix ne deviennent trop bas
            finalPercent = Math.max(finalPercent, 0.0);
            int newPrice = (int) Math.round(seed.getPrice() * (1.0 + finalPercent / 100.0));

            // on force une augmentation d'au moins 1
            if (newPrice<= seed.getPrice() && finalPercent > 0) {
                newPrice = seed.getPrice() + 1;
            }
            seed.setPrice(newPrice);
        }
        for (Facility facility : facilities) {
            double randVariation = (rand.nextDouble() * 2.0 - 1.0) * volatilityPercent; // entre -volatilityPercent et +volatilityPercent
            double finalPercent = percent + randVariation;

            if (facility.getPrice() > expens) {
                finalPercent += rand.nextDouble() * expensExtra; // ajouter un bonus aléatoire pour les produits chers
            }
            finalPercent = Math.max(finalPercent, 90.0); //
            int newPrice = (int) Math.round(facility.getPrice() * (1.0 + finalPercent / 100.0));
            if (facility.getPrice() <= expens && finalPercent < 0) {
                newPrice = facility.getPrice() + 1; // limiter la baisse à 10% pour les produits non chers
            }

            facility.setPrice( newPrice);
        }
    }

    /**
     * Implémentation de DayChangeListener : augmente automatiquement les prix d'un petit pourcentage
     * à chaque début de jour. Valeur choisie par défaut : +30% par jour, ce qui correspond à une inflation assez forte
     * pour être perceptible, mais pas trop rapide pour ne pas devenir ingérable.
     */
    @Override
    public void onNewDay(int day) {
        currentDay = day;
        // Politique simple :
        increasePricesByPercent(3.0);

        /*
         * Au jour 10, on rouvre une seule fois l'achat du compost.
         * On ajoute 1 au stock total du produit, ce qui permet un nouvel achat
         * sans toucher au reste de la logique du panier.
         */
        if (!compostRestockUnlocked && day >= COMPOST_RESTOCK_DAY) {
            Facility compost = getFacilityByType(FacilityType.COMPOST);
            if (compost != null) {
                compost.setQuantity(compost.getQuantity() + 1);
            }
            compostRestockUnlocked = true;
        }
    }

}
