package org.example;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannel;

import java.io.*;

public class Producer implements CSProcess {
    private final One2OneChannel<Request> in;
    private final One2OneChannel<Request> req;
    private final int max_value;
    private final int producer_id;
    private int succes=0;
    private int all_requests = 0;

    public Producer(One2OneChannel<Request> in, One2OneChannel<Request> req, int max_value, int id) {
        this.in = in;
        this.req = req;
        this.max_value = max_value;
        this.producer_id = id;
    }

    public void run() {
        while(true) {
            int payload = (int) (Math.random() * max_value) + 1;
            Request request = new Request(MessageType.PRODUCER_REQUEST, payload, in);
            System.out.println("P[" + producer_id + "]: Chce wstawić [" + request.quantity + "]");
            req.out().write(request);

            Request response = in.in().read();

            if (response.status == 1) {
                System.out.println("P[" + producer_id + "]: Wstawił [" + response.quantity + "]");
                this.succes++;
            }

            if (response.status == 0)
                System.out.println("P[" + producer_id + "]: Program nie potrafił obsłużyć żądania");

            all_requests++;
            System.out.println("P[" + producer_id + "]: S[" +this.succes +"/"+ this.all_requests+"]");
        }
    }

    public One2OneChannel<Request> getReq(){
        return req;
    }
}