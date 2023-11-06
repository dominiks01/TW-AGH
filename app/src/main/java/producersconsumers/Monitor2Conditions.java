package producersconsumers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;
import java.util.Map.Entry;

class Monitor2Conditions{
    public final int M = 10;
    private final int MAX_BUFFER = 2*M;

    private int buffer = 0;
    private final Lock lock = new ReentrantLock();
    private final Condition producerCondition = lock.newCondition();
    private final Condition consumerCondition = lock.newCondition();

    private HashMap<String, Integer> producersMap = new HashMap<String, Integer>();
    private HashMap<String, Integer> consumersMap = new HashMap<String, Integer>();

    private void printMessage(String label, String threadId){

        System.out.print("P[ ");

        for(Entry<String, Integer> a : producersMap.entrySet()){
            System.out.print(a.getKey() + ":" + a.getValue() + " ");            
        }

        System.out.print("]  C[ ");
        
        for(Entry<String, Integer> a : consumersMap.entrySet()){
            System.out.print(a.getKey() + ":" + a.getValue() + " ");            
        }

        System.out.println("]  "+label);
   }

 
    public void ProduceData(int quantity, String threadId) throws InterruptedException{
        try {
            lock.lock();
            printMessage("P["+threadId +"]: wyprodukował: [" + quantity +"]", threadId);
            producersMap.putIfAbsent(threadId, 0);

            while (buffer + quantity >  MAX_BUFFER ){
                printMessage("P[" + threadId+ "]: czeka na buffor", threadId);
                // producersMap.replace(threadId, producersMap.get(threadId) + 1);
                producerCondition.await();
            }


            buffer += quantity;
            printMessage("P["+threadId+ "]: przekazuje [" + quantity +"] zapełnienie [" + buffer + "/ "+MAX_BUFFER+"]", threadId);
            consumerCondition.signal();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producersMap.replace(threadId, producersMap.get(threadId) + 1);
            lock.unlock();
        }
    }

    public void ConsumeData(int quantity, String threadId) throws InterruptedException{
        try {
            lock.lock();
            printMessage("C["+threadId +"]: chce pobrać: [" + quantity +"]", threadId);
            consumersMap.putIfAbsent(threadId, 0);

            while (buffer < quantity) {
                printMessage("C[" + threadId+ "]: czeka na buffor", threadId);
                // consumersMap.replace(threadId, consumersMap.get(threadId)+1);
                consumerCondition.await();
            }

            buffer -= quantity;
            printMessage("C["+threadId+ "]: pobiera [" + quantity + "] zapełnienie [" + buffer + "/ "+MAX_BUFFER +"]", threadId);
            producerCondition.signal();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            consumersMap.replace(threadId, consumersMap.get(threadId) + 1);
            lock.unlock();
        }
    }
}