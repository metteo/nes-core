package net.novaware.nes.core.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * @author gemini
 */
public class JBezel extends JComponent {

    public JBezel() {
        // Ensure the panel is transparent where the bezel curves away
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();
        // Enable anti-aliasing for smooth, non-jagged curves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Define padding so the curves don't clip at the component edges
        int padding = 10;
        int x = padding;
        int y = padding;
        int w = width - (padding * 2);
        int h = height - (padding * 2);

        // Calculate corners
        int left = x;
        int right = x + w;
        int top = y;
        int bottom = y + h;

        int centerX = x + w / 2;
        int centerY = y + h / 2;

        // "Bloat" factor: how much the edges bow outward. 
        // Adjust these values to get the exact retro feel you want.
        int horizontalBow = 15;
        int verticalBow = 15;

        // Create the curved CRT bezel path
        Path2D crtPath = new Path2D.Double();

        // Start at top-left corner
        crtPath.moveTo(left, top);

        // Top edge: curves from top-left to top-right, pulled upward by the control point
        crtPath.quadTo(centerX, top - verticalBow, right, top);

        // Right edge: curves from top-right to bottom-right, pulled rightward
        crtPath.quadTo(right + horizontalBow, centerY, right, bottom);

        // Bottom edge: curves from bottom-right to bottom-left, pulled downward
        crtPath.quadTo(centerX, bottom + verticalBow, left, bottom);

        // Left edge: curves from bottom-left back to top-left, pulled leftward
        crtPath.quadTo(left - horizontalBow, centerY, left, top);

        crtPath.closePath();

        // 1. Paint the outer bezel structure (the TV housing)
        g2d.setColor(new Color(45, 45, 45)); // Retro dark gray/charcoal
        g2d.fill(crtPath);

        // 2. Draw a thick border to simulate the plastic rim depth
        g2d.setColor(new Color(30, 30, 30));
        g2d.setStroke(new BasicStroke(6f));
        g2d.draw(crtPath);

        // 3. Optional: Paint the inner screen area
        // For a true inner screen, you could scale down the path or create an inner path

        g2d.dispose();
    }

}
