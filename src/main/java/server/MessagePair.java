package server;

import java.io.Serializable;

/**
 * Created by christiaan on 4/24/17.
 */
public class MessagePair {
    public final Serializable message;
    public final ServerConnection client;

    public MessagePair(Serializable message, ServerConnection client) {
        this.message = message;
        this.client = client;
    }
}
