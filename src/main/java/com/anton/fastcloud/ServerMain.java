package com.anton.fastcloud;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;

import javax.servlet.ServletException;
import java.nio.ByteBuffer;

public class ServerMain {
    public static void main(String[] args) {
        INonStaticSerializer nonStaticSerializer = SerializersClassLoader.getNonStaticSerializer(User.class);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        User userOld = new User("123", "456", true, new User[] {new User("789", "012", false, new User[]{null})});
        System.out.println(userOld);
        nonStaticSerializer.serializeNonStatic(buffer, userOld);
        buffer.rewind();
        User userNew = (User) nonStaticSerializer.deserializeNonStatic(buffer);
        System.out.println(userNew);

        PathHandler path = Handlers.path();

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(path)
                .build();
        server.start();

        ServletContainer container = ServletContainer.Factory.newInstance();

        DeploymentInfo builder = new DeploymentInfo()
                .setClassLoader(ServerMain.class.getClassLoader())
                .setContextPath("/")
                .addWelcomePage("index.html")
                .setResourceManager(new ClassPathResourceManager(ServerMain.class.getClassLoader()))
                .addServletContextAttribute(
                        WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                        new WebSocketDeploymentInfo()
                                .setBuffers(new DefaultByteBufferPool(true, 100))
                                .addEndpoint(WebSocketEndpoint.class)
                )
                .setDeploymentName("fastcloud.war");

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();
        try {
            path.addPrefixPath("/", manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
