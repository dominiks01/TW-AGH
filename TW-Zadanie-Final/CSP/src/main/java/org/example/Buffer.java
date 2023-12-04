package org.example;

import org.jcsp.lang.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/*
    Buffer Class 
*/

public class Buffer implements CSProcess {
    private int buffer_size;
    private int buffer_value;
    private final One2OneChannel<Batch> forward_buffer;
    private final One2OneChannel<Batch> backward_buffer;
    private final List<DualChannel> clients;
    private final int buffer_index;
    private final Deque<Batch> queue;
    private boolean token;

    public Buffer(One2OneChannel<Batch> backward_buffer, One2OneChannel<Batch> forward_buffer, List<DualChannel> clients, int size, int index) {
        this.forward_buffer = forward_buffer;
        this.backward_buffer = backward_buffer;
        this.clients = clients;
        this.buffer_value = 0;
        this.buffer_size = size;
        this.buffer_index = index;
        this.queue = new ArrayDeque<>();
        this.token = index == 0;
    }

    public void run() {
        Guard[] guards = clients.stream().map(DualChannel::getIn).toArray(Guard[]::new);
        Alternative alt = new Alternative(guards);

        while (true) {
            while(queue.size() == 0){
                System.out.println("Bufor " + buffer_index + ": Czekam na klienta");
                int index = alt.select();
                Batch batch = clients.get(index).readFromRequest();
                System.out.println("Bufor " + buffer_index + ": Dostaję od klienta (w pętli while) -> ");
                batch.setOwner(this.buffer_index);
                processOrder(batch);
            }
            System.out.println("Bufor " + buffer_index + ": Wyszedłem z while, queue.size = " + queue.size());
            
            if(!token) {
                waitForToken();
                Batch batch = waitForBatchFromBackBuffer();
                if(batch.owner == this.buffer_index) { // jak wróciła wiadomość po kółku to odsyłam failure klientowi
                    System.out.println("Bufor " + buffer_index + ": Wraca do mnie wiadomość po kółku - robię failure");
                    batch.status = -1;
                    batch.getChannel().write(batch);
                } else {
                    processOrder(batch);
                }
            }
            sendTokenToNextBuffer();
            sendFirstQueueItemToNextBuffer();
        }
    }

    private void processOrder(Batch Batch) {
        if (Batch.type == MessageType.ORDER_POST) {
            if (this.buffer_value + Batch.payload <= this.buffer_size) {
                System.out.println("Bufor " + buffer_index + ": Dodaję -> " + Batch.payload);
                this.buffer_value += Batch.payload;
                Batch response = new Batch(MessageType.RESPONSE_POST);
                response.status = 0;
                Batch.getChannel().write(response);
                System.out.println("Bufor " + buffer_index + ": Koniec produckji");
            } else {
                queue.add(Batch);
                System.out.println("Bufor " + buffer_index + "Wsadzam do queue. Queue size: " + queue.size() + " | Task to: " + Batch.payload);
                System.out.println("Bufor " + buffer_index + ": I have in buffer " + buffer_value);
            }
        }

        if (Batch.type == MessageType.ORDER_GET) {
            if (this.buffer_value - Batch.payload >= 0) {
                System.out.println("Bufor " + buffer_index + ": Odejmuję -> " + Batch.payload);
                this.buffer_value -= Batch.payload;
                Batch response = new Batch(MessageType.RESPONSE_GET, Batch.payload);
                response.status = 0;
                Batch.getChannel().write(response);
        System.out.println("Bufor " + buffer_index + ": Koniec konsumpcji");
            } else {
                queue.add(Batch);
                System.out.println("Wsadzam do queue. Queue size: " + queue.size() + " | Task to: " + Batch.payload * -1);
                System.out.println("Buffer value: " + buffer_value);
            }
        }

        if (Batch.type == MessageType.TOKEN) {
            System.out.println("Bufor " + buffer_index + ": Dostałem Token");
            this.token = true;
        }
    }

    private void waitForToken() {
        System.out.println("Bufor " + buffer_index + ": Czekam na token...");
        Batch Batch = backward_buffer.in().read();
        processOrder(Batch);
    }

    private Batch waitForBatchFromBackBuffer() {
        System.out.println("Bufor " + buffer_index + ": Czekam na wiadomość od back buffera...");
        Batch Batch = backward_buffer.in().read(); // czeka na prawdziwą wiadomość
        return Batch;
    }

    private void sendTokenToNextBuffer() {
        System.out.println("Bufor " + buffer_index + ": Wysyłam token dalej...");
        sendBatchToNextBuffer(new Batch( MessageType.TOKEN));
        this.token = false;
    }

    private void sendBatchToNextBuffer(Batch Batch) {
        System.out.println("Bufor " + buffer_index + ": Wysyłam wiadomość dalej... Batch = " + Batch);
        forward_buffer.out().write(Batch);
    }

    private void sendFirstQueueItemToNextBuffer() {
        Batch BatchToSendForward = queue.poll();
        sendBatchToNextBuffer(BatchToSendForward);
    }

    public One2OneChannel<Batch> getForwardBuffer() {
        return forward_buffer;
    }

    public One2OneChannel<Batch> getBackwardBuffer() {
        return backward_buffer;
    }
}