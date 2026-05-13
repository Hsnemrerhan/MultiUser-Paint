package com.multipaint.client;

import com.multipaint.protocol.Protocol;
import com.multipaint.server.CanvasRoom;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class CanvasPanel extends JPanel {
    public enum Tool {
        PEN, ERASER, SELECT
    }

    private final BufferedImage surface;
    private final NetworkClient net;
    private volatile boolean joined;

    private Tool tool = Tool.PEN;
    private Color penColor = Color.BLACK;
    private int brushSize = 3;

    private final List<Point> stroke = new ArrayList<>();
    private Point selectStart;
    private Rectangle selection;
    private BufferedImage clipboard;
    private Point lastCanvasPoint = new Point(40, 40);

    public CanvasPanel(NetworkClient net) {
        this.net = net;
        this.surface = new BufferedImage(CanvasRoom.DEFAULT_WIDTH, CanvasRoom.DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        DrawOps.applyFullClear(surface);
        setPreferredSize(new Dimension(CanvasRoom.DEFAULT_WIDTH, CanvasRoom.DEFAULT_HEIGHT));
        setBackground(Color.LIGHT_GRAY);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!joined) {
                    return;
                }
                Point p = toImage(e);
                lastCanvasPoint = p;
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastCanvasPoint = p;
                    return;
                }
                if (tool == Tool.SELECT) {
                    selectStart = p;
                    selection = null;
                } else {
                    stroke.clear();
                    stroke.add(p);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!joined) {
                    return;
                }
                Point p = toImage(e);
                lastCanvasPoint = p;
                if (tool == Tool.SELECT) {
                    if (selectStart != null) {
                        int x = Math.min(selectStart.x, p.x);
                        int y = Math.min(selectStart.y, p.y);
                        int w = Math.abs(selectStart.x - p.x);
                        int h = Math.abs(selectStart.y - p.y);
                        selection = new Rectangle(x, y, w, h);
                    }
                    repaint();
                    return;
                }
                stroke.add(p);
                if (stroke.size() >= 2) {
                    Point a = stroke.get(stroke.size() - 2);
                    Point b = stroke.get(stroke.size() - 1);
                    String colorName = tool == Tool.ERASER ? "eraser" : colorToProtocol(penColor);
                    DrawOps.applyDrawPath(surface, a.x + "," + a.y + ";" + b.x + "," + b.y, colorName, Integer.toString(brushSize));
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!joined) {
                    return;
                }
                Point p = toImage(e);
                lastCanvasPoint = p;
                if (tool == Tool.SELECT) {
                    if (selectStart != null) {
                        int x = Math.min(selectStart.x, p.x);
                        int y = Math.min(selectStart.y, p.y);
                        int w = Math.abs(selectStart.x - p.x);
                        int h = Math.abs(selectStart.y - p.y);
                        selection = new Rectangle(x, y, w, h);
                    }
                    selectStart = null;
                    repaint();
                    return;
                }
                if (stroke.isEmpty()) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < stroke.size(); i++) {
                    Point pt = stroke.get(i);
                    if (i > 0) {
                        sb.append(';');
                    }
                    sb.append(pt.x).append(',').append(pt.y);
                }
                String colorName = tool == Tool.ERASER ? "eraser" : colorToProtocol(penColor);
                net.drawPath(sb.toString(), colorName, brushSize);
                stroke.clear();
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private static Point toImage(MouseEvent e) {
        int x = Math.max(0, Math.min(CanvasRoom.DEFAULT_WIDTH - 1, e.getX()));
        int y = Math.max(0, Math.min(CanvasRoom.DEFAULT_HEIGHT - 1, e.getY()));
        return new Point(x, y);
    }

    private static String colorToProtocol(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
        if (!joined) {
            DrawOps.applyFullClear(surface);
            selection = null;
            clipboard = null;
        }
        repaint();
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    public void setPenColor(Color penColor) {
        this.penColor = penColor;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = Math.max(1, Math.min(80, brushSize));
    }

    public void applyNetworkLine(String line) {
        try {
            if (line.startsWith(Protocol.DRAW_PATH)) {
                String[] p = line.split("\\|", 4);
                if (p.length >= 4) {
                    DrawOps.applyDrawPath(surface, p[1], p[2], p[3]);
                }
            } else if (line.startsWith(Protocol.CUT_AREA)) {
                String[] p = line.split("\\|", 5);
                if (p.length >= 5) {
                    int x = Integer.parseInt(p[1]);
                    int y = Integer.parseInt(p[2]);
                    int w = Integer.parseInt(p[3]);
                    int h = Integer.parseInt(p[4]);
                    DrawOps.applyClearRect(surface, x, y, w, h);
                }
            } else if (line.startsWith(Protocol.PASTE_AREA)) {
                String[] p = line.split("\\|", 4);
                if (p.length >= 4) {
                    int dx = Integer.parseInt(p[1]);
                    int dy = Integer.parseInt(p[2]);
                    byte[] raw = Base64.getDecoder().decode(p[3]);
                    DrawOps.applyPastePng(surface, dx, dy, raw);
                }
            } else if (line.startsWith(Protocol.CLEAR)) {
                DrawOps.applyFullClear(surface);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Tuval guncelleme", JOptionPane.WARNING_MESSAGE);
        }
        repaint();
    }

    public void clearLocalOnly() {
        DrawOps.applyFullClear(surface);
        repaint();
    }

    public void copySelection() {
        if (selection == null || selection.width <= 0 || selection.height <= 0) {
            JOptionPane.showMessageDialog(this, "Once alan secin.", "Kopyala", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        clipboard = copySubimage(surface, selection.x, selection.y, selection.width, selection.height);
    }

    public void cutSelection() {
        if (selection == null || selection.width <= 0 || selection.height <= 0) {
            JOptionPane.showMessageDialog(this, "Once alan secin.", "Kes", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int x = selection.x;
        int y = selection.y;
        int w = selection.width;
        int h = selection.height;
        clipboard = copySubimage(surface, x, y, w, h);
        net.cutArea(x, y, w, h);
        DrawOps.applyClearRect(surface, x, y, w, h);
        repaint();
    }

    private static BufferedImage copySubimage(BufferedImage src, int x, int y, int w, int h) {
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        try {
            g.drawImage(src.getSubimage(x, y, w, h), 0, 0, null);
        } finally {
            g.dispose();
        }
        return dest;
    }

    public void pasteAtLastPoint() {
        if (clipboard == null) {
            JOptionPane.showMessageDialog(this, "Pano bos. Once kopyalayin veya kesin.", "Yapistir", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            String b64 = NetworkClient.encodePngBase64(clipboard);
            int x = lastCanvasPoint.x;
            int y = lastCanvasPoint.y;
            DrawOps.applyPastePng(surface, x, y, java.util.Base64.getDecoder().decode(b64));
            net.pasteArea(x, y, b64);
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Yapistir", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void requestClearAll() {
        net.clearCanvas();
        DrawOps.applyFullClear(surface);
        repaint();
    }

    public void savePng(Component parent) {
        JFileChooser ch = new JFileChooser();
        ch.setSelectedFile(new java.io.File("canvas.png"));
        if (ch.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        java.io.File f = ch.getSelectedFile();
        try {
            javax.imageio.ImageIO.write(surface, "png", f);
            JOptionPane.showMessageDialog(parent, "Kaydedildi: " + f.getAbsolutePath(), "PNG", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, ex.getMessage(), "PNG", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(surface, 0, 0, null);
        if (selection != null && tool == Tool.SELECT) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 120, 255, 180));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[]{6, 6}, 0));
            g2.drawRect(selection.x, selection.y, selection.width, selection.height);
            g2.dispose();
        }
    }
}
