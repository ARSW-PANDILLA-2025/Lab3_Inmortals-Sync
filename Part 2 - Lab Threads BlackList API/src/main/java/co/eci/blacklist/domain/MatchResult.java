package co.eci.blacklist.domain;

import java.util.List;

/**
 * Result of a blacklist checking operation.
 *
 * @param ip             The IP address that was checked.
 * @param trustworthy    True if the IP is considered trustworthy.
 * @param matches        List of server indices where the IP was found.
 * @param checkedServers The actual number of servers checked.
 * @param totalServers   The total number of servers available for checking.
 * @param elapsedMs      The total execution time in milliseconds.
 * @param threads        The number of threads used for parallel processing.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public record MatchResult(
                String ip,
                boolean trustworthy,
                List<Integer> matches,
                int checkedServers,
                int totalServers,
                long elapsedMs,
                int threads) {
        /**
         * Constructor that validates parameters.
         */
        public MatchResult {
                if (ip == null || ip.trim().isEmpty()) {
                        throw new IllegalArgumentException("IP address cannot be null or empty");
                }
                if (matches == null) {
                        throw new IllegalArgumentException("Matches list cannot be null");
                }
                if (checkedServers < 0) {
                        throw new IllegalArgumentException("Checked servers cannot be negative");
                }
                if (totalServers < 0) {
                        throw new IllegalArgumentException("Total servers cannot be negative");
                }
                if (checkedServers > totalServers) {
                        throw new IllegalArgumentException("Checked servers cannot exceed total servers");
                }
                if (elapsedMs < 0) {
                        throw new IllegalArgumentException("Elapsed time cannot be negative");
                }
                if (threads <= 0) {
                        throw new IllegalArgumentException("Thread count must be positive");
                }

                matches = List.copyOf(matches);
        }

        /**
         * Returns the efficiency of the checking operation as a percentage.
         *
         * @return Percentage of servers actually checked (0-100).
         */
        public double getEfficiency() {
                return totalServers == 0 ? 0.0 : (double) checkedServers / totalServers * 100.0;
        }

        /**
         * Indicates whether early stopping was activated during the check.
         *
         * @return True if fewer servers were checked than available, false otherwise.
         */
        public boolean hasEarlyStopping() {
                return checkedServers < totalServers;
        }
}
