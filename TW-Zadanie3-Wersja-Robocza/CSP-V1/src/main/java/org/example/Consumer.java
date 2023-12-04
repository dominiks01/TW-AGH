package org.example;

import org.jcsp.lang.CSProcess;

public class Consumer implements CSProcess {
    private final DualChannel buffer;
    private final int max_value;
    private final int user_id;

    public Consumer(DualChannel buffer, int max_value, int index) {
        this.buffer = buffer;
        this.max_value = max_value;
        this.user_id = index;
    }

    public void run() {
        while(true) {
            int value = (int) ((Math.random() * (max_value - 1)) + 1);
            Batch request = new Batch(MessageType.ORDER_GET, value, buffer.getResponseChannel());
            System.out.println("Consumer " + user_id + ": " + request);
            buffer.writeToRequest(request);
            Batch response = buffer.readFromResponse();
            System.out.println("Consumer " + user_id + " odpowiedź : " + response);
        }
    }

    public DualChannel getBuffer() {
        return this.buffer;
    }
}