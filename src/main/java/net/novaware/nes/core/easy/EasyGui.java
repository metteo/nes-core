package net.novaware.nes.core.easy;

import net.novaware.nes.core.memory.MemoryBus;
import org.checkerframework.checker.signedness.qual.Unsigned;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

import static net.novaware.nes.core.easy.memory.EasyMemMap.PICTURE_SEGMENT_START;
import static net.novaware.nes.core.util.UTypes.sint;
import static net.novaware.nes.core.util.UTypes.ubyte;
import static net.novaware.nes.core.util.UTypes.ushort;

/**
 * EasyGui provides a 32x32 pixel display for the Easy6502 environment.
 * It monitors the VRAM area and handles keyboard input.
 *
 * @author Gemini
 */
@SuppressWarnings({"method.invocation", "argument", "assignment", "signedness"}) // checker
public class EasyGui extends JFrame {

    private static final int SCREEN_WIDTH = 32;
    private static final int SCREEN_HEIGHT = 32;
    private static final int SCALE = 16;

    private final MemoryBus bus;
    private final Consumer<Byte> keyHandler;

    private final byte[] screenCache = new byte[SCREEN_WIDTH * SCREEN_HEIGHT];
    private final DisplayPanel displayPanel;

    // Standard Easy6502 palette mapping 0-15 to Colors
    private static final Color[] PALETTE = {
            Color.BLACK,                                // 0: Black
            Color.WHITE,                                // 1: White
            new Color(0x88, 0x00, 0x00),                // 2: Red
            new Color(0xAA, 0xFF, 0xEE),                // 3: Cyan
            new Color(0xCC, 0x44, 0xCC),                // 4: Purple
            new Color(0x00, 0xCC, 0x55),                // 5: Green
            new Color(0x00, 0x00, 0xAA),                // 6: Blue
            new Color(0xEE, 0xEE, 0x77),                // 7: Yellow
            new Color(0xDD, 0x88, 0x55),                // 8: Orange
            new Color(0x66, 0x44, 0x00),                // 9: Brown
            new Color(0xFF, 0x77, 0x77),                // 10: Light red
            new Color(0x33, 0x33, 0x33),                // 11: Dark gray
            new Color(0x77, 0x77, 0x77),                // 12: Grey
            new Color(0xAA, 0xFF, 0x66),                // 13: Light green
            new Color(0x00, 0x88, 0xFF),                // 14: Light blue
            new Color(0xBB, 0xBB, 0xBB)                 // 15: Light gray
    };

    public EasyGui(MemoryBus bus, Consumer<Byte> keyHandler) {
        this.bus = bus;
        this.keyHandler = keyHandler;

        setTitle("Easy6502 Display");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        displayPanel = new DisplayPanel();
        add(displayPanel);
        pack();

        // Ensure the window is focusable to receive keyboard input
        setFocusable(true);
        requestFocusInWindow();

        // Use keyPressed for better responsiveness in games
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar();
                // Only send valid ASCII characters to the 6502 input register
                if (c != KeyEvent.CHAR_UNDEFINED) {
                    keyHandler.accept(ubyte(c));
                }
            }
        });

        setLocationRelativeTo(null);
    }

    public void refresh() {
        boolean dirty = false;
        synchronized (screenCache) {
            for (int i = 0; i < screenCache.length; i++) {
                @Unsigned short address = ushort(sint(PICTURE_SEGMENT_START) + i);
                @Unsigned byte data = bus.peek(address);

                if (screenCache[i] != data) {
                    screenCache[i] = data;
                    dirty = true;
                }
            }
        }

        if (dirty) {
            displayPanel.repaint();
        }
    }

    private class DisplayPanel extends JPanel {
        DisplayPanel() {
            setPreferredSize(new Dimension(SCREEN_WIDTH * SCALE, SCREEN_HEIGHT * SCALE));
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            synchronized (screenCache) {
                for (int i = 0; i < screenCache.length; i++) {
                    int x = i % SCREEN_WIDTH;
                    int y = i / SCREEN_WIDTH;
                    int colorIdx = screenCache[i] & 0x0F;
                    g.setColor(PALETTE[colorIdx]);
                    g.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
                }
            }
        }
    }
}
