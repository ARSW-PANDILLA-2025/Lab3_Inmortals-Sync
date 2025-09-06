package co.eci.blacklist.labs.part1;

/**
 * Thread class for counting numbers in a specified range.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public class CountThread extends Thread {
    private final int from;
    private final int to;

    /**
     * Constructor to initialize the counting range.
     *
     * @param from The starting number of the range (inclusive).
     * @param to   The ending number of the range (inclusive).
     */
    public CountThread(int from, int to) {
        this.from = from;
        this.to = to;
        setName("CountThread-" + from + "-" + to);
    }

    /**
     * Runs the thread to count numbers from 'from' to 'to'.
     */
    @Override
    public void run() {
        for (int i = from; i <= to; i++) {
            System.out.println("[" + getName() + "] " + i);
        }
    }
}