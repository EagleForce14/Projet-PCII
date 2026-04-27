package view;

import java.awt.Component;

/**
 * Thread dédié au moteur de rendu pour mettre à jour l'affichage.
 * Il demande à la vue de se redessiner à intervalle régulier.
 */
public class RenderThread extends Thread {
    // Permet de savoir quel élément graphique doit être redessiné.
    private final Component component;
    // Indique si la boucle de rendu doit continuer à tourner.
    private volatile boolean running = true;
    // Délai cible entre deux repaints.
    private final int DELAY = 16; // ~60 FPS

    /**
     * On prépare le thread de rendu pour un composant précis.
     */
    public RenderThread(Component component) {
        this.component = component;
    }

    /**
     * Redémarrer une partie dans la même fenêtre veut dire
     * qu'il faut aussi savoir arrêter l'ancien thread de rendu.
     */
    public void arreter() {
        running = false;
        interrupt();
    }

    /**
     * On redessine régulièrement le composant tant que le thread de rendu reste actif.
     */
    @Override
    public void run() {
        while (running) {
            // On demande juste un repaint ; c'est Swing qui regroupe ensuite les vrais dessins.
            component.repaint();

            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                interrupt();
                return;
            }
        }
    }
}
