package model.objective;

import java.util.HashSet;
import java.util.Set;

import model.culture.Type;

/** Classe représentant un objectif de type compteur pour plusieurs types de cultures */
public class ObjectifCompteurTypes implements ObjectifJournalier {

    /** Attribut qui stocke le type de l'objectif */
    private TypeObjectif type;

    /** Attribut qui stocke le valeur cible */
    private int valeurCible;

    /** Attribut qui stocke la progression de l'objectif */
    private int progression;

    /** Attribut qui stocke les types de cultures déjà vus */
    private Set<Type> typesCultures;

    /** Constructeur de la classe ObjectifCompteurTypes */
    public ObjectifCompteurTypes(TypeObjectif type, int valeurCible) {
        this.type = type;
        this.valeurCible = valeurCible;
        this.progression = 0;
        this.typesCultures = new HashSet<>();
    }

    /** Getter qui renvoie le type de l'objectif */
    @Override
    public TypeObjectif getType() {
        return type;
    }

    /** Getter qui renvoie la valeur cible */
    public int getValeurCible() {
        return valeurCible;
    }

    /** Getter qui renvoie la progression de l'objectif */
    @Override
    public String getProgression() {
        return progression + " / " + valeurCible; // Affiche la progression sous la forme "progression / valeur cible"
    }

    /** Méthode qui vérifie si l'objectif est atteint */
    @Override
    public boolean estAtteint() {
        return progression >= valeurCible; // L'objectif est atteint si la progression est supérieure ou égale à la valeur cible

    }

    /** Méthode qui met à jour la progression de l'objectif */
    public void mettreAJourProgression(Type typeCulture) {
        if (!typesCultures.contains(typeCulture)) {
            typesCultures.add(typeCulture); // Ajoute le type de culture à la liste des types de cultures déjà vus
            this.progression++; // Incrémente la progression uniquement si le type de culture n'a pas déjà été vu
        }
    }

}
