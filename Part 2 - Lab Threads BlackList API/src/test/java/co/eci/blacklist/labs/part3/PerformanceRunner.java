package co.eci.blacklist.labs.part3;

import java.util.LinkedHashMap;
import java.util.Map;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import co.eci.blacklist.domain.Policies;
import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

public class PerformanceRunner {

    private static final String HOST = "202.24.34.55";
    private static final int REPS = 3;

    /**
     * Main method to run performance tests.
     *
     * @param args Command line arguments.
     * @throws Exception if an error occurs during execution.
     */

    public static void main(String[] args) throws Exception {
        int[] threadCounts = {1, 2, 4, 8, 16, 32, 50, 100};

        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

        Policies policies = new Policies();
        policies.setAlarmCount(5);

        // Saving results in a map (ordered)
        Map<Integer, Long> results = new LinkedHashMap<>();

        for (int n : threadCounts) {
            long sumMs = 0L;
            for (int i = 0; i < REPS; i++) {
                BlacklistChecker checker = new BlacklistChecker(facade, policies);
                MatchResult r = checker.checkHost(HOST, n);
                sumMs += r.elapsedMs();
            }
            long avg = sumMs / REPS;
            results.put(n, avg);
        }

        // Now we print the table at the end (without intermediate noise)
        System.out.println("\n=====================================");
        System.out.printf("%-10s | %-20s%n", "Threads", "Avg Time (ms)");
        System.out.println("=====================================");
        for (Map.Entry<Integer, Long> entry : results.entrySet()) {
            System.out.printf("%-10d | %-20d%n", entry.getKey(), entry.getValue());
        }
        System.out.println("=====================================");

        Thread.sleep(3000);
    }
}