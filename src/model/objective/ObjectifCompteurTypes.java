package model.objective;

import java.util.HashSet;
import java.util.Set;

import model.culture.Type;

/** Classe représentant un objectif de type compteur pour plusieurs types de cultures */
public class ObjectifCompteurTypes extends ObjectifCompteur {

    /** Attribut qui stocke les types de cultures déjà vus */
    private final Set<Type> typesCultures;

    /** Constructeur de la classe ObjectifCompteurTypes */
    public ObjectifCompteurTypes(TypeObjectif type, int valeurCible) {
        super(type, valeurCible);
        this.typesCultures = new HashSet<>();
    }

    /** Méthode qui met à jour la progression de l'objectif */
    public void mettreAJourProgression(Type typeCulture) {
        if (!typesCultures.contains(typeCulture)) {
            typesCultures.add(typeCulture); // Ajoute le type de culture à la liste des types de cultures déjà vus
            this.mettreAJourProgression(1); // Incrémente la progression uniquement si le type de culture n'a pas déjà été vu
        }
    }

}
