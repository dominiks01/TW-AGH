package org.example;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannel;

public class Producer implements CSProcess {
    private final One2OneChannel in;
    private final One2OneChannel req;
    private final int maxToProduce;

    public Producer(One2OneChannel in, One2OneChannel req, int maxToProduce) {
        this.in = in;
        this.req = req;
        this.maxToProduce = maxToProduce;
    }

    public void run() {
        while(true) {
            int payload = (int) (Math.random() * maxToProduce) + 1;
            Request request = new Request(MessageType.ORDER_POST, payload, in);
            System.out.println("P[" + ProcessHandle.current().pid() + "]: Chce wstawić [" + request.payload + "]");
            req.out().write(request);
            Request response = (Request) in.in().read();

            if (response.status == Status.SUCCESS)
                System.out.println("P[" + ProcessHandle.current().pid() + "]: Wstawił [" + response.payload + "]");

            if (response.status == Status.FAILURE)
                System.out.println("P[" + ProcessHandle.current().pid() + "]: Program nie potrafił obsłużyć żądania");
        }
    }

    public One2OneChannel getIn(){
        return in;
    }

    public One2OneChannel getReq(){
        return req;
    }
}