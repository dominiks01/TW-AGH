package org.example;

import org.jcsp.lang.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Buffer implements CSProcess {
    private final int max_buffer;
    private int buffer;
    private final int buffer_index;
    private final One2OneChannel<Request> next_buffer;
    private final One2OneChannel<Request> token_input;
    private final List<One2OneChannel<Request>> clients_requests;
    private final Deque<Request> queue;
    private boolean token;

    public Buffer(One2OneChannel<Request> prev_buffer, One2OneChannel<Request> next_buffer, List<One2OneChannel<Request>> clientsIn, List<One2OneChannel<Request>> clientsReq , int size, int index) {
        this.next_buffer = next_buffer;
        this.token_input = prev_buffer;
        this.clients_requests = clientsReq;
        this.buffer = 0;
        this.max_buffer = size;
        this.queue = new ArrayDeque<>();
        this.token = index == 0;
        this.buffer_index = index;
    }

    public void run() {
        Guard[] guards = new Guard[clients_requests.size()];

        for(int i = 0; i < clients_requests.size() ; i++)
            guards[i] = clients_requests.get(i).in();

        Alternative alt = new Alternative(guards);

        while (true) {
            while(queue.isEmpty()){
                int index = alt.select();
                Request request = clients_requests.get(index).in().read();
                request.buffer_id = this.buffer_index;
                processOrder(request);
            }

            if(!token) {
                waitForToken();
                Request request = waitForMessageFromBackBuffer();

                if(request.buffer_id == this.buffer_index) {
                    request.setStatus(Status.FAILURE);
                    request.callback.out().write(request);
                } else {
                    processOrder(request);
                }
            }

            sendMessageToNextBuffer(new Request(MessageType.TOKEN));
            this.token = false;
            Request requestToSendForward = queue.poll();
            sendMessageToNextBuffer(requestToSendForward);
        }
    }

    private void processOrder(Request request) {
        if (request.type == MessageType.ORDER_POST) {
            if (this.buffer + request.payload <= this.max_buffer) {
                this.buffer += request.payload;
                request.setStatus(Status.SUCCESS);
                request.callback(request);
            } else {
                queue.add(request);
            }
        }

        if (request.type == MessageType.ORDER_GET) {
            if ( this.buffer - request.payload >= 0) {
                this.buffer -= request.payload;
                Request response = new Request(MessageType.RESPONSE_GET, request.payload);
                response.setStatus(Status.SUCCESS);
                request.callback(response);
            } else {
                queue.add(request);
            }
        }
        else if (request.type == MessageType.TOKEN) {
            this.token = true;
        }
    }

    private void waitForToken() {
        Request message = (Request) getPrev().in().read();
        processOrder(message);
    }

    private Request waitForMessageFromBackBuffer() {
        Request message = (Request) getPrev().in().read();
        return message;
    }

    public One2OneChannel getNext() {
        return next_buffer;
    }

    public One2OneChannel getPrev() {
        return token_input;
    }

    private void sendMessageToNextBuffer(Request message) {
        getNext().out().write(message);
    }


}