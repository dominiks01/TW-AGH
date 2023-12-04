package org.example;

import org.jcsp.lang.One2OneChannel;

public class Request {
    MessageType type;
    int status;
    int quantity;
    One2OneChannel<Request> return_channel;
    int owner;

    public Request(MessageType type) {
        this.type = type;
    }

    public Request(MessageType type, int quantity, One2OneChannel<Request> channel) {
        this.type = type;
        this.quantity = quantity;
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
}
