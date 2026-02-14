package view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagLayout;

public class Main {
     static void main(String[] args) {
        JFrame frame = new JFrame("Projet PCII");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(960, 540));
        frame.setPreferredSize(new Dimension(1280, 720));

        JPanel root = new JPanel(new GridBagLayout());
        root.add(new FieldPanel());
        frame.setContentPane(root);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
