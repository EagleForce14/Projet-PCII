package model.culture;

import model.shop.ShopKind;

/**
 * Enumération unique des cultures du jeu.
 * Chaque valeur représente une culture concrète et embarque sa catégorie
 * afin d'indiquer simplement si c'est une fleur ou un légume.
 */
public enum Type {
    TULIPE("Tulipe", Categorie.FLEUR, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    ROSE("Rose", Categorie.FLEUR, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    MARGUERITE("Marguerite", Categorie.FLEUR, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    ORCHIDEE("Orchidee", Categorie.FLEUR, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    CAROTTE("Carotte", Categorie.LEGUME, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    RADIS("Radis", Categorie.LEGUME, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    CHOUFLEUR("Choufleur", Categorie.LEGUME, FieldZone.RIGHT_OF_RIVER, ShopKind.MAIN),
    NENUPHAR("Nénuphar", Categorie.FLEUR, FieldZone.LEFT_OF_RIVER, ShopKind.STALL),
    IRIS_DES_MARAIS("Iris des marais", Categorie.FLEUR, FieldZone.LEFT_OF_RIVER, ShopKind.STALL);

    /** Petite catégorie embarquée pour éviter les anciens sous-types. */
    public enum Categorie {
        FLEUR,
        LEGUME
    }

    private final String displayName;
    private final Categorie categorie;
    private final FieldZone allowedFieldZone;
    private final ShopKind shopKind;

    Type(String displayName, Categorie categorie, FieldZone allowedFieldZone, ShopKind shopKind) {
        this.displayName = displayName;
        this.categorie = categorie;
        this.allowedFieldZone = allowedFieldZone;
        this.shopKind = shopKind;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public FieldZone getAllowedFieldZone() {
        return allowedFieldZone;
    }

    public ShopKind getShopKind() {
        return shopKind;
    }

    public boolean isSoldInMainShop() {
        return shopKind == ShopKind.MAIN;
    }

    public boolean isSoldInStallShop() {
        return shopKind == ShopKind.STALL;
    }

    public boolean isFleur() {
        return categorie == Categorie.FLEUR;
    }

    public boolean isLegume() {
        return categorie == Categorie.LEGUME;
    }

    public boolean canBePlantedIn(FieldZone fieldZone) {
        return fieldZone != null && fieldZone == allowedFieldZone;
    }
}
