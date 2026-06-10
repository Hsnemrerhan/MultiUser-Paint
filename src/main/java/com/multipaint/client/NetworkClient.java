package com.multipaint.client;

import com.multipaint.grpc.Envelope;
import com.multipaint.grpc.PaintServiceGrpc;
import com.multipaint.protocol.Protocol;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * gRPC istemcisi: {@link PaintServiceGrpc#session} cift yonlu akis.
 */
public final class NetworkClient implements Closeable {

    private ManagedChannel channel;
    private StreamObserver<Envelope> requestStream;
    private volatile boolean closed;
    private Consumer<String> onLine;
    private Consumer<Exception> onError;

    public void connect(String host, int port) throws Exception {
        closeQuietly();
        closed = false;
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        PaintServiceGrpc.PaintServiceStub stub = PaintServiceGrpc.newStub(channel);

        StreamObserver<Envelope> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(Envelope value) {
                if (!closed && onLine != null && value != null) {
                    String line = value.getLine().trim();
                    if (!line.isEmpty()) {
                        onLine.accept(line);
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                if (!closed && onError != null) {
                    onError.accept(new IOException(t.getMessage(), t));
                }
            }

            @Override
            public void onCompleted() {
                if (!closed && onError != null) {
                    onError.accept(new IOException("Sunucu akisi kapandi"));
                }
            }
        };

        requestStream = stub.session(responseObserver);
    }

    public void sendLine(String line) {
        if (requestStream != null && !closed) {
            requestStream.onNext(Envelope.newBuilder().setLine(line).build());
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
        this.onLine = onLine;
        this.onError = onError;
    }

    private void closeQuietly() {
        closed = true;
        if (requestStream != null) {
            try {
                requestStream.onCompleted();
            } catch (Exception ignored) {
            }
            requestStream = null;
        }
        if (channel != null) {
            channel.shutdown();
            try {
                channel.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                channel.shutdownNow();
            }
            channel = null;
        }
    }

    @Override
    public void close() {
        closeQuietly();
    }

    public static String encodePngBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }
}
