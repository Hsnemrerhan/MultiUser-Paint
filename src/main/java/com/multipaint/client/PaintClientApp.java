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
    private final JTextField newCanvasField = new JTextField(14);
    private final CanvasPanel canvas = new CanvasPanel(net);
    private final JLabel status = new JLabel("Hazir");
    private volatile boolean joinedRoom;

    public PaintClientApp() {
        super("MultiUser Paint");
        UiTheme.installFonts();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setBackground(UiTheme.BG_APP);
        setLayout(new BorderLayout(0, 0));

        JPanel top = buildTopBar();
        JPanel left = buildSidebar();

        JScrollPane scroll = UiTheme.wrapCanvas(canvas);
        scroll.setPreferredSize(new Dimension(920, 620));

        add(top, BorderLayout.NORTH);
        add(left, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
        pack();
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UiTheme.BORDER_SUBTLE),
                new EmptyBorder(4, 16, 4, 16)));

        JLabel logo = new JLabel("MultiUser Paint");
        logo.setFont(logo.getFont().deriveFont(Font.BOLD, 16f));
        logo.setForeground(new Color(0x1e, 0x29, 0x3b));
        p.add(logo);

        p.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 28));
        }});

        styleTopField(hostField);
        styleTopField(portField);
        styleTopField(userField);

        p.add(wrapField("Sunucu", hostField));
        p.add(wrapField("Port", portField));
        p.add(wrapField("Kullanici", userField));

        JButton connect = UiTheme.primaryButton("Baglan");
        connect.addActionListener(e -> connect());
        p.add(Box.createHorizontalStrut(4));
        p.add(connect);
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
        f.setForeground(new Color(0x1e, 0x29, 0x3b));
    }

    private JPanel buildSidebar() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setBackground(UiTheme.BG_SIDEBAR);
        col.setBorder(new EmptyBorder(16, 16, 16, 16));
        col.setPreferredSize(new Dimension(300, 0));

        col.add(UiTheme.sectionTitle("TUVALLAR"));
        col.add(Box.createVerticalStrut(6));

        UiTheme.styleSidebarField(newCanvasField);
        newCanvasField.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        newCanvasField.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(newCanvasField);
        col.add(Box.createVerticalStrut(8));

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
        canvasList.setSelectionBackground(new Color(0x31, 0x31, 0x45));
        canvasList.setSelectionForeground(Color.WHITE);
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
        sp.setPreferredSize(new Dimension(260, 120));
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, 140));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(refresh);
        col.add(Box.createVerticalStrut(6));
        col.add(create);
        col.add(Box.createVerticalStrut(8));
        col.add(sp);

        JButton join = UiTheme.primaryButton("Secili tuvala katil");
        join.setAlignmentX(Component.LEFT_ALIGNMENT);
        join.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        join.addActionListener(e -> joinSelected());
        col.add(Box.createVerticalStrut(10));
        col.add(join);

        col.add(Box.createVerticalStrut(22));
        col.add(UiTheme.sectionTitle("ARACLAR"));
        col.add(Box.createVerticalStrut(8));

        JToggleButton pen = new JToggleButton("Kalem", true);
        JToggleButton eraser = new JToggleButton("Silgi");
        JToggleButton sel = new JToggleButton("Secim");
        ButtonGroup g = new ButtonGroup();
        g.add(pen);
        g.add(eraser);
        g.add(sel);
        UiTheme.styleToggleTool(pen);
        UiTheme.styleToggleTool(eraser);
        UiTheme.styleToggleTool(sel);
        pen.setBackground(UiTheme.ACCENT);
        pen.setForeground(Color.WHITE);
        eraser.setBackground(UiTheme.BG_SIDEBAR_ELEVATED);
        eraser.setForeground(UiTheme.TEXT_ON_SIDEBAR);
        sel.setBackground(UiTheme.BG_SIDEBAR_ELEVATED);
        sel.setForeground(UiTheme.TEXT_ON_SIDEBAR);
        pen.addActionListener(e -> canvas.setTool(CanvasPanel.Tool.PEN));
        eraser.addActionListener(e -> canvas.setTool(CanvasPanel.Tool.ERASER));
        sel.addActionListener(e -> canvas.setTool(CanvasPanel.Tool.SELECT));

        JButton pickColor = UiTheme.ghostButton("Renk sec...");
        pickColor.setAlignmentX(Component.LEFT_ALIGNMENT);
        pickColor.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        pickColor.addActionListener(e -> {
            Color c = JColorChooser.showDialog(this, "Renk", Color.BLACK);
            if (c != null) {
                canvas.setPenColor(c);
            }
        });

        JSpinner brush = new JSpinner(new SpinnerNumberModel(3, 1, 80, 1));
        brush.setAlignmentX(Component.LEFT_ALIGNMENT);
        brush.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
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

        addFullWidth(col, pen);
        addFullWidth(col, eraser);
        addFullWidth(col, sel);
        col.add(Box.createVerticalStrut(6));
        addFullWidth(col, pickColor);
        col.add(UiTheme.mutedLabel("Firca kalinligi"));
        col.add(Box.createVerticalStrut(4));
        addFullWidth(col, brush);
        col.add(Box.createVerticalStrut(10));
        addFullWidth(col, copy);
        addFullWidth(col, cut);
        addFullWidth(col, paste);
        addFullWidth(col, clear);
        addFullWidth(col, savePng);
        col.add(Box.createVerticalStrut(12));
        JLabel hint = new JLabel("<html><div style='color:#64748b;font-size:11px'>Yapistirma hedefi:<br/>tuvalde sag tik</div></html>");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        col.add(hint);
        col.add(Box.createVerticalGlue());
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
        bar.setBorder(new EmptyBorder(8, 16, 8, 16));
        status.setForeground(new Color(0x94, 0xa3, 0xb8));
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
            status.setText("Baglandi — " + host + ":" + port + " · " + user);
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
                status.setText("Tuval: " + room + " — cizmeye baslayabilirsiniz");
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
