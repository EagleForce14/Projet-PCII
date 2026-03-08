package view;
/**
 * Thread dédié au moteur de rendu pour mettre à jour l'affichage.
 * Il demande à la vue de se redessiner à intervalle régulier.
 */
public class RenderThread extends Thread {
    private final MovementView view;
    private volatile boolean running = true;
    private final int DELAY = 16; // ~60 FPS

    public RenderThread(MovementView view) {
        this.view = view;
    }

    @Override
    public void run() {
        while (running) {
            // Demande de redessin
            view.repaint();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
