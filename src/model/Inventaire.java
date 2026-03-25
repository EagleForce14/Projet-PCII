package model;

import java.util.HashMap;
public class Inventaire {
    /** Une classe qui modélise l'inventaire du joueur et la gestion de ses ressources **/

    // Attributs
    // liste des nombres de graines que le joueur possède
    // pour l'instant on suppose qu'il ne possède que les graines de tulipe
    
    // On garde deux rangements séparés, mais avec le type unique du projet.
    private final HashMap<Type, Integer> graines;
    private final HashMap<FacilityType, Integer> installations;
    
    // constructeur
    public Inventaire() {
        graines = new HashMap<>();
        installations = new HashMap<>();
    
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
            //traiter les graines par case selon leur type


                
            // si le joueur possède déjà la graine, on rajoute les graines achetées à la quantité de base
            if (graines.containsKey(graine.getType())) {
                graines.put(graine.getType(), graines.get(graine.getType()) + quantite);
            } else {
                // sinon, on ajoute la nouvelle graine avec sa quantité
                graines.put(graine.getType(), quantite);
            }


        }

        

        /** ajoutInstallation : méthode pour ajouter de nouvelle installations dans l'inventaire
         * @param installation : l'installation que le joueur à acheté
         * @param quantite : la quantité d'installations que le joueurs à acheté
         * **/
        public void ajoutInstallation(Facility installation, int quantite) {
            // si le joueur possède déjà l'installation, on rajoute les installations achetées à la quantité de base

            // ici on a pas besoin de faire du trie 
            FacilityType type = installation.getType();
            if (installations.containsKey(type)) {
                installations.put(  type, installations.get(type) + quantite);
            } else {
                // sinon, on ajoute la nouvelle installation avec sa quantité
                installations.put(type, quantite);
            }
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
        return graines.isEmpty() && installations.isEmpty();
    }

    /** Méthode qui vérifie si le joueur possède une graine donnée
     * @param type : le type de la graine à vérifier
     * @return boolean : true si le joueur possède la graine, false sinon
     */
    public boolean possedeGraine(Type type) {
        return graines.containsKey(type) && graines.get(type) > 0;
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
}
