package com.multipaint.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class PaintServer {
    private final int port;
    private final Map<String, CanvasRoom> rooms = new ConcurrentHashMap<>();
    private Server grpcServer;

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
        grpcServer = ServerBuilder.forPort(port)
                .addService(new PaintGrpcService(this))
                .build()
                .start();
        System.out.println("gRPC sunucusu dinleniyor: " + port);
        System.out.println("Durdurmak icin Ctrl+C");
        grpcServer.awaitTermination();
    }

    public void stop() throws Exception {
        if (grpcServer != null) {
            grpcServer.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws Exception {
        int p = args.length > 0 ? Integer.parseInt(args[0]) : 9100;
        PaintServer server = new PaintServer(p);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (Exception ignored) {
            }
        }));
        server.start();
    }
}
