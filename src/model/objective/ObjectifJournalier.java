package model.objective;

/** Interface représentant un objectif */
public interface ObjectifJournalier {

    TypeObjectif getType();
    String getProgression();
    boolean estAtteint();
}
