package com.multipaint.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PaintServer {
    private final int port;
    private final Map<String, CanvasRoom> rooms = new ConcurrentHashMap<>();
    private volatile boolean running;

    public PaintServer(int port) {
        this.port = port;
    }

    public boolean createRoom(String name) {
        CanvasRoom existing = rooms.putIfAbsent(name, new CanvasRoom(name));
        return existing == null;
    }

    public CanvasRoom getRoom(String name) {
        return rooms.get(name);
    }

    public String listRoomsCsv() {
        List<String> names = new ArrayList<>(rooms.keySet());
        Collections.sort(names);
        return String.join(",", names);
    }

    public void start() throws Exception {
        running = true;
        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("Paint sunucusu dinleniyor: " + port);
            while (running) {
                Socket s = ss.accept();
                ClientSession session = new ClientSession(s, this);
                new Thread(session, "client-" + s.getRemoteSocketAddress()).start();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int p = args.length > 0 ? Integer.parseInt(args[0]) : 9100;
        new PaintServer(p).start();
    }
}
