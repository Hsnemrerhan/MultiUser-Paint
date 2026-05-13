package com.multipaint.client;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Tek katmanli tuval uzerinde yerel cizim yardimcilari (sunucu ile ayni semantik). */
public final class DrawOps {
    private DrawOps() {}

    public static void applyDrawPath(BufferedImage surface, String points, String colorName, String widthStr) {
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

    public static void applyClearRect(BufferedImage surface, int x, int y, int rw, int rh) {
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE);
            g.fillRect(x, y, rw, rh);
        } finally {
            g.dispose();
        }
    }

    public static void applyPastePng(BufferedImage surface, int dx, int dy, byte[] pngBytes) throws Exception {
        BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(pngBytes));
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

    public static void applyFullClear(BufferedImage surface) {
        Graphics2D g = surface.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, surface.getWidth(), surface.getHeight());
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
}
