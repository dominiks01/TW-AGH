public interface ICosiek {
    void consume(int idx, int portion) throws InterruptedException;
    void produce(int idx, int portion) throws InterruptedException;
    void printAmount();
}
