package co.eci.blacklist.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;

/**
 * Test 4 - Integration tests for BlacklistChecker domain service.
 * Tests the main BlacklistChecker implementation with virtual threads.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public class BlacklistCheckerTest {

    /**
     * Test 4.1: Verifies early stopping functionality to avoid scanning all servers.
     * Tests the main implementation with the production BlacklistChecker.
     */
    @Test
    void test4_1_earlyStopShouldAvoidScanningAllServers() {
        Policies policies = new Policies();
        policies.setAlarmCount(5);
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();
        
        String ip = "200.24.34.55";
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(ip, Math.max(2, Runtime.getRuntime().availableProcessors()));

        assertNotNull(result);
        assertEquals(ip, result.ip());
        assertFalse(result.trustworthy(), "Should be NOT trustworthy when threshold reached");
        assertTrue(result.matches().size() >= policies.getAlarmCount());
        assertTrue(result.checkedServers() < result.totalServers(), "Should stop early and not scan all servers");
    }

    /**
     * Test 4.2: Host clean -> should be trustworthy and scan (almost) all servers (no early stop).
     */
    @Test
    void test4_2_cleanHostShouldBeTrustworthyAndNotEarlyStop() {
        Policies policies = new Policies();
        policies.setAlarmCount(5);
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

        String cleanIp = "212.24.24.55"; // IP presumed clean in dataset
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(cleanIp, 4);

        assertNotNull(result);
        assertEquals(cleanIp, result.ip());
        assertTrue(result.trustworthy(), "Clean IP should be trustworthy");
        assertTrue(result.matches().isEmpty(), "No matches expected for clean IP");
        assertEquals(result.totalServers(), result.checkedServers(), "Should scan all servers (no early stop)");
    }

    /**
     * Test 4.3: Exact threshold reached -> host not trustworthy; matches size >= threshold.
     */
    @Test
    void test4_3_exactThresholdMakesHostUntrustworthy() {
        Policies policies = new Policies();
        policies.setAlarmCount(3); // Lower threshold to increase chance
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

        String ip = "200.24.34.55"; // Likely to have multiple hits
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(ip, 6);

        assertNotNull(result);
        assertFalse(result.trustworthy(), "Should be untrustworthy when threshold reached");
        assertTrue(result.matches().size() >= policies.getAlarmCount());
        assertTrue(result.checkedServers() < result.totalServers(), "Should early stop at threshold");
    }

    /**
     * Test 4.4: Single thread behavior -> logic still correct and early stop works.
     */
    @Test
    void test4_4_singleThreadStillStopsEarly() {
        Policies policies = new Policies();
        policies.setAlarmCount(4);
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

        String ip = "200.24.34.55";
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(ip, 1); // single thread

        assertNotNull(result);
        assertFalse(result.trustworthy());
        assertTrue(result.matches().size() >= policies.getAlarmCount());
        assertTrue(result.checkedServers() < result.totalServers());
    }

    /**
     * Test 4.5: High thread count > total servers -> should cap and still work.
     */
    @Test
    void test4_5_highThreadCountCapped() {
        Policies policies = new Policies();
        policies.setAlarmCount(5);
        HostBlackListsDataSourceFacade facade = HostBlackListsDataSourceFacade.getInstance();

        String ip = "200.24.34.55";
        BlacklistChecker checker = new BlacklistChecker(facade, policies);
        MatchResult result = checker.checkHost(ip, 10_000); // absurdly high

        assertNotNull(result);
        assertFalse(result.trustworthy());
        assertTrue(result.matches().size() >= policies.getAlarmCount());
    }
}
