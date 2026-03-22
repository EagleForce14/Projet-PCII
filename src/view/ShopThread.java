package view;

import java.util.Scanner;

import model.*;

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

    /**Référence vers l'inventaire du joueur pour afficher ce qu'il possède**/

    private final Inventaire inventaire;

    /**
     * Construit un thread de boutique.
     * @param shop magasin du jeu
     * @param playerMoney argent du joueur
     */
    public ShopThread(Shop shop, Money playerMoney, Inventaire inventaire) {
        this.shop = shop;
        this.playerMoney = playerMoney;
        this.inventaire = inventaire;
    }

    /**
     * Exécute l'interaction console de la boutique.
     */
    @Override
    public void run() {

        // Affiche le contenu du shop , l'argent du joueur et le panier
        System.out.println("Ouverture de la boutique");
        System.out.println("Argent du joueur : " + playerMoney.getAmount() + "€");
        System.out.println("Graines disponibles : ");

        // parcours du stock dispo pour tous les afficher
        for (Seed seed : shop.getSeeds()) {
            System.out.println("- " + seed.getName() + " (Type: " + seed.getType() + ", Prix: " + seed.getPrice() + ", Quantité disponible: " + seed.getQuantity() + ")");
        }

        // parcours du stock d'installation dispo pou tous les afficher
        System.out.println("Installations disponibles : ");
        for (Facility facility : shop.getFacilities()) {
            System.out.println("- " + facility.getName() + " (Type: " + facility.getType() + ", Prix: " + facility.getPrice() + ", Quantité disponible: " + facility.getQuantity() + ")");
        }

        // la même chose pour le panier
        // au début vide
        afficherPanier();

        // demande à l'utilisateur ce qu'il veut acheter et la quantité
        System.out.println("Entrez le nom du produit:");
        String nomProduit = SCANNER.nextLine();
        System.out.println("Entrez la quantité:");
        int quantite = lireQuantite();

        // recherche du produit dans le magasin
        Product productToBuy = null;
        // première recherche dans les graines
        for (Seed seed : shop.getSeeds()) {
            if (seed.getName().equalsIgnoreCase(nomProduit)) {
                productToBuy = seed;
                break;
            }
        }

        // si pas trouvé dans graines, on cherche dans les installations
        if (productToBuy == null) {
            for (Facility facility : shop.getFacilities()) {
                if (facility.getName().equalsIgnoreCase(nomProduit)) {
                    productToBuy = facility;
                    break;
                }
            }
        }

        // ici , normalement, on a trouvé le produit
        if (productToBuy != null) {
            // ajout du produit dans le panier
            boolean achatReussi = shop.addToShoppingCard(productToBuy, quantite);
            // sinon on redemande une quantité raisonnable à chaque fois que la quantité est invalide
            while (!achatReussi) {
                System.out.println("La quantité n'est pas valide, veuillez réessayer : ");
                System.out.println("Entrez la quantité:");
                quantite = lireQuantite();
                achatReussi = shop.addToShoppingCard(productToBuy, quantite);
            }
            // on va alors afficher de nouveau le panier mis à jours
            afficherPanier();
            // faire une nouvelle demande pour si le joueur valide le panier ou s'il veut continuer à acheter
            demandeValidationPanier();

        } else {

            // si tout ces étapes on été skip, on en déduit que le produit n'existe pas dans le magasin
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

    /**
     * Affichage du contenu du panier
     *
     * **/
    private void afficherPanier() {
        System.out.println("Panier : ");
        for (CartItem item : shop.getShoppingCard()) {
            if (item.getProduct() instanceof Seed) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Seed) item.getProduct()).getType() + ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            } else if (item.getProduct() instanceof Facility) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Facility) item.getProduct()).getType() + ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            }
        }
    }

    /** terminal pour demander si le joueur veut continuer à acheter ou valider le panier
     * **/
    private void demandeValidationPanier(){
        System.out.println("Voulez-vous continuer poursuivre vos achats (vous serez débité pour la totalité lorsque que vous aurez fini vos achats) ? (oui,non) ");
        String reponse = SCANNER.nextLine().trim().toLowerCase();
        while (!reponse.equals("oui") && !reponse.equals("non")) {
            System.out.println("Veuillez répondre par 'oui' ou 'non'. Voulez-vous continuer à acheter (vous serez débité pour la totalité lorsque que vous aurez fini vos achats) ? (oui,non)");
            reponse = SCANNER.nextLine().trim().toLowerCase();
        }
        if (reponse.equals("oui")) {
            run(); // relance le processus d'achat
        } else {
            if (shop.buyProducts(playerMoney,inventaire)) {
                System.out.println("merci pour votre achat ! Votre solde actuel est de " + playerMoney.getAmount() + "€");
                inventaire.afficherInventaire();
                // on ferme la boutique et on retourne au jeu

            } else {
                System.out.println("Vous n'avez pas assez d'argent pour acheter les produits du panier. Enlevez des produits du panier ou gagnez plus d'argent pour pouvoir acheter les produits du panier.");
                System.out.println("Prix total des produits dans le panier : " + shop.getShoppingCardTotalPrice()+ "€");
                System.out.println("Votre argent actuel : " + playerMoney + "€");

                // on lui demande alors s'il veut enlever certains produits du panier ou tout vider et recommencer
                // on va parcourir le panier et lui demander pour chaque produit combien il veut en acheter et on affiche le prix total et l'argent du joueur

                for (CartItem item : shop.getShoppingCard()) {
                    System.out.println("Voulez-vous enlever " + item.getProduct().getName() + " du panier ? (oui,non)");
                    String reponseEnlever = SCANNER.nextLine().trim().toLowerCase();
                    while (!reponseEnlever.equals("oui") && !reponseEnlever.equals("non")) {
                        System.out.println("Veuillez répondre par 'oui' ou 'non'. Voulez-vous enlever " + item.getProduct().getName() + " du panier ? (oui,non)");
                        reponseEnlever = SCANNER.nextLine().trim().toLowerCase();
                    }
                    // dans le cas non, on garde le produit et on passe au suivant
                    if (reponseEnlever.equals("oui")) {
                        System.out.println("Combien voulez-vous en enlever ? (quantité actuelle : " + item.getQuantity() + ")");
                        int quantiteEnlever = lireQuantite();
                        shop.removeFromShoppingCard(item.getProduct(),quantiteEnlever );
                        System.out.println(quantiteEnlever + item.getProduct().getName() + " a été enlevé du panier.");
                    }
                }

                demandeValidationPanier();
            }


        }
    }
}
