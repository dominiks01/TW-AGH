import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Cosiek implements ICosiek {
    private final int _maxBufferLength;
    private int _nItems = 0, _takePtr = 0, _putPtr = 0;
    public int[] _buffer;

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
        _maxBufferLength = maxBuffer;
        _buffer = new int[_maxBufferLength];
    }

    public void produce(int idx, int portion) throws InterruptedException {
        lock.lock();

        while(isFirstProducerWaiting) {
            producersCondition.await();
        }

        while (_nItems + portion > _maxBufferLength) {
            isFirstProducerWaiting = true;
            firstProducerCondition.await();
        }
        isFirstProducerWaiting = false;


        for (int i = 0; i < portion; i++) {
            _buffer[_putPtr] = 1;
            _putPtr = (_putPtr + 1) % _maxBufferLength;
            _nItems++;
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

        while (_nItems - portion < 0) {
            isFirstConsumerWaiting = true;
            firstConsumerCondition.await();
        }
        isFirstConsumerWaiting = false;


        for (int i = 0; i < portion; i++) {
            _buffer[_takePtr] = 0;
            _takePtr = (_takePtr + 1) % _maxBufferLength;
            _nItems--;
        }


        consumersCondition.signal();
        firstProducerCondition.signal();

        lock.unlock();
    }

    public synchronized void printAmount() {
        System.out.println("Counter value: " + _nItems);
    }
}
