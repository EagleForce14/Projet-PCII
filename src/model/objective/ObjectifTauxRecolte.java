package model.objective;

public class ObjectifTauxRecolte implements ObjectifJournalier {

    /** Attribut qui stocke le type de l'objectif */
    private TypeObjectif type;

    /** Attribut qui stocke le taux cible */
    private int tauxCible;

    /** Attribut qui stocke le nombre de cultures plantées */
    private int nombreCulturesPlantees;

    /** Attribut qui stocke le nombre de cultures récoltées */
    private int nombreCulturesRecoltees;

    /** Constructeur de la classe ObjectifTauxRecolte */
    public ObjectifTauxRecolte(TypeObjectif type, int tauxCible) {
        this.type = type;
        this.tauxCible = tauxCible;
        this.nombreCulturesPlantees = 0;
        this.nombreCulturesRecoltees = 0;
    }

    /** Getter qui renvoie le type de l'objectif */
    @Override
    public TypeObjectif getType() {
        return type;
    }

    /** Getter qui renvoie le taux cible */
    public int getTauxCible() {
        return tauxCible;
    }

    /** Getter qui renvoie la progression de l'objectif */
    @Override
    public String getProgression() {
        return calculerTauxRecolte() + "%/" + tauxCible + "%"; // Calcule le taux de récolte en pourcentage
    }

    /** Méthode qui calcule le taux de récolte */
    public int calculerTauxRecolte() {
        if (nombreCulturesPlantees == 0) {
            return 100; // Évite la division par zéro, le taux de récolte est de 100% si aucune culture n'est plantée
        }
        return (int) ((nombreCulturesRecoltees / (float) nombreCulturesPlantees) * 100);
    }

    /** Méthode qui vérifie si l'objectif est atteint */
    @Override
    public boolean estAtteint() {
        return calculerTauxRecolte() >= tauxCible;
    }

    /** Méthode qui met à jour le nombre de cultures plantées */
    public void mettreAJourNombreCulturesPlantees() {
        this.nombreCulturesPlantees += 1;
    }

    /** Méthode qui met à jour le nombre de cultures récoltées */
    public void mettreAJourNombreCulturesRecoltees() {
        this.nombreCulturesRecoltees += 1;
    }
}