package server;

import messages.ClientHello;

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

    /**
     * Method used to create the socket and listen for connections.
     */
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

    /**
     * ServerConnections will call this method in order for a message to processed
     * @param connection Reference to the ServerConnection that received the message
     * @param message The message that was received
     */
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
                MessagePair messagePair = messageQueue.take();
                Serializable message = messagePair.message;
                ServerConnection connection = messagePair.client;

                processMessage(connection, message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(ServerConnection connection, Serializable message) {
        // Connection reference will be used to respond to the right client
        if (message instanceof ClientHello) {
            System.out.println("Received ClientHello");
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
