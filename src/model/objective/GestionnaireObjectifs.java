package model.objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import model.culture.Type;
import model.runtime.Jour;

/** Classe qui gère les objectifs */
public class GestionnaireObjectifs {

    /** Constante qui stocke la valeur de départ pour chaque type d'objectif */
    public static final Map<TypeObjectif, Integer> VALEUR_DEPART_OBJECTIFS = Map.ofEntries(
        Map.entry(TypeObjectif.PLANTER_CULTURES, 0),
        Map.entry(TypeObjectif.PLANTER_TYPES_CULTURE, 1),
        Map.entry(TypeObjectif.RECOLTER_CULTURES, 0),
        Map.entry(TypeObjectif.RECOLTER_TYPES_CULTURE, 1),
        Map.entry(TypeObjectif.LABOURER_CHAMPS, 0),
        Map.entry(TypeObjectif.ARROSER_CULTURES, 0),
        Map.entry(TypeObjectif.COUPER_ARBRES, 0),
        Map.entry(TypeObjectif.ACHETER_ITEMS_BOUTIQUE, 0),
        Map.entry(TypeObjectif.TAUX_RECOLTE_CULTURES, 50),
        Map.entry(TypeObjectif.CULTURES_MANGEES, 15),
        Map.entry(TypeObjectif.REPOUSSER_LAPINS, 1)
    );

    /** Attribut qui stocke les objectifs */
    Map<TypeObjectif, ObjectifJournalier> objectifs;

    /** Attribut qui stocke le jour */
    Jour jour;

    /** Attribut qui stocke le nombre d'objectifs à valider */
    int nombreObjectifsAValider;

    /** Attribut qui stocke le nombre d'objectifs atteints pour le jour courant */
    int nombreObjectifsAtteints;

    /** Constructeur de la classe GestionnaireObjectifs */
    public GestionnaireObjectifs(Jour jour) {
        this.jour = jour;
        this.objectifs = new HashMap<>();
        this.nombreObjectifsAtteints = 0;
        genererObjectifs(); // Génère les objectifs pour le jour actuel lors de l'initialisation du gestionnaire d'objectifs
    }

    /** Méthode qui génère les objectifs en fonction du jour */
    private void genererObjectifs() {
        // Vide les objectifs précédents pour générer de nouveaux objectifs pour le nouveau jour
        objectifs.clear();
        int nombreObjectifs = 3 + (int) (jour.getJour() * 0.4); // Nombre d'objectifs à générer en fonction du jour, augmente avec le temps pour rendre le jeu plus difficile
        if (nombreObjectifs > TypeObjectif.values().length) {
            nombreObjectifs = TypeObjectif.values().length; // Limite le nombre d'objectifs au nombre de types d'objectifs disponibles
        }
        nombreObjectifsAValider = getNombreMinimumObjectifsAValider(); // Met à jour le nombre d'objectifs à valider pour le jour actuel
        for (TypeObjectif type : selectionnerNTypeObjectifs(nombreObjectifs)) {
            int valeurCible;
            if (type == TypeObjectif.TAUX_RECOLTE_CULTURES) {
                // Pour l'objectif TAUX_RECOLTE_CULTURES, la valeur cible augmente avec le temps pour encourager le joueur à améliorer son taux de récolte
                valeurCible = jour.getJour() * 4 + VALEUR_DEPART_OBJECTIFS.get(type);
                if (valeurCible > 100) {
                    valeurCible = 100; // Assure que la valeur cible ne dépasse pas 100%
                }
                objectifs.put(type, new ObjectifTauxRecolte(type, valeurCible));
            } else if (type == TypeObjectif.CULTURES_MANGEES) {
                // Pour l'objectif CULTURES_MANGEES, la valeur cible diminue avec le temps pour encourager le joueur à mieux protéger ses cultures
                valeurCible = VALEUR_DEPART_OBJECTIFS.get(TypeObjectif.CULTURES_MANGEES) - (jour.getJour() * 2); 
                if (valeurCible < 0) {
                    valeurCible = 0; // Assure que la valeur cible ne soit pas négative
                }
                objectifs.put(type, new ObjectifCompteur(type, valeurCible));
            } else if (type == TypeObjectif.PLANTER_TYPES_CULTURE || type == TypeObjectif.RECOLTER_TYPES_CULTURE) {
                // Pour les objectifs PLANTER_TYPES_CULTURE et RECOLTER_TYPES_CULTURE, la valeur cible augmente avec le temps pour encourager le joueur à diversifier ses cultures
                valeurCible = (int) (jour.getJour()*0.7) + VALEUR_DEPART_OBJECTIFS.get(type);
                if (valeurCible > Type.values().length) {
                    valeurCible = Type.values().length; // Assure que la valeur cible ne dépasse pas le nombre de types de cultures disponibles
                }
                objectifs.put(type, new ObjectifCompteurTypes(type, valeurCible));
            } else {
                // Pour les autres types d'objectifs, la valeur cible augmente avec le temps pour encourager le joueur à progresser
                valeurCible = jour.getJour() * 2 + VALEUR_DEPART_OBJECTIFS.get(type);
                if (valeurCible > 20) {
                    valeurCible = 20; // Assure que la valeur cible ne dépasse pas 20 pour les objectifs de type compteur
                }
                objectifs.put(type, new ObjectifCompteur(type, valeurCible));
            }
        }
    }

