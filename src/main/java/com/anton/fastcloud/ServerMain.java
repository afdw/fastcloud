package com.anton.fastcloud;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.WebSockets;

import java.nio.ByteBuffer;

public class ServerMain {
    public static void main(String[] args) {
        ISerializer<User> serializer = SerializersClassLoader.getSerializer(User.class);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        User userOld = new User("123", "456", true, new User[] {new User("789", "012", false, new User[] {null})});
        System.out.println(userOld);
        serializer.serialize(buffer, userOld);
        buffer.rewind();
        User userNew = serializer.deserialize(buffer);
        System.out.println(userNew);

        Undertow server = Undertow.builder()
                .setWorker(IOUtils.createWorker())
                .addHttpListener(8080, "localhost")
                .setHandler(
                        Handlers.path()
                                .addPrefixPath("/websocket", Handlers.websocket((exchange, channel) -> {
                                    WebSockets.sendText("Hello", channel, null);
                                    channel.getReceiveSetter().set(new AbstractReceiveListener() {
                                    });
                                    channel.resumeReceives();
                                }))
                                .addPrefixPath("/", Handlers.resource(new ClassPathResourceManager(ServerMain.class.getClassLoader())))
                )
                .build();
        server.start();
    }
}
