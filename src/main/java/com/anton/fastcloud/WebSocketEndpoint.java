package com.anton.fastcloud;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.function.Predicate;

@ServerEndpoint("/websocket")
public class WebSocketEndpoint {
    @OnOpen
    public void open(Session session) {
        session.getAsyncRemote().sendText("Hello");
    }

    @OnMessage
    public void message(String message, Session session) {
        System.out.println(message);
        session.getOpenSessions().stream().filter(Predicate.isEqual(session).negate()).map(Session::getAsyncRemote).forEach(async -> async.sendText(message));
    }
}