    /** Méthode qui sélectionne n type d'objectifs différents au hasard*/
    public ArrayList<TypeObjectif> selectionnerNTypeObjectifs(int n) {
        ArrayList<TypeObjectif> typesSelectionnes = new ArrayList<>();
        TypeObjectif[] typesDisponibles = TypeObjectif.values(); // Récupère tous les types d'objectifs disponibles

        // Sélectionne des types d'objectifs aléatoires jusqu'à ce que n types soient sélectionnés ou que tous les types disponibles soient sélectionnés
        while (typesSelectionnes.size() < n && typesSelectionnes.size() < typesDisponibles.length) {
            int index = new Random().nextInt(typesDisponibles.length); // Génère un index aléatoire pour sélectionner un type d'objectif
            TypeObjectif typeSelectionne = typesDisponibles[index]; // Récupère le type d'objectif sélectionné
            if (!typesSelectionnes.contains(typeSelectionne)) {
                // Si le type d'objectif sélectionné n'est pas déjà dans la liste des types sélectionnés, l'ajoute à la liste
                typesSelectionnes.add(typeSelectionne);
            }
        }
        return typesSelectionnes;
    }

    /** Méthode qui renvoie si le jour est validé, c'est-à-dire si au moins un certain nombre d'objectifs sont atteints en fonction du jour*/
    public boolean estJourValide() {
        System.out.println("Nombre minimum d'objectifs à atteindre pour valider le jour : " + nombreObjectifsAValider);
        int minimumEffectif = Math.min(nombreObjectifsAValider, objectifs.size());
        int objectifsAtteints = 0;
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.estAtteint()) {
                objectifsAtteints++;
            }
        }
        nombreObjectifsAtteints = objectifsAtteints;
        return objectifsAtteints >= minimumEffectif; // Le jour est validé si le nombre d'objectifs atteints est supérieur ou égal au nombre minimum d'objectifs à atteindre
    }

    /** Méthode qui renvoie le nombre minimum d'objectifs à atteindre pour valider le jour */
    public int getNombreMinimumObjectifsAValider() {
        return 1 + (int) (jour.getJour() * 0.5);
    }

    /** Méthode qui renvoie le nombre minimum d'objectifs à valider pour le jour courant en tenant compte des objectifs disponibles */
    public int getNombreObjectifsAValiderEffectif() {
        return Math.min(nombreObjectifsAValider, objectifs.size());
    }

    /** Méthode qui renvoie le nombre d'objectifs atteints pour le jour courant */
    public int getNombreObjectifsAtteints() {
        return nombreObjectifsAtteints;
    }

    /** Méthode qui renvoie la progression de chaque objectif sous forme d'une map */
    public Map<TypeObjectif, String> getProgressionObjectifs() {
        Map<TypeObjectif, String> progression = new HashMap<>();
        for (ObjectifJournalier objectif : objectifs.values()) {
            progression.put(objectif.getType(), objectif.getType() + " : " + objectif.getProgressionString());
        }
        return progression;
    }

    /** Getter qui renvoie une copie des objectifs actifs */
    public Map<TypeObjectif, ObjectifJournalier> getObjectifs() {
        return new HashMap<>(objectifs);
    }

    /** Méthode qui met à jour les objectifs liés à la plantation */
    public void mettreAJourObjectifsPlanter(Type typeCulture) {
        for (ObjectifJournalier objectif : objectifs.values()) {
            switch (objectif.getType()) {
                case PLANTER_CULTURES:
                    ((ObjectifCompteur) objectif).mettreAJourProgression(1); // Incrémente la progression de l'objectif PLANTER_CULTURES de 1 à chaque plantation de culture
                    break;
                case PLANTER_TYPES_CULTURE:
                    ((ObjectifCompteurTypes) objectif).mettreAJourProgression(typeCulture);
                    break;
                case TAUX_RECOLTE_CULTURES:
                    ((ObjectifTauxRecolte) objectif).mettreAJourNombreCulturesPlantees();
                    break;
                default:
                    break;
            }
        }
        afficherProgressionObjectifs(); // Affiche la progression de chaque objectif dans la console après la mise à jour
    }

    /** Méthode qui met à jour les objectifs liés à la récolte */
    public void mettreAJourObjectifsRecolter(Type typeCulture) {
        for (ObjectifJournalier objectif : objectifs.values()) {
            switch (objectif.getType()) {
                case RECOLTER_CULTURES:
                    ((ObjectifCompteur) objectif).mettreAJourProgression(1); // Incrémente la progression de l'objectif RECOLTER_CULTURES de la quantité récoltée à chaque récolte de culture
                    break;
                case RECOLTER_TYPES_CULTURE:
                    ((ObjectifCompteurTypes) objectif).mettreAJourProgression(typeCulture);
                    break;
                case TAUX_RECOLTE_CULTURES:
                    ((ObjectifTauxRecolte) objectif).mettreAJourNombreCulturesRecoltees();
                    break;
                default:
                    break;
            }
        }
        afficherProgressionObjectifs(); // Affiche la progression de chaque objectif dans la console après la mise à jour
    }

    /** Méthode qui met à jour les objectifs liés à l'arrosage */
    public void mettreAJourObjectifsArroser() {
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.ARROSER_CULTURES) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(1);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui met à jour les objectifs liés au labourage */
    public void mettreAJourObjectifsLabourer() {
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.LABOURER_CHAMPS) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(1);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui met à jour les objectifs liés à la coupe d'arbres */
    public void mettreAJourObjectifsCouperArbres() {
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.COUPER_ARBRES) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(1);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui met à jour les objectifs liés aux achats en boutique */
    public void mettreAJourObjectifsAcheterItems(int quantiteAchetee) {
        if (quantiteAchetee <= 0) {
            return;
        }

        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.ACHETER_ITEMS_BOUTIQUE) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(quantiteAchetee);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui met à jour l'objectif lié aux cultures mangées */
    public void mettreAJourObjectifsManger() {
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.CULTURES_MANGEES) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(1);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui met à jour l'objectif lié aux repoussage des lapins */
    public void mettreAJourObjectifsFuite() {
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.getType() == TypeObjectif.REPOUSSER_LAPINS) {
                ((ObjectifCompteur) objectif).mettreAJourProgression(1);
                break;
            }
        }
        afficherProgressionObjectifs();
    }

    /** Méthode qui affiche dans la console la progression de chaque objectif */
    public void afficherProgressionObjectifs() {
        System.out.println("Progression des objectifs :");
        for (ObjectifJournalier objectif : objectifs.values()) {
            System.out.println(objectif.getType() + " : " + objectif.getProgressionString());
        }
    }

    /** Méthode qui applique les changements liés au jour */
    public boolean appliquerChangementsJour() {
        if (estJourValide()) {
            System.out.println("Jour " + jour.getJour() + " validé !");
            genererObjectifs(); // Génère de nouveaux objectifs pour le nouveau jour
            return true;
        } else {
            System.out.println("Jour " + jour.getJour() + " non validé. Vous devez atteindre au moins " + getNombreObjectifsAValiderEffectif() + " objectifs pour valider le jour.");
            return false;
        }
    }
    
}
