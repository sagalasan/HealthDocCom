package client;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by christiaan on 4/24/17.
 */
public class Client {
    private String host;
    private int port;

    private SSLSocket socket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private LinkedBlockingQueue<Serializable> messageQueue = new LinkedBlockingQueue<>();

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        new Thread(this::handleMessages).start();

        SSLSocketFactory socketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try {
            socket = (SSLSocket) socketFactory.createSocket(host, port);

            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            while (true) {
                Serializable message = (Serializable) inputStream.readObject();
                messageQueue.add(message);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleMessages() {
        while (true) {
            try {
                Serializable message = messageQueue.take();
                System.out.println(message);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Client <host> <port>");
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        new Client(host, port).start();
    }
}
