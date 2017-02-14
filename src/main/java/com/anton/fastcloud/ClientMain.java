package com.anton.fastcloud;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
                System.out.println(message.getData());
            }
        });
        channel.resumeReceives();
    }
}
