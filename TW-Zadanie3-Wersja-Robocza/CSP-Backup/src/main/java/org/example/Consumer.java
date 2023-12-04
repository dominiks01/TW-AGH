package org.example;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannel;

public class Consumer implements CSProcess {
    private final One2OneChannel<Request> in;
    private final One2OneChannel<Request> req;
    private final int max_value;
    private final int consumer_id;
    private int succes=0, failure=0;


    public Consumer(One2OneChannel<Request> in, One2OneChannel<Request> req, int maxToConsume, int id) {
        this.in = in;
        this.req = req;
        this.max_value = maxToConsume;
        this.consumer_id = id;
    }

    public void run() {
        while(true) {
            int toConsume = (int) ((Math.random() * (max_value - 1)) + 1);
            Request request = new Request(MessageType.ORDER_GET, toConsume, this.in);
            System.out.println("C[" + ProcessHandle.current().pid() + "]: Chce pobrać [" + toConsume+"]");
            req.out().write(request);
            Request response = in.in().read();

            if (response.status == 1)
                System.out.println("C[" + ProcessHandle.current().pid() + "]: Otrzymuje [" + response.payload + "]");

            if (response.status == 0)
                System.out.println("C[" + ProcessHandle.current().pid() + "]: Program nie potrafił obsłużyć żądania");

        }
    }

    public One2OneChannel<Request> getIn(){
        return in;
    }

    public One2OneChannel<Request> getReq(){
        return req;
    }
}