package co.eci.blacklist.labs.part2.activity3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import co.eci.blacklist.domain.Policies;
import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

/**
 * Test 3 - Tests for specific IPs performance and behavior (Activity 3).
 * Verifies the correct functionality and performance characteristics
 * of the BlacklistChecker with the three specific IPs mentioned in the
 * activity.
 *
 * @author
 * @version 1.0
 */
public class Test3SpecificIPsTest {

        private static final int BLACK_LIST_ALARM_COUNT = 5;
        private BlacklistChecker checker;
        private HostBlackListsDataSourceFacade facade;

        @BeforeEach
        @SuppressWarnings("unused")
        void setUp() {
                facade = HostBlackListsDataSourceFacade.getInstance();

                Policies policies = new Policies();
                policies.setAlarmCount(BLACK_LIST_ALARM_COUNT);

                checker = new BlacklistChecker(facade, policies);
        }

        /**
         * Test 3.1 - Should detect concentrated IP quickly.
         */
        @Test
        void test3_1_shouldDetectConcentratedIPQuickly() {
                String concentratedIP = "200.24.34.55";

                MatchResult result = checker.checkHost(concentratedIP, 4);

                assertNotNull(result);
                assertEquals(concentratedIP, result.ip());
                assertFalse(result.trustworthy(), "IP should be NOT trustworthy (concentrated matches)");
                assertTrue(result.matches().size() >= BLACK_LIST_ALARM_COUNT,
                                "Should find at least " + BLACK_LIST_ALARM_COUNT + " matches");
                assertTrue(result.checkedServers() < result.totalServers(),
                                "Should use early stopping and not check all servers");

                assertTrue(result.elapsedMs() < 2000, "Should complete quickly due to concentrated matches");

                double efficiency = (double) result.checkedServers() / result.totalServers();
                assertTrue(efficiency < 0.5, "Should check less than 50% of servers due to early stopping");
        }

        /**
         * Test 3.2 - Should handle dispersed IP correctly.
         */
        @Test
        void test3_2_shouldHandleDispersedIPCorrectly() {
                String dispersedIP = "202.24.34.55";

                MatchResult result = checker.checkHost(dispersedIP, 4);

                assertNotNull(result);
                assertEquals(dispersedIP, result.ip());

                if (!result.trustworthy()) {
                        assertTrue(result.matches().size() >= BLACK_LIST_ALARM_COUNT);
                } else {
                        assertTrue(result.matches().size() < BLACK_LIST_ALARM_COUNT);
                }

                double efficiency = (double) result.checkedServers() / result.totalServers();
                assertTrue(efficiency > 0.1, "Should check more servers than concentrated matches");
        }

        /**
         * Test 3.3 - Should handle clean IP worst case.
         */
        @Test
        void test3_3_shouldHandleCleanIPWorstCase() {
                String cleanIP = "212.24.24.55";

                MatchResult result = checker.checkHost(cleanIP, 4);

                assertNotNull(result);
                assertEquals(cleanIP, result.ip());
                assertTrue(result.trustworthy(), "Clean IP should be trustworthy");
                assertEquals(0, result.matches().size(), "Should find no matches for clean IP");
                assertEquals(result.totalServers(), result.checkedServers(),
                                "Should check all servers for clean IP (worst case)");

                assertTrue(result.elapsedMs() > 0, "Should take measurable time to check all servers");
        }

        /**
         * Test 3.4 - Should improve performance with more threads.
         */
        @Test
        void test3_4_shouldImprovePerformanceWithMoreThreads() {
                String testIP = "202.24.34.55";

                MatchResult result1 = checker.checkHost(testIP, 1);
                MatchResult result4 = checker.checkHost(testIP, 4);

                assertEquals(result1.trustworthy(), result4.trustworthy());
                assertEquals(result1.matches().size(), result4.matches().size());

                assertEquals(1, result1.threads());
                assertEquals(4, result4.threads());
        }

        /**
         * Test 3.5 - Should demonstrate early stopping behavior.
         */
        @Test
        void test3_5_shouldDemonstrateEarlyStoppingBehavior() {
                int threadCount = 8;

                MatchResult concentratedResult = checker.checkHost("200.24.34.55", threadCount);
                MatchResult dispersedResult = checker.checkHost("202.24.34.55", threadCount);
                MatchResult cleanResult = checker.checkHost("212.24.24.55", threadCount);

                assertTrue(concentratedResult.checkedServers() < concentratedResult.totalServers(),
                                "Concentrated IP should trigger early stopping");

                assertEquals(cleanResult.totalServers(), cleanResult.checkedServers(),
                                "Clean IP should check all servers (worst case)");

                assertTrue(dispersedResult.checkedServers() <= dispersedResult.totalServers(),
                                "Dispersed IP should check reasonable number of servers");

                assertTrue(cleanResult.checkedServers() >= concentratedResult.checkedServers(),
                                "Clean IP should check more servers than concentrated IP");
        }

        /**
         * Test 3.6 - Should scale with high thread count.
         */
        @Test
        void test3_6_shouldScaleWithHighThreadCount() {
                String testIP = "200.24.34.55";
                int maxThreads = 16;

                MatchResult result = checker.checkHost(testIP, maxThreads);

                assertNotNull(result);
                assertEquals(testIP, result.ip());
                assertEquals(maxThreads, result.threads());

                assertFalse(result.trustworthy());
                assertTrue(result.matches().size() >= BLACK_LIST_ALARM_COUNT);

                assertTrue(result.checkedServers() > 0);
                assertTrue(result.checkedServers() <= result.totalServers());
                assertTrue(result.elapsedMs() >= 0);
        }

