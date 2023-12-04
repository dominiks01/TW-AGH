package org.example;

import org.jcsp.lang.CSProcess;
import org.jcsp.lang.One2OneChannel;

public class Consumer implements CSProcess {
    private final One2OneChannel in;
    private final One2OneChannel req;
    private final int maxToConsume;

    public Consumer(One2OneChannel in, One2OneChannel req, int maxToConsume) {
        this.in = in;
        this.req = req;
        this.maxToConsume = maxToConsume;
    }

    public void run() {
        while(true) {
            int toConsume = (int) ((Math.random() * (maxToConsume - 1)) + 1);
            Request request = new Request(MessageType.ORDER_GET, toConsume, this.in);

            System.out.println("C[" + ProcessHandle.current().pid() + "]: Chce pobrać [" + toConsume+"]");
            req.out().write(request);
            Request response = (Request) in.in().read();

            if (response.status == Status.SUCCESS)
                System.out.println("C[" + ProcessHandle.current().pid() + "]: Otrzymuje [" + response.payload + "]");

            if (response.status == Status.FAILURE)
                System.out.println("C[" + ProcessHandle.current().pid() + "]: Program nie potrafił obsłużyć żądania");
        }
    }

    public One2OneChannel getIn(){
        return in;
    }

    public One2OneChannel getReq(){
        return req;
    }
}