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
    public static final Map<TypeObjectif, Integer> VALEUR_DEPART_OBJECTIFS = Map.of(
        TypeObjectif.PLANTER_CULTURES, 0,
        TypeObjectif.PLANTER_TYPES_CULTURE, 1,
        TypeObjectif.RECOLTER_CULTURES, 0,
        TypeObjectif.RECOLTER_TYPES_CULTURE, 1,
        TypeObjectif.TAUX_RECOLTE_CULTURES, 50,
        TypeObjectif.CULTURES_MANGEES, 15,
        TypeObjectif.REPOUSSER_LAPINS, 1
    );

    /** Attribut qui stocke les objectifs */
    Map<TypeObjectif, ObjectifJournalier> objectifs;

    /** Attribut qui stocke le jour */
    Jour jour;

    /** Constructeur de la classe GestionnaireObjectifs */
    public GestionnaireObjectifs(Jour jour) {
        this.jour = jour;
        this.objectifs = new HashMap<>();
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
        int nombreMinimumObjectifsAtteints = getNombreMinimumObjectifsAtteints(); // Nombre minimum d'objectifs à atteindre pour valider le jour
        System.out.println("Nombre minimum d'objectifs à atteindre pour valider le jour : " + nombreMinimumObjectifsAtteints);
        if (nombreMinimumObjectifsAtteints > objectifs.size()) {
            nombreMinimumObjectifsAtteints = objectifs.size(); // Limite le nombre minimum d'objectifs à atteindre au nombre total d'objectifs disponibles
        }
        int objectifsAtteints = 0;
        for (ObjectifJournalier objectif : objectifs.values()) {
            if (objectif.estAtteint()) {
                objectifsAtteints++;
            }
        }
        return objectifsAtteints >= nombreMinimumObjectifsAtteints;
    }

    /** Méthode qui renvoie le nombre minimum d'objectifs à atteindre pour valider le jour */
    public int getNombreMinimumObjectifsAtteints() {
        return 1 + (int) (jour.getJour() * 0.5);
    }

    /** Méthode qui renvoie la progression de chaque objectif sous forme d'une map */
    public Map<TypeObjectif, String> getProgressionObjectifs() {
        Map<TypeObjectif, String> progression = new HashMap<>();
        for (ObjectifJournalier objectif : objectifs.values()) {
            progression.put(objectif.getType(), objectif.getType() + " : " + objectif.getProgressionString());
        }
        return progression;
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

    /** Méthode qui affiche dans la console la progression de chaque objectif */
    public void afficherProgressionObjectifs() {
        System.out.println("Progression des objectifs :");
        for (ObjectifJournalier objectif : objectifs.values()) {
            System.out.println(objectif.getType() + " : " + objectif.getProgressionString());
        }
    }

    /** Méthode qui applique les changements liés au jour */
    public void appliquerChangementsJour() {
        if (estJourValide()) {
            System.out.println("Jour " + jour.getJour() + " validé !");
        } else {
            System.out.println("Jour " + jour.getJour() + " non validé. Vous devez atteindre au moins " + getNombreMinimumObjectifsAtteints() + " objectifs pour valider le jour.");
        }
        genererObjectifs(); // Génère de nouveaux objectifs pour le nouveau jour
    }
    
}
