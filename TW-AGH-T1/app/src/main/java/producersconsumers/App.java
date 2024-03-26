package producersconsumers;

import java.util.Arrays;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        System.out.println(Arrays.toString(args));

        // Monitor2Conditions monitor = new Monitor2Conditions();
        MonitorHasWaiters monitor = new MonitorHasWaiters();
        // MonitorBoolean monitor = new MonitorBoolean();
        // Monitor3Locks monitor = new Monitor3Locks();

        int NUMBER_OF_PRODUCERS = 5;
        int NUMBER_OF_BIG_PRODUCERS = 0;
        int NUMBER_OF_CONSUMERS = 6;
        int NUMBER_OF_BIG_CONSUMERS = 0;

        Thread producers[] = new Thread[NUMBER_OF_PRODUCERS + NUMBER_OF_BIG_PRODUCERS];
        Thread consumers[] = new Thread[NUMBER_OF_CONSUMERS + NUMBER_OF_BIG_CONSUMERS];
    
        Runnable big_producer = () -> {
            try { 
                while (true) {
                    int quantity = monitor.M ;   
                    monitor.ProduceData(quantity, 
                        "P" + String.valueOf(Thread.currentThread().getName()).substring(7)
                    );
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        };
    
        Runnable producer_task = () -> {
            try {
                while (true) {
                    int quantity = (int) (Math.random() * monitor.M) + 1;
                    monitor.ProduceData(quantity, 
                        "P" + String.valueOf(Thread.currentThread().getName()).substring(7)
                    );
                } 
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        };

        Runnable consumer_task = () -> {
            while (true) {
                int quantity = (int) (Math.random() * monitor.M) + 1;
                try {
                    monitor.ConsumeData(quantity,                       
                        "C" + String.valueOf(Thread.currentThread().getName()).substring(7)
                    );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Runnable big_consumer = () -> {
            while (true) {
                int quantity = monitor.M;
                try {
                    monitor.ConsumeData(quantity,                       
                        "C" + String.valueOf(Thread.currentThread().getName()).substring(7)
                    );                
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        for(int i = 0; i < NUMBER_OF_PRODUCERS + NUMBER_OF_BIG_PRODUCERS; i++){
            producers[i] = (i < NUMBER_OF_PRODUCERS) ? new Thread(producer_task) : new Thread(big_producer);
            producers[i].start();
        }

        for(int i = 0; i < NUMBER_OF_CONSUMERS + NUMBER_OF_BIG_CONSUMERS; i++){
            consumers[i] = (i < NUMBER_OF_CONSUMERS) ? new Thread(consumer_task) : new Thread(big_consumer);
            consumers[i].start();
        }
    }
}