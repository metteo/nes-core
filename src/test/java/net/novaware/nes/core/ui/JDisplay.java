package net.novaware.nes.core.ui;

import net.novaware.nes.core.config.BorderRegion;
import net.novaware.nes.core.config.VideoStandard;
import net.novaware.nes.core.util.Hex;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JDisplay extends JComponent implements ChangeListener {

    private DisplayModel model;

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

    public JDisplay(DisplayModel model) {
        this.model = model;
        this.model.addChangeListener(this);

        registerMouseClicked(model);

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

    public DisplayModel getModel() {
        return model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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
                g.setColor(pixelColor);

                int rectX = paddingLeft + (x * pixelWidth);
                int rectY = paddingTop + (y * pixelHeight);
                g.fillRect(rectX, rectY, pixelWidth, pixelHeight);

                // https://en.wikipedia.org/wiki/Moir%C3%A9_pattern
                if (pixelWidth > 1 && pixelHeight > 1 && drawMask) {
                    int rectXPlusW = rectX + pixelWidth - 1;
                    int rectYPlusH = rectY + pixelHeight - 1;

//                    vertical lines don't look that good
//                    g.setColor(pixelColor.brighter());
//                    g.drawLine(rectXPlusW, rectY, rectXPlusW, rectYPlusH);

                    g.setColor(pixelColor.darker());
                    g.drawLine(rectX, rectYPlusH, rectXPlusW, rectYPlusH);


                }
            }
        }

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        repaint();
    }
}
