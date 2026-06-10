package net.novaware.nes.core.ui;

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

    private int pixelHeight;
    private int paddingTop;

    private int pixelWidth;
    private int paddingLeft;

    private boolean drawMask = false;

    public JDisplay(DisplayModel model) {
        this.model = model;
        this.model.addChangeListener(this);

        registerMouseClicked(model);
    }

    private void registerMouseClicked(DisplayModel model) {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int pixelX = (e.getX() - paddingLeft) / pixelWidth;
                int pixelY = (e.getY() - paddingTop) / pixelHeight;
                Color c = model.getColor(pixelY, pixelX);
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

        videoStandard = VideoStandard.NTSC;

        int height = getHeight();
        pixelHeight = height / videoStandard.getActiveHeight();
        paddingTop = (height - pixelHeight * videoStandard.getActiveHeight()) / 2;

        int width = getWidth();
        pixelWidth = pixelHeight * 8 / 7; // TODO: support PAL too
        paddingLeft = (width - pixelWidth * videoStandard.getActiveWidth()) / 2;

        for (int y = 0; y < videoStandard.getActiveHeight(); y++) {
            for (int x = 0; x < videoStandard.getActiveWidth(); x++) {

                g.setColor(model.getColor(y, x));

                int rectX = paddingLeft + (x * pixelWidth);
                int rectY = paddingTop + (y * pixelHeight);
                g.fillRect(rectX, rectY, pixelWidth, pixelHeight);

                if (pixelWidth > 2 && pixelHeight > 2 && drawMask) {
                    int rectXPlusW = rectX + pixelWidth - 1;
                    int rectYPlusH = rectY + pixelHeight - 1;

                    g.setColor(Color.BLACK);
                    g.drawLine(rectX, rectYPlusH, rectXPlusW, rectYPlusH);
                    g.drawLine(rectXPlusW, rectY, rectXPlusW, rectYPlusH);
                }
            }
        }

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        repaint();
    }
}
