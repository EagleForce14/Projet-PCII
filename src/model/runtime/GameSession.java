package model.runtime;

import model.culture.GrilleCulture;
import model.enemy.EnemyPhysicsThread;
import model.environment.TreeThread;
import model.movement.PhysicsThread;
import model.workshop.WorkshopConstructionManager;
import view.RenderThread;

/**
 * Regroupe tout ce qui vit pendant une partie précise.
 * L'idée est simple : quand on relance une partie, on veut pouvoir arrêter
 * proprement l'ancienne sans laisser un thread ou une culture continuer dans son coin.
 */
public final class GameSession {
    private final Jour jour;
    private final GrilleCulture grilleCulture;
    private final PhysicsThread physicsThread;
    private final EnemyPhysicsThread enemyPhysicsThread;
    private final RenderThread renderThread;
    private final TreeThread treeThread;
    private final WorkshopConstructionManager workshopConstructionManager;

    public GameSession(Jour jour, GrilleCulture grilleCulture, PhysicsThread physicsThread,
                       EnemyPhysicsThread enemyPhysicsThread, RenderThread renderThread, TreeThread treeThread,
                       WorkshopConstructionManager workshopConstructionManager) {
        this.jour = jour;
        this.grilleCulture = grilleCulture;
        this.physicsThread = physicsThread;
        this.enemyPhysicsThread = enemyPhysicsThread;
        this.renderThread = renderThread;
        this.treeThread = treeThread;
        this.workshopConstructionManager = workshopConstructionManager;
    }

    public void shutdown() {
        /*
         * On commence par les cultures, parce que ce sont elles qui ont le plus de chances
         * de continuer à évoluer "silencieusement" si on oublie de les couper.
         */
        grilleCulture.arreterToutesLesCultures();

        /*
         * Ensuite on arrête chaque boucle de jeu.
         * Le but est que le bouton "Rejouer" se comporte vraiment comme un redémarrage net.
         */
        jour.arreter();
        jour.interrupt();
        physicsThread.interrupt();
        enemyPhysicsThread.interrupt();
        renderThread.arreter();
        treeThread.arreter();
        if (workshopConstructionManager != null) {
            workshopConstructionManager.shutdown();
        }
    }
}
