package model.objective;

/** Interface représentant un objectif */
public interface ObjectifJournalier {

    TypeObjectif getType();
    int getProgression();
    String getProgressionString();
    boolean estAtteint();
}
