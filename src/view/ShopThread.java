package view;

import java.util.Scanner;

import model.CartItem;
import model.Facility;
import model.Money;
import model.Product;
import model.Seed;
import model.Shop;

/**
 * Thread dédié à l'ouverture de la boutique dans le terminal.
 * Il affiche le contenu du magasin puis attend la saisie utilisateur.
 */
public class ShopThread extends Thread {
    /** Scanner console partagé pour lire les saisies utilisateur */
    private static final Scanner SCANNER = new Scanner(System.in);

    /** Référence vers le magasin pour afficher le stock et ajouter au panier */
    private final Shop shop;

    /** Référence vers l'argent du joueur pour afficher son solde courant */
    private final Money playerMoney;

    /**
     * Construit un thread de boutique.
     * @param shop magasin du jeu
     * @param playerMoney argent du joueur
     */
    public ShopThread(Shop shop, Money playerMoney) {
        this.shop = shop;
        this.playerMoney = playerMoney;
    }

    /**
     * Exécute l'interaction console de la boutique.
     */
    @Override
    public void run() {
        System.out.println("Ouverture de la boutique");
        System.out.println("Argent du joueur : " + playerMoney.getAmount() + "€");
        System.out.println("Graines disponibles : ");
        for (Seed seed : shop.getSeeds()) {
            System.out.println("- " + seed.getName() + " (Type: " + seed.getType() + ", Prix: " + seed.getPrice() + ", Quantité disponible: " + seed.getQuantity() + ")");
        }
        System.out.println("Installations disponibles : ");
        for (Facility facility : shop.getFacilities()) {
            System.out.println("- " + facility.getName() + " (Type: " + facility.getType() + ", Prix: " + facility.getPrice() + ", Quantité disponible: " + facility.getQuantity() + ")");
        }
        System.out.println("Panier : ");
        for (CartItem item : shop.getShoppingCard()) {
            if (item.getProduct() instanceof Seed) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Seed) item.getProduct()).getType() + ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            } else if (item.getProduct() instanceof Facility) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Facility) item.getProduct()).getType() + ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            }
        }

        System.out.println("Entrez le nom du produit:");
        String nomProduit = SCANNER.nextLine();
        System.out.println("Entrez la quantité:");
        int quantite = lireQuantite();

        Product productToBuy = null;
        for (Seed seed : shop.getSeeds()) {
            if (seed.getName().equalsIgnoreCase(nomProduit)) {
                productToBuy = seed;
                break;
            }
        }

        if (productToBuy == null) {
            for (Facility facility : shop.getFacilities()) {
                if (facility.getName().equalsIgnoreCase(nomProduit)) {
                    productToBuy = facility;
                    break;
                }
            }
        }

        if (productToBuy != null) {
            boolean achatReussi = shop.addToShoppingCard(productToBuy, quantite);
            while (!achatReussi) {
                System.out.println("La quantité n'est pas valide, veuillez réessayer : ");
                System.out.println("Entrez la quantité:");
                quantite = lireQuantite();
                achatReussi = shop.addToShoppingCard(productToBuy, quantite);
            }
        } else {
            System.out.println("Le produit demandé n'existe pas dans le magasin.");
        }
    }

    /**
     * Lit une quantité entière strictement positive dans le terminal.
     * @return quantité valide saisie par l'utilisateur
     */
    private int lireQuantite() {
        while (true) {
            try {
                int quantite = Integer.parseInt(SCANNER.nextLine().trim());
                if (quantite > 0) {
                    return quantite;
                }
            } catch (NumberFormatException exception) {
                // On redemande une quantité valide tant que la saisie n'est pas correcte.
            }

            System.out.println("Veuillez entrer un entier strictement positif.");
        }
    }
}
