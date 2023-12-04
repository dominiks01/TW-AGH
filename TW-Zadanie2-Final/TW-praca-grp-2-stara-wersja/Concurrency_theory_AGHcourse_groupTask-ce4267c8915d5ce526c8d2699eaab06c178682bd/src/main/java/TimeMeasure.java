import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.lang.management.ThreadMXBean;
import com.opencsv.CSVWriter;

public class TimeMeasure {
    ThreadMXBean _threadMXBean = ManagementFactory.getThreadMXBean();
    Collection<Thread> _threads;
    private final long _startTime;
    private long _realTime = 0, _cpuTime = 0;

    public TimeMeasure(Collection<Thread> threads) throws IOException {
        _threads = threads;
        _startTime = System.nanoTime();
    }

    public static String deltaToString(long delta) {
        StringBuilder s = new StringBuilder();
        while (delta > 0) {
            int frag = (int) (delta % 1000);
            s.insert(0, " ");
            s.insert(0, String.format("%3d", frag).replace(' ', '0'));
            delta /= 1000;
        }

        return s.toString().replaceFirst("^0+(?!$)", "");
    }

    public long getCpuTime() {
        return _cpuTime;
    }

    public long getRealTime() {
        return _realTime;
    }

    public void print() {
        System.out.printf("%-20s%sns\n", "Czas procesora:", deltaToString(_cpuTime));
        System.out.printf("%-20s%sns\n", "Czas rzeczywisty: ", deltaToString(_realTime));
    }

    public void save(CSVWriter writer) {
        String[] data = {Long.toString(_cpuTime), Long.toString(_realTime)};
        writer.writeNext(data);
    }

    public void start() {
        // Oczekiwanie na zakończenie dowolnego, jednego wątku
        boolean threadTerminated = false;
        while (!threadTerminated) {
            for (Thread thread : _threads) {
                if (thread.getState() == Thread.State.TERMINATED) {
                    threadTerminated = true;
                    break;
                }
            }
        }


        // Pomiar czasu rzeczywistego
        _realTime = System.nanoTime() - _startTime;

        // Pomiar czasu procesora
        LinkedList<Long> times = new LinkedList<>();
        for (Thread thread : _threads) {
            long id = thread.getId();
            long time = _threadMXBean.getThreadCpuTime(id); // zwraca -1 jak thread jest martwy, bo jest GUPIE
            times.add(time);
        }
        // hack na uzupełnienie -1 z zakończonego wątku, +- działa
        // wątek zakończony pracował zwykle najdłużej, więc przybliżamy jego czas najdłuższym z pozostałych
        times.add(Collections.max(times));
        _cpuTime = times.stream().mapToLong(Long::longValue).sum();


        // Zatrzymanie pozostałych wątków
        for (Thread thread : _threads) {
            thread.stop();
        }
    }
}
