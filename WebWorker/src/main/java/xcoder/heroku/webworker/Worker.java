package xcoder.heroku.webworker;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This is abstract implementation of the worker class. Worker made by extending this class is
 * automatically handled by the web worker framework. This worker is capable of communicating with
 * the web-server without any AMQP library. It also scales automatically. You only need to implement
 * the abstract methods which tells the worker how to process your task, while other things are automatically
 * managed by the framework.
 */
public abstract class Worker {
    private final BlockingQueue<byte[]> tasks = new ArrayBlockingQueue<>(50);

    public Worker(String serverURL) throws InterruptedException {
        WebSocketClient client = new WebSocketClient(URI.create(serverURL)) {
            @Override
            public void onOpen(ServerHandshake session) {
                onStart();
            }

            @Override
            public void onMessage(String message) {

            }

            @Override
            public void onMessage(ByteBuffer bytes) {
                tasks.offer(bytes.array());
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Disconnected from server : " + reason);
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        client.connectBlocking(30, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            while (true) {
                byte[] data = tasks.poll(1, TimeUnit.MINUTES);
                if (data == null)
                    break;
                else
                    executor.submit(() -> process(data));
            }
            client.close(1000, "Worker Idle, sleeping ...");
        } catch (InterruptedException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public abstract void process(byte[] data);

    public void onStart(){

    }

}
