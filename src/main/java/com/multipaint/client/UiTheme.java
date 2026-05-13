package com.multipaint.client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/** Uygulama renkleri ve bilesen stilleri */
public final class UiTheme {
    public static final Color BG_APP = new Color(0xf8, 0xfa, 0xfc);
    public static final Color BG_SIDEBAR = new Color(0x1e, 0x1e, 0x2e);
    public static final Color BG_SIDEBAR_ELEVATED = new Color(0x25, 0x25, 0x35);
    public static final Color ACCENT = new Color(0x63, 0x66, 0xf1);
    public static final Color ACCENT_HOVER = new Color(0x81, 0x83, 0xf4);
    public static final Color TEXT_ON_SIDEBAR = new Color(0xe2, 0xe8, 0xf0);
    public static final Color TEXT_MUTED = new Color(0x94, 0xa3, 0xb8);
    public static final Color BORDER_SUBTLE = new Color(0xe2, 0xe8, 0xf0);
    public static final Color STATUS_BG = new Color(0x0f, 0x17, 0x2a);

    private UiTheme() {}

    public static void installFonts() {
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        UIManager.put("defaultFont", base);
    }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x33, 0x33, 0x44)),
                new EmptyBorder(6, 8, 6, 8));
    }

    public static void styleSidebarField(JTextField f) {
        f.setBorder(fieldBorder());
        f.setBackground(BG_SIDEBAR_ELEVATED);
        f.setForeground(TEXT_ON_SIDEBAR);
        f.setCaretColor(TEXT_ON_SIDEBAR);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(ACCENT);
            }
        });
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setForeground(TEXT_ON_SIDEBAR);
        b.setBackground(BG_SIDEBAR_ELEVATED);
        b.setBorder(new EmptyBorder(7, 12, 7, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleToggleTool(JToggleButton t) {
        t.setOpaque(true);
        t.setFocusPainted(false);
        t.setForeground(TEXT_ON_SIDEBAR);
        t.setBackground(BG_SIDEBAR_ELEVATED);
        t.setBorder(new EmptyBorder(8, 10, 8, 10));
        t.addItemListener(e -> {
            if (t.isSelected()) {
                t.setBackground(ACCENT);
                t.setForeground(Color.WHITE);
            } else {
                t.setBackground(BG_SIDEBAR_ELEVATED);
                t.setForeground(TEXT_ON_SIDEBAR);
            }
        });
    }

    public static JScrollPane wrapSidebarList(JComponent inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0x33, 0x33, 0x44)));
        sp.getViewport().setBackground(BG_SIDEBAR_ELEVATED);
        inner.setBackground(BG_SIDEBAR_ELEVATED);
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        return sp;
    }

    public static JScrollPane wrapCanvas(JComponent inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(4, 4, 4, 4)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    public static JLabel mutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static final class DarkScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x4a, 0x4a, 0x5e);
            thumbHighlightColor = thumbColor;
            thumbDarkShadowColor = thumbColor;
            thumbLightShadowColor = thumbColor;
            trackColor = BG_SIDEBAR_ELEVATED;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zeroButton();
        }

        private static JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
    }
}
