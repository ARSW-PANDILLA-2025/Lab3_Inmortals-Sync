package co.eci.blacklist.infrastructure;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simplified, thread-safe facade inspired by the ARSW lab.
 * In the original lab this class is provided and should not be modified.
 * Here we provide a minimal in-memory implementation suitable for the REST service and tests.
 */
public final class HostBlackListsDataSourceFacade {

    private static final Logger logger = Logger.getLogger(HostBlackListsDataSourceFacade.class.getName());
    private static final HostBlackListsDataSourceFacade INSTANCE = new HostBlackListsDataSourceFacade();

    private final int registeredServersCount;
    private final ConcurrentMap<String, Set<Integer>> blacklistedByIp = new ConcurrentHashMap<>();

    private HostBlackListsDataSourceFacade() {
        this.registeredServersCount = 10_000;
        // Seed some deterministic data for demo purposes
        seed("200.24.34.55", List.of(0,1,2,3,4,5,6,7,8,9)); // concentrated early
        seed("202.24.34.55", List.of(5,111,999,2048,4096,8191)); // dispersed
        // 212.24.24.55 intentionally not seeded - no matches (worst case scenario)
    }

    /**
     * Returns the singleton instance of the HostBlackListsDataSourceFacade.
     *
     * @return The singleton instance.
     */
    public static HostBlackListsDataSourceFacade getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the total number of registered servers.
     *
     * @return The count of registered servers.
     */
    public int getRegisteredServersCount() {
        return registeredServersCount;
    }

    /**
     * Checks if the given IP address is blacklisted on the specified server.
     *
     * @param serverIndex The index of the server to check.
     * @param ip The IP address to verify.
     * @return True if the IP is blacklisted on the server, false otherwise.
     */
    public boolean isInBlackListServer(int serverIndex, String ip) {
        Set<Integer> set = blacklistedByIp.get(ip);
        return set != null && set.contains(serverIndex);
    }

    /**
     * Returns the set of server indices where the given IP address is blacklisted.
     *
     * @param ip The IP address to check.
     * @return A set of server indices where the IP is blacklisted, or an empty set if not found.
     */
    public void reportAsTrustworthy(String ip) {
        logger.log(Level.INFO, "HOST {0} Reported as trustworthy", ip);
    }

    /**
     * Reports the given IP address as not trustworthy.
     *
     * @param ip The IP address to report.
     */
    public void reportAsNotTrustworthy(String ip) {
        logger.log(Level.INFO, "HOST {0} Reported as NOT trustworthy", ip);
    }

    /**
     * Seeds the facade with initial data for testing purposes.
     *
     * @param ip The IP address to seed.
     * @param indices The server indices where the IP is blacklisted.
     */
    public void seed(String ip, List<Integer> indices) {
        blacklistedByIp.computeIfAbsent(ip, k -> ConcurrentHashMap.newKeySet()).addAll(indices);
    }

    /**
     * Clears the blacklist data for the given IP address.
     *
     * @param ip The IP address to clear.
     */
    public void clear(String ip) {
        blacklistedByIp.remove(ip);
    }
}
