package controller;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import model.*;
import view.*;

/**
 * Le contrôleur chargé de gérer les intéractions.
 */
public class MovementController implements KeyListener {
    private final MovementModel model;
    private final GrilleCulture grilleCulture;
    private final Money playerMoney;
    private final Shop shop;
    private final Inventaire inventaire;

    // Constructeur de la classe

    public MovementController(MovementModel model, MovementView view, SidebarPanel sidebarPanel,
                              GrilleCulture grilleCulture, Money playerMoney, Shop shop, Inventaire inventaire) {
        this.model = model;
        this.grilleCulture = grilleCulture;
        this.playerMoney = playerMoney;
        this.shop = shop;
        this.inventaire = inventaire;
        // On s'abonne aux événements clavier.
        view.addKeyListener(this);

        JButton plantButton = sidebarPanel.getPlantButton();
        plantButton.addActionListener(this::planterSurCaseActive);

        JButton harvestButton = sidebarPanel.getHarvestButton();
        harvestButton.addActionListener(this::recolterCaseActive);

        JButton cleanButton = sidebarPanel.getCleanButton();
        cleanButton.addActionListener(this::nettoyerCaseActive);

        JButton shopButton = sidebarPanel.getShopButton();
        shopButton.addActionListener(this::ouvrirBoutique);



    }
    
    
    

    /**
     * On plante uniquement sur la case actuellement surlignée.
     * Pour le moment, le bouton utilise un type par défaut unique.
     */
    private void planterSurCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }

        // Tant qu'il n'existe pas encore de sélecteur de graines, on plante toujours le même type.
        if (grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y) == null) {
            grilleCulture.planterCulture(activeFieldCell.x, activeFieldCell.y, Type.FLEURS);
        }
    }

    /**
     * La récolte ne s'exécute que sur une plante mature, puis on crédite le joueur.
     */
    private void recolterCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        // Si le jardinier n'est sur aucune case du champ, il n'y a rien à récolter.
        if (activeFieldCell == null) {
            return;
        }

        Culture culture = grilleCulture.getCulture(activeFieldCell.x, activeFieldCell.y);
        // On garde la vérification ici même si le bouton est déjà filtré par la vue.
        // Comme ça, la règle reste vraie même si cette méthode est réutilisée ailleurs plus tard.
        if (culture == null || culture.getStadeCroissance() != Stade.MATURE) {
            return;
        }

        // On calcule le gain associé.
        int gain = grilleCulture.recolterCulture(activeFieldCell.x, activeFieldCell.y);

        // Le portefeuille du joueur est mis à jour à part pour garder une logique bien découpée.
        playerMoney.credit(gain);
    }

    /**
     * Le bouton nettoyer retire uniquement une culture flétrie sur la case du jardinier.
     */
    private void nettoyerCaseActive(ActionEvent event) {
        Point activeFieldCell = model.getActiveFieldCell();
        if (activeFieldCell == null) {
            return;
        }
        grilleCulture.nettoyerCultureFletrie(activeFieldCell.x, activeFieldCell.y);
    }

    /** Ouverture de la boutique dans la console POUR l'INSTANT
     * Affichage de tout ce que la boutique contient (graines et installations)
     * Affichage des prix de chaque objet, de la quantité dispo et de l'argent du joueur
     * et affichage du panier
     **/


    private void ouvrirBoutique(ActionEvent event) {
        System.out.println("Ouverture de la boutique");
        System.out.println("Argent du joueur : " + playerMoney.getAmount() + "€");
        System.out.println("Graines disponibles : ");
        for (Seed seed : (shop.getSeeds())) {
            System.out.println("- " + seed.getName() + " (Type: " + seed.getType() + ", Prix: " + seed.getPrice() + ", Quantité disponible: " + seed.getQuantity() + ")");
        }
        System.out.println("Installations disponibles : ");
        for (Facility facility : shop.getFacilities()) {
            System.out.println("- " + facility.getName() + " (Type: " + facility.getType() + ", Prix: " + facility.getPrice() + ", Quantité disponible: " + facility.getQuantity() + ")");
        }
        System.out.println("Panier : ");
        for (CartItem item : shop.getShoppingCard()) {
            if (item.getProduct() instanceof Seed) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Seed)item.getProduct()).getType()+ ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            } else if (item.getProduct() instanceof Facility) {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Facility)item.getProduct()).getType()+ ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            } else {
                System.out.println("- " + item.getProduct().getName() + " (Type: " + ((Seed)item.getProduct()).getType()+ ", Prix: " + item.getProduct().getPrice() + ", Quantité: " + item.getQuantity() + ")");
            }
        }

        // entrée des valeur utilisateur pour acheter des produits
        Scanner Scaner = new Scanner(System.in);
        System.out.println("Entrez le nom du produit:");
        String nomProduit = Scaner.nextLine();
        System.out.println("Entrez la quantité:");
        int quantite = Scaner.nextInt();
        Scaner.close();
        // achat du produit
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
            // cas où le produit existe dans le magasin, on essaye de l'ajouter au panier
            boolean achatReussi = shop.addToShoppingCard(productToBuy, quantite);
            while(!achatReussi) {
                System.out.println("La qunnatité n'est pas valide, veuillez réessayer : ");
                System.out.println("Entrez le nom du produit:");
                nomProduit = Scaner.nextLine();
                System.out.println("Entrez la quantité:");
                quantite = Scaner.nextInt();

                // on revérifie que l'acchat est valide de nouveau
                achatReussi = shop.addToShoppingCard(productToBuy, quantite);
                
            }
        }else {
            System.out.println("Le produit demandé n'existe pas dans le magasin.");
        }

        Scaner.close();

        


    }

    // On implémente KeyListener (Gestion des Touches)
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // On récupère l'unité du joueur "actif" à contrôler
        Unit player = model.getPlayerUnit();
        if (player == null) return; // Sécurité si pas de joueur
        
        // Mise à jour des flags selon la touche clavier enfoncée
        if (key == KeyEvent.VK_UP) {
            player.setMoveUp(true);
            player.setMoveDown(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_DOWN) {
            player.setMoveDown(true);
            player.setMoveUp(false);
            player.setMoveLeft(false);
            player.setMoveRight(false);
        }
        else if (key == KeyEvent.VK_LEFT) {
            player.setMoveLeft(true);
            player.setMoveRight(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
        else if (key == KeyEvent.VK_RIGHT) {
            player.setMoveRight(true);
            player.setMoveLeft(false);
            player.setMoveUp(false);
            player.setMoveDown(false);
        }
    }

    // Désactivation de tous les flags de déplacement de l'unité lors du relâchement d’une touche.
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        Unit player = model.getPlayerUnit();
        if (player == null) return;
        
        // Désactivation des flags sur l'unité
        if (key == KeyEvent.VK_UP) player.setMoveUp(false);
        if (key == KeyEvent.VK_DOWN) player.setMoveDown(false);
        if (key == KeyEvent.VK_LEFT) player.setMoveLeft(false);
        if (key == KeyEvent.VK_RIGHT) player.setMoveRight(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}
