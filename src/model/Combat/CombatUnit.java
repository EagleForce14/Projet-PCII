package model.Combat;


/** Classe qui représente une unité de combat */
public class CombatUnit {
    // Constantes pour la vie et l'attaque de base
    public static final int BASE_HEALTH = 100;
    public static final int BASE_ATTACK_POWER = 20;

    private final int maxHealth;
    private int health;
    private final int attackPower;

    /* Constructeur de l'unité de combat */
    public CombatUnit() {
        this(BASE_HEALTH, BASE_ATTACK_POWER);
    }

    /**
     * Variante utilisée pour spécialiser les statistiques
     * d'un combattant précis sans dupliquer la logique de vie.
     */
    public CombatUnit(int maxHealth, int attackPower) {
        this.maxHealth = Math.max(1, maxHealth);
        this.health = this.maxHealth;
        this.attackPower = Math.max(0, attackPower);
    }

    /** Méthode qui fait subir des dégâts à l'unité */
    public synchronized void receiveDamage(int damage) {
        if (damage <= 0 || health <= 0) {
            return;
        }

        health = Math.max(0, health - damage);
    }

    /** Méthode qui vérifie si l'unité est encore vivante */
    public synchronized boolean isAlive() {
        return health > 0;
    }

    /** Méthode qui renvoie les points de vie de l'unité */
    public synchronized int getHealth() {
        return health;
    }

    /** Renvoie la santé maximale de l'unité. */
    public int getMaxHealth() {
        return maxHealth;
    }

    /** Méthode qui renvoie la puissance d'attaque de l'unité */
    public int getAttackPower() {
        return attackPower;
    }

    /**
     * Réinitialise complètement la vie de l'unité.
     * On s'en sert par exemple au début d'une nouvelle tentative dans la grotte.
     */
    public synchronized void healToFull() {
        health = maxHealth;
    }

    /**
     * Petite aide d'affichage pour dessiner une barre de vie
     * sans recalculer le ratio à plusieurs endroits.
     */
    public synchronized double getHealthRatio() {
        return Math.max(0.0, Math.min(1.0, health / (double) maxHealth));
    }
}
