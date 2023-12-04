import java.util.ArrayList;

public class threadsRunning extends Thread{
    private final ArrayList<Thread> threatsToCheck;
    private final ArrayList<Thread> threadsToKill;

    public threadsRunning(ArrayList<Thread> threatsToCheck, ArrayList<Thread> threadsToKill) {
        this.threatsToCheck = threatsToCheck;
        this.threadsToKill = threadsToKill;
    }

    @Override
    public void run() {
        for (Thread thread : threatsToCheck) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Thread th : threadsToKill) {
            if(th.getState() != State.TERMINATED) {
                th.interrupt();
            }
        }
    }
}
