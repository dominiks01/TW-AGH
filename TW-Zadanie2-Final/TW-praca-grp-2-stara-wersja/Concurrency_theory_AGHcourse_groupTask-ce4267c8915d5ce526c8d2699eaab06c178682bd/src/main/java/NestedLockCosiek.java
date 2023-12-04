import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class NestedLockCosiek implements ICosiek {
    private final int _maxBufferLength;
    private int _nItems = 0, _takePtr = 0, _putPtr = 0;
    public int[] _buffer;

    private final ReentrantLock
            commonLock = new ReentrantLock(),
            consumerLock = new ReentrantLock(),
            producerLock = new ReentrantLock();

    private final Condition commonCondition = commonLock.newCondition();

    public NestedLockCosiek(int maxBuffer) {
        _maxBufferLength = maxBuffer;
        _buffer = new int[_maxBufferLength];
    }

    public void produce(int idx, int portion) throws InterruptedException {
        producerLock.lock();
        commonLock.lock();

        while (_nItems + portion > _maxBufferLength) {
            commonCondition.await();
        }

        for (int i = 0; i < portion; i++) {
            _buffer[_putPtr] = 1;
            _putPtr = (_putPtr + 1) % _maxBufferLength;
            _nItems++;
        }

        commonCondition.signal();
        commonLock.unlock();
        producerLock.unlock();
    }

    public void consume(int idx, int portion) throws InterruptedException {
        consumerLock.lock();
        commonLock.lock();

        while (_nItems - portion < 0) {
            commonCondition.await();
        }

        for (int i = 0; i < portion; i++) {
            _buffer[_takePtr] = 0;
            _takePtr = (_takePtr + 1) % _maxBufferLength;
            _nItems--;
        }

        commonCondition.signal();
        commonLock.unlock();
        consumerLock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + _nItems);
    }
}
