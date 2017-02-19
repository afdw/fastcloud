package com.anton.fastcloud.client;

import com.anton.fastcloud.util.IOUtils;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class ClientMain {
    public static void main(String[] args) {
        WebSocketChannel channel;
        try {
            channel = WebSocketClient.connectionBuilder(
                    IOUtils.createWorker(),
                    new DefaultByteBufferPool(true, 2048),
                    new URI("ws://localhost:8080/websocket")
            ).connect().getInterruptibly();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                WebSockets.sendBinary(ByteBuffer.allocate(128), channel, null);
                System.out.println(message.getData());
            }
        });
        channel.resumeReceives();
    }
}
