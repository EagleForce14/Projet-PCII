package model;

/**
 * Thread dédié à la mise à jour des ennemis.
 */
public class EnemyPhysicsThread extends Thread {
    private final EnemyModel enemyModel;
    private final int DELAY = 16; // ~60 Hz

    public EnemyPhysicsThread(EnemyModel enemyModel) {
        this.enemyModel = enemyModel;
    }

    @Override
    public void run() {
        while (true) {
            enemyModel.update();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}