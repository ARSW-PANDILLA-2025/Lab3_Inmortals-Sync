package co.eci.blacklist.labs.part1;

/**
 * Main class to demonstrate the use of CountThread for counting numbers in parallel.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
*/
public class CountMainThreads {

    /**
     * Main method to create and start multiple CountThread instances.
     *
     * @param args Command line arguments (not used).
     * @throws InterruptedException If the current thread is interrupted while waiting for threads to finish.
     */
    @SuppressWarnings("CallToThreadRun")
    public static void main(String[] args) throws InterruptedException {
        CountThread t1 = new CountThread(0, 99);
        CountThread t2 = new CountThread(99, 199);
        CountThread t3 = new CountThread(200, 299);

        // We run the CountThreads because we want to execute them in parallel.

        // t1.start();
        // t2.start();
        // t3.start();


        t1.run();
        t2.run();
        t3.run();


        t1.join();
        t2.join();
        t3.join();
    }
}