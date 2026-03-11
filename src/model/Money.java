package model;

public class Money {
    /**
     * Classe réprésentant le système d'argent du jeu. 
     * Stockage de l'argent en Integer pour évité les arondissemnt lié au float et double.
     * **/
    
    // attribut 
    private int amount;

    // constructeur
    /**
     * Constructeur de la classe Money.
     * @param amount
     * Création de l'instance d'argent
    */
    public Money(int amount) {
        this.amount = amount;
    }

    // getter et setter
    /**
     * Getter de l'attribut amount.
     * @return int
     * Retourne la quantité d'argent.
    **/
    public int getAmount() {
        return amount;
    }

    /**
     * Méthode pour ajouter de l'argent à l'instance actuelle.
     * @param other
     * L'instance d'argent à ajouter.
     * Cette méthode modifie l'instance actuelle en ajoutant la quantité d'argent de l'autre instance.
    **/
    public void add(Money other) {
        this.amount += other.amount;
    }
    /**
     * Méthode pour soustraire de l'argent de l'instance actuelle.
     * @param other
     * L'instance d'argent à soustraire.
     * Cette méthode modifie l'instance actuelle en soustrayant la quantité d'argent de l'autre instance.
     * */
    public void subtract(Money other) {
        this.amount -= other.amount;
    }

    @Override
    public String toString() {
        return String.format("%d", amount);
    }
    /**
     * Méthode pour comparer deux instances de Money.
     * @param obj
     * L'instance d'argent à comparer.
     * @return boolean
     * Retourne true si les deux instances sont égales, false sinon.
    **/
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Money money = (Money) obj;
        return amount == money.amount;
    }
}