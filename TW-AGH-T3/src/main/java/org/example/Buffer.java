package org.example;

import org.jcsp.lang.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Buffer implements CSProcess {
    private final int buffer_size;
    private int value;
    private One2OneChannel<Request> next_buffer;
    private One2OneChannel<Request> prev_buffer;
    private final List<One2OneChannel<Request>> requests;
    private final int buffer_id;
    private final Deque<Request> queue;
    private boolean token;

    private final int SUCCES = 1;
    private final int FAILURE = 0;

    public Buffer(One2OneChannel<Request> prev_buffer, One2OneChannel<Request> next_buffer, List<One2OneChannel<Request>> requests , int size, int index) {
        this.next_buffer = next_buffer;
        this.prev_buffer = prev_buffer;
        this.requests = requests;
        this.value = 0;
        this.buffer_size = size;
        this.buffer_id = index;
        this.queue = new ArrayDeque<>();
        this.token = buffer_id == 0;
    }

    public void run() {
        Guard[] guards = new Guard[requests.size()];

        for(int i = 0; i < requests.size() ; i++)
            guards[i] = requests.get(i).in();

        Alternative alt = new Alternative(guards);

        while (true) {
            while(queue.isEmpty()){
                int index = alt.select();
                Request request = requests.get(index).in().read();
                request.setOwner(this.buffer_id);

                System.out.println("B[" + this.buffer_id + "]: Obsługuję clienta");
                handleRequest(request);

                if (!queue.isEmpty())
                    break;
            }

            if(!token) {
                Request request = prev_buffer.in().read();
                handleRequest(request);

                System.out.println("B[" + this.buffer_id + "]: Otrzymałem token, czekam na requesta od poprzedniego bufora");
                request = prev_buffer.in().read();

                if(request.owner == this.buffer_id) {
                    request.callback(FAILURE);
                } else {
                    handleRequest(request);
                }
            }

            System.out.println("B[" + this.buffer_id + "]: Przekazuję token");
            next_buffer.out().write(new Request(MessageType.TOKEN));
            this.token = false;

            if(queue.isEmpty())
                next_buffer.out().write(new Request(MessageType.TOKEN));
            else
                next_buffer.out().write(queue.poll());
        }
    }

    private void handleRequest(Request request) {
        if (request.type == MessageType.PRODUCER_REQUEST) {
            System.out.println("B[" + this.buffer_id + "]: Obsługuję producenta");
            if (this.value + request.quantity <= this.buffer_size) {
                System.out.println("B[" + this.buffer_id + "]: Wstawiam do bufora [" + request.quantity + "]");
                this.value += request.quantity;
                request.callback(SUCCES);
            } else {
                System.out.println("B[" + this.buffer_id + "]: Brak zasobów -> " +
                        "Wstawiam do kolejki i czekam na token [" + request.quantity + "]");
                queue.add(request);
            }
        }

        if (request.type == MessageType.CONSUMER_REQUEST) {
            System.out.println("B[" + this.buffer_id + "]: Obsługuję konsumenta");
            if (this.value - request.quantity >= 0) {
                System.out.println("B[" + this.buffer_id + "]: Pobieram z bufora [" + request.quantity + "]");
                this.value -= request.quantity;
                request.callback(SUCCES);
            } else {
                System.out.println("B[" + this.buffer_id + "]: Brak zasobów -> " +
                        "Wstawiam do kolejki i czekam na token [" + request.quantity + "]");
                queue.add(request);
            }
        }

        if (request.type == MessageType.TOKEN) {
            System.out.println("B[" + this.buffer_id +"] Otrzymałem token");
            this.token = true;
        }
    }

    public void setNext(One2OneChannel<Request> next) {
        this.next_buffer = next;
    }

    public void setPrev(One2OneChannel<Request> next) {
        this.prev_buffer = next;
    }

    public One2OneChannel<Request> getNext() {
        return next_buffer;
    }

    public One2OneChannel<Request> getPrev() {
        return prev_buffer;
    }

    public void setToken() {
        this.token = true;
    }
}