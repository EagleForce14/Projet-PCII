package model.objective;

/** Interface représentant un objectif */
public interface ObjectifJournalier {

    /** Getter qui renvoie le type de l'objectif */
    TypeObjectif getType(); 

    /** Getter qui renvoie la progression de l'objectif */
    int getProgression();

    /** Getter qui renvoie la progression de l'objectif sous forme de chaîne de caractères */
    String getProgressionString();
    
    /** Vérifie si l'objectif est atteint */
    boolean estAtteint();
}
