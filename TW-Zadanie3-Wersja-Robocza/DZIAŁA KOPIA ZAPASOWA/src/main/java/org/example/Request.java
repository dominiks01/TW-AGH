package org.example;

import org.jcsp.lang.One2OneChannel;

public class Request {
    MessageType type;
    int status;
    int payload;
    One2OneChannel return_channel;
    int owner = -1;

    public Request(MessageType type) {
        this.type = type;
    }

    public Request(MessageType type, int payload) {
        this(type);
        this.payload = payload;
    }

    public Request(MessageType type, int payload, One2OneChannel channel) {
        this(type, payload);
        this.return_channel = channel;
    }

    public void setOwner(int id) {
        this.owner = id;
    }

    public void callback(int status){
        this.type = this.type.getResponse();
        this.status = status;
        this.return_channel.out().write(this);
    }

    public String toString() {
        return "Message: " + type + ", " + status + ", " + payload;
    }
}
