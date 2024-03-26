import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreeLockCosiek implements ICosiek {
    private final int maxBufferLength;
    public ArrayList<Integer> buffer;

    private final ReentrantLock
            commonLock = new ReentrantLock(),
            consumerLock = new ReentrantLock(),
            producerLock = new ReentrantLock();

    private final Condition commonCondition = commonLock.newCondition();

    public ThreeLockCosiek(int maxBuffer) {
        maxBufferLength = maxBuffer;
        buffer = new ArrayList<>();
    }

    public void produce(int idx, int portion) throws InterruptedException {
        producerLock.lock();
        commonLock.lock();

        while (buffer.size() + portion > maxBufferLength) {
            commonCondition.await();
        }

        for (int i = 0; i < portion; i++) {
            buffer.add(1);
        }

        commonCondition.signal();
        commonLock.unlock();
        producerLock.unlock();
    }

    public void consume(int idx, int portion) throws InterruptedException {
        consumerLock.lock();
        commonLock.lock();

        while (buffer.size() - portion < 0) {
            commonCondition.await();
        }

        for (int i = 0; i < portion; i++) {
            buffer.remove(0);
        }

        commonCondition.signal();
        commonLock.unlock();
        consumerLock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + buffer.size());
    }
}
