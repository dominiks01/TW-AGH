import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collection;

public class TimeCounter {
    ThreadMXBean _threadMXBean = ManagementFactory.getThreadMXBean();
    ArrayList<Thread> threadsProducers;
    ArrayList<Thread> threadsConsumers;
    private final long startTime;
    private long realTime = 0, cpuTime = 0;

    public TimeCounter(ArrayList<Thread> threadsProducers, ArrayList<Thread> threadsConsumers) {
        this.threadsConsumers = threadsConsumers;
        this.threadsProducers = threadsProducers;
        startTime = System.nanoTime();
    }

    public void stop() {
        realTime = System.nanoTime() - startTime;

        threadsProducers.forEach(th->{
            cpuTime += ((Producer) th).CPUTime;
        });

        threadsConsumers.forEach(th->{
            cpuTime += ((Consumer) th).CPUTime;
        });
    }

    public long getRealTime() {
        return realTime;
    }

    public long getCpuTime() {
        return cpuTime;
    }
}
