package com.matthewjackswann;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

public class RestrictedRobot extends Robot {

    public static final double IMAGE_SCALE = 1.5;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    private RestrictedRobot(GraphicsDevice screen, Rectangle bounds) throws AWTException {
        super(screen);
        assert(bounds.x >= 0 && bounds.y >= 0 && bounds.width >= 1 && bounds.height >= 1);
        this.minX = bounds.x;
        this.minY = bounds.y;
        this.maxX = bounds.width + minX;
        this.maxY = bounds.height + minY;
    }

    @Override
    public synchronized void mouseMove(int x, int y) {
        assert (x >= 0 && y >= 0);
        assert (minX + x <= maxX && minY + y <= maxY);
        super.mouseMove(minX + x, minY + y);
    }

    @Override
    public synchronized Color getPixelColor(int x, int y) {
        assert (x >= 0 && y >= 0);
        assert (minX + x <= maxX && minY + y <= maxY);
        return super.getPixelColor(minX + x, minY + y);
    }

    public synchronized BufferedImage createScreenCapture() {
        return super.createScreenCapture(new Rectangle(minX, minY, maxX - minX, maxY - minY));
    }

    @Override
    public synchronized BufferedImage createScreenCapture(Rectangle bounds) {
        if (bounds.x < 0 || bounds.y < 0 || bounds.width < 1 || bounds.height < 1
            || (minX + bounds.x + bounds.width) > maxX || (minY + bounds.y + bounds.height) > maxY) {
            return null;
        }
        Rectangle r = new Rectangle(
                bounds.x + minX,bounds.y + minY,
                bounds.width, bounds.height);
        return super.createScreenCapture(r);
    }

    public static RestrictedRobot getRestrictedRobot() {
        GraphicsDevice[] screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        if (screens.length == 0) throw new RuntimeException("No GraphicsDevice(s) found");
        GraphicsDevice screen;
        if (screens.length == 1) screen = screens[0];
        else screen = getGraphicsDevice(screens);
        if (screen == null) throw new RuntimeException("Selected screen is null");
        Rectangle restrictedTo = restrictRange(screen);
        if (restrictedTo == null) throw new RuntimeException("Restriction not given");
        Rectangle screenRectangle = screen.getDefaultConfiguration().getBounds();
        try {
            return new RestrictedRobot(screen,
                    new Rectangle(
                            screenRectangle.x + restrictedTo.x,
                            screenRectangle.y + restrictedTo.y,
                            restrictedTo.width,
                            restrictedTo.height));
        } catch (AWTException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static GraphicsDevice getGraphicsDevice(GraphicsDevice[] screens) {
        JFrame frame = new JFrame("Choose Display");
        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());
        frame.setLayout(new BorderLayout());
        frame.add(header, BorderLayout.PAGE_START);

        final boolean[] waitSelected = {true, false};

        ImageIcon[] screenViews = new ImageIcon[screens.length];
        for (int index = 0; index < screens.length; index++) {
            GraphicsDevice screen = screens[index];
            try {
                BufferedImage image = new Robot(screen).createScreenCapture(screen.getDefaultConfiguration().getBounds());
                screenViews[index] = new ImageIcon(image.getScaledInstance(
                        (int) Math.floor(image.getWidth()/ IMAGE_SCALE),
                        (int) Math.floor(image.getHeight()/ IMAGE_SCALE), Image.SCALE_DEFAULT));
            } catch (Exception ignored) {}
        }

        // sets up the multiple choice
        JComboBox<GraphicsDevice> screenChoice = new JComboBox<>(screens);
        header.add(screenChoice);

        // sets up the button
        JButton button = new JButton("Select");
        button.addActionListener(actionEvent -> {
            waitSelected[1] = true;
            frame.dispose();
        });
        header.add(button);

//        // sets up screen viewer
        JLabel label = new JLabel(screenViews[screenChoice.getSelectedIndex()]);
        frame.add(label, BorderLayout.CENTER);
        screenChoice.addActionListener(actionEvent -> label.setIcon(screenViews[screenChoice.getSelectedIndex()]));

        // sets up display
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                waitSelected[0] = false;
            }
        });
        frame.pack();
        frame.setVisible(true);

        // waits to get a response
        while (waitSelected[0]) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {}
        }

        if (!waitSelected[1]) throw new RuntimeException("No screen selected"); // if no screen is selected throws error
        return (GraphicsDevice) screenChoice.getSelectedItem();
    }

     private static Rectangle restrictRange(GraphicsDevice screen) {
        JFrame frame = new JFrame("Restrict Display View");
        JPanel header = new JPanel();
        header.setLayout(new FlowLayout());
        frame.setLayout(new BorderLayout());
        frame.add(header, BorderLayout.PAGE_START);

        final boolean[] waitSelected = {true, false};

        ImageIcon screenView = null;
        try {
            BufferedImage image = new Robot(screen).createScreenCapture(screen.getDefaultConfiguration().getBounds());
            screenView = new ImageIcon(image.getScaledInstance(
                    (int) Math.floor(image.getWidth()/ IMAGE_SCALE),
                    (int) Math.floor(image.getHeight()/ IMAGE_SCALE), Image.SCALE_DEFAULT));
        } catch (Exception ignored) {}
        if (screenView == null) throw new RuntimeException("Couldn't get screen capture of display");

        ImageRegionSelector regionSelector = new ImageRegionSelector(screenView);
        frame.add(regionSelector, BorderLayout.CENTER);

        JButton clear = new JButton("Clear");
        clear.addActionListener(actionEvent -> regionSelector.clear());
        header.add(clear);
        JButton confirm = new JButton("Confirm");
        confirm.addActionListener(actionEvent -> {
            waitSelected[1] = true;
            frame.dispose();
        });
        header.add(confirm);

        // sets up display
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                waitSelected[0] = false;
            }
        });
        frame.pack();
        frame.setVisible(true);

        // waits to get a response
        while (waitSelected[0]) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {}
        }
        if (!waitSelected[1]) return null;
        Rectangle region = regionSelector.getRegion();
        return new Rectangle((int) Math.floor(region.x * IMAGE_SCALE),
                (int) Math.floor(region.y * IMAGE_SCALE),
                (int) Math.floor(region.width * IMAGE_SCALE),
                (int) Math.floor(region.height * IMAGE_SCALE));
    }
}
