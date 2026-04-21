package model.management;

import model.culture.Type;
import model.shop.FacilityType;
import model.shop.Seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Inventaire {
    /** Une classe qui modélise l'inventaire du joueur et la gestion de ses ressources **/

    /*
     * Cette définition représente le vrai "catalogue" de l'inventaire affiché au joueur.
     * Les drops de la grotte s'appuient sur cette même source pour éviter toute divergence.
     * Ainsi, si l'on ajoute ou retire un type ici plus tard,
     * l'UI de l'inventaire et les drops évolueront ensemble.
     */
    private static final Type[] MAIN_ZONE_SEED_SLOT_ORDER = {
            Type.TULIPE,
            Type.ROSE,
            Type.MARGUERITE,
            Type.ORCHIDEE,
            Type.CAROTTE,
            Type.RADIS,
            Type.CHOUFLEUR
    };
    private static final Type[] LEFT_ZONE_SEED_SLOT_ORDER = {
            Type.NENUPHAR,
            Type.IRIS_DES_MARAIS
    };
    private static final FacilityType[] FACILITY_SLOT_ORDER = {
            FacilityType.CLOTURE,
            FacilityType.CHEMIN,
            FacilityType.COMPOST,
            FacilityType.PONT
    };

    // Attributs
    // liste des nombres de graines que le joueur possède
    // pour l'instant on suppose qu'il ne possède que les graines de tulipe
    
    // On garde deux rangements séparés, mais avec le type unique du projet.
    private final HashMap<Type, Integer> graines;
    private final HashMap<FacilityType, Integer> installations;
    private int woodQuantity;
    
    // constructeur
    public Inventaire() {
        graines = new HashMap<>();
        installations = new HashMap<>();
        woodQuantity = 0;
    }
    // Méthodes
    
        // getter et setter
        /** getFleurs : méthode pour récupérer la liste des graines de fleurs que le joueur possède
        * @return HashMap<Type, Integer> : la liste des graines de fleurs 
        **/
        public HashMap<Type, Integer> getGraines() {
            return graines;
        }   

        
        /** getInstallations : méthode pour récupérer la liste des installations que le joueur possède
        * @return HashMap<InstallationType, Integer> : la liste des installations que le joueur possède
        **/
        public HashMap<FacilityType, Integer> getInstallations() {
            return installations;
        }
        
        
        // SETTER

        /** ajoutGraine : méthode pour ajouter les graine dans leur inventaire resopectif
         * @param quantite : la quantité de graines que le joueur à acheté 
         * **/
        public void ajoutGraine(Seed graine, int quantite) {
            if (graine == null) {
                return;
            }

            ajoutGraine(graine.getType(), quantite);
        }

        /**
         * Variante directe utilisée par les récompenses ou les drops :
         * on peut créditer l'inventaire sans avoir à recréer artificiellement
         * un objet boutique complet.
         */
        public void ajoutGraine(Type type, int quantite) {
            if (type == null || quantite <= 0) {
                return;
            }

            int quantiteActuelle = getQuantiteGraine(type);
            graines.put(type, quantiteActuelle + quantite);
        }

        /**
         * Ajoute une installation dans l'inventaire à partir de son type.
         * Cela couvre donc à la fois :
         * - un achat depuis la boutique,
         * - un objet remis dans l'inventaire après avoir été posé dans le monde.
         */
        public void ajoutInstallation(FacilityType type, int quantite) {
            if (type == null || quantite <= 0) {
                return;
            }

            int quantiteActuelle = getQuantiteInstallation(type);
            installations.put(type, quantiteActuelle + quantite);
        }

        // utilsation des installations et des graines 

        /**
         * UseGraine : méthode pour utiliser une graine que ce soit Légume ou fleure
         * @param nomGraine : le nom de la graine à utiliser
         **/
        public void UseGraineFleure(Type nomGraine) {
            // on verifie que le joueur possède la graine
            if (graines.containsKey(nomGraine)) {
                // on recupere le nombre de graines possédées
                int quantite = graines.get(nomGraine);

                // On verifie la quantité pour ne pas passer en négatif
                if (quantite > 0) {
                    graines.put(nomGraine, quantite - 1);
                }
            }
        
        }

        /**
        * UseInstallation : méthode pour utiliser une installation
        * @param nomInstallation : le nom de l'installation à utiliser
        **/
        public void UseInstallation(FacilityType nomInstallation) {
            // on verifie que le joueur possède l'installation
            if (installations.containsKey(nomInstallation)) {
                // on recupere le nombre d'installations possédées
                int quantite = installations.get(nomInstallation);

                // On verifie la quantité pour ne pas passer en négatif
                if (quantite > 0) {
                    installations.put(nomInstallation, quantite - 1);
                }
            }
        }

    /** estVide : méthode pour vérifier si l'inventaire du joueur est vide ou pas
         * @return boolean : true si l'inventaire est vide, false sinon
     * **/
    public boolean estVide() {
        return graines.isEmpty() && installations.isEmpty() && woodQuantity <= 0;
    }

    /** Méthode qui vérifie si le joueur possède une graine donnée
     * @param type : le type de la graine à vérifier
     * @return boolean : true si le joueur possède la graine, false sinon
     */
    public boolean possedeGraine(Type type) {
        return graines.containsKey(type) && graines.get(type) > 0;
    }

    /**
     * Même principe que pour les graines :
     * on veut pouvoir interroger l'inventaire sans répéter la même condition partout.
     */
    public boolean possedeInstallation(FacilityType type) {
        return !installations.containsKey(type) || installations.get(type) <= 0;
    }

    /**
     * Petit helper de lecture :
     * on renvoie simplement 0 si la graine n'a jamais ete achetée,
     * ce qui évite de faire trainer des tests null partout dans le code.
     */
    public int getQuantiteGraine(Type type) {
        Integer quantite = graines.get(type);
        return quantite == null ? 0 : quantite;
    }

    /**
     * Meme idée pour les installations.
     */
    public int getQuantiteInstallation(FacilityType type) {
        Integer quantite = installations.get(type);
        return quantite == null ? 0 : quantite;
    }

    /**
     * Le bois alimente les constructions de la menuiserie.
     * On le garde hors des installations pour bien marquer qu'il s'agit
     * d'une ressource brute et non d'un objet à poser dans le monde.
     */
    public int getQuantiteBois() {
        return Math.max(0, woodQuantity);
    }

    /**
     * Retourne un instantané des graines réellement disponibles.
     *
     * On itère directement sur le contenu courant de l'inventaire
     * au lieu d'utiliser une liste codée en dur.
     * Si de nouveaux types sont ajoutés plus tard dans l'inventaire,
     * ils entreront automatiquement dans ce résultat.
     */
    public List<Type> getAvailableSeedTypes() {
        List<Type> availableSeedTypes = new ArrayList<>();
        for (Map.Entry<Type, Integer> entry : new HashMap<>(graines).entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                availableSeedTypes.add(entry.getKey());
            }
        }
        return availableSeedTypes;
    }

    /**
     * Même logique pour les installations :
     * seul le stock réellement positif est proposé au système de drops.
     */
    public List<FacilityType> getAvailableFacilityTypes() {
        List<FacilityType> availableFacilityTypes = new ArrayList<>();
        for (Map.Entry<FacilityType, Integer> entry : new HashMap<>(installations).entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null && entry.getValue() > 0) {
                availableFacilityTypes.add(entry.getKey());
            }
        }
        return availableFacilityTypes;
    }

    public boolean possedeBois(int quantiteDemandee) {
        return quantiteDemandee > 0 && getQuantiteBois() >= quantiteDemandee;
    }

    /**
     * Chaque getter renvoie une copie afin de protéger la définition officielle des slots.
     */
    public static Type[] getMainZoneSeedSlotOrder() {
        return MAIN_ZONE_SEED_SLOT_ORDER.clone();
    }

    public static Type[] getLeftZoneSeedSlotOrder() {
        return LEFT_ZONE_SEED_SLOT_ORDER.clone();
    }

    public static FacilityType[] getFacilitySlotOrder() {
        return FACILITY_SLOT_ORDER.clone();
    }

    /**
     * Retourne les graines autorisées pour les drops de grotte.
     *
     * On ne filtre volontairement pas par quantité possédée :
     * une nouvelle partie démarre avec un stock à zéro,
     * mais les types existent déjà dans l'inventaire visible.
     * Sans cela, aucun drop n'apparaîtrait jamais au début de la partie.
     */
    public List<Type> getDropCandidateSeedTypes() {
        List<Type> dropCandidateSeedTypes = new ArrayList<>();
        for (Type type : MAIN_ZONE_SEED_SLOT_ORDER) {
            if (type != null) {
                dropCandidateSeedTypes.add(type);
            }
        }
        for (Type type : LEFT_ZONE_SEED_SLOT_ORDER) {
            if (type != null) {
                dropCandidateSeedTypes.add(type);
            }
        }
        return dropCandidateSeedTypes;
    }

    /**
     * Même logique pour les installations montrées dans l'inventaire.
     */
    public List<FacilityType> getDropCandidateFacilityTypes() {
        List<FacilityType> dropCandidateFacilityTypes = new ArrayList<>();
        for (FacilityType type : FACILITY_SLOT_ORDER) {
            if (type != null) {
                dropCandidateFacilityTypes.add(type);
            }
        }
        return dropCandidateFacilityTypes;
    }

    /**
     * Le bois fait toujours partie des ressources de base affichées dans l'inventaire.
     */
    public boolean supportsWoodDrops() {
        return true;
    }

    public void ajoutBois(int quantite) {
        if (quantite <= 0) {
            return;
        }

        woodQuantity += quantite;
    }

    public void retirerBois(int quantite) {
        if (quantite <= 0) {
            return;
        }
        if (quantite > getQuantiteBois()) {
            throw new IllegalStateException("Quantité de bois insuffisante dans l'inventaire.");
        }

        woodQuantity -= quantite;
    }
}
