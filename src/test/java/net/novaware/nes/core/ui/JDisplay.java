package net.novaware.nes.core.ui;

import net.novaware.nes.core.config.VideoStandard;

import javax.swing.*;
import java.awt.*;

public class JDisplay extends JComponent {

    private DisplayModel model;

    private double scale;

    private final Insets insets = new Insets(0, 0, 0, 0); // reused if no border


    public JDisplay(DisplayModel model) {
        this.model = model;
    }

    public DisplayModel getModel() {
        return model;
    }

    public void setModel(DisplayModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Insets insets = getInsets(this.insets);

        VideoStandard vs = VideoStandard.NTSC;

        int height = getHeight();
        int pixelHeight = height / vs.getActiveHeight();
        int paddingTop = (height - pixelHeight * vs.getActiveHeight()) / 2;

        int width = getWidth();
        int pixelWidth = pixelHeight * 8 / 7; // TODO: support PAL too
        int paddingLeft = (width - pixelWidth * vs.getActiveWidth()) / 2;

        boolean prevBlack = false;

        for (int y = 0; y < vs.getActiveHeight(); y++) {
            for (int x = 0; x < vs.getActiveWidth(); x++) {
                g.setColor(prevBlack ? Color.WHITE : Color.BLACK);
                int rectX = paddingLeft + (x * pixelWidth);
                int rectY = paddingTop + (y * pixelHeight);
                g.fillRect(rectX, rectY, pixelWidth, pixelHeight);

                prevBlack = !prevBlack;
            }
            prevBlack = !prevBlack;
        }

    }
}
