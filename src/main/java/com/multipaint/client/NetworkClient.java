package com.multipaint.client;

import com.multipaint.protocol.Protocol;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Ham TCP: {@link Socket}, {@link BufferedReader}, {@link PrintWriter} — harici socket kutuphanesi yok.
 */
public final class NetworkClient implements Closeable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread readerThread;
    private volatile boolean closed;

    public void connect(String host, int port) throws IOException {
        closeQuietly();
        socket = new Socket(host, port);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        closed = false;
    }

    public void sendLine(String line) {
        if (out == null) {
            return;
        }
        synchronized (out) {
            out.print(line);
            out.print('\n');
            out.flush();
        }
    }

    public void hello(String username) {
        sendLine(Protocol.HELLO + Protocol.SEP + username);
    }

    public void fileCreate(String name) {
        sendLine(Protocol.FILE_CREATE + Protocol.SEP + name);
    }

    public void fileListRequest() {
        sendLine(Protocol.FILE_LIST_REQUEST);
    }

    public void join(String canvasName) {
        sendLine(Protocol.JOIN + Protocol.SEP + canvasName);
    }

    public void drawPath(String points, String color, int brush) {
        sendLine(Protocol.DRAW_PATH + Protocol.SEP + points + Protocol.SEP + color + Protocol.SEP + brush);
    }

    public void cutArea(int x, int y, int w, int h) {
        sendLine(Protocol.CUT_AREA + Protocol.SEP + x + Protocol.SEP + y + Protocol.SEP + w + Protocol.SEP + h);
    }

    public void pasteArea(int x, int y, String base64Png) {
        sendLine(Protocol.PASTE_AREA + Protocol.SEP + x + Protocol.SEP + y + Protocol.SEP + base64Png);
    }

    public void clearCanvas() {
        sendLine(Protocol.CLEAR);
    }

    public void startReader(Consumer<String> onLine, Consumer<Exception> onError) {
        if (in == null) {
            return;
        }
        readerThread = new Thread(() -> {
            try {
                String line;
                while (!closed && (line = in.readLine()) != null) {
                    onLine.accept(line);
                }
            } catch (Exception e) {
                if (!closed) {
                    onError.accept(e);
                }
            }
        }, "paint-net-read");
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void closeQuietly() {
        closed = true;
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
        socket = null;
        in = null;
        out = null;
    }

    @Override
    public void close() {
        closeQuietly();
    }

    public static String encodePngBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        return java.util.Base64.getEncoder().encodeToString(bos.toByteArray());
    }
}
