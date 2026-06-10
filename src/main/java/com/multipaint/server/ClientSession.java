package com.multipaint.server;

import com.multipaint.protocol.Protocol;

import java.util.List;

/**
 * Tek istemci oturumu: protokol komutlarini isler.
 * Mesaj gonderimi {@link MessageSink} uzerinden (or. gRPC stream).
 */
public final class ClientSession {

    /** Satir protokolunu istemciye iletir. */
    @FunctionalInterface
    public interface MessageSink {
        void sendLine(String line);
    }

    private final PaintServer server;
    private final MessageSink sink;
    private volatile String username;
    private volatile CanvasRoom room;

    public ClientSession(PaintServer server, MessageSink sink) {
        this.server = server;
        this.sink = sink;
    }

    public void sendLine(String line) {
        sink.sendLine(line);
    }

    public void sendError(String msg) {
        sendLine(Protocol.ERROR + Protocol.SEP + msg);
    }

    /** Baglanti kapandiginda cagrilir. */
    public void onDisconnect() {
        cleanup();
    }

    public void handleLine(String line) throws Exception {
        if (line == null || line.isEmpty()) {
            return;
        }
        line = line.trim();
        if (line.isEmpty()) {
            return;
        }

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
                if (!server.createRoom(name)) {
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
