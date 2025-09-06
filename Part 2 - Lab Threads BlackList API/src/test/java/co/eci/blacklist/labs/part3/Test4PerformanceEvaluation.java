package co.eci.blacklist.labs.part3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import co.eci.blacklist.domain.Policies;
import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

/**
 * Test 4 - Performance Evaluation with Threading Configurations.
 * Evaluates the impact of the number of threads on execution time using the
 * dispersed IP (202.24.34.55).
 *
 * Scenarios tested:
 * - 1 thread
 * - Physical cores
 * - Double the cores
 * - 50 threads
 * - 100 threads
 */
public class Test4PerformanceEvaluation {

    private static final String DISPERSED_IP = "202.24.34.55";
    private BlacklistChecker checker;
    private HostBlackListsDataSourceFacade facade;
    private int physicalCores;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() {
        try {
            facade = HostBlackListsDataSourceFacade.getInstance();
            checker = new BlacklistChecker(facade, new Policies());
            physicalCores = Runtime.getRuntime().availableProcessors();
            
            assertNotNull(facade, "Facade should not be null");
            assertNotNull(checker, "Checker should not be null");
            assertTrue(physicalCores > 0, "Physical cores should be greater than 0");
            
            System.out.println("Setup completed: " + physicalCores + " available cores, "
                    + facade.getRegisteredServersCount() + " registered servers.");
        } catch (Exception e) {
            System.err.println("Error during setup: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Test 4.1 - Complete Performance Evaluation.
     */
    @Test
    void test4_completePerformanceEvaluation() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("PART III — PERFORMANCE EVALUATION");
        System.out.println("=".repeat(80));
        System.out.println("IP under test: " + DISPERSED_IP);
        System.out.println("Processor: " + physicalCores + " available cores");
        System.out.println("Total servers: " + facade.getRegisteredServersCount());
        System.out.println();

        int[] threadConfigurations = {
                1,
                physicalCores,
                physicalCores * 2,
                50,
                100
        };

        String[] configDescriptions = {
                "1 thread (sequential)",
                physicalCores + " threads (physical cores)",
                (physicalCores * 2) + " threads (double cores)",
                "50 threads (high concurrency)",
                "100 threads (very high concurrency)"
        };

        MatchResult[] results = new MatchResult[threadConfigurations.length];
        long[] executionTimes = new long[threadConfigurations.length];

        for (int i = 0; i < threadConfigurations.length; i++) {
            int threads = threadConfigurations[i];
            String description = configDescriptions[i];

            System.out.printf("Experiment %d: %s\n", i + 1, description);

            long startTime = System.nanoTime();
            MatchResult result = checker.checkHost(DISPERSED_IP, threads);
            long endTime = System.nanoTime();

            long executionTimeMs = (endTime - startTime) / 1_000_000;

            results[i] = result;
            executionTimes[i] = executionTimeMs;

            System.out.printf("   Result: %s\n", result.trustworthy() ? "RELIABLE" : "UNRELIABLE");
            System.out.printf("   Matches: %d\n", result.matches().size());
            System.out.printf("   Checked servers: %,d / %,d (%.1f%%)\n",
                    result.checkedServers(), result.totalServers(),
                    (100.0 * result.checkedServers()) / result.totalServers());
            System.out.printf("   Reported time: %d ms\n", result.elapsedMs());
            System.out.printf("   Measured time: %d ms\n", executionTimeMs);
            System.out.printf("   Threads used: %d\n\n", result.threads());

            assertNotNull(result);
            assertEquals(DISPERSED_IP, result.ip());
            assertEquals(threads, result.threads());
            assertTrue(result.elapsedMs() >= 0);
            assertTrue(executionTimeMs >= 0);
        }

        performComparativeAnalysis(threadConfigurations, configDescriptions, results, executionTimes);
        performScalabilityAnalysis(threadConfigurations, executionTimes);

        assertTrue(true, "Performance evaluation completed successfully");
    }

    /**
     * Perform comparative analysis on the results.
     *
     * @param threadConfigs Array of thread counts used in experiments.
     * @param descriptions  Array of description strings for each configuration.
     * @param results       Array of MatchResult objects for each configuration.
     * @param times         Array of execution times for each configuration.
     */
    private void performComparativeAnalysis(int[] threadConfigs, String[] descriptions,
            MatchResult[] results, long[] times) {
        System.out.println("COMPARATIVE ANALYSIS:");
        System.out.println("-".repeat(80));

        System.out.printf("%-6s %-30s %-12s %-15s %-12s %-10s%n",
                "Exp.", "Configuration", "Time (ms)", "Servers", "Efficiency", "Speedup");
        System.out.println("-".repeat(80));

        long baselineTime = times[0];

        for (int i = 0; i < threadConfigs.length; i++) {
            double efficiency = (100.0 * results[i].checkedServers()) / results[i].totalServers();
            double speedup = (double) baselineTime / times[i];

            System.out.printf("%-6d %-30s %-12d %-15s %-12.1f%% %-10.2fx%n",
                    i + 1,
                    descriptions[i],
                    times[i],
                    results[i].checkedServers() + "/" + results[i].totalServers(),
                    efficiency,
                    speedup);
        }
        System.out.println();
    }

    /**
     * Perform scalability analysis on the results.
     *
     * @param threadConfigs Array of thread counts used in experiments.
     * @param times         Array of execution times for each configuration.
     */
    private void performScalabilityAnalysis(int[] threadConfigs, long[] times) {
        System.out.println("SCALABILITY ANALYSIS:");
        System.out.println("-".repeat(80));

        int fastestIndex = 0;
        long fastestTime = times[0];

        for (int i = 1; i < times.length; i++) {
            if (times[i] < fastestTime) {
                fastestTime = times[i];
                fastestIndex = i;
            }
        }

        System.out.printf("• Fastest configuration: %d threads (%d ms)\n",
                threadConfigs[fastestIndex], fastestTime);

        double theoreticalSpeedup = (double) threadConfigs[fastestIndex] / threadConfigs[0];
        double actualSpeedup = (double) times[0] / times[fastestIndex];
        double efficiency = (actualSpeedup / theoreticalSpeedup) * 100;

        System.out.printf("• Theoretical speedup: %.2fx\n", theoreticalSpeedup);
        System.out.printf("• Actual speedup: %.2fx\n", actualSpeedup);
        System.out.printf("• Parallelization efficiency: %.1f%%\n", efficiency);

        long time50 = times[3];
        long time100 = times[4];

        if (time100 > time50) {
            double degradation = ((double) time100 / time50 - 1) * 100;
            System.out.printf("  - Degradation with 100 vs 50 threads: +%.1f%% time\n", degradation);
        } else {
            System.out.println("  - No significant degradation with 100 threads");
        }
        System.out.println();
    }

    /**
     * Test 4.2 - High concurrency with 100 threads.
     */

    @Test
    void test4_1_baselineWithSingleThread() {
        MatchResult result = checker.checkHost(DISPERSED_IP, 1);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(1, result.threads());
        assertTrue(result.elapsedMs() >= 0);
    }

    /**
     * Test 4.3 - High concurrency with 100 threads.
     */

    @Test
    void test4_2_optimalWithPhysicalCores() {
        MatchResult result = checker.checkHost(DISPERSED_IP, physicalCores);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(physicalCores, result.threads());
        assertTrue(result.elapsedMs() >= 0);
    }

    /**
     * Test 4.4 - High concurrency with 100 threads.
     */

    @Test
    void test4_3_highConcurrencyWith100Threads() {
        MatchResult result = checker.checkHost(DISPERSED_IP, 100);

        assertNotNull(result);
        assertEquals(DISPERSED_IP, result.ip());
        assertEquals(100, result.threads());
        assertTrue(result.elapsedMs() >= 0);
    }
}
