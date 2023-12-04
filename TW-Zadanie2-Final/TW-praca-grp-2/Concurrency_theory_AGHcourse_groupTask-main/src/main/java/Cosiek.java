import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Cosiek implements ICosiek {
    private final int maxBuffer;
    public ArrayList<Integer> buffer;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition
            firstConsumerCondition = lock.newCondition(),
            firstProducerCondition = lock.newCondition(),
            consumersCondition = lock.newCondition(),
            producersCondition = lock.newCondition();

    private boolean
            isFirstProducerWaiting = false,
            isFirstConsumerWaiting = false;


    public Cosiek(int maxBuffer) {
        this.maxBuffer = maxBuffer;
        buffer = new ArrayList<>();
    }

    public void produce(int idx, int portion) throws InterruptedException {
        lock.lock();

        while(isFirstProducerWaiting) {
            producersCondition.await();
        }

        while (buffer.size() + portion > maxBuffer) {
            isFirstProducerWaiting = true;
            firstProducerCondition.await();
        }
        isFirstProducerWaiting = false;


        for (int i = 0; i < portion; i++) {
            buffer.add(1);
        }


        producersCondition.signal();
        firstConsumerCondition.signal();

        lock.unlock();
    }

    public void consume(int idx, int portion) throws InterruptedException {
        lock.lock();

        while(isFirstConsumerWaiting) {
            consumersCondition.await();
        }

        while (buffer.size() - portion < 0) {
            isFirstConsumerWaiting = true;
            firstConsumerCondition.await();
        }
        isFirstConsumerWaiting = false;


        for (int i = 0; i < portion; i++) {
            buffer.remove(0);
        }


        consumersCondition.signal();
        firstProducerCondition.signal();

        lock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + buffer.size());
    }
}
