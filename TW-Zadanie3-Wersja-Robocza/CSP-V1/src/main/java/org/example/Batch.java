package org.example;

import org.jcsp.lang.ChannelOutput;
import org.jcsp.lang.One2OneChannel;

public class Batch {
    int payload;
    One2OneChannel<Batch> direct_response_channel;
    int owner = -1;
    MessageType type; 
    int status = 0;

    public Batch(MessageType type){
        this.type = type;
    }

    public Batch(MessageType type, int payload) {
        this.type = type;
        this.payload = payload;
    }

    public Batch(MessageType type, int payload, One2OneChannel<Batch> channel) {
        this(type, payload);
        this.direct_response_channel = channel;
    }

    public ChannelOutput<Batch> getChannel(){
        return direct_response_channel.out();
    }

    public void setOwner(int id) {
        this.owner = id;
    }
}
