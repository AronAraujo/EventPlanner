package eventplanner.view;

import javax.swing.*;
import java.awt.*;

public class RoundPanel extends JPanel {
    private int radius;
    private Color bg;

    public RoundPanel(int radius, Color bg) {
        this.radius = radius;
        this.bg = bg;
        setOpaque(false);
    }

    public RoundPanel(int radius) {
        this(radius, Theme.BG_CARD);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
        super.paintComponent(g);
    }
}