import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Consumer extends Thread {
    private final int counter, id;
    private final ICosiek cosiek;
    private static final int SEED = 1;
    private final int maxRandom;
    private int operationsDone = 0;
    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    public long CPUTime = 0;

    public Consumer(ICosiek cosiek, int counter, int id, int maxRandom) {
        this.counter = counter;
        this.cosiek = cosiek;
        this.id = id;
        this.maxRandom = maxRandom;
    }

    public void interrupt() {
//        System.out.println("Producent " + id + ": przerwano mi");
//        System.out.println(operationsDone);
        CPUTime = threadMXBean.getThreadCpuTime(this.getId());
        this.stop();
    }

    @Override
    public void run() {
        for (int i = 0; i < counter; i++) {
            try {
                int portion = Main.USE_THREAD_LOCAL_RANDOM
                        ? ThreadLocalRandom.current().nextInt(maxRandom - 1) + 1
                        : new Random(SEED).nextInt(maxRandom - 1) + 1;

                cosiek.consume(id, portion);
                operationsDone += 1;
            } catch (InterruptedException e) {
                System.out.println("Konsument: przerwano mi");
                System.out.println(operationsDone);
                throw new RuntimeException(e);
            }
        }
        CPUTime = threadMXBean.getThreadCpuTime(this.getId());
        //System.out.println(CPUTime);
    }
}
