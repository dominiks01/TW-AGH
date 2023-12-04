package org.example;

import org.jcsp.lang.AltingChannelInput;
import org.jcsp.lang.One2OneChannel;

public class DualChannel {
    private final One2OneChannel<Batch> request_channel;
    private final One2OneChannel<Batch> response_channel;

    public DualChannel(One2OneChannel<Batch> request_channel, One2OneChannel<Batch> response_channel) {
        this.request_channel = request_channel;
        this.response_channel = response_channel;
    }

    public void writeToRequest(Batch value) {
        request_channel.out().write(value);
    }

    public void writeToResponse(Batch value) {
        response_channel.out().write(value);
    }

    public Batch readFromRequest() {
        return request_channel.in().read();
    }

    public Batch readFromResponse() {
        return response_channel.in().read();
    }

    public AltingChannelInput<Batch> getIn() {
        return request_channel.in();
    }

    public One2OneChannel<Batch> getResponseChannel() {
        return response_channel;
    }
}
