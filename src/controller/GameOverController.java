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
    private final GameSession session;

    public GameOverController(JFrame frame, GameSession session, GameOverOverlay... overlays) {
        this.frame = frame;
        this.session = session;

        if (overlays == null) {
            return;
        }

        for (GameOverOverlay overlay : overlays) {
            if (overlay == null) {
                continue;
            }

            overlay.getReplayButton().addActionListener(this::restartGame);
        }
    }

    private void restartGame(ActionEvent event) {
        if (session == null || frame == null) {
            return;
        }

        session.shutdown();
        Main.installNewGame(frame, false);
    }
}
