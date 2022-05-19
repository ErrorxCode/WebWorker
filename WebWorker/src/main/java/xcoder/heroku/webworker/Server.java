package xcoder.heroku.webworker;

import com.heroku.api.HerokuAPI;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;

import spark.Spark;

/**
 * WebServer is the class which has the methods to control the worker process.
 * The worker process has the queue to store the tasks/jobs and the web server to
 * serve the requests. You can start, stop, serve task to the worker.
 */
public class Server {
    private static HerokuAPI heroku;
    private static String app;

    /**
     * Starts the worker without any tasks. The worker will wait at most for 10 seconds
     * for the tasks to be added. If no tasks are added, the worker will stop.
     */
    private static void startWorker(){
        heroku.scale(app,"worker",1);
    }

    /**
     * Force fully and immediately stops the worker. All the queued tasks will be lost.
     */
    protected static void stopWorker(){
        heroku.scale(app,"worker",0);
    }

    /**
     * Adds the array of items to the worker's queue which workers uses to process the task.
     * @param item The item to be added to the queue.
     */
    private static boolean queueData(byte[][] item){
        for (byte[] i : item) {
            try {
                WebSocketServer.server.getRemote().sendBytes(ByteBuffer.wrap(i));
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Starts the worker to process s single task with the data.
     * This data is not added to the queue, it is processed immediately.
     * @param item The data to be processed.
     */
    private static boolean startWorker(byte[] item){
        startWorker();
        try {
            Thread.sleep(5000);
            WebSocketServer.server.getRemote().sendBytes(ByteBuffer.wrap(item));
            return true;
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }

    public static void start(){
        heroku = new HerokuAPI(System.getenv("API_KEY"));
        app = System.getenv("APP_NAME");
        Spark.port(Integer.parseInt(System.getenv("PORT") == null ? "3000" : System.getenv("PORT")));
        Spark.webSocket("/worker", WebSocketServer.class);
        Spark.get("/start", (request, response) -> {
            startWorker();
            return true;
        });
        Spark.post("/start",(request,response) -> {
            try {
                return startWorker(request.bodyAsBytes());
            } catch (Exception e){
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        });
        Spark.get("/stop",((request, response) -> {
            stopWorker();
            return true;
        }));
        Spark.patch("/queue","multipart/form-data",((request, response) -> {
            HttpServletRequest servlet = request.raw();
            servlet.setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
            byte[][] bytes = servlet.getParts().stream().map(part -> {
                try {
                    return part.getInputStream().readAllBytes();
                } catch (IOException e) {
                    return null;
                }
            }).toArray(byte[][]::new);
            return queueData(bytes);
        }));
    }
}