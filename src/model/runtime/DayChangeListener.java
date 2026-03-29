package model.runtime;

/**
 * Interface simple pour être notifié lors d'un changement de jour.
 */
public interface DayChangeListener {
    /**
     * Appelé quand un nouveau jour commence.
     * @param day le numéro du jour courant (déjà incrémenté)
     */
    void onNewDay(int day);
}

