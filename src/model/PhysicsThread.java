package model;
/**
 * Thread dédié au moteur physique pour la mise à jour du modèle.
 * Il appelle la méthode update() du modèle à intervalle régulier (environ à 60 FPS).
 */
public class PhysicsThread extends Thread {
    private final MovementModel model;
    private final int DELAY = 16; // ~60 Hz

    public PhysicsThread(MovementModel model) {
        this.model = model;
    }

    @Override
    public void run() {
        while (true) {
            // On met à jour les informations du modèle
            model.update();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
