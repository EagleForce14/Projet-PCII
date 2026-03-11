package view;

import java.awt.Component;

/**
 * Thread dédié au moteur de rendu pour mettre à jour l'affichage.
 * Il demande à la vue de se redessiner à intervalle régulier.
 */
public class RenderThread extends Thread {
    private final Component component;
    private volatile boolean running = true;
    private final int DELAY = 16; // ~60 FPS

    public RenderThread(Component component) {
        this.component = component;
    }

    @Override
    public void run() {
        while (running) {
            component.repaint();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
