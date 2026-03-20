package model;

import java.util.HashMap;
public class Inventaire {
    /** Une classe qui modélise l'inventaire du joueur et la gestion de ses ressources **/

    // Attributs
    // liste des nombres de graines que le joueur possède
    // pour l'instant on suppose qu'il ne possède que les graines de tulipe
    
    // tout les graine de type FLEURS seront stocké dans cet attribut, on peut faire de même pour les autres types de graines   
    private HashMap<FleurType, Integer> fleurs;
    private HashMap<LegumeType, Integer> legumes;
    private HashMap<FacilityType, Integer> installations;
    
    // constructeur
    public Inventaire() {
        fleurs = new HashMap<>();
        legumes = new HashMap<>();
        installations = new HashMap<>();
    
    }
    // Méthodes
    
        // getter et setter
        /** getFleurs : méthode pour récupérer la liste des graines de fleurs que le joueur possède
        * @return HashMap<FleurType, Integer> : la liste des graines de fleurs 
        **/
        public HashMap<FleurType, Integer> getFleurs() {
            return fleurs;
        }   

        /** getLegumes : méthode pour récupérer la liste des graines de légumes que le joueur possède
        * @return HashMap<LegumeType, Integer> : la liste des graines de légumes que le joueur possède
        **/
        public HashMap<LegumeType, Integer> getLegumes() {
            return legumes;
        }
        
        /** getInstallations : méthode pour récupérer la liste des installations que le joueur possède
        * @return HashMap<InstallationType, Integer> : la liste des installations que le joueur possède
        **/
        public HashMap<FacilityType, Integer> getInstallations() {
            return installations;
        }
        
        
        // SETTER

        /** ajoutGraine : méthode pour ajouter les graine dans leur inventaire resopectif
         * @param type : le type de la graine (fleur ou légume)
         * @param quantite : la quantité de graines que le joueur à acheté 
         * **/
        public void ajoutGraine(Seed graine, int quantite) {
            //traiter les graines par case selon leur type

            // dans le cas  où la graine est une fleure
            if (graine.getType() == Type.FLEURS) {
                
                // si le joueur possède déjà la graine, on rajoute les graines achetées à la quantité de base
                if (fleurs.containsKey(graine.getFleurType())) {
                    fleurs.put(graine.getFleurType(), fleurs.get(graine.getFleurType()) + quantite);
                } else {
                    // sinon, on ajoute la nouvelle graine avec sa quantité
                    fleurs.put(graine.getFleurType(), quantite);
                }
                fleurs.put(graine.getFleurType(), quantite);
            } else if (graine.getType() == Type.LEGUMES) {

                    // de même pour les graines de légumes 
                if (legumes.containsKey(graine.getLegumeType())) {
                    
                    legumes.put(graine.getLegumeType(), legumes.get(graine.getLegumeType()) + quantite);
                } else {
                    
                    legumes.put(graine.getLegumeType(), quantite);
                }
                
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
         * UseGraine : méthode pour utiliser une graine de fleur
         * @param nomGraine : le nom de la graine à utiliser
         **/
        public void UseGraineFleure(FleurType nomGraine) {

            // véfifie si la graine est une fleure ou un légume
        
                // on verifie que le joueur possède la graine
                if (fleurs.containsKey(nomGraine)) {
                    // on recupere le nombre de graines possédées
                    int quantite = fleurs.get(nomGraine);

                    // On verifie la quantité pour ne pas passer en négatif

                    if (quantite > 0) {
                        fleurs.put(nomGraine, quantite - 1);
                    }
                }
        
        }

        public void UseGraineLegume(LegumeType nomGraine) {
            // on verifie que le joueur possède la graine
            if (legumes.containsKey(nomGraine)) {

                // on recupere le nombre de graines possédées
                int quantite = legumes.get(nomGraine);

                // On verifie la quantité pour ne pas passer en négatif

                if (quantite > 0) {
                    legumes.put(nomGraine, quantite - 1);
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

    
        

    

}