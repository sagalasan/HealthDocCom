package server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by christiaan on 4/24/17.
 */
public class Server {
    private int port;
    private SSLServerSocket serverSocket;

    private List<ServerConnection> connections = Collections.synchronizedList(new ArrayList<>());
    private LinkedBlockingQueue<MessagePair> messageQueue = new LinkedBlockingQueue<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        new Thread(this::handleMessages).start();
        try {
            SSLServerSocketFactory socketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            serverSocket = (SSLServerSocket) socketFactory.createServerSocket(port);

            serverSocket.setEnabledCipherSuites(new String[] {"TLS_DH_anon_WITH_AES_128_GCM_SHA256"});

            handleConnections();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void addMessage(ServerConnection connection, Serializable message) {
        messageQueue.add(new MessagePair(message, connection));
    }

    private void handleConnections() {
        System.out.println("Listening for connections...");
        while (true) {
            try {
                SSLSocket socket = (SSLSocket) serverSocket.accept();
                ServerConnection connection = new ServerConnection(socket, this);
                connection.start();

                connections.add(connection);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void handleMessages() {
        while (true) {
            try {
                System.out.println("hi");
                MessagePair messagePair = messageQueue.take();
                Serializable message = messagePair.message;
                ServerConnection connection = messagePair.client;

                // TODO: Actuall process messages
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Server <port>");
            System.exit(-1);
        }

        int port = Integer.parseInt(args[0]);
        new Server(port).start();
    }
}
