package model.grotte.combat;

import model.runtime.GamePauseController;
import model.runtime.ThreadActivationGate;

/**
 * Thread dédié à la boucle de combat de la grotte.
 * Il reste inactif tant que le joueur n'est pas dans cette zone.
 */
public final class CaveCombatThread extends Thread {
    private static final int DELAY_MS = 16;

    private final CaveCombatModel caveCombatModel;
    private final GamePauseController pauseController;
    private final ThreadActivationGate activationGate;

    public CaveCombatThread(CaveCombatModel caveCombatModel, boolean initiallyActive) {
        this.caveCombatModel = caveCombatModel;
        this.pauseController = GamePauseController.getInstance();
        this.activationGate = new ThreadActivationGate(initiallyActive);
    }

    public void setThreadActive(boolean active) {
        activationGate.setActive(active);
    }

    @Override
    public void run() {
        while (true) {
            try {
                activationGate.awaitActivation();
                pauseController.awaitIfPaused();
                if (!activationGate.isActive()) {
                    continue;
                }
                caveCombatModel.update();
                pauseController.sleep(DELAY_MS);
            } catch (InterruptedException exception) {
                interrupt();
                return;
            }
        }
    }
}
