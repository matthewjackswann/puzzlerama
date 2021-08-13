package com.matthewjackswann;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;

public class ImageRegionSelector extends JLabel {

    private Point moveFrom = new Point(0,0);
    private Point moveTo;
    private final int baseImageWidth;
    private final int baseImageHeight;


    ImageRegionSelector(ImageIcon imageIcon) {
        super(imageIcon);
        this.baseImageWidth = imageIcon.getIconWidth();
        this.baseImageHeight = imageIcon.getIconHeight();
        this.moveTo = new Point(imageIcon.getIconWidth(), imageIcon.getIconHeight());
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                moveFrom = e.getPoint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                moveTo = e.getPoint();
                repaint();
            }
        });
    }

    public Rectangle getRegion() {
        Rectangle r = new Rectangle(moveFrom);
        r.add(moveTo);
        return r;
    }

    public void clear() {
        moveTo = new Point(baseImageWidth, baseImageHeight);
        moveFrom = new Point(0,0);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        Area outer = new Area(new Rectangle(0, 0, baseImageWidth, baseImageHeight));
        outer.subtract(new Area(getRegion()));
        g2d.setColor(new Color(0, 0, 0, 192));
        g2d.fill(outer);
    }
}
