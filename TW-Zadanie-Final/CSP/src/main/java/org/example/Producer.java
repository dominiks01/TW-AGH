package org.example;

import org.jcsp.lang.CSProcess;

public class Producer implements CSProcess {
    private final DualChannel buffer;
    private final int max_value;
    private final int user_id;

    public Producer(DualChannel buffer, int max_value, int index) {
        this.buffer = buffer;
        this.max_value = max_value;
        this.user_id = index;
    }

    public void run() {
        while(true) {
            int payload = (int) (Math.random() * max_value) + 1;
            Batch request = new Batch(MessageType.ORDER_POST, payload, buffer.getResponseChannel());
            System.out.println("Producer " + user_id + ": " + request);
            buffer.writeToRequest(request);
            Batch response = buffer.readFromResponse();
            System.out.println("Producer " + user_id + ": " + response);
        }
    }

    public DualChannel getBuffer() {
        return this.buffer;
    }
}