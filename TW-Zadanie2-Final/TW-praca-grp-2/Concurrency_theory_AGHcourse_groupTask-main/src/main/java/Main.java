import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static final int
            maxBufferSize = 1000,
            nProducers = 30,
            nConsumers = 30,
            nProducerLoops = 1000,
            nConsumerLoops = 1000,
            nTests = 10;

    private static int maxRandom = maxBufferSize / 4;

    public static final boolean USE_THREAD_LOCAL_RANDOM = true;
    public static boolean USE_THREE_LOCK_COSIEK = false;

    private static void test(CSVWriter writer) throws InterruptedException {
        long meanRealTime = 0, meanCpuTime = 0;

        for (int test = 0; test < nTests; test++) {
            ArrayList<Thread> producentThreads = new ArrayList<>();
            ArrayList<Thread> consumentThreads = new ArrayList<>();
            TimeCounter time;

            ICosiek cosiek = USE_THREE_LOCK_COSIEK ?
                    new ThreeLockCosiek(maxBufferSize) :
                    new Cosiek(maxBufferSize);


            // Tworzymy producentów
            for(int id = 0; id < nProducers; id++) {
                Producer producent = new Producer(cosiek, nProducerLoops, id, maxRandom);
                producentThreads.add(producent);
            }

//            // Tworzymy konsumentów
            for(int id = 0; id < nConsumers; id++) {
                Consumer consument = new Consumer(cosiek, nConsumerLoops, id, maxRandom);
                consumentThreads.add(consument);
            }

            time = new TimeCounter(producentThreads, consumentThreads);
            producentThreads.forEach(Thread::start);
            consumentThreads.forEach(Thread::start);

            Thread observingThread1 = new threadsRunning(consumentThreads, producentThreads);
            Thread observingThread2 = new threadsRunning(producentThreads, consumentThreads);

            observingThread1.start();
            observingThread2.start();
            observingThread1.join();
            observingThread2.join();

            time.stop();
            meanCpuTime = (meanCpuTime / (test + 1) * test + time.getCpuTime() / (test + 1));
            meanRealTime = (meanRealTime / (test + 1) * test + time.getRealTime() / (test + 1));
        }



        System.out.printf("%-30s%sns\n", "> Średni czas procesora:", meanCpuTime);
        System.out.printf("%-30s%sns\n", "> Średni czas rzeczywisty: ", meanRealTime);
        String[] data = {USE_THREE_LOCK_COSIEK ? "3-lock" : "4-condition",
                String.valueOf(meanCpuTime), String.valueOf(meanRealTime),
                String.valueOf(maxRandom), String.valueOf(maxBufferSize),
                String.valueOf(nProducers * nProducerLoops + nConsumers * nConsumerLoops),
                USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global"
        };
        writer.writeNext(data);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File("measures_" + (USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global") + nConsumers + ".CSV");

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        String[] header = {"typ bufora", "średni czas procesora", "średni czas rzeczywisty", "maksymalna porcja", "bufor", "ilość operacji", "typ random"};
        writer.writeNext(header);

        int n_maxRandom_sizes = 10;
        int incr = maxBufferSize / (2 * n_maxRandom_sizes);
        int maxRand = incr;

        while (maxRandom <= maxBufferSize / 2 && n_maxRandom_sizes-- > 0) {
            maxRandom = maxRand;
            USE_THREE_LOCK_COSIEK = false;
            test(writer);

            USE_THREE_LOCK_COSIEK = true;
            test(writer);

            maxRand += incr;
        }

        writer.close();
    }
}
