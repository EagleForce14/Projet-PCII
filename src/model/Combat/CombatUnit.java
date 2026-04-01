package model.Combat;


/** Classe qui représente une unité de combat */
public class CombatUnit {
    // Constantes pour la vie et l'attaque de base
    public static final int BASE_HEALTH = 100;
    public static final int BASE_ATTACK_POWER = 20;

    // // Unité associée à l'unité de combat
    // private Unit unit;
    private int health; // Points de vie de l'unité
    private int attackPower; // Puissance d'attaque de l'unité

    /* Constructeur de l'unité de combat */
    public CombatUnit() {
        this.health = BASE_HEALTH;
        this.attackPower = BASE_ATTACK_POWER;
    }

    /** Méthode qui fait subir des dégâts à l'unité */
    public void receiveDamage(int damage) {
        health -= damage;
        // S'assurer que les points de vie ne deviennent pas négatifs
        if (health < 0) {
            health = 0;
        }
    }

    /** Méthode qui vérifie si l'unité est encore vivante */
    public boolean isAlive() {
        return health > 0;
    }

    /** Méthode qui renvoie les points de vie de l'unité */
    public int getHealth() {
        return health;
    }

    /** Méthode qui renvoie la puissance d'attaque de l'unité */
    public int getAttackPower() {
        return attackPower;
    }
}
