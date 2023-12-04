import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static final int
            _maxBufferSize = 1000,
            _nProducers = 6,
            _nConsumers = 6,
            _nProducerLoops = 10000,
            _nConsumerLoops = 10000,
            _nTests = 50;

    public static Random random = new Random();

    private static int _maxRandom = _maxBufferSize / 4;

    public static final boolean USE_THREAD_LOCAL_RANDOM = true;
    public static boolean USE_NESTED_LOCK_COSIEK = true;

    private static void test(CSVWriter writer) throws IOException {
        long meanRealTime = 0, meanCpuTime = 0;

        System.out.printf("%s%d %15s%d  %15s%s %15s%s\n",
                "TEST: max_random=", _maxRandom,
                "buf_size=", _maxBufferSize,
                "buf_type=", USE_NESTED_LOCK_COSIEK ? "3-LOCK" : "4-COND",
                "rng=", USE_THREAD_LOCAL_RANDOM ? "LOCAL" : "GLOBAL");

        for (int test = 0; test < _nTests; test++) {
            ArrayList<Thread> threads = new ArrayList<>();

            ICosiek cosiek = USE_NESTED_LOCK_COSIEK ?
                    new NestedLockCosiek(_maxBufferSize) :
                    new Cosiek(_maxBufferSize);


            // Tworzymy producentów
            for(int id = 0; id < _nProducers; id++) {
                Producer producent = new Producer(cosiek, _nProducerLoops, id, _maxRandom);
                threads.add(producent);
            }

            // Tworzymy konsumentów
            for(int id = 0; id < _nConsumers; id++) {
                Consumer consument = new Consumer(cosiek, _nConsumerLoops, id, _maxRandom);
                threads.add(consument);
            }

            // Inicjalizacja pomiaru czasu
            TimeMeasure timeMeasure = new TimeMeasure(threads);

            for (Thread t : threads) {
                t.start();
            }

            // Pomiar czasu
            timeMeasure.start();
            //timeMeasure.print();
            //timeMeasure.save(writer);
            meanCpuTime = (meanCpuTime / (test + 1) * test + timeMeasure.getCpuTime() / (test + 1));
            meanRealTime = (meanRealTime / (test + 1) * test + timeMeasure.getRealTime() / (test + 1));
        }

        System.out.printf("%-30s%sns\n", "> Średni czas procesora:", TimeMeasure.deltaToString(meanCpuTime));
        System.out.printf("%-30s%sns\n", "> Średni czas rzeczywisty: ", TimeMeasure.deltaToString(meanRealTime));
        String[] data = {USE_NESTED_LOCK_COSIEK ? "3-lock" : "4-condition",
                String.valueOf(meanCpuTime), String.valueOf(meanRealTime),
                String.valueOf(_maxRandom), String.valueOf(_maxBufferSize),
                String.valueOf(_nProducers * _nProducerLoops + _nConsumers * _nConsumerLoops),
                USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global"
        };
        writer.writeNext(data);
    }

    public static void main(String[] args) throws IOException {
        File file = new File("measures_" + (USE_THREAD_LOCAL_RANDOM ? "Thread-Local" : "Global") + ".CSV");

        CSVWriter writer = new CSVWriter(new FileWriter(file));
        String[] header = {"typ bufora", "średni czas procesora", "średni czas rzeczywisty", "maksymalna porcja", "bufor", "ilość operacji", "typ random"};
        writer.writeNext(header);

        int n_maxRandom_sizes = 10;
        int incr = _maxBufferSize / (2 * n_maxRandom_sizes);
        int max_rand = incr;

        while (max_rand <= _maxBufferSize / 2 && n_maxRandom_sizes-- > 0) {
            _maxRandom = max_rand;

            USE_NESTED_LOCK_COSIEK = false;
            test(writer);

            USE_NESTED_LOCK_COSIEK = true;
            test(writer);

            max_rand += incr;
        }

        writer.close();
    }
}
