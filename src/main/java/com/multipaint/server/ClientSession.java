package com.multipaint.server;

import com.multipaint.protocol.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class ClientSession implements Runnable {
    private final Socket socket;
    private final PaintServer server;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile String username;
    private volatile CanvasRoom room;

    public ClientSession(Socket socket, PaintServer server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    public void sendLine(String line) {
        synchronized (out) {
            out.print(line);
            out.print('\n');
            out.flush();
        }
    }

    public void sendError(String msg) {
        sendLine(Protocol.ERROR + Protocol.SEP + msg);
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                handleLine(line);
            }
        } catch (Exception e) {
            // baglanti koptu
        } finally {
            cleanup();
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleLine(String line) throws Exception {
        String[] parts = line.split("\\|", 3);
        String cmd = parts[0];
        switch (cmd) {
            case Protocol.HELLO -> {
                if (parts.length < 2) {
                    sendError("HELLO icin kullanici adi gerekli");
                    return;
                }
                String u = parts[1].trim();
                if (u.isEmpty()) {
                    sendError("Gecersiz kullanici adi");
                    return;
                }
                if (u.indexOf('|') >= 0) {
                    sendError("Kullanici adi | karakteri icermemeli");
                    return;
                }
                username = u;
            }
            case Protocol.FILE_CREATE -> {
                if (username == null) {
                    sendError("Once HELLO gonderin");
                    return;
                }
                if (parts.length < 2 || parts[1].isBlank()) {
                    sendError("Canvas adi gerekli");
                    return;
                }
                String name = parts[1].trim();
                if (name.indexOf('|') >= 0) {
                    sendError("Tuval adi | karakteri icermemeli");
                    return;
                }
                if (server.createRoom(name)) {
                    // bos
                } else {
                    sendError("Canvas zaten var: " + name);
                }
            }
            case Protocol.FILE_LIST_REQUEST -> {
                sendLine(Protocol.FILE_LIST_RESPONSE + Protocol.SEP + server.listRoomsCsv());
            }
            case Protocol.JOIN -> {
                if (username == null) {
                    sendError("Once HELLO gonderin");
                    return;
                }
                if (parts.length < 2) {
                    sendError("JOIN icin canvas adi gerekli");
                    return;
                }
                String name = parts[1].trim();
                if (name.indexOf('|') >= 0) {
                    sendError("Tuval adi | karakteri icermemeli");
                    return;
                }
                CanvasRoom r = server.getRoom(name);
                if (r == null) {
                    sendError("Canvas bulunamadi: " + name);
                    return;
                }
                if (room != null) {
                    room.removeMember(this);
                }
                room = r;
                room.addMember(this);
                sendLine(Protocol.JOIN_OK + Protocol.SEP + name);
                List<String> hist = room.snapshotHistory();
                for (String h : hist) {
                    sendLine(h);
                }
            }
            case Protocol.DRAW_PATH, Protocol.CUT_AREA, Protocol.PASTE_AREA, Protocol.CLEAR -> {
                if (room == null) {
                    sendError("Once bir canvas'a JOIN yapin");
                    return;
                }
                room.handleClientMessage(line, this);
            }
            default -> sendError("Bilinmeyen veya bu baglamda gecersiz komut: " + cmd);
        }
    }

    private void cleanup() {
        if (room != null) {
            room.removeMember(this);
            room = null;
        }
    }
}
