package com.mall.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom modern-styled button with:
 * - Rounded corners
 * - Hover and press effects
 * - Manual text centering
 * - Disabled state support
 */
public class ModernButton extends JButton {
    // Corner radius for rounded rectangle background
    private int radius = 12;
    // Current background color, updated on hover/press
    private Color currentBg = UIConstants.PRIMARY_COLOR;
    // Track whether the button is disabled
    private boolean disabled;

    /**
     * Constructor: sets up typography, spacing, cursor, and interaction logic.
     *
     * @param text the button label
     */
    public ModernButton(String text) {
        super(text);

        // 1. Remove default Swing visuals (background, focus, border)
        disabled = false;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        // 2. Typography and spacing
        setFont(UIConstants.BUTTON_FONT);
        setForeground(UIConstants.SURFACE_COLOR);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 45));
        setBorder(new EmptyBorder(10, 25, 10, 25));

        // 3. Interaction logic: hover, press, release effects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR; // hover color on enter
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentBg = UIConstants.PRIMARY_COLOR; // revert on exit
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR.darker(); // pressed state
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR; // release hover state
                repaint();
            }
        });
    }

    /**
     * Disable the button visually by changing background and blocking interactions.
     */
    public void disable(){
        disabled = true;
    }

    /**
     * Custom painting of the button.
     * - Draws rounded rectangle background
     * - Centers text manually
     * - Applies high-quality rendering hints
     *
     * @param g the Graphics object
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Update background if disabled
        if (disabled){
            currentBg = new Color(131, 1, 25, 255);  ;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        // Enable high-quality rendering for smooth edges and text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw rounded rectangle background
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

        // Draw text manually, centered horizontally and vertically
        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);

        g2.dispose(); // release Graphics2D resources
    }
}
