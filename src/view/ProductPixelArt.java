package view;

import model.culture.Type;
import model.shop.Facility;
import model.shop.FacilityType;
import model.shop.Product;
import model.shop.Seed;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 * Petit renderer réutilisable pour les items pixel-art du projet.
 * On centralise les motifs ici pour que l'inventaire et la boutique
 * partagent exactement les memes illustrations.
 */
public final class ProductPixelArt {
    // Largeur par défaut des petits dessins quand aucun sprite dédié n'existe.
    private static final int DEFAULT_ART_COLUMNS = 5;
    // Hauteur par défaut des petits dessins quand aucun sprite dédié n'existe.
    private static final int DEFAULT_ART_ROWS = 5;
    // Taille maximale utilisée quand on redimensionne un sprite de graine illustrée.
    private static final int ILLUSTRATED_SEED_IMAGE_MAX_SIDE = 6;
    // Largeur du motif de clôture dessiné directement en pixel-art.
    private static final int FENCE_ART_COLUMNS = 9;
    // Hauteur du motif de clôture dessiné directement en pixel-art.
    private static final int FENCE_ART_ROWS = 7;
    // Taille maximale du petit visuel de chemin.
    private static final int PATH_ART_MAX_SIDE = 7;
    // Taille maximale du petit visuel de compost.
    private static final int COMPOST_ART_MAX_SIDE = 6;
    // Taille maximale du petit visuel de pont.
    private static final int BRIDGE_ART_MAX_SIDE = 8;
    // Taille maximale du petit visuel de bois.
    private static final int WOOD_ART_MAX_SIDE = 8;
    // Taille maximale du petit visuel de pièce.
    private static final int COIN_ART_MAX_SIDE = 7;
    // Sprite utilisé pour représenter le chemin dans les aperçus produit.
    private static final Image PATH_IMAGE = ImageLoader.load("/assets/stone_with_grass.png");
    // Sprite utilisé pour représenter le compost dans les aperçus produit.
    private static final Image COMPOST_IMAGE = ImageLoader.load("/assets/Compost.png");
    // Sprite utilisé pour représenter le pont dans les aperçus produit.
    private static final Image BRIDGE_IMAGE = ImageLoader.load("/assets/bridge.png");
    // Sprite utilisé pour représenter le bois dans les aperçus produit.
    private static final Image WOOD_IMAGE = ImageLoader.load("/assets/wood.png");
    // Sprite utilisé pour représenter les pièces dans les aperçus produit.
    private static final Image COIN_IMAGE = ImageLoader.load("/assets/coin.png");
    // Sprite mature de la rose.
    private static final Image ROSE_IMAGE = ImageLoader.load("/assets/rose_mature.png");
    // Sprite mature de la marguerite.
    private static final Image DAISY_IMAGE = ImageLoader.load("/assets/marguerithe_mature.png");
    // Sprite mature de la tulipe.
    private static final Image TULIP_IMAGE = ImageLoader.load("/assets/tulipe_mature.png");
    // Sprite mature de la carotte.
    private static final Image CARROT_IMAGE = ImageLoader.load("/assets/carotte_mature.png");
    // Sprite mature du radis.
    private static final Image RADISH_IMAGE = ImageLoader.load("/assets/radis_mature.png");
    // Sprite mature du chou-fleur.
    private static final Image CAULIFLOWER_IMAGE = ImageLoader.load("/assets/choufleur_mature.png");
    // Sprite mature du nénuphar.
    private static final Image WATER_LILY_IMAGE = ImageLoader.load("/assets/nenuphar_mature.png");
    // Sprite mature de l'iris des marais.
    private static final Image MARSH_IRIS_IMAGE = ImageLoader.load("/assets/iris_marais_mature.png");

    /**
     * On interdit l'instanciation car toute cette classe sert juste de boîte à outils statique.
     */
    private ProductPixelArt() {}

