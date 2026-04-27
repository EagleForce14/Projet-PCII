package model.management;

import java.util.ArrayList;
import java.util.List;

public class Money {
    /**
     * Classe réprésentant le système d'argent du jeu. 
     * Stockage de l'argent en Integer pour évité les arondis liés au float et double.
     * **/
    
    // Montant total actuellement possédé par le joueur.
    private int amount;
    // Effets temporaires utilisés pour animer les gains récents à l'écran.
    private final List<MoneyRewardEffect> rewardEffects;

    // constructeur
    /**
     * Constructeur de la classe Money.
     * @param amount
     * Création de l'instance d'argent
    */
    public Money(int amount) {
        this.amount = amount;
        this.rewardEffects = new ArrayList<>();
    }

    // getter et setter
    /**
     * Getter de l'attribut amount.
     * @return int
     * Retourne la quantité d'argent.
    **/
    public synchronized int getAmount() {
        return amount;
    }

    /**
     * Ajoute un montant brut.
     * Utile quand une action de jeu rapporte directement un gain.
     */
    public void credit(int amount) {
        creditInternal(amount, 0, 0, false);
    }

    /**
     * Variante utilisée quand le gameplay connaît la position d'origine du gain.
     * C'est le cas idéal pour animer une pièce qui part réellement du monde
     * jusqu'au HUD d'argent.
     */
    public void creditFromWorld(int amount, int sourceWorldX, int sourceWorldY) {
        creditInternal(amount, sourceWorldX, sourceWorldY, true);
    }

    /**
     * Expose les gains récents sous forme d'instantané.
     * La vue consomme cette liste en lecture seule pour dessiner les animations.
     */
    public synchronized List<MoneyRewardEffect> getActiveRewardEffects() {
        long now = System.currentTimeMillis();
        rewardEffects.removeIf(effect -> effect == null || effect.isExpired(now));
        return new ArrayList<>(rewardEffects);
    }

    /**
     * On centralise ici le vrai crédit d'argent et la création de son effet visuel éventuel.
     */
    private synchronized void creditInternal(int amount, int sourceWorldX, int sourceWorldY, boolean explicitSource) {
        if (amount < 0) {
            throw new IllegalArgumentException("Un gain d'argent ne peut pas être négatif.");
        }
        if (amount == 0) {
            return;
        }

        this.amount += amount;
        long now = System.currentTimeMillis();
        rewardEffects.add(explicitSource
                ? new MoneyRewardEffect(amount, sourceWorldX, sourceWorldY, true, now)
                : new MoneyRewardEffect(amount, now));
    }

    /**
     * Méthode pour ajouter de l'argent à l'instance actuelle.
     * @param other
     * L'instance d'argent à ajouter.
     * Cette méthode modifie l'instance actuelle en ajoutant la quantité d'argent de l'autre instance.
    **/
    public void add(Money other) {
        credit(other.amount);
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

    /**
     * On renvoie le montant sous forme de texte simple.
     */
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
