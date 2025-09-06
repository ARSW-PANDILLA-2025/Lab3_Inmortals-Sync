package co.eci.blacklist.infrastructure;

import co.eci.blacklist.domain.BlacklistChecker;
import co.eci.blacklist.domain.Policies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class for infrastructure layer dependency injection.
 *
 * @author ARSW-PANDILLA-2025
 * @version 1.0
 */
@Configuration
public class DataSourceConfig {

    /**
     * Provides the singleton instance of the blacklist data source facade.
     *
     * @return The singleton HostBlackListsDataSourceFacade instance.
     */
    @Bean
    public HostBlackListsDataSourceFacade hostBlackListsDataSourceFacade() {
        return HostBlackListsDataSourceFacade.getInstance();
    }

    /**
     * Creates and configures the main blacklist checking service.
     *
     * @param facade The data source facade providing access to blacklist servers.
     * @param policies The configuration policies including alarm count threshold.
     * @return A fully configured BlacklistChecker instance.
     */
    @Bean
    public BlacklistChecker blacklistChecker(HostBlackListsDataSourceFacade facade, Policies policies) {
        return new BlacklistChecker(facade, policies);
    }
}
