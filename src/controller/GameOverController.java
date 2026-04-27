package controller;

import main.Main;
import model.runtime.GameSession;
import view.GameOverOverlay;

import javax.swing.JFrame;
import java.awt.event.ActionEvent;

/**
 * Contrôleur dédié au redémarrage après défaite.
 */
public final class GameOverController {
    private final JFrame frame;
    private final GameSession session; // Référence à la session de jeu en cours

    public GameOverController(JFrame frame, GameSession session, GameOverOverlay... overlays) {
        this.frame = frame;
        this.session = session;

        if (overlays == null) {
            return; // Si aucun overlay n'est fourni, on ne fait rien.
        }

        // Ajoute un ActionListener à chaque bouton de replay dans les overlays fournis
        for (GameOverOverlay overlay : overlays) {
            if (overlay == null) {
                continue;
            }

            overlay.getReplayButton().addActionListener(this::restartGame);
        }
    }

    // Méthode pour redémarrer le jeu lorsque le bouton de replay est cliqué
    private void restartGame(ActionEvent event) {
        if (session == null || frame == null) {
            return;
        }

        // Ferme la session de jeu actuelle avant de lancer une nouvelle partie
        session.shutdown();
        Main.installNewGame(frame, false);
    }
}