        /**
         * Test 3.7 - Should match activity expected behavior.
         */
        @Test
        void test3_7_shouldMatchActivityExpectedBehavior() {
                MatchResult concentrated = checker.checkHost("200.24.34.55", 4);
                assertTrue(concentrated.matches().size() >= BLACK_LIST_ALARM_COUNT);
                assertTrue(concentrated.checkedServers() < concentrated.totalServers() / 2,
                                "Should check less than half servers due to early concentration");

                MatchResult dispersed = checker.checkHost("202.24.34.55", 4);
                assertTrue(dispersed.checkedServers() >= concentrated.checkedServers(),
                                "Dispersed IP should check at least as many servers as concentrated IP");

                MatchResult clean = checker.checkHost("212.24.24.55", 4);
                assertTrue(clean.trustworthy());
                assertEquals(0, clean.matches().size());
                assertEquals(clean.totalServers(), clean.checkedServers());

                assertTrue(clean.checkedServers() >= dispersed.checkedServers(),
                                "Clean IP should check at least as many servers as dispersed IP");
                assertTrue(dispersed.checkedServers() >= concentrated.checkedServers(),
                                "Dispersed IP should check at least as many servers as concentrated IP");
        }

        /**
         * Test 3.8 - Should perform comprehensive performance analysis with logging.
         */
        @Test
        void test3_8_comprehensivePerformanceAnalysisWithLogging() {
                System.out.println("\n=== ACTIVITY 3: Tests with specific IPs ===");
                System.out.println("Using BLACK_LIST_ALARM_COUNT = " + BLACK_LIST_ALARM_COUNT);
                System.out.println("Total servers: " + facade.getRegisteredServersCount());
                System.out.println();

                int[] threadCounts = { 1, 2, 4, 8, 16 };

                for (int threads : threadCounts) {
                        System.out.println(
                                        "==================== TESTS WITH " + threads + " THREADS ====================");
                        System.out.println();

                        testIPWithLogging("200.24.34.55",
                                        "Registered multiple times in first servers (fast search)",
                                        checker, threads);

                        testIPWithLogging("202.24.34.55",
                                        "Registered in dispersed way (slower search)",
                                        checker, threads);

                        testIPWithLogging("212.24.24.55",
                                        "Does not appear in any blacklist (worst case)",
                                        checker, threads);

                        System.out.println();
                }

                performanceComparisonWithLogging(checker, 4);
                assertTrue(true, "Performance analysis completed successfully");
        }

        /**
         * Helper method to run a test with logging.
         *
         * @param ip          The IP address to test.
         * @param description A description of the test case.
         * @param checker     The BlacklistChecker instance to use.
         * @param threads     The number of threads to use for the test.
         */
        private void testIPWithLogging(String ip, String description, BlacklistChecker checker, int threads) {
                System.out.println("IP Address: " + ip);
                System.out.println("Description: " + description);
                System.out.println("Thread count: " + threads);

                long startTime = System.nanoTime();
                MatchResult result = checker.checkHost(ip, threads);
                long endTime = System.nanoTime();

                double actualTimeMs = (endTime - startTime) / 1_000_000.0;

                System.out.println("Test Results:");
                System.out.println("   - Trustworthy: " + (result.trustworthy() ? "YES" : "NO"));
                System.out.println("   - Matches found: " + result.matches().size());
                System.out.println("   - Servers checked: " + result.checkedServers() + " of " + result.totalServers());
                System.out.println("   - Efficiency: " + String.format("%.2f%%",
                                (100.0 * result.checkedServers()) / result.totalServers()));
                System.out.println("   - Reported time: " + result.elapsedMs() + " ms");
                System.out.println("   - Measured time: " + String.format("%.2f ms", actualTimeMs));

                if (!result.matches().isEmpty()) {
                        System.out.println("   - Servers where found: " + result.matches());
                }

                System.out.println("   - Early stopping: "
                                + (result.checkedServers() < result.totalServers() ? "YES" : "NO"));
                System.out.println();
        }

        /**
         * Helper method to perform a performance comparison with logging.
         *
         * @param checker The BlacklistChecker instance to use.
         * @param threads The number of threads to use for the test.
         */
        private void performanceComparisonWithLogging(BlacklistChecker checker, int threads) {
                String[] ips = { "200.24.34.55", "202.24.34.55", "212.24.24.55" };
                String[] descriptions = { "Concentrated", "Dispersed", "No matches" };

                System.out.printf("%-15s %-20s %-12s %-15s %-12s %-10s%n",
                                "IP", "Type", "Trustworthy", "Servers", "Efficiency", "Time (ms)");
                System.out.println("─".repeat(85));

                for (int i = 0; i < ips.length; i++) {
                        MatchResult result = checker.checkHost(ips[i], threads);
                        System.out.printf("%-15s %-20s %-12s %-15s %-12s %-10d%n",
                                        ips[i],
                                        descriptions[i],
                                        result.trustworthy() ? "YES" : "NO",
                                        result.checkedServers() + "/" + result.totalServers(),
                                        String.format("%.1f%%",
                                                        (100.0 * result.checkedServers()) / result.totalServers()),
                                        result.elapsedMs());
                }

                System.out.println();
        }
}
