package producersconsumers;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Monitor3Locks{
    public final int M = 10;
    private final int MAX_BUFFER = 2*M;
    private int buffer = 0;

    private final ReentrantLock lock = new ReentrantLock();
    private final ReentrantLock producerLock = new ReentrantLock();
    private final ReentrantLock consumerLock = new ReentrantLock();

    private final Condition lockCondition = lock.newCondition();

    public void ConsumeData(int quantity, String threadId) throws InterruptedException {
        consumerLock.lock();
        try {
            lock.lock();
            System.out.println("C["+threadId +"]: chce pobrać: [" + quantity +"]");

            while (buffer < quantity){
                System.out.println("C[" + threadId + "]: czeka na buffor");
                lockCondition.await();
            }

            buffer -= quantity;
            System.out.println("C["+ threadId + "]: pobiera [" + quantity + "] zapełnienie [" + buffer + "/ "+MAX_BUFFER +"]");

            lockCondition.signal();
            lock.unlock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            consumerLock.unlock();
        }
    }

    public void ProduceData(int quantity, String threadId) throws InterruptedException {
        producerLock.lock();
        try{
            lock.lock();
            System.out.println("P["+threadId +"]: wyprodukował: [" + quantity +"]");

            while (MAX_BUFFER - buffer < quantity){
                System.out.println("P[" + threadId+ "]: czeka na buffor");
                lockCondition.await();
            }

            buffer += quantity;
            System.out.println("P["+threadId+ "]: przekazuje [" + quantity +"] zapełnienie [" + buffer + "/ "+MAX_BUFFER+"]");

            lockCondition.signal();
            lock.unlock();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            producerLock.unlock();
        }
    }
}