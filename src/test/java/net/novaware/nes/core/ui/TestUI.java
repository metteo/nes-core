package net.novaware.nes.core.ui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestUI {

    static void main() {
        SwingUtilities.invokeLater(TestUI::createAndShowGui);
    }

    public static void createAndShowGui() {
        createAndShowGui(new PaletteDisplayModel(), new AtomicInteger());
    }

    public static void createAndShowGui(DisplayModel displayModel, AtomicInteger keyState) {
        JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        frame.setLocation(20, 20); //TODO: remember last location and reopen in the same?
        frame.setFocusable(true); //TODO: input focus should be probably on something more specific?
        frame.requestFocusInWindow();

        JDisplay display = new JDisplay(displayModel, keyState);
        display.setPreferredSize(new Dimension(1280, 800)); // steam deck
        //display.setPreferredSize(new Dimension(256 + 2, 240 + 2)); // 1x scale

        //JBezel bezel = new JBezel();
        //bezel.setPreferredSize(new Dimension(1280, 800));

        frame.add(display);
        //frame.add(bezel);
        frame.pack();

        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
