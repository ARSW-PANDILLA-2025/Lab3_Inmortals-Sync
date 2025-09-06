package co.eci.blacklist.labs.part2;

import co.eci.blacklist.infrastructure.HostBlackListsDataSourceFacade;
import java.util.ArrayList;
import java.util.List;

/**
 * BlacklistChecker2 class for checking if an IP is blacklisted using multiple threads.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
public class BlacklistChecker2 extends Thread {
    private final String ip;
    private final int start, end;
    private final HostBlackListsDataSourceFacade facade;
    private final List<Integer> found = new ArrayList<>();

    /**
     * Constructor to initialize the BlacklistChecker2 with IP and range.
     *
     * @param ip The IP address to check.
     * @param start The starting index of the server range.
     * @param end The ending index of the server range.
     * @param facade The facade to access blacklist servers.
     */
    public BlacklistChecker2 (String ip, int start, int end, HostBlackListsDataSourceFacade facade) {
        this.ip = ip;
        this.start = start;
        this.end = end;
        this.facade = facade;
    }

    /**
     * Runs the thread to check if the IP is in the blacklist servers within the specified range.
     */
    @Override
    public void run() {
        for (int i = start; i < end; i++) {
            if (facade.isInBlackListServer(i, ip)) {
                found.add(i);
            }
        }
    }

    /**
     * Returns the list of found blacklist server indices for the IP.
     *
     * @return List of indices where the IP is found in blacklists.
     */
    public List<Integer> getFound() {
        return found;
    }
}
