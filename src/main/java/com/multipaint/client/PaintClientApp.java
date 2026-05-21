package com.multipaint.client;

import com.multipaint.protocol.Protocol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public final class PaintClientApp extends JFrame {
    private final NetworkClient net = new NetworkClient();
    private final DefaultListModel<String> canvasListModel = new DefaultListModel<>();
    private final JList<String> canvasList = new JList<>(canvasListModel);
    private final JTextField hostField = new JTextField("127.0.0.1", 10);
    private final JTextField portField = new JTextField("9100", 5);
    private final JTextField userField = new JTextField("Kullanici", 10);
    private final JTextField newCanvasField = UiTheme.placeholderField("Yeni canvas isminiz", 14);    private final CanvasPanel canvas = new CanvasPanel(net);
    private final JLabel status = new JLabel("Hazir");
    private final JPanel colorSwatch = new JPanel();
    private volatile boolean joinedRoom;

    public PaintClientApp() {
        super("MultiUser Paint");
        UiTheme.installFonts();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(UiTheme.BG_APP);
        setLayout(new BorderLayout(0, 0));

        JPanel top = buildTopBar();
        JScrollPane sidebarScroll = UiTheme.wrapSidebar(buildSidebar());

        JScrollPane scroll = UiTheme.wrapCanvas(canvas);
        scroll.setPreferredSize(new Dimension(920, 620));

        add(top, BorderLayout.NORTH);
        add(sidebarScroll, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(18, 0));
        p.setBackground(UiTheme.BG_PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UiTheme.BORDER_SUBTLE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel logo = new JLabel("MultiUser Paint");
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 16f));
        logo.setForeground(UiTheme.TEXT);
        p.add(logo, BorderLayout.WEST);

        styleTopField(hostField);
        styleTopField(portField);
        styleTopField(userField);

        JPanel connection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        connection.setOpaque(false);
        connection.add(wrapField("Sunucu", hostField));
        connection.add(wrapField("Port", portField));
        connection.add(wrapField("Kullanici", userField));

        JButton connect = UiTheme.primaryButton("Baglan");
        connect.addActionListener(e -> connect());
        connection.add(connect);
        p.add(connection, BorderLayout.CENTER);
        return p;
    }

    private static JPanel wrapField(String title, JComponent field) {
        JPanel w = new JPanel(new BorderLayout(0, 2));
        w.setOpaque(false);
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(10f));
        t.setForeground(UiTheme.TEXT_MUTED);
        w.add(t, BorderLayout.NORTH);
        w.add(field, BorderLayout.CENTER);
        return w;
    }

    private static void styleTopField(JTextField f) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_SUBTLE),
                new EmptyBorder(6, 8, 6, 8)));
        f.setBackground(Color.WHITE);
        f.setForeground(UiTheme.TEXT);
    }

    private JPanel buildSidebar() {
        JPanel col = UiTheme.createSidebarPanel();

        JPanel canvases = UiTheme.panelBlock();
        canvases.add(UiTheme.sectionTitle("TUVALLAR"));
        canvases.add(Box.createVerticalStrut(8));

        UiTheme.styleSidebarField(newCanvasField);
        newCanvasField.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        newCanvasField.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvases.add(newCanvasField);
        canvases.add(Box.createVerticalStrut(8));

        JButton refresh = UiTheme.ghostButton("Listeyi yenile");
        refresh.setAlignmentX(Component.LEFT_ALIGNMENT);
        refresh.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        refresh.addActionListener(e -> net.fileListRequest());

        JButton create = UiTheme.ghostButton("Yeni tuval olustur");
        create.setAlignmentX(Component.LEFT_ALIGNMENT);
        create.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        create.addActionListener(e -> {
            String name = newCanvasField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tuval adi girin.", "Olustur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            net.fileCreate(name);
            net.fileListRequest();
        });

        canvasList.setBackground(UiTheme.BG_SIDEBAR_ELEVATED);
        canvasList.setForeground(UiTheme.TEXT_ON_SIDEBAR);
        canvasList.setSelectionBackground(UiTheme.ACCENT_SOFT);
        canvasList.setSelectionForeground(UiTheme.ACCENT_HOVER);
        canvasList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        canvasList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
                if (!isSelected) {
                    c.setBackground(UiTheme.BG_SIDEBAR_ELEVATED);
                    c.setForeground(UiTheme.TEXT_ON_SIDEBAR);
                }
                return c;
            }
        });

        JScrollPane sp = UiTheme.wrapSidebarList(canvasList);
        sp.setPreferredSize(new Dimension(280, 120));
        sp.setMinimumSize(new Dimension(280, 280));
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 420));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        canvases.add(refresh);
        canvases.add(Box.createVerticalStrut(6));
        canvases.add(create);
        canvases.add(Box.createVerticalStrut(8));
        canvases.add(sp);

        JButton join = UiTheme.primaryButton("Secili tuvala katil");
        join.setAlignmentX(Component.LEFT_ALIGNMENT);
        join.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        join.addActionListener(e -> joinSelected());
        canvases.add(Box.createVerticalStrut(10));
        canvases.add(join);
        col.add(canvases);

        col.add(Box.createVerticalStrut(14));
        JPanel tools = UiTheme.panelBlock();
        tools.add(UiTheme.sectionTitle("ARACLAR"));
        tools.add(Box.createVerticalStrut(10));

        JToggleButton pen = UiTheme.iconToggleTool("✎", "Kalem");
        JToggleButton eraser = UiTheme.iconToggleTool("⌫", "Silgi");
        JToggleButton sel = UiTheme.iconToggleTool("▣", "Secim");

        ButtonGroup g = new ButtonGroup();
        g.add(pen);
        g.add(eraser);
        g.add(sel);

        pen.setSelected(true);

        pen.addActionListener(e -> {
            canvas.setTool(CanvasPanel.Tool.PEN);
            status.setText("Arac secildi: Kalem");
        });

        eraser.addActionListener(e -> {
            canvas.setTool(CanvasPanel.Tool.ERASER);
            status.setText("Arac secildi: Silgi");
        });

        sel.addActionListener(e -> {
            canvas.setTool(CanvasPanel.Tool.SELECT);
            status.setText("Arac secildi: Secim");
        });

        JPanel toolGrid = new JPanel(new GridLayout(1, 3, 8, 0));
        toolGrid.setOpaque(false);
        toolGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolGrid.setMaximumSize(new Dimension(Short.MAX_VALUE, 46));
        toolGrid.add(pen);
        toolGrid.add(eraser);
        toolGrid.add(sel);
        tools.add(toolGrid);

        JPanel toolNames = new JPanel(new GridLayout(1, 3, 8, 0));
        toolNames.setOpaque(false);
        toolNames.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolNames.setMaximumSize(new Dimension(Short.MAX_VALUE, 18));
        toolNames.add(UiTheme.smallCenterLabel("Kalem"));
        toolNames.add(UiTheme.smallCenterLabel("Silgi"));
        toolNames.add(UiTheme.smallCenterLabel("Secim"));
        tools.add(Box.createVerticalStrut(4));
        tools.add(toolNames);
        tools.add(Box.createVerticalStrut(12));
        JPanel colorRow = new JPanel(new BorderLayout(8, 0));
        colorRow.setOpaque(false);
        colorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorRow.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        colorSwatch.setBackground(Color.BLACK);
        colorSwatch.setPreferredSize(new Dimension(38, 34));
        colorSwatch.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_SUBTLE));
        colorRow.add(colorSwatch, BorderLayout.WEST);
        JButton pickColor = UiTheme.ghostButton("Renk sec...");
        pickColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Renk", Color.BLACK);
            if (c != null) {
                canvas.setPenColor(c);
                colorSwatch.setBackground(c);
            }
        });
        colorRow.add(pickColor, BorderLayout.CENTER);

        JSpinner brush = new JSpinner(new SpinnerNumberModel(3, 1, 80, 1));
        brush.setAlignmentX(Component.LEFT_ALIGNMENT);
        brush.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        brush.addChangeListener(e -> canvas.setBrushSize(((Number) brush.getValue()).intValue()));
        JComponent editor = brush.getEditor();
        if (editor instanceof JSpinner.DefaultEditor se) {
            se.getTextField().setBackground(UiTheme.BG_SIDEBAR_ELEVATED);
            se.getTextField().setForeground(UiTheme.TEXT_ON_SIDEBAR);
            se.getTextField().setBorder(UiTheme.fieldBorder());
        }

        JButton copy = UiTheme.ghostButton("Kopyala");
        JButton cut = UiTheme.ghostButton("Kes");
        JButton paste = UiTheme.ghostButton("Yapistir");
        JButton clear = UiTheme.ghostButton("Tuvali temizle");
        clear.setForeground(UiTheme.DANGER);
        JButton savePng = UiTheme.ghostButton("PNG kaydet");
        for (JButton b : new JButton[]{copy, cut, paste, clear, savePng}) {
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        }
        copy.addActionListener(e -> canvas.copySelection());
        cut.addActionListener(e -> canvas.cutSelection());
        paste.addActionListener(e -> canvas.pasteAtLastPoint());
        clear.addActionListener(e -> {
            if (joinedRoom && JOptionPane.showConfirmDialog(this, "Tum tuvali temizlemek istiyor musunuz?", "Temizle",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                canvas.requestClearAll();
            }
        });
        savePng.addActionListener(e -> canvas.savePng(this));

        tools.add(colorRow);
        tools.add(Box.createVerticalStrut(10));
        tools.add(UiTheme.mutedLabel("Firca kalinligi"));
        tools.add(Box.createVerticalStrut(4));
        addFullWidth(tools, brush);
        tools.add(Box.createVerticalStrut(4));
        addFullWidth(tools, copy);
        addFullWidth(tools, cut);
        addFullWidth(tools, paste);
        addFullWidth(tools, clear);
        addFullWidth(tools, savePng);
        tools.add(Box.createVerticalStrut(8));
        JLabel hint = new JLabel("<html><div style='color:#64748b;font-size:11px'>Yapistirma hedefi: tuvalde sag tik</div></html>");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        tools.add(hint);
        col.add(tools);
        return col;
    }

    private static void addFullWidth(JPanel col, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setMaximumSize(new Dimension(Short.MAX_VALUE, c.getPreferredSize().height + 4));
        col.add(c);
        col.add(Box.createVerticalStrut(6));
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UiTheme.STATUS_BG);
        bar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, UiTheme.BORDER_SUBTLE),
                new EmptyBorder(8, 16, 8, 16)));
        status.setForeground(UiTheme.TEXT_MUTED);
        status.setFont(status.getFont().deriveFont(12f));
        bar.add(status, BorderLayout.WEST);
        return bar;
    }

    private void connect() {
        try {
            String host = hostField.getText().trim();
            int port = Integer.parseInt(portField.getText().trim());
            String user = userField.getText().trim();
            if (user.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Kullanici adi girin.", "Baglanti", JOptionPane.WARNING_MESSAGE);
                return;
            }
            net.close();
            net.connect(host, port);
            net.hello(user);
            joinedRoom = false;
            canvas.setJoined(false);
            startReader();
            status.setText("Baglandi - " + host + ":" + port + " - " + user);
            net.fileListRequest();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Baglanti hatasi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startReader() {
        net.startReader(this::handleLine, ex -> SwingUtilities.invokeLater(() ->
                status.setText("Baglanti kesildi")));
    }

    private void handleLine(String line) {
        SwingUtilities.invokeLater(() -> {
            if (line.startsWith(Protocol.ERROR)) {
                String[] p = line.split("\\|", 2);
                String msg = p.length > 1 ? p[1] : line;
                JOptionPane.showMessageDialog(this, msg, "Sunucu hatasi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (line.startsWith(Protocol.FILE_LIST_RESPONSE)) {
                canvasListModel.clear();
                String[] p = line.split("\\|", 2);
                if (p.length == 2 && !p[1].isBlank()) {
                    String[] names = p[1].split(",");
                    for (String n : names) {
                        String t = n.trim();
                        if (!t.isEmpty()) {
                            canvasListModel.addElement(t);
                        }
                    }
                }
                return;
            }
            if (line.startsWith(Protocol.JOIN_OK)) {
                joinedRoom = true;
                canvas.setJoined(true);
                canvas.clearLocalOnly();
                String[] p = line.split("\\|", 2);
                String room = p.length > 1 ? p[1] : "";
                status.setText("Tuval: " + room + " - cizmeye baslayabilirsiniz");
                return;
            }
            if (line.startsWith(Protocol.DRAW_PATH)
                    || line.startsWith(Protocol.CUT_AREA)
                    || line.startsWith(Protocol.PASTE_AREA)
                    || line.startsWith(Protocol.CLEAR)) {
                if (joinedRoom) {
                    canvas.applyNetworkLine(line);
                }
            }
        });
    }

    private void joinSelected() {
        String sel = canvasList.getSelectedValue();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Listeden tuval secin.", "Katil", JOptionPane.WARNING_MESSAGE);
            return;
        }
        net.join(sel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new PaintClientApp().setVisible(true);
        });
    }
}
