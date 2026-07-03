package net.novaware.nes.core.ui;

import net.novaware.nes.core.config.BorderRegion;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.ppu.unit.PaletteData;
import net.novaware.nes.core.util.Bin;
import net.novaware.nes.core.util.Hex;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_X;
import static java.awt.event.KeyEvent.VK_Z;
import static net.novaware.nes.core.util.UTypes.ubyte;

public class JDisplay extends JComponent implements ChangeListener {

    private DisplayModel model;
    private AtomicInteger keyState;

    private double scale;

    private final Insets insets = new Insets(0, 0, 0, 0); // reused if no border
    private VideoStandard videoStandard;
    private BorderRegion borderRegion;

    private final int screenHeight;
    private final int screenWidth;

    private int pixelHeight;
    private int paddingTop;

    private int pixelWidth;
    private int paddingLeft;

    private boolean drawMask = true;

    public JDisplay(DisplayModel model, AtomicInteger keyState) {
        this.model = model;
        this.keyState = keyState;
        this.model.addChangeListener(this);

        setFocusable(true);

        registerMouseClicked(model);
        registerKeyListener();

        videoStandard = VideoStandard.NTSC;
        borderRegion = BorderRegion.of(videoStandard);

        // TODO: adjust to the model instead
        screenHeight = borderRegion.getTop() + videoStandard.getActiveHeight() + borderRegion.getBottom();
        screenWidth = borderRegion.getLeft() + videoStandard.getActiveWidth() + borderRegion.getRight();
    }

    private void registerMouseClicked(DisplayModel model) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();

                int pixelX = (e.getX() - paddingLeft) / pixelWidth - borderRegion.getLeft();
                int pixelY = (e.getY() - paddingTop) / pixelHeight - borderRegion.getTop();
                Color c = model.getColor(pixelY, pixelX); // FIXME: throws exception because out of model bounds
                String pixelC = Hex.s(c.getRGB());

                System.out.println("x: " + pixelX + ", y: " + pixelY + ", c: " + pixelC);

                drawMask = !drawMask;
                repaint();
            }
        });
    }

    // TODO: use FFM / Panama for input
    private void registerKeyListener() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                updateBit(e.getKeyCode(), true);

                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                updateBit(e.getKeyCode(), false);

                repaint();
            }
        });
    }

    private void updateBit(int keyCode, boolean high) {
        int shift = switch(keyCode) {
            case VK_Z     -> 7; // A, first to go when shifting left
            case VK_X     -> 6; // B
            case VK_SHIFT -> 5; // Select
            case VK_ENTER -> 4; // Start
            case VK_UP    -> 3;
            case VK_DOWN  -> 2;
            case VK_LEFT  -> 1;
            case VK_RIGHT -> 0;
            default       -> -1;
        };

        if (shift < 0) { return; }

        int maskIn = 1 << shift;

        IntBinaryOperator accumulator = high
                ? ((current, mask) -> current | mask)
                : ((current, mask) -> current & ~mask);

        keyState.accumulateAndGet(maskIn, accumulator);
    }

    public DisplayModel getModel() {
        return model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

//        long start = System.nanoTime();

        g.drawString(Bin.s(ubyte(keyState.get())), 0, 10);

        Insets insets = getInsets(this.insets);

        // TODO: account for overscan

        int height = getHeight();
        pixelHeight = height / screenHeight;
        paddingTop = (height - pixelHeight * screenHeight) / 2;

        int width = getWidth();
        pixelWidth = pixelHeight * 8 / 7; // TODO: support PAL too
        paddingLeft = (width - pixelWidth * screenWidth) / 2;

        for (int y = 0; y < screenHeight; y++) {
            for (int x = 0; x < screenWidth; x++) {
                Color pixelColor = model.getColor(y, x);
                g.setColor(pixelColor); // FIXME: massive garbage

                int rectX = paddingLeft + (x * pixelWidth);
                int rectY = paddingTop + (y * pixelHeight);
                g.fillRect(rectX, rectY, pixelWidth, pixelHeight); // FIXME: slow. Paint to a BufferedImage

                // https://en.wikipedia.org/wiki/Moir%C3%A9_pattern
                if (pixelWidth > 1 && pixelHeight > 1 && drawMask) {
                    int rectXPlusW = rectX + pixelWidth - 1;
                    int rectYPlusH = rectY + pixelHeight - 1;

//                    vertical lines don't look that good
//                    g.setColor(pixelColor.brighter());
//                    g.drawLine(rectXPlusW, rectY, rectXPlusW, rectYPlusH);

                    g.setColor(PaletteData.getDarker(pixelColor)); // FIXME: massive garbage
                    g.drawLine(rectX, rectYPlusH, rectXPlusW, rectYPlusH);


                }
            }
        }

//        long renderTime = System.nanoTime() - start;

//        System.out.println("Frame render time: " + renderTime);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        repaint(); // can be called from another thread
    }
}
