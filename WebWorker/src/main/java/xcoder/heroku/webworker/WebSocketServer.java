package xcoder.heroku.webworker;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.io.InputStream;

@WebSocket
public class WebSocketServer {
    protected static Session server;

    @OnWebSocketConnect
    public void onConnect(Session user) {
        server = user;
        System.out.println("Worker started");
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) throws IOException {
        System.out.println("Worker finished with status : " + statusCode);
        Server.stopWorker();
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws IOException {

    }

    @OnWebSocketMessage
    public void onMessage(InputStream bytes) {

    }
}
