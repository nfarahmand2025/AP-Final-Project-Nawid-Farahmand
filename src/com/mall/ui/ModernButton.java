package com.mall.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModernButton extends JButton {
    private int radius = 12;
    private Color currentBg = UIConstants.PRIMARY_COLOR;

    public ModernButton(String text) {
        super(text);
        // 1. Remove default Swing artifacts
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        // 2. Apply typography and spacing
        setFont(UIConstants.BUTTON_FONT);
        setForeground(UIConstants.SURFACE_COLOR);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(getPreferredSize().width, 45));
        setBorder(new EmptyBorder(10, 25, 10, 25));

        // 3. Interaction Logic
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                currentBg = UIConstants.PRIMARY_COLOR;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR.darker();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                currentBg = UIConstants.HOVER_COLOR;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Enable High-Quality Rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draw the rounded background
        g2.setColor(currentBg);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

        // Draw text manually to ensure it centers perfectly
        FontMetrics metrics = g2.getFontMetrics(getFont());
        int x = (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

        g2.setColor(getForeground());
        g2.setFont(getFont());
        g2.drawString(getText(), x, y);

        g2.dispose();
    }
}