package net.novaware.nes.core.ui;

import javax.swing.*;
import java.awt.*;

public class TestUI {

    static void main() {
        SwingUtilities.invokeLater(TestUI::createAndShowGui);
    }

    static void createAndShowGui() {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same?
        frame.setFocusable(true); //TODO: input focus should be probably on something more specific?
        frame.requestFocusInWindow();

        DisplayModel displayModel = new DefaultDisplayModel();
        JDisplay display = new JDisplay(displayModel);
        display.setPreferredSize(new Dimension(1280, 800)); // steam deck

        JBezel bezel = new JBezel();
        bezel.setPreferredSize(new Dimension(1280, 800));

        frame.add(display);
        //frame.add(bezel);
        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
