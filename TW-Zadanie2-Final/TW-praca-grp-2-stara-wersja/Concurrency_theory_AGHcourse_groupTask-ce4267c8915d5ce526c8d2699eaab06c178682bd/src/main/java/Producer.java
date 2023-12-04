import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Producer extends Thread {
    private final int counter, id;
    private final ICosiek cosiek;
    private static final int SEED = 1;
    private final int maxRandom;

    public Producer(ICosiek cosiek, int counter, int id, int maxRandom) {
        this.counter = counter;
        this.cosiek = cosiek;
        this.id = id;

        this.maxRandom = maxRandom;
    }

    @Override
    public void run() {
        for (int i = 0; i < counter; i++) {
            try {
                int portion = Main.USE_THREAD_LOCAL_RANDOM
                        ? ThreadLocalRandom.current().nextInt(maxRandom - 1) + 1
                        : Main.random.nextInt(maxRandom - 1) + 1;

                cosiek.produce(id, portion);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
