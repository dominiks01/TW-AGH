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
    private final List<One2OneChannel<Request>>  clients;
    private final int buffer_id;
    private final Deque<Request> queue;
    private boolean token;

    private int SUCCES = 1;
    private int FAILURE = 1;

    public Buffer(
            One2OneChannel<Request> next_buffer,
            One2OneChannel<Request> prev_buffer,
            List<One2OneChannel<Request>> clientsReq ,
            int size,
            int index) {
        this.next_buffer = next_buffer;
        this.prev_buffer = prev_buffer;
        this.clients = clientsReq;
        this.value = 0;
        this.buffer_size = size;
        this.buffer_id = index;
        this.queue = new ArrayDeque<>();
        this.token = buffer_id == 0;
    }

    public void run() {
        Guard[] guards = new Guard[clients.size()];

        for(int i = 0 ; i < clients.size() ; i++) {
            guards[i] = clients.get(i).in();
        }

        Alternative alt = new Alternative(guards);

        while (true) {
            while(queue.isEmpty()){
                int index = alt.select();
                Request request = clients.get(index).in().read();
                request.setOwner(this.buffer_id);
                processOrder(request);

                if (!queue.isEmpty())
                    break;
            }

            if(!token) {
                System.out.println("B[" + this.buffer_id + "]: Czekam na token");

                Request request = prev_buffer.in().read();
                processOrder(request);

                System.out.println("B[" + this.buffer_id + "]: Mam token - obsługuję zlecenie poprzedniego bufora");
                request = prev_buffer.in().read();

                if(request.owner == this.buffer_id) {
                    request.callback(FAILURE);
                } else {
                    processOrder(request);
                }
            }

            System.out.println("B[" + this.buffer_id + "]: Przekazuję token i requesta do następnego bufora");
            next_buffer.out().write(new Request(MessageType.TOKEN));
            this.token = false;
            if (queue.isEmpty())
                System.out.println("XD");
            Request requestToSendForward = queue.poll();
            next_buffer.out().write(requestToSendForward);
        }
    }

    private void processOrder(Request request) {
        if (request.type == MessageType.ORDER_POST) {
            System.out.println("B[" + this.buffer_id + "]: obsługuję producenta");
            if (this.value + request.payload <= this.buffer_size) {
                System.out.println("B[" + this.buffer_id + "]: wstawiam do bufora [" + request.payload + "]");
                this.value += request.payload;
                request.callback(SUCCES);
            } else {
                System.out.println("B[" + this.buffer_id + "]: BRAK ZASOBÓW wstawiam do kolejki [" + request.payload + "]");
                queue.add(request);
            }
        }

        if (request.type == MessageType.ORDER_GET) {
            System.out.println("B[" + this.buffer_id + "]: obsługuję konsumenta");
            if (this.value - request.payload >= 0) {
                System.out.println("B[" + this.buffer_id + "]: pobieram z bufora [" + request.payload + "]");
                this.value -= request.payload;
                request.callback(SUCCES);
            } else {
                System.out.println("B[" + this.buffer_id + "]: BRAK ZASOBÓW wstawiam do kolejki [" + request.payload + "]");
                queue.add(request);
            }
        }

        if (request.type == MessageType.TOKEN) {
            System.out.println("B[" + this.buffer_id +"] Odebrałem token");
            this.token = true;
        }
    }

    public void setNext(One2OneChannel<Request> next) {
        this.next_buffer = next;
    }

    public void setPrev_buffer(One2OneChannel<Request> next) {
        this.prev_buffer = next;
    }

    public One2OneChannel<Request> getNext_buffer() {
        return next_buffer;
    }

    public One2OneChannel<Request> getPrev_buffer() {
        return prev_buffer;
    }

    public void setToken() {
        this.token = true;
    }
}