    /**
     * On calcule la largeur du visuel à afficher pour n'importe quel produit de boutique.
     */
    public static int getProductArtWidth(Product product, int pixelSize) {
        if (product instanceof Seed) {
            return getSeedArtWidth(((Seed) product).getType(), pixelSize);
        }
        if (product instanceof Facility) {
            return getFacilityArtWidth(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    /**
     * On calcule la hauteur du visuel à afficher pour n'importe quel produit de boutique.
     */
    public static int getProductArtHeight(Product product, int pixelSize) {
        if (product instanceof Seed) {
            return getSeedArtHeight(((Seed) product).getType(), pixelSize);
        }
        if (product instanceof Facility) {
            return getFacilityArtHeight(((Facility) product).getType(), pixelSize);
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    /**
     * On renvoie la largeur du dessin d'une graine selon son type réel.
     */
    public static int getSeedArtWidth(Type type, int pixelSize) {
        Image illustratedSeedImage = getIllustratedSeedImage(type);
        if (illustratedSeedImage != null) {
            return getScaledImageSize(illustratedSeedImage, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize).width;
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    /**
     * On renvoie la hauteur du dessin d'une graine selon son type réel.
     */
    public static int getSeedArtHeight(Type type, int pixelSize) {
        Image illustratedSeedImage = getIllustratedSeedImage(type);
        if (illustratedSeedImage != null) {
            return getScaledImageSize(illustratedSeedImage, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize).height;
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    /**
     * Les drops de grotte réutilisent toujours le rendu mature de la culture.
     * On évite ainsi de faire apparaître au sol une jeune pousse ou une graine
     * alors que le joueur récupère un vrai loot lisible.
     */
    public static int getMatureCultureArtWidth(Type type, int pixelSize) {
        Image matureCultureImage = getMatureCultureImage(type);
        if (matureCultureImage == null) {
            return DEFAULT_ART_COLUMNS * pixelSize;
        }
        return getScaledImageSize(matureCultureImage, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize).width;
    }

    /**
     * On renvoie la hauteur du visuel mature utilisé pour un drop de culture.
     */
    public static int getMatureCultureArtHeight(Type type, int pixelSize) {
        Image matureCultureImage = getMatureCultureImage(type);
        if (matureCultureImage == null) {
            return DEFAULT_ART_ROWS * pixelSize;
        }
        return getScaledImageSize(matureCultureImage, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize).height;
    }

    /**
     * On renvoie la largeur du dessin d'un équipement de boutique.
     */
    public static int getFacilityArtWidth(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_COLUMNS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return getScaledImageSize(PATH_IMAGE, PATH_ART_MAX_SIDE * pixelSize).width;
        }
        if (type == FacilityType.COMPOST) {
            return getScaledImageSize(COMPOST_IMAGE, COMPOST_ART_MAX_SIDE * pixelSize).width;
        }
        if (type == FacilityType.PONT) {
            return getScaledImageSize(BRIDGE_IMAGE, BRIDGE_ART_MAX_SIDE * pixelSize).width;
        }
        return DEFAULT_ART_COLUMNS * pixelSize;
    }

    /**
     * On renvoie la hauteur du dessin d'un équipement de boutique.
     */
    public static int getFacilityArtHeight(FacilityType type, int pixelSize) {
        if (type == FacilityType.CLOTURE) {
            return FENCE_ART_ROWS * pixelSize;
        }
        if (type == FacilityType.CHEMIN) {
            return getScaledImageSize(PATH_IMAGE, PATH_ART_MAX_SIDE * pixelSize).height;
        }
        if (type == FacilityType.COMPOST) {
            return getScaledImageSize(COMPOST_IMAGE, COMPOST_ART_MAX_SIDE * pixelSize).height;
        }
        if (type == FacilityType.PONT) {
            return getScaledImageSize(BRIDGE_IMAGE, BRIDGE_ART_MAX_SIDE * pixelSize).height;
        }
        return DEFAULT_ART_ROWS * pixelSize;
    }

    /**
     * On renvoie la largeur du petit visuel de bois.
     */
    public static int getWoodArtWidth(int pixelSize) {
        return getScaledImageSize(WOOD_IMAGE, WOOD_ART_MAX_SIDE * pixelSize).width;
    }

    /**
     * On renvoie la hauteur du petit visuel de bois.
     */
    public static int getWoodArtHeight(int pixelSize) {
        return getScaledImageSize(WOOD_IMAGE, WOOD_ART_MAX_SIDE * pixelSize).height;
    }

    /**
     * On renvoie la largeur du petit visuel de pièce.
     */
    public static int getCoinArtWidth(int pixelSize) {
        return getScaledImageSize(COIN_IMAGE, COIN_ART_MAX_SIDE * pixelSize).width;
    }

    /**
     * On renvoie la hauteur du petit visuel de pièce.
     */
    public static int getCoinArtHeight(int pixelSize) {
        return getScaledImageSize(COIN_IMAGE, COIN_ART_MAX_SIDE * pixelSize).height;
    }

    /**
     * On dessine automatiquement le bon visuel selon le vrai type de produit reçu.
     */
    public static void drawProduct(Graphics2D g2d, Product product, int x, int y, int pixelSize) {
        if (product instanceof Seed) {
            drawSeed(g2d, ((Seed) product).getType(), x, y, pixelSize);
        } else if (product instanceof Facility) {
            drawFacility(g2d, ((Facility) product).getType(), x, y, pixelSize);
        }
    }

    /**
     * On dessine une graine, soit via son sprite dédié, soit via un petit motif manuel.
     */
    public static void drawSeed(Graphics2D g2d, Type type, int x, int y, int pixelSize) {
        Image illustratedSeedImage = getIllustratedSeedImage(type);
        if (illustratedSeedImage != null) {
            drawScaledImage(g2d, illustratedSeedImage, x, y, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize);
        }
    }

    /**
     * On dessine la version mature de la culture utilisée pour les drops au sol.
     */
    public static void drawMatureCulture(Graphics2D g2d, Type type, int x, int y, int pixelSize) {
        Image matureCultureImage = getMatureCultureImage(type);
        if (matureCultureImage != null) {
            drawScaledImage(g2d, matureCultureImage, x, y, ILLUSTRATED_SEED_IMAGE_MAX_SIDE * pixelSize);
            return;
        }

        drawSeed(g2d, type, x, y, pixelSize);
    }

    /**
     * Certaines graines réutilisent directement leur sprite mature.
     * On centralise cette liste ici pour ne pas répéter les mêmes conditions
     * dans la largeur, la hauteur et le dessin.
     */
    private static Image getIllustratedSeedImage(Type type) {
        return getDedicatedMatureCultureImage(type);
    }

    /**
     * Cette table décrit le visuel "au sol" d'une culture mature.
     * Toutes les cultures actuellement jouables possèdent leur sprite mature dédié.
     */
    private static Image getMatureCultureImage(Type type) {
        return getDedicatedMatureCultureImage(type);
    }

    /**
     * Regroupe les cultures qui possèdent leur propre sprite mature dédié.
     * Cette correspondance est réutilisée à la fois par les graines illustrées
     * et par les drops de cultures matures pour garder un rendu cohérent.
     */
    private static Image getDedicatedMatureCultureImage(Type type) {
        if (type == Type.CAROTTE) {
            return CARROT_IMAGE;
        }
        if (type == Type.RADIS) {
            return RADISH_IMAGE;
        }
        if (type == Type.CHOUFLEUR) {
            return CAULIFLOWER_IMAGE;
        }
        if (type == Type.ROSE) {
            return ROSE_IMAGE;
        }
        if (type == Type.MARGUERITE) {
            return DAISY_IMAGE;
        }
        if (type == Type.TULIPE) {
            return TULIP_IMAGE;
        }
        if (type == Type.NENUPHAR) {
            return WATER_LILY_IMAGE;
        }
        if (type == Type.IRIS_DES_MARAIS) {
            return MARSH_IRIS_IMAGE;
        }
        return null;
    }

    /**
     * On dessine un équipement de boutique avec son visuel adapté au type demandé.
     */
    public static void drawFacility(Graphics2D g2d, FacilityType type, int x, int y, int pixelSize) {
        switch (type) {
            case CLOTURE:
                drawFence(g2d, x, y, pixelSize);
                break;
            case CHEMIN:
                drawScaledImage(g2d, PATH_IMAGE, x, y, PATH_ART_MAX_SIDE * pixelSize);
                break;
            case COMPOST:
                drawScaledImage(g2d, COMPOST_IMAGE, x, y, COMPOST_ART_MAX_SIDE * pixelSize);
                break;
            case PONT:
                drawScaledImage(g2d, BRIDGE_IMAGE, x, y, BRIDGE_ART_MAX_SIDE * pixelSize);
                break;
            default:
                break;
        }
    }

    /**
     * On dessine l'icône de ressource utilisée pour le bois.
     */
    public static void drawWoodResource(Graphics2D g2d, int x, int y, int pixelSize) {
        drawScaledImage(g2d, WOOD_IMAGE, x, y, WOOD_ART_MAX_SIDE * pixelSize);
    }

    /**
     * On dessine l'icône de ressource utilisée pour les pièces.
     */
    public static void drawCoinResource(Graphics2D g2d, int x, int y, int pixelSize) {
        drawScaledImage(g2d, COIN_IMAGE, x, y, COIN_ART_MAX_SIDE * pixelSize);
    }

    /**
     * On dessine le petit motif manuel utilisé pour représenter une clôture.
     */
    private static void drawFence(Graphics2D g2d, int x, int y, int pixelSize) {
        Color cap = new Color(72, 56, 46);
        Color woodLight = new Color(255, 173, 49);
        Color wood = new Color(255, 155, 36);
        Color woodDark = new Color(219, 128, 11);
        Color slat = new Color(239, 117, 34);
        Color slatDark = new Color(191, 86, 21);

        // Les deux poteaux sont volontairement dessinés avec exactement le même motif.
        fillGridRect(g2d, x, y, pixelSize, 1, 0, 2, 1, cap);
        fillGridRect(g2d, x, y, pixelSize, 6, 0, 2, 1, cap);

        fillGridRect(g2d, x, y, pixelSize, 1, 1, 2, 5, wood);
        fillGridRect(g2d, x, y, pixelSize, 6, 1, 2, 5, wood);
        fillGridRect(g2d, x, y, pixelSize, 1, 1, 1, 5, woodLight);
        fillGridRect(g2d, x, y, pixelSize, 6, 1, 1, 5, woodLight);
        fillGridRect(g2d, x, y, pixelSize, 2, 3, 1, 2, woodDark);
        fillGridRect(g2d, x, y, pixelSize, 7, 3, 1, 2, woodDark);

        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slatDark);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slatDark);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 3, 1, slat);
        fillGridRect(g2d, x, y, pixelSize, 3, 2, 1, 1, new Color(255, 145, 61));
        fillGridRect(g2d, x, y, pixelSize, 3, 4, 1, 1, new Color(255, 145, 61));
    }

    /**
     * On redimensionne et dessine une image dans la boîte prévue pour ce mini-visuel.
     */
    private static void drawScaledImage(Graphics2D g2d, Image image, int x, int y, int maxSide) {
        if (image == null) {
            return;
        }

        Dimension scaledSize = getScaledImageSize(image, maxSide);
        Graphics2D imageGraphics = (Graphics2D) g2d.create();
        imageGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        imageGraphics.drawImage(image, x, y, scaledSize.width, scaledSize.height, null);
        imageGraphics.dispose();
    }

    /**
     * Calcule une seule fois la taille affichée d'une image.
     * On évite ainsi de dupliquer la même lecture
     * de largeur/hauteur source dans plusieurs méthodes.
     */
    private static Dimension getScaledImageSize(Image image, int maxSide) {
        if (image == null) {
            return new Dimension(maxSide, maxSide);
        }

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        if (imageWidth <= 0 || imageHeight <= 0) {
            return new Dimension(maxSide, maxSide);
        }

        if (imageWidth >= imageHeight) {
            return new Dimension(maxSide, Math.max(1, (maxSide * imageHeight) / imageWidth));
        }
        return new Dimension(Math.max(1, (maxSide * imageWidth) / imageHeight), maxSide);
    }

    /**
     * On remplit un rectangle entier du motif exprimé en coordonnées de grille.
     */
    private static void fillGridRect(Graphics2D g2d, int originX, int originY, int pixelSize,
                                     int gridX, int gridY, int gridWidth, int gridHeight, Color color) {
        g2d.setColor(color);
        g2d.fillRect(
                originX + (gridX * pixelSize),
                originY + (gridY * pixelSize),
                gridWidth * pixelSize,
                gridHeight * pixelSize
        );
    }
}
