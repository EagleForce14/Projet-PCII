package model.runtime;

import model.culture.GrilleCulture;
import model.enemy.EnemyPhysicsThread;
import model.environment.TreeThread;
import model.grotte.ShrineHazardThread;
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
    private final PhysicsThread cavePhysicsThread;
    private final EnemyPhysicsThread enemyPhysicsThread;
    private final EnemyPhysicsThread caveEnemyPhysicsThread;
    private final RenderThread renderThread;
    private final TreeThread treeThread;
    private final ShrineHazardThread shrineHazardThread;
    private final WorkshopConstructionManager workshopConstructionManager;

    public GameSession(Jour jour, GrilleCulture grilleCulture, PhysicsThread physicsThread,
                       PhysicsThread cavePhysicsThread,
                       EnemyPhysicsThread enemyPhysicsThread,
                       EnemyPhysicsThread caveEnemyPhysicsThread,
                       RenderThread renderThread,
                       TreeThread treeThread,
                       ShrineHazardThread shrineHazardThread,
                       WorkshopConstructionManager workshopConstructionManager) {
        this.jour = jour;
        this.grilleCulture = grilleCulture;
        this.physicsThread = physicsThread;
        this.cavePhysicsThread = cavePhysicsThread;
        this.enemyPhysicsThread = enemyPhysicsThread;
        this.caveEnemyPhysicsThread = caveEnemyPhysicsThread;
        this.renderThread = renderThread;
        this.treeThread = treeThread;
        this.shrineHazardThread = shrineHazardThread;
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
        if (cavePhysicsThread != null) {
            cavePhysicsThread.interrupt();
        }
        enemyPhysicsThread.interrupt();
        if (caveEnemyPhysicsThread != null) {
            caveEnemyPhysicsThread.interrupt();
        }
        renderThread.arreter();
        treeThread.arreter();
        if (shrineHazardThread != null) {
            shrineHazardThread.shutdown();
        }
        if (workshopConstructionManager != null) {
            workshopConstructionManager.shutdown();
        }
    }
}
