package co.eci.blacklist.domain;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration policies for blacklist checking operations.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
@ConfigurationProperties(prefix = "blacklist")
public class Policies {
    
    /**
     * The minimum number of blacklist matches required to classify an IP address
     * as NOT trustworthy. Default value is 5.
     */
    private int alarmCount = 5;

    /**
     * Returns the current alarm count threshold.
     *
     * @return The minimum number of matches required to flag an IP as untrusted.
     */
    public int getAlarmCount() {
        return alarmCount;
    }

    /**
     * Sets the alarm count threshold for blacklist matching.
     *
     * @param alarmCount The minimum number of matches to flag an IP as untrusted.
     */
    public void setAlarmCount(int alarmCount) {
        if (alarmCount < 0) {
            throw new IllegalArgumentException("Alarm count cannot be negative");
        }
        this.alarmCount = alarmCount;
    }
}
