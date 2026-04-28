package model.enemy;

import model.runtime.ActivatableGameLoopThread;

/**
 * Thread dédié à la mise à jour des ennemis.
 */
public class EnemyPhysicsThread extends ActivatableGameLoopThread {
    // Modèle ennemi que la boucle doit faire avancer.
    private final EnemyModel enemyModel;
    // Délai cible entre deux mises à jour pour rester proche de 60 Hz.
    private static final int DELAY_MS = 16; // ~60 Hz

    /**
     * On crée le thread ennemi en le laissant actif par défaut.
     */
    public EnemyPhysicsThread(EnemyModel enemyModel) {
        this(enemyModel, true);
    }

    /**
     * On prépare ici la boucle physique avec son état d'activation initial.
     */
    public EnemyPhysicsThread(EnemyModel enemyModel, boolean initiallyActive) {
        super(initiallyActive, DELAY_MS);
        this.enemyModel = enemyModel;
    }

    /**
     * Un tick ennemi correspond simplement à une mise à jour du modèle.
     */
    @Override
    protected void performTick() {
        enemyModel.update();
    }
}
