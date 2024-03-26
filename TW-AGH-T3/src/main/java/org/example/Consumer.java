package org.example;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannel;
import java.io.*;


public class Consumer implements CSProcess {
    private final One2OneChannel<Request> in;
    private final One2OneChannel<Request> req;
    private final int max_value;
    private final int consumer_id;
    private int succes=0;
    private int all_requests = 0;

    public Consumer(One2OneChannel<Request> in, One2OneChannel<Request> req, int max_value, int id) {
        this.in = in;
        this.req = req;
        this.max_value = max_value;
        this.consumer_id = id;
    }

    public void run() {
        while(true) {
            int toConsume = (int) ((Math.random() * (max_value - 1)) + 1);
            Request request = new Request(MessageType.CONSUMER_REQUEST, toConsume, this.in);
            System.out.println("C[" + consumer_id + "]: Chce pobrać [" + toConsume + "]");
            req.out().write(request);
            Request response = in.in().read();

            if (response.status == 1){
                System.out.println("C[" + consumer_id + "]: Otrzymuje [" + response.quantity + "]");
                this.succes++;
            }

            if (response.status == 0)
                System.out.println("C[" + consumer_id + "]: Program nie potrafił obsłużyć żądania");

            this.all_requests++;
            System.out.println("C[" + consumer_id + "]: S[" +this.succes +"/"+ this.all_requests+"]");
        }
    }


    public One2OneChannel<Request> getIn(){
        return in;
    }

    public One2OneChannel<Request> getReq(){
        return req;
    }
}