package com.multipaint.client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/** Uygulama renkleri ve bilesen stilleri */
public final class UiTheme {
    public static final Color BG_APP = new Color(0xf3, 0xf6, 0xf9);
    public static final Color BG_PANEL = new Color(0xff, 0xff, 0xff);
    public static final Color BG_SIDEBAR = new Color(0xf8, 0xfa, 0xfc);
    public static final Color BG_SIDEBAR_ELEVATED = new Color(0xff, 0xff, 0xff);
    public static final Color ACCENT = new Color(0x25, 0x6d, 0x85);
    public static final Color ACCENT_HOVER = new Color(0x1f, 0x5b, 0x70);
    public static final Color ACCENT_SOFT = new Color(0xe2, 0xf2, 0xf6);
    public static final Color TEXT = new Color(0x1f, 0x29, 0x37);
    public static final Color TEXT_ON_SIDEBAR = TEXT;
    public static final Color TEXT_MUTED = new Color(0x64, 0x74, 0x8b);
    public static final Color BORDER_SUBTLE = new Color(0xd8, 0xe1, 0xea);
    public static final Color STATUS_BG = new Color(0xff, 0xff, 0xff);
    public static final Color DANGER = new Color(0xb4, 0x23, 0x18);

    private UiTheme() {}

    public static void installFonts() {
        Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
        UIManager.put("defaultFont", base);
    }

    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
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
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT),
                new EmptyBorder(8, 16, 8, 16)));
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
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setForeground(TEXT);
        b.setBackground(BG_SIDEBAR_ELEVATED);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(7, 12, 7, 12)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(0xf1, 0xf5, 0xf9));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(BG_SIDEBAR_ELEVATED);
            }
        });
        return b;
    }

    public static void styleToggleTool(JToggleButton t) {
        t.setOpaque(true);
        t.setContentAreaFilled(true);
        t.setFocusPainted(false);
        t.setForeground(TEXT);
        t.setBackground(BG_SIDEBAR_ELEVATED);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(8, 10, 8, 10)));
        t.addItemListener(e -> {
            if (t.isSelected()) {
                t.setBackground(ACCENT_SOFT);
                t.setForeground(ACCENT_HOVER);
                t.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT),
                        new EmptyBorder(8, 10, 8, 10)));
            } else {
                t.setBackground(BG_SIDEBAR_ELEVATED);
                t.setForeground(TEXT);
                t.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_SUBTLE),
                        new EmptyBorder(8, 10, 8, 10)));
            }
        });
    }

    public static JTextField placeholderField(String placeholder, int columns) {
    return new PlaceholderTextField(placeholder, columns);
}

public static JToggleButton iconToggleTool(String icon, String tooltip) {
    JToggleButton t = new JToggleButton(icon);
    t.setToolTipText(tooltip);
    t.setOpaque(true);
    t.setContentAreaFilled(true);
    t.setFocusPainted(false);
    t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
    t.setForeground(TEXT);
    t.setBackground(BG_SIDEBAR_ELEVATED);
    t.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SUBTLE),
            new EmptyBorder(7, 10, 7, 10)));
    t.setPreferredSize(new Dimension(54, 42));

    t.addItemListener(e -> {
        if (t.isSelected()) {
            t.setBackground(ACCENT_SOFT);
            t.setForeground(ACCENT_HOVER);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT, 2),
                    new EmptyBorder(6, 9, 6, 9)));
        } else {
            t.setBackground(BG_SIDEBAR_ELEVATED);
            t.setForeground(TEXT);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_SUBTLE),
                    new EmptyBorder(7, 10, 7, 10)));
        }
    });

    t.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseEntered(java.awt.event.MouseEvent e) {
            if (!t.isSelected()) {
                t.setBackground(new Color(0xf1, 0xf5, 0xf9));
            }
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent e) {
            if (!t.isSelected()) {
                t.setBackground(BG_SIDEBAR_ELEVATED);
            }
        }
    });

    return t;
}

public static JLabel smallCenterLabel(String text) {
    JLabel l = new JLabel(text, SwingConstants.CENTER);
    l.setForeground(TEXT_MUTED);
    l.setFont(l.getFont().deriveFont(Font.PLAIN, 10f));
    return l;
}


    /** BoxLayout ile doldurulacak; JScrollPane icinde dikey kaydirma icin Scrollable. */
    public static JPanel createSidebarPanel() {
        return new ScrollableSidebarPanel();
    }

    /** Sol panelin tamami (tuvaller + araclar) pencere kucukken dikey kaydirilir. */
    public static JScrollPane wrapSidebar(JPanel sidebar) {
        JScrollPane sp = new JScrollPane(sidebar);
        sp.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_SUBTLE));
        sp.setBackground(BG_SIDEBAR);
        sp.getViewport().setBackground(BG_SIDEBAR);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar vBar = sp.getVerticalScrollBar();
        vBar.setUnitIncrement(20);
        vBar.setBlockIncrement(80);
        vBar.setPreferredSize(new Dimension(14, 0));
        vBar.setUI(new SoftScrollBarUI());
        sp.setPreferredSize(new Dimension(336, 0));
        return sp;
    }

    public static JScrollPane wrapSidebarList(JComponent inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        sp.getViewport().setBackground(BG_SIDEBAR_ELEVATED);
        inner.setBackground(BG_SIDEBAR_ELEVATED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getVerticalScrollBar().setUI(new SoftScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SoftScrollBarUI());
        return sp;
    }

    public static JScrollPane wrapCanvas(JComponent inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(10, 10, 10, 10)));
        sp.setBackground(BG_APP);
        sp.getViewport().setBackground(BG_APP);
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

    public static JPanel panelBlock() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(12, 12, 12, 12)));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    private static final class PlaceholderTextField extends JTextField {
    private final String placeholder;

    private PlaceholderTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(0x94, 0xa3, 0xb8));
            g2.setFont(getFont().deriveFont(Font.PLAIN));

            Insets insets = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int x = insets.left;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(placeholder, x, y);
            g2.dispose();
        }
    }
}


    /**
     * JScrollPane icinde icerik yuksekligi viewport'a ezilmesin; asagi kaydirma calissin.
     */
    private static final class ScrollableSidebarPanel extends JPanel implements Scrollable {
        ScrollableSidebarPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(BG_SIDEBAR);
            setBorder(new EmptyBorder(16, 16, 16, 16));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(300, 500);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class SoftScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(0x94, 0xa3, 0xb8);
            thumbHighlightColor = thumbColor;
            thumbDarkShadowColor = thumbColor;
            thumbLightShadowColor = thumbColor;
            trackColor = new Color(0xe2, 0xe8, 0xf0);
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(8, 48);
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
