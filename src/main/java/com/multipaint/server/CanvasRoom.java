package com.multipaint.server;

import com.multipaint.protocol.Protocol;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tek tuval katmanı: sunucu tarafında otoriter görüntü + yeniden oynatma günlüğü.
 */
public final class CanvasRoom {
    public static final int DEFAULT_WIDTH = 1200;
    public static final int DEFAULT_HEIGHT = 800;

    private final String name;
    private final BufferedImage surface;
    private final List<String> history = new CopyOnWriteArrayList<>();
    private final List<ClientSession> members = new CopyOnWriteArrayList<>();

    public CanvasRoom(String name) {
        this.name = name;
        this.surface = new BufferedImage(DEFAULT_WIDTH, DEFAULT_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(255, 255, 255, 255));
            g.fillRect(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        } finally {
            g.dispose();
        }
    }

    public String getName() {
        return name;
    }

    public int getWidth() {
        return DEFAULT_WIDTH;
    }

    public int getHeight() {
        return DEFAULT_HEIGHT;
    }

    public List<String> snapshotHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    public void addMember(ClientSession s) {
        members.add(s);
    }

    public void removeMember(ClientSession s) {
        members.remove(s);
    }

    public void broadcastExcept(String line, ClientSession except) {
        for (ClientSession m : members) {
            if (m != except) {
                m.sendLine(line);
            }
        }
    }

    /**
     * Komutu günlüğe ekler, tuvali günceller, diğer üyelere iletir (gönderen hariç).
     */
    public synchronized void handleClientMessage(String line, ClientSession sender) throws Exception {
        if (line == null || line.isEmpty()) {
            return;
        }
        String[] head = line.split("\\|", 2);
        String cmd = head[0];
        switch (cmd) {
            case Protocol.DRAW_PATH -> {
                String[] p = line.split("\\|", 4);
                if (p.length < 4) {
                    sender.sendError("DRAW_PATH eksik alan");
                    return;
                }
                applyDrawPath(p[1], p[2], p[3]);
                history.add(line);
                broadcastExcept(line, sender);
            }
            case Protocol.CUT_AREA -> {
                String[] p = line.split("\\|", 5);
                if (p.length < 5) {
                    sender.sendError("CUT_AREA eksik alan");
                    return;
                }
                int x = Integer.parseInt(p[1]);
                int y = Integer.parseInt(p[2]);
                int w = Integer.parseInt(p[3]);
                int h = Integer.parseInt(p[4]);
                applyClearRect(x, y, w, h);
                history.add(line);
                broadcastExcept(line, sender);
            }
            case Protocol.PASTE_AREA -> {
                String[] p = line.split("\\|", 4);
                if (p.length < 4) {
                    sender.sendError("PASTE_AREA eksik alan (beklenen: x|y|base64Png)");
                    return;
                }
                int dx = Integer.parseInt(p[1]);
                int dy = Integer.parseInt(p[2]);
                String b64 = p[3];
                applyPasteImage(dx, dy, b64);
                history.add(line);
                broadcastExcept(line, sender);
            }
            case Protocol.CLEAR -> {
                applyFullClear();
                history.add(line);
                broadcastExcept(line, sender);
            }
            default -> sender.sendError("Bilinmeyen komut: " + cmd);
        }
    }

    private void applyDrawPath(String points, String colorName, String widthStr) {
        float w = Float.parseFloat(widthStr);
        boolean eraser = "eraser".equalsIgnoreCase(colorName) || "silgi".equalsIgnoreCase(colorName);
        Color stroke = eraser ? Color.WHITE : parseColor(colorName);

        Graphics2D g = surface.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.SrcOver);
            g.setColor(stroke);
            g.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            String[] segs = points.split(";");
            int[][] pts = new int[segs.length][2];
            for (int i = 0; i < segs.length; i++) {
                String[] xy = segs[i].split(",");
                pts[i][0] = Integer.parseInt(xy[0].trim());
                pts[i][1] = Integer.parseInt(xy[1].trim());
            }
            if (pts.length == 1) {
                g.fillOval(pts[0][0] - (int) (w / 2), pts[0][1] - (int) (w / 2), (int) w, (int) w);
            } else {
                for (int i = 1; i < pts.length; i++) {
                    g.drawLine(pts[i - 1][0], pts[i - 1][1], pts[i][0], pts[i][1]);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private static Color parseColor(String name) {
        try {
            return switch (name.toLowerCase()) {
                case "red" -> Color.RED;
                case "green" -> Color.GREEN;
                case "blue" -> Color.BLUE;
                case "black" -> Color.BLACK;
                case "white" -> Color.WHITE;
                case "yellow" -> Color.YELLOW;
                case "orange" -> Color.ORANGE;
                case "cyan" -> Color.CYAN;
                case "magenta" -> Color.MAGENTA;
                case "gray", "grey" -> Color.GRAY;
                default -> Color.decode(name.startsWith("#") ? name : "#" + name);
            };
        } catch (Exception e) {
            return Color.BLACK;
        }
    }

    private void applyClearRect(int x, int y, int w, int h) {
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(new Color(255, 255, 255, 255));
            g.fillRect(x, y, w, h);
        } finally {
            g.dispose();
        }
    }

    private void applyPasteImage(int dx, int dy, String b64) throws Exception {
        byte[] raw = Base64.getDecoder().decode(b64);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
        if (img == null) {
            throw new IllegalArgumentException("PNG okunamadi");
        }
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.SrcOver);
            g.drawImage(img, dx, dy, null);
        } finally {
            g.dispose();
        }
    }

    private void applyFullClear() {
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, surface.getWidth(), surface.getHeight());
        } finally {
            g.dispose();
        }
    }

    /** PNG olarak otoriter anlık görüntü (isteğe bağlı kullanım) */
    public synchronized byte[] encodePng() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(surface, "png", bos);
        return bos.toByteArray();
    }
}
