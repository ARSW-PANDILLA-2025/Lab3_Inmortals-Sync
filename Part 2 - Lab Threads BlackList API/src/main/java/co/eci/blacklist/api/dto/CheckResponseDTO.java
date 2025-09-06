package co.eci.blacklist.api.dto;

import co.eci.blacklist.domain.MatchResult;
import java.util.List;

/**
 * Data Transfer Object for blacklist check response.
 *
 * @param ip             The checked IP address.
 * @param trustworthy    Whether the IP is considered safe for network access.
 * @param matches        List of blacklist server indices where the IP was
 *                       found.
 * @param checkedServers Number of servers actually queried.
 * @param totalServers   Total number of blacklist servers available.
 * @param elapsedMs      Total execution time in milliseconds.
 * @param threads        Number of parallel threads used for processing.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public record CheckResponseDTO(
        String ip,
        boolean trustworthy,
        List<Integer> matches,
        int checkedServers,
        int totalServers,
        long elapsedMs,
        int threads) {

    /**
     * Creates a CheckResponseDTO from a domain MatchResult object.
     *
     * @param matchResult The domain result object to convert.
     * @return A new CheckResponseDTO with data copied from the MatchResult.
     */
    public static CheckResponseDTO from(MatchResult matchResult) {
        if (matchResult == null) {
            throw new IllegalArgumentException("MatchResult cannot be null");
        }

        return new CheckResponseDTO(
                matchResult.ip(),
                matchResult.trustworthy(),
                matchResult.matches(),
                matchResult.checkedServers(),
                matchResult.totalServers(),
                matchResult.elapsedMs(),
                matchResult.threads());
    }
}
