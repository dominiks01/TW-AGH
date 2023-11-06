package producersconsumers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import java.util.HashMap;
import java.util.Map.Entry;

class MonitorHasWaiters {
    public final int M = 10;
    private final int MAX_BUFFER = 2*M;
    private int buffer = 0;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition firstProd = lock.newCondition();
    private final Condition restProd = lock.newCondition();
    private final Condition firstCons = lock.newCondition();
    private final Condition restCons = lock.newCondition();

    private HashMap<String, Integer> producersMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> consumersMap = new HashMap<String, Integer>();

    private HashMap<String, Integer> producersStarvation = new HashMap<String, Integer>();
    private HashMap<String, Integer> consumersStarvation = new HashMap<String, Integer>();  

    private synchronized void printMessage(String label, String threadId){

        System.out.print("P[ ");

        for(Entry<String, Integer> a : producersStarvation.entrySet()){
            System.out.print(a.getKey() + ":" + a.getValue() + " ");            
        }

        System.out.print("]  C[ ");
        
        for(Entry<String, Integer> a : consumersStarvation.entrySet()){
            System.out.print(a.getKey() + ":" + a.getValue() + " ");            
        }

        System.out.println("]  "+label);
   }

    public void ConsumeData(int quantity, String threadId) throws InterruptedException {
        try {
            lock.lock();
            consumersMap.putIfAbsent(threadId, 0);
            consumersStarvation.putIfAbsent(threadId, 0);

            printMessage("C[" + threadId + "]: chce pobrać [" + quantity +"]", threadId);

            while(this.lock.hasWaiters(this.firstCons)){
                consumersMap.replace(threadId, 1);
                restCons.await();
                printMessage("C[" + threadId + "]: czeka na buffor [reszta]", threadId);
            }

            consumersMap.replace(threadId, 2);

            while (buffer < quantity){

                consumersMap.replace(
                    threadId,
                    consumersMap.get(threadId) + 1
                );

                printMessage("C[" + threadId + "]: czeka na buffor [pierwszy]", threadId);
                firstCons.await();
            }

            consumersMap.replace(threadId, 0);
            buffer -= quantity;               
            printMessage("C["+threadId + "]: pobiera [" + quantity + "] zapełnienie [" + buffer + "/ "+MAX_BUFFER +"]", threadId);

            restCons.signal();
            firstProd.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            consumersStarvation.replace(threadId, consumersStarvation.get(threadId) + 1);
            lock.unlock();
        }
    }

    public void ProduceData(int quantity, String threadId) throws InterruptedException {
        try{
            lock.lock();
            producersMap.putIfAbsent(threadId, 0);
            producersStarvation.putIfAbsent(threadId, 0);

            printMessage("P[" + threadId + "]: wyprodukował [" + quantity +"]", threadId);

            while (this.lock.hasWaiters(this.firstProd)){
                producersMap.replace(threadId, 1);
                printMessage("P[" + threadId + "]: czeka na buffor [reszta]", threadId);
                restProd.await();
            }

            producersMap.replace(threadId, 2);

            while (MAX_BUFFER - buffer < quantity){
                producersMap.replace(
                    threadId, 
                    producersMap.get(threadId) + 1
                );

                printMessage("P[" + threadId + "]: czeka na buffor [pierwszy]", threadId);
                firstProd.await();
            }

            producersMap.replace(threadId, 0);
            buffer += quantity;           
            printMessage("P["+threadId + "]: przekazuje [" + quantity +"] zapełnienie [" + buffer + "/ "+MAX_BUFFER+"]", threadId);

            restProd.signal();
            firstCons.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producersStarvation.replace(threadId, producersStarvation.get(threadId) + 1);
            lock.unlock();
        }
    }
}