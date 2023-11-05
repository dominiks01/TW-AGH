package producersconsumers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class MonitorBoolean {
    public final int M = 10;
    private final int MAX_BUFFER = 2*M;
    private int buffer = 0;

    private final ReentrantLock lock = new ReentrantLock();

    private boolean isFirstProducerWaiting = false  ;
    private boolean isFirstConsumerWaiting = false;

    private final Condition firstProd = lock.newCondition();
    private final Condition restProd  = lock.newCondition();
    private final Condition firstCons = lock.newCondition();
    private final Condition restCons  = lock.newCondition();

    public void ConsumeData(int quantity, String threadId) throws InterruptedException {
        try {
            lock.lock();
            System.out.println("C["+threadId +"]: chce pobrać: [" + quantity +"]");

            while(this.isFirstConsumerWaiting){
                restCons.await();
            }

            while (buffer < quantity){
                this.isFirstConsumerWaiting = true;
                System.out.println("C[" + threadId + "]: czeka na buffor");
                firstCons.await();
            }

            buffer -= quantity;
            System.out.println("C["+threadId + "]: pobiera [" + quantity +"] jest [" + buffer + "]");

            this.isFirstConsumerWaiting = false;
            restCons.signal();
            firstProd.signal();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void ProduceData(int quantity, String threadId) throws InterruptedException {
        try{
            lock.lock();
            System.out.println("P["+threadId +"]: wyprodukował: [" + quantity +"]");

            while (this.isFirstProducerWaiting){
                restProd.await();
            }

            while (MAX_BUFFER  - buffer < quantity){
                this.isFirstProducerWaiting = true;
                System.out.println("P[" + threadId + "]: czeka na buffor");
                firstProd.await();
            }

            buffer += quantity;
            System.out.println("P["+threadId + "]: przekazuje [" + quantity +"] jest [" + buffer + "]");

            this.isFirstProducerWaiting = false;
            restProd.signal();
            firstCons.signal();
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}