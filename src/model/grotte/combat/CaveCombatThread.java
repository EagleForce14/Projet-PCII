package model.grotte.combat;

import model.runtime.ActivatableGameLoopThread;

/**
 * Thread dédié à la boucle de combat de la grotte.
 * Il reste inactif tant que le joueur n'est pas dans cette zone.
 */
public final class CaveCombatThread extends ActivatableGameLoopThread {
    private static final int DELAY_MS = 16;

    private final CaveCombatModel caveCombatModel;

    public CaveCombatThread(CaveCombatModel caveCombatModel, boolean initiallyActive) {
        super(initiallyActive, DELAY_MS);
        this.caveCombatModel = caveCombatModel;
    }

    @Override
    protected void performTick() {
        caveCombatModel.update();
    }
}
