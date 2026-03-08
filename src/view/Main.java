package view;

import controller.MovementController;
import model.MovementModel;
import model.PhysicsThread;
import model.Unit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridBagLayout;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Projet PCII");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(960, 540));
            frame.setPreferredSize(new Dimension(1280, 720));

            MovementModel model = new MovementModel();
            Unit playerUnit = new Unit(0, 0);
            model.setPlayerUnit(playerUnit);

            FieldPanel fieldPanel = new FieldPanel();
            MovementView movementView = new MovementView(model, fieldPanel);
            movementView.setAlignmentX(0.5f);
            movementView.setAlignmentY(0.5f);

            JPanel fieldLayer = new JPanel(new GridBagLayout());
            fieldLayer.setOpaque(false);
            fieldLayer.setAlignmentX(0.5f);
            fieldLayer.setAlignmentY(0.5f);
            fieldLayer.add(fieldPanel);

            JPanel root = new JPanel();
            root.setLayout(new OverlayLayout(root));
            root.add(movementView);
            root.add(fieldLayer);
            frame.setContentPane(root);

            new MovementController(model, movementView);

            (new PhysicsThread(model)).start();

            (new RenderThread(movementView)).start();

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            movementView.requestFocusInWindow();
        });
    }
}
