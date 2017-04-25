package server;

import messages.ServerHello;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by christiaan on 4/24/17.
 */
public class ServerConnection extends Thread {
    private SSLSocket socket;
    private Server server;

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;

    private LinkedBlockingQueue<Serializable> messageQueue = new LinkedBlockingQueue<>();

    public ServerConnection(SSLSocket socket, Server server) {

        this.socket = socket;
        this.server = server;

        try {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        System.out.println("New client connected...");
        try {
            send(new ServerHello());

            while (true) {
                Serializable message = (Serializable) inputStream.readObject();
                server.addMessage(this, message);
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized void send(Serializable message) {
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
