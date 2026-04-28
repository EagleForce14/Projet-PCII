package model.objective;

/** Classe représentant un objectif avec un compteur */
public class ObjectifCompteur implements ObjectifJournalier {

    /** Attribut qui stocke le type de l'objectif */
    private final TypeObjectif type;

    /** Attribut qui stocke le valeur cible */
    private final int valeurCible;

    /** Attribut qui stocke la progression de l'objectif */
    private int progression;

    /** Constructeur de la classe ObjectifCompteur */
    public ObjectifCompteur(TypeObjectif type, int valeurCible) {
        this.type = type;
        this.valeurCible = valeurCible;
        this.progression = 0;
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

    /** Getter qui renvoie la progression de l'objectif sous forme de chaîne de caractères */
    @Override
    public String getProgressionString() {
        return progression + " / " + valeurCible; // Affiche la progression sous la forme "progression / valeur cible"
    }

    /** Getter qui renvoie la progression de l'objectif */
    @Override
    public int getProgression() {
        return progression;
    }

    /** Méthode qui vérifie si l'objectif est atteint */
    @Override
    public boolean estAtteint() {
        if (type == TypeObjectif.CULTURES_MANGEES) {
            return progression <= valeurCible; // L'objectif est atteint si le nombre de cultures mangées par les lapins est inférieur ou égal à la valeur cible
        } else {
            return progression >= valeurCible; // L'objectif est atteint si la progression est supérieure ou égale à la valeur cible
        }
    }

    /** Méthode qui met à jour la progression de l'objectif */
    public void mettreAJourProgression(int increment) {
        this.progression += increment;
    }
}
