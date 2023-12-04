package org.example;

import org.jcsp.lang.One2OneChannel;

public class Request {
    MessageType type;
    Status status;
    int payload;
    One2OneChannel callback;
    int buffer_id;

    public Request(MessageType type) {
        this.type = type;
    }

    public Request(MessageType type, int payload) {
        this(type);
        this.payload = payload;
    }

    public Request(MessageType type, int payload, One2OneChannel channel) {
        this(type, payload);
        this.callback = channel;
    }

    public void callback(Request r){
        this.callback.out().write(this);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString() {
        return "Message: " + type + ", " + status + ", " + payload;
    }
}
