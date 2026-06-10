package com.multipaint.server;

import com.multipaint.grpc.Envelope;
import com.multipaint.grpc.PaintServiceGrpc;

import io.grpc.stub.StreamObserver;

/**
 * gRPC cift yonlu akis: her {@link Envelope} tek metin satir protokolu tasir.
 */
public final class PaintGrpcService extends PaintServiceGrpc.PaintServiceImplBase {

    private final PaintServer paintServer;

    public PaintGrpcService(PaintServer paintServer) {
        this.paintServer = paintServer;
    }

    @Override
    public StreamObserver<Envelope> session(StreamObserver<Envelope> responseObserver) {
        ClientSession session = new ClientSession(paintServer, line -> {
            if (line != null) {
                responseObserver.onNext(Envelope.newBuilder().setLine(line).build());
            }
        });

        return new StreamObserver<>() {
            @Override
            public void onNext(Envelope value) {
                try {
                    session.handleLine(value.getLine());
                } catch (Exception e) {
                    session.sendError("Sunucu hatasi: " + e.getMessage());
                }
            }

            @Override
            public void onError(Throwable t) {
                session.onDisconnect();
            }

            @Override
            public void onCompleted() {
                session.onDisconnect();
                responseObserver.onCompleted();
            }
        };
    }
}
