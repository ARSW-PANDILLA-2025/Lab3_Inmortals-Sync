package co.eci.blacklist.application;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.MatchResult;
import org.springframework.stereotype.Service;

/**
 * Application service for blacklist verification operations.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
@Service
public class BlacklistService {

    /** The domain service for blacklist checking logic */
    private final BlacklistChecker checker;

    /**
     * Constructor of the BlacklistService class.
     *
     * @param checker The blacklist checker for domain operations.
     */
    public BlacklistService(BlacklistChecker checker) {
        this.checker = checker;
    }

    /**
     * Performs a blacklist check for the specified IP address.
     *
     * @param ip      The IP address to check against blacklists.
     * @param threads The number of threads to use for parallel processing.
     * @return MatchResult containing the check results and performance metrics.
     */
    public MatchResult check(String ip, int threads) {
        return checker.checkHost(ip, threads);
    }
}
