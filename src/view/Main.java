package view;

import controller.MovementController;
import model.EnemyPhysicsThread;
import model.EnemyModel;
import model.MovementModel;
import model.PhysicsThread;
import model.Unit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Image;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Projet PCII");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(960, 540));
        frame.setPreferredSize(new Dimension(1280, 720));

        // Image de fond globale de la fenêtre.
        Image backgroundImage = ImageLoader.load("/assets/Main_Background.png");

        MovementModel model = new MovementModel();
        Unit playerUnit = new Unit(0, 0);
        model.setPlayerUnit(playerUnit);

        EnemyModel enemyModel = new EnemyModel();
        enemyModel.setPlayer(playerUnit);

        FieldPanel fieldPanel = new FieldPanel();
        MovementView movementView = new MovementView(model, fieldPanel);
        movementView.setAlignmentX(0.5f);
        movementView.setAlignmentY(0.5f);

        EnemyView enemyView = new EnemyView(enemyModel, fieldPanel);
        enemyView.setAlignmentX(0.5f);
        enemyView.setAlignmentY(0.5f);

        JPanel fieldLayer = new JPanel(new GridBagLayout());
        fieldLayer.setOpaque(false);
        fieldLayer.setAlignmentX(0.5f);
        fieldLayer.setAlignmentY(0.5f);
        fieldLayer.add(fieldPanel);

        JPanel root = createRootPanel(backgroundImage);
        root.setLayout(new OverlayLayout(root));
        root.add(movementView);
        root.add(enemyView);
        root.add(fieldLayer);
        frame.setContentPane(root);

        new MovementController(model, movementView);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        enemyModel.setViewportSize(root.getWidth(), root.getHeight());

        (new PhysicsThread(model)).start();
        (new EnemyPhysicsThread(enemyModel)).start();
        (new RenderThread(root)).start();

        movementView.requestFocusInWindow();
    }

    private static JPanel createRootPanel(Image backgroundImage) {
        return new BackgroundPanel(backgroundImage);
    }
}
