package model;
/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends Thread {
    private final MovementModel movementModel;
    private final int DELAY = 16; // ~60 Hz

    public PhysicsThread(MovementModel model) {
        this.movementModel = model;
    }

    @Override
    public void run() {
        while (true) {
            movementModel.update();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
