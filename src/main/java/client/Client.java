package client;

import messages.ClientHello;
import messages.ServerHello;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
            socket.setEnabledCipherSuites(new String[] {"TLS_DH_anon_WITH_AES_128_GCM_SHA256"});

            System.out.println("Connected");

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

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
                processMessage(message);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void processMessage(Serializable message) {
        if (message instanceof ServerHello) {
            System.out.println("Received ServerHello");
            send(new ClientHello());
        }
    }

    private synchronized void send(Serializable message) {
        try {
            outputStream.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
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